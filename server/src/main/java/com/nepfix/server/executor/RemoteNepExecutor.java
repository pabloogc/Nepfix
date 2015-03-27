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
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RemoteNepExecutor implements MessageListener {

    @Expose private final Nep nep;
    @Expose private final long computationId;
    private final ConnectionFactory rabbitConnectionFactory;
    private final ActiveQueuesRepository activeQueuesRepository;
    private final NepRepository nepRepository;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbit;
    private final Logger logger;
    private final List<String> remoteQueues;
    private final String localQueueName;
    private final Exchange nepExchange;
    private final Semaphore sync;
    private final Queue localQueue;
    private List<Word> localWords = new Vector<>();
    private SimpleMessageListenerContainer messageListenerContainer;

    RemoteNepExecutor(
            RabbitTemplate rabbit,
            RabbitAdmin rabbitAdmin,
            ConnectionFactory rabbitConnectionFactory,
            ActiveQueuesRepository activeQueuesRepository,
            long computationId,
            NepRepository nepRepository,
            NepBlueprint nepBlueprint) {
        this.rabbit = rabbit;
        this.rabbitConnectionFactory = rabbitConnectionFactory;
        this.rabbitAdmin = rabbitAdmin;
        this.activeQueuesRepository = activeQueuesRepository;
        this.nepRepository = nepRepository;
        this.nep = nepBlueprint.create(computationId);
        this.logger = Logger.getLogger(this.getClass() + " : " + nep.getId());
        this.localQueueName = nep.getMd5() + "-" + computationId;
        this.nepExchange = new DirectExchange(nep.getId(), false, true);
        this.localQueue = new Queue(localQueueName, false, true, true);
        this.computationId = computationId;
        this.remoteQueues = nepRepository
                .getAllRemoteQueues(nep.getId()).stream()
                .map(md5 -> md5 + "-" + computationId)
                .filter(remoteQueue -> !remoteQueue.equals(localQueueName))
                .collect(Collectors.toList());
        this.sync = new Semaphore(0);
    }

    public List<Word> execute(ComputationRequest request) {
        initializeRemoteQueues();
        startListening();

        Node inputNode = nep.getInputNode();
        localWords = new Vector<>();
        localWords.add(new Word(request.getInput(), inputNode.getId(), 0));

        while (nep.getNepOutput().size() < request.getMaxOutputs()
                && nep.getConfiguration() < request.getMaxConfiguration()) {

            remoteQueues.parallelStream().forEach(this::sendStep);
            step();

            sync.acquireUninterruptibly(remoteQueues.size());

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
                        nepRepository.findRemoteNode(nep.getId(), word.getDestinyNode()) + "-" + computationId,
                        word,
                        remoteWords);
        }
        remoteWords.forEach(this::sendWords);
    }


    public void sendStep(String remoteQueue) {
        rabbit.send(
                nep.getId(),
                remoteQueue,
                Util.emptyMessageBuilder()
                        .setReplyTo(localQueueName)
                        .setHeader("action", "step")
                        .build());

        logger.info("Step sent to: " + remoteQueue);
    }

    public void sendStepReply(String remoteQueue) {
        rabbit.send(
                nep.getId(),
                remoteQueue,
                Util.emptyMessageBuilder()
                        .setHeader("action", "step_done")
                        .build());

        logger.info("Step Reply sent to: " + remoteQueue);

    }

    private void sendWords(String remoteQueue, List<Word> words) {
        Message response = null;
        int retryCount = 0;
        while (retryCount < 3 && response == null) {
            response = rabbit.sendAndReceive(nep.getId(),
                    remoteQueue,
                    MessageBuilder
                            .withBody(GenericMessage.GSON.toJson(words).getBytes())
                            .setHeader("action", "addWords")
                            .build());
            retryCount++;
            if (response == null)
                logger.info("Sending " + words.size() + " words to " + remoteQueue + " failed, RETRYING");

        }
        if (response == null) {
            logger.severe("Sending " + words.size() + " words to " + remoteQueue + " FAILED");
        } else {
            logger.info("Sent " + words.size() + " words to " + remoteQueue);
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
        Binding binding = BindingBuilder
                .bind(localQueue)
                .to(nepExchange)
                .with(localQueueName)
                .noargs();

        rabbitAdmin.declareBinding(binding);
        messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        messageListenerContainer.setQueueNames(localQueueName);
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.setAutoStartup(true);
        messageListenerContainer.start();
        logger.info("Nep listening on: " + localQueue.getName());

    }

    public Nep getNep() {
        return nep;
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
}
