package com.nepfix.server.executor;


import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.nepfix.server.AppConfiguration;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveServersRepository;
import com.nepfix.server.rabbit.RabbitUtil;
import com.nepfix.server.rabbit.event.ComputationFinishedEvent;
import com.nepfix.server.rabbit.event.ComputationStartedEvent;
import com.nepfix.server.rabbit.event.GenericMessage;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.elements.util.Misc;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configurable
public class RemoteNepExecutor implements MessageListener {

    private final Log logger;

    //State
    private SimpleMessageListenerContainer messageListenerContainer;
    private List<String> remoteQueues;
    private final TopicExchange nepExchange;
    private final Semaphore sync;
    private final Queue queue;
    @Expose private final Nep nep;
    @Expose private final long computationId;
    @Expose private final List<Word> localWords;

    //Communications
    @Autowired private ConnectionFactory rabbitConnectionFactory;
    @Autowired private ActiveServersRepository activeQueuesRepository;
    @Autowired private NepRepository nepRepository;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbit;

    RemoteNepExecutor(AutowireCapableBeanFactory autowireCapableBeanFactory, long computationId,
                      NepBlueprint nepBlueprint) {
        autowireCapableBeanFactory.autowireBean(this);
        //State
        this.computationId = computationId;
        this.nep = nepBlueprint.create(computationId);
        this.localWords = new Vector<>(); //Thread safe
        //Communications
        this.logger = LogFactory.getLog(this.getClass().getName() + "::" + nep.getId() + "::" + computationId);
        this.nepExchange = new TopicExchange(nep.getId(), false, true);
        this.queue = rabbitAdmin.declareQueue();
        this.sync = new Semaphore(0);
        this.remoteQueues = nepRepository.getRemoteQueues(nep.getId())
                .stream()
                .filter(q -> !q.equals(queue.getName()))
                .collect(Collectors.toList());

    }

    public List<Word> execute(ComputationRequest request) {
        initializeRemoteQueues();
        startListening();

        Node inputNode = nep.getInputNode();
        localWords.add(new Word(request.getInput(), inputNode.getId(), 0));

        while (nep.getNepOutput().size() < request.getMaxOutputs()
                && nep.getConfiguration() < request.getMaxConfigurations()) {

            sendStep();
            step();

            try {
                boolean completed = sync.tryAcquire(remoteQueues.size() - 1, 10, TimeUnit.SECONDS);
                if (completed)
                    logger.debug("step completed");
                else {
                    logger.error("Time out on:" + computationId);
                    break;
                }
            } catch (InterruptedException e) {
                logger.error("Time out on: " + computationId);
                break;
            }
        }
        removeRemoteQueues();
        stopListening();
        return nep.getNepOutput();
    }

    private void step() {
        nep.putWords(localWords);
        localWords.clear();
        HashMap<String, List<Word>> remoteWords = new HashMap<>();
        List<Word> nextConfig = nep.step();

        for (Word word : nextConfig) {
            if (nep.hasNode(word.getDestinyNode()))
                localWords.add(word);
            else
                Misc.putInListHashMap(
                        word.getDestinyNode(),
                        word,
                        remoteWords);
        }
        remoteWords.values().forEach(this::sendWords);
    }


    /**
     * Sent from clock server to all nodes participating in the network
     */
    public void sendStep() {
        rabbit.send(nep.getId(), //nep exchange
                RabbitUtil.routingKey(computationId, "__sync__"),
                RabbitUtil.emptyMessageBuilder()
                        .setReplyTo(queue.getName())
                        .setHeader("action", "step")
                        .build());

        logger.debug("sent action " + "step " + nep.getConfiguration());
    }

    /**
     * Sent from all servers to clock server after step is done
     */
    public void sendStepDone(String clockQueue) {
        rabbit.send(
                clockQueue, //direct message
                RabbitUtil.emptyMessageBuilder().setHeader("action", "step_done")
                        .build());

        logger.debug("sent action " + "step done " + nep.getConfiguration());

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
                    MessageBuilder
                            .withBody(GenericMessage.GSON.toJson(words).getBytes())
                            .setHeader("action", "addWords")
                            .build());
            retryCount++;
            if (response == null)
                logger.debug("Sending " + words.size() + " words to " + destinyNode + " failed, RETRYING");

        }
        if (response == null) {
            logger.error("Sending " + words.size() + " words to " + destinyNode + " FAILED");
        } else {
            logger.debug("sent action " + "addWords" + words.size());
        }

    }

    private void initializeRemoteQueues() {
        rabbitAdmin.declareExchange(nepExchange);
        logger.debug("Created exchange for Nep: " + nep.getId());

        Message message = new ComputationStartedEvent(nep.getId(), computationId).toMessage();
        for (String serverQueue : activeQueuesRepository.getServerQueues()) {
            if (serverQueue.equals(AppConfiguration.SERVER_QUEUE)) continue;
            Message response = rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
            logger.debug("Server " + serverQueue + " ready to compute " + computationId);
        }
    }

    /**
     * {@link com.nepfix.server.rabbit.ServerMessageHandler#handleComputationFinished(Message)}
     */
    public void stopListening() {
        messageListenerContainer.stop();
        rabbitAdmin.deleteQueue(queue.getName());
    }

    private void removeRemoteQueues() {
        Message message = new ComputationFinishedEvent(nep.getId(), computationId).toMessage();
        for (String serverQueue : activeQueuesRepository.getServerQueues()) {
            if (serverQueue.equals(AppConfiguration.SERVER_QUEUE)) continue;
            Message response = rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
            logger.debug("Server " + serverQueue + " computation completed " + computationId);
        }
    }


    /**
     * {@link com.nepfix.server.rabbit.ServerMessageHandler#handleComputationStarted(Message)}
     */
    public void startListening() {
        //TODO, only bind frontier nodes
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
                .with(RabbitUtil.routingKey(computationId, "__sync__"));
        rabbitAdmin.declareBinding(syncBinding);

        messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        messageListenerContainer.setQueueNames(queue.getName());
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.start();
        logger.debug("Nep listening on: " + queue.getName());

    }

    @Override public void onMessage(Message message) {
        String action = (String) message.getMessageProperties().getHeaders().getOrDefault("action", "");
        String replyTo = message.getMessageProperties().getReplyTo();
        if (queue.getName().equals(replyTo)) return; //Message comes from this machine

        logger.debug("got action" + action);

        switch (action) {
            case "addWords":
                final Type type = new TypeToken<List<Word>>() {
                }.getType();
                List<Word> words = GenericMessage.GSON.fromJson(new String(message.getBody()), type);
                localWords.addAll(words);
                rabbit.send(replyTo, RabbitUtil.emptyMessage());
                break;

            case "step":
                step();
                sendStepDone(replyTo);
                break;

            case "step_done":
                sync.release();
                break;
        }
    }

    public String getExecutorId() {
        return executorId(nep.getId(), computationId);
    }

    public static String executorId(String nepId, long computationId){
        return nepId + "." + computationId;
    }
}
