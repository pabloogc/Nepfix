package com.nepfix.server.executor;


import com.google.gson.annotations.Expose;
import com.nepfix.server.AppConfiguration;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.rabbit.RabbitUtil;
import com.nepfix.server.rabbit.messages.Action;
import com.nepfix.server.rabbit.messages.NepComputationInfo;
import com.nepfix.server.rabbit.messages.NepMessage;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepUtils;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Configurable
public class RemoteNepExecutor implements MessageListener {

    private static final String SYNC_KEY = "__sync__";
    private final Log logger;

    //State
    private SimpleMessageListenerContainer messageListenerContainer;
    private List<String> remoteServerQueues;
    private final TopicExchange nepExchange;
    private final Semaphore sync;
    private final Queue queue;
    @Expose private final Nep nep;
    @Expose private final long computationId;
    @Expose private final List<Word> localWords;

    //Communications
    @Autowired private ConnectionFactory rabbitConnectionFactory;
    @Autowired private NepRepository nepRepository;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbit;

    //Splitting
    private Semaphore pauseSync = new Semaphore(0);
    private Semaphore pauseCallerWait = new Semaphore(0);
    private AtomicBoolean pauseRequested = new AtomicBoolean(false);

    RemoteNepExecutor(AutowireCapableBeanFactory autowireCapableBeanFactory, long computationId,
                      NepBlueprint nepBlueprint) {
        autowireCapableBeanFactory.autowireBean(this);

        //State
        this.computationId = computationId;
        this.nep = nepBlueprint.create(computationId);
        this.localWords = new Vector<>(); //Thread safe
        //Communications
        this.logger = LogFactory.getLog(this.getClass().getName() + "." + nep.getId() + "." + computationId);
        this.nepExchange = new TopicExchange(nep.getId(), false, true);
        this.queue = rabbitAdmin.declareQueue();
        this.sync = new Semaphore(0);
        this.remoteServerQueues = nepRepository.getRemoteQueues(nep.getId())
                .stream()
                .filter(q -> !q.equals(AppConfiguration.SERVER_QUEUE))
                .collect(Collectors.toList());

    }

