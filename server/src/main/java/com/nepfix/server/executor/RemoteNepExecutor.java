package com.nepfix.server.executor;


import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.nepfix.server.AppConfiguration;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import com.nepfix.server.rabbit.Util;
import com.nepfix.server.rabbit.event.ComputationFinishedEvent;
import com.nepfix.server.rabbit.event.ComputationStartedEvent;
import com.nepfix.server.rabbit.event.GenericMessage;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.elements.util.Misc;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configurable
public class RemoteNepExecutor implements MessageListener {

    private final Logger logger;

    //State
    private SimpleMessageListenerContainer messageListenerContainer;
    private List<String> remoteQueues;
    private final String localQueueName;
    private final TopicExchange nepExchange;
    private final Semaphore sync;
    private final Queue localQueue;
    @Expose private final Nep nep;
    @Expose private final long computationId;
    @Expose private final List<Word> localWords;

    //Communications
    @Autowired private ConnectionFactory rabbitConnectionFactory;
    @Autowired private ActiveQueuesRepository activeQueuesRepository;
    @Autowired private NepRepository nepRepository;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbit;

    RemoteNepExecutor(long computationId,
                      NepBlueprint nepBlueprint) {
        //State
        this.computationId = computationId;
        this.nep = nepBlueprint.create(computationId);
        this.localWords = new Vector<>(); //Thread safe
        //Communications
        this.logger = Logger.getLogger(this.getClass() + " : " + nep.getId());
        this.localQueueName = nep.getMd5() + "::" + computationId;
        this.nepExchange = new TopicExchange(nep.getId(), false, true);
        this.localQueue = new Queue(localQueueName, false, true, true);
        this.sync = new Semaphore(0);

    }

    void init() {
        this.remoteQueues = nepRepository
                .getAllRemoteQueues(nep.getId()).stream()
                .map(md5 -> md5 + "::" + computationId)
                .filter(remoteQueue -> !remoteQueue.equals(localQueueName))
                .collect(Collectors.toList());
    }

    public List<Word> execute(ComputationRequest request) {
        initializeRemoteQueues();
        startListening();

        Node inputNode = nep.getInputNode();
        localWords.add(new Word(request.getInput(), inputNode.getId(), 0));

        while (nep.getNepOutput().size() < request.getMaxOutputs()
                && nep.getConfiguration() < request.getMaxConfiguration()) {

            remoteQueues.parallelStream().forEach(this::sendStep);
            step();

            try {
                boolean completed = sync.tryAcquire(remoteQueues.size(), 10, TimeUnit.SECONDS);
                if (completed)
                    logger.debug("Step completed for: " + computationId);
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


    public void sendStep(String remoteQueue) {
        rabbit.send(nep.getId(), //nep exchange
                computationId + "." + remoteQueue, //routing key
                Util.emptyMessageBuilder().setReplyTo(localQueueName)
                        .setHeader("action", "step")
                        .build());

        logger.info("Step sent to: " + remoteQueue);
    }

    public void sendStepReply(String remoteQueue) {
        rabbit.send(nep.getId(), //nep exchange
                computationId + "." + remoteQueue, //routing key
                Util.emptyMessageBuilder().setHeader("action", "step_done")
                        .build());

        logger.info("Step Reply sent to: " + remoteQueue);

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
                    computationId + "." + destinyNode, //routing key
                    MessageBuilder
                            .withBody(GenericMessage.GSON.toJson(words).getBytes())
                            .setHeader("action", "addWords")
                            .build());
            retryCount++;
            if (response == null)
                logger.info("Sending " + words.size() + " words to " + destinyNode + " failed, RETRYING");

        }
        if (response == null) {
            logger.error("Sending " + words.size() + " words to " + destinyNode + " FAILED");
        } else {
            logger.info("Sent " + words.size() + " words to " + destinyNode);
        }

    }

    private void initializeRemoteQueues() {
        rabbitAdmin.declareExchange(nepExchange);
        logger.info("Created exchange for Nep: " + nep.getId());

        Message message = new ComputationStartedEvent(nep.getId(), computationId).toMessage();
        for (String serverQueue : activeQueuesRepository.getServerQueues()) {
            if (serverQueue.equals(AppConfiguration.SERVER_QUEUE)) continue;
            Message response = rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
            logger.info("Server " + serverQueue + " ready to compute " + computationId);
        }
    }

    public void stopListening() {
        messageListenerContainer.stop();
        rabbitAdmin.deleteQueue(localQueueName);
    }

    private void removeRemoteQueues() {
        Message message = new ComputationFinishedEvent(nep.getId(), computationId).toMessage();
        for (String serverQueue : activeQueuesRepository.getServerQueues()) {
            if (serverQueue.equals(AppConfiguration.SERVER_QUEUE)) continue;
            Message response = rabbit.sendAndReceive(
                    serverQueue,
                    message
            );//Discarded response, just RPC
            logger.info("Server " + serverQueue + " computation completed " + computationId);
        }
    }

    public void startListening() {
        rabbitAdmin.declareQueue(localQueue);

        //TODO, only bind frontier nodes
        //Information binding
        for (Node node : nep.getNodes()) {
            Binding binding = BindingBuilder
                    .bind(localQueue)
                    .to(nepExchange)
                    .with(computationId + "." + node.getId());
            rabbitAdmin.declareBinding(binding);
        }

        //Sync binding
        Binding binding = BindingBuilder
                .bind(localQueue)
                .to(nepExchange)
                .with(computationId + "." + localQueueName);
        rabbitAdmin.declareBinding(binding);

        messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        messageListenerContainer.setQueueNames(localQueueName);
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.start();
        logger.info("Nep listening on: " + localQueue.getName());

    }

    @Override public void onMessage(Message message) {
        String action = (String) message.getMessageProperties().getHeaders().getOrDefault("action", "");
        String replyTo = message.getMessageProperties().getReplyTo();

        switch (action) {
            case "addWords":
                final Type type = new TypeToken<List<Word>>() {
                }.getType();
                List<Word> words = GenericMessage.GSON.fromJson(new String(message.getBody()), type);
                localWords.addAll(words);
                logger.debug("Words received: " + words.size());
                rabbit.send(replyTo, Util.emptyMessage());
                break;

            case "step":
                step();
                sendStepReply(replyTo);
                break;

            case "step_done":
                sync.release();
                break;
        }
    }

    public String getIdentifier() {
        return nep.getId() + "-" + computationId;
    }
}