    public List<Word> execute(ComputationRequest request) {
        initializeRemoteQueues();
        startListening();

        Node inputNode = nep.getInputNode();
        localWords.add(new Word(request.getInput(), inputNode.getId(), 0));
        try {
            while (nep.getNepOutput().size() < request.getMaxOutputs()
                    && nep.getConfiguration() < request.getMaxConfigurations()) {

                sendStep();
                step();

                try {
                    boolean completed = sync.tryAcquire(remoteServerQueues.size(), 15, TimeUnit.MINUTES);
                    if (completed) {
                        logger.debug("------ ALL Steps Completed");
                    } else {
                        logger.error("Time out on:" + computationId);
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("Time out on: " + computationId);
                    break;
                }
            }
        } finally {
            removeRemoteQueues();
            stopListening();
        }
        return nep.getNepOutput();
    }

    private void step() {
        logger.debug("Computing...");
        nep.putWords(localWords);
        localWords.clear();
        HashMap<String, List<Word>> remoteWords = new HashMap<>();
        List<Word> nextConfig = nep.step();

        for (Word word : nextConfig) {
            if (nep.hasNode(word.getDestinyNode()))
                localWords.add(word);
            else
                NepUtils.putInListHashMap(
                        word.getDestinyNode(),
                        word,
                        remoteWords);
        }
        remoteWords.values().forEach(this::sendWords);

        if (pauseRequested.get()) {
            pauseCallerWait.release();
            pauseSync.acquireUninterruptibly();
        }

        logger.debug("-- Step completed");
    }


    /**
     * Sent from clock server to all nodes participating in the network
     */
    public void sendStep() {
        rabbit.send(nep.getId(), //nep exchange
                RabbitUtil.routingKey(computationId, SYNC_KEY),
                NepMessage.toBuilder(Action.N_STEP)
                        .setReplyTo(queue.getName())
                        .build());

        logger.debug("Sent action -> step: " + nep.getConfiguration());
    }

    /**
     * Sent from all servers to clock server after step is done
     */
    public void replyStepCompleted(String clockQueue) {
        rabbit.send(
                clockQueue, //direct message
                NepMessage.toMessage(Action.N_STEP_REPLY));
        logger.debug("Sent action -> step completed: " + nep.getConfiguration());
    }

    private void sendWords(List<Word> words) {
        Message response = null;
        if (words.isEmpty()) {
            return;
        }

        int retryCount = 0;
        String destinyNode = words.get(0).getDestinyNode();
        while (retryCount < 3 && response == null) {
            response = rabbit.sendAndReceive(
                    nep.getId(), //nep exchange
                    RabbitUtil.routingKey(computationId, destinyNode), //routing key
                    NepMessage.toMessage(Action.N_ADD_WORDS, words));
            retryCount++;
            if (response == null)
                logger.debug("Sending " + words.size() + " words to " + destinyNode + " failed, RETRYING");

        }
        if (response == null) {
            logger.error("Sending " + words.size() + " words to " + destinyNode + " FAILED");
        } else {
            logger.debug("Sent action -> " + "addWords count: " + words.size());
        }

    }

    private void initializeRemoteQueues() {

        rabbitAdmin.declareExchange(nepExchange);

        for (String serverQueue : remoteServerQueues) {
            Message message = NepMessage.toMessage(
                    Action.S_COMPUTATION_STARTED,
                    new NepComputationInfo(nep.getId(), computationId));
            rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
            logger.debug("Server " + serverQueue + " ready to compute " + computationId);
        }
    }

    /**
     * {@link com.nepfix.server.rabbit.ServerMessageHandler#handleComputationStarted(Message)}
     */
    public void startListening() {
        //No way to know what nodes get words from other servers, so bind all
        for (Node node : nep.getNodes()) {
            Binding binding = BindingBuilder
                    .bind(queue)
                    .to(nepExchange)
                    .with(RabbitUtil.routingKey(computationId, node.getId()));
            rabbitAdmin.declareBinding(binding);
        }

        //Sync syncBinding
        Binding syncBinding = BindingBuilder
                .bind(queue)
                .to(nepExchange)
                .with(RabbitUtil.routingKey(computationId, SYNC_KEY));
        rabbitAdmin.declareBinding(syncBinding);

        messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        messageListenerContainer.setQueueNames(queue.getName());
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.start();
        logger.debug("Nep listening on: " + queue.getName());

    }

    /**
     * {@link com.nepfix.server.rabbit.ServerMessageHandler#handleComputationFinished(Message)}
     */
    public void stopListening() {
        messageListenerContainer.stop();
        rabbitAdmin.deleteQueue(queue.getName());
    }


    private void removeRemoteQueues() {
        for (String serverQueue : remoteServerQueues) {
            Message message = NepMessage.toMessage(
                    Action.S_COMPUTATION_FINISHED,
                    new NepComputationInfo(nep.getId(), computationId));

            rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
        }
    }

    @Override public void onMessage(Message message) {
        Action action = NepMessage.getAction(message);
        String replyTo = message.getMessageProperties().getReplyTo();

        if (queue.getName().equals(replyTo)) return;

        switch (action) {
            case N_ADD_WORDS:
                List<Word> words = NepMessage.parse(message);
                logger.debug("Got action <- Added words: " + words.size());
                localWords.addAll(words);
                rabbit.send(replyTo, RabbitUtil.emptyMessage()); //ack
                break;
            case N_STEP:
                logger.debug("Got action <- Do step");
                step();
                replyStepCompleted(replyTo);
                break;
            case N_STEP_REPLY:
                logger.debug("Got action <- Reply to step");
                sync.release();
                break;
            default:
                logger.error("Unknown action: " + action);
        }
    }

    public String getExecutorId() {
        return executorId(nep.getId(), computationId);
    }

    public static String executorId(String nepId, long computationId) {
        return nepId + "." + computationId;
    }
}
