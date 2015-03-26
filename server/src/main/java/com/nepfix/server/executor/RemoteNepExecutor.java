package com.nepfix.server.executor;


import com.google.gson.annotations.Expose;
import com.nepfix.server.AppConfiguration;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.rabbit.event.ComputationStartedEvent;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RemoteNepExecutor implements MessageListener {

    @Expose private final Nep nep;
    @Expose private final long computationId;
    private final ConnectionFactory rabbitConnectionFactory;
    private final NepRepository nepRepository;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbit;
    private final Logger logger;
    private final List<String> remoteQueues;
    private final String localQueueName;
    private final Semaphore sync;
    private List<Word> localWords;
    private List<Word> remoteWords;
    private SimpleMessageListenerContainer messageListenerContainer;

    RemoteNepExecutor(
            RabbitTemplate rabbit,
            RabbitAdmin rabbitAdmin,
            ConnectionFactory rabbitConnectionFactory,
            long computationId,
            NepRepository nepRepository,
            NepBlueprint nepBlueprint) {
        this.rabbit = rabbit;
        this.rabbitConnectionFactory = rabbitConnectionFactory;
        this.rabbitAdmin = rabbitAdmin;
        this.nepRepository = nepRepository;
        this.nep = nepBlueprint.create(computationId);
        this.logger = Logger.getLogger(this.getClass() + " : " + nep.getId());
        this.localQueueName = nep.getMd5() + "-" + computationId;
        this.computationId = computationId;
        this.remoteQueues = nepRepository
                .getAllRemoteQueues(nep.getId()).stream()
                .map(md5 -> md5 + "-" + computationId)
                .filter(remoteQueue -> !remoteQueue.equals(localQueueName))
                .collect(Collectors.toList());
        this.sync = new Semaphore(0);
    }

    public List<Word> execute(ComputationRequest request) {
        init();
        int count = 0;
        try {
            while (count < 10) {
                for (String remoteQueue : remoteQueues) {
                    rabbit.send(nep.getId(),
                            remoteQueue,
                            MessageBuilder
                                    .withBody(("Calculate: " + count).getBytes())
                                    .setHeader("reply-to", localQueueName)
                                    .build());
                }

                logger.info("Main Doing work... " + count);
                Thread.sleep((long) (Math.random() % 5000) + 1000);
                count++;
                logger.info("Waiting for " + remoteQueues.size());
                sync.acquire(remoteQueues.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cleanUp();
        return Collections.emptyList();


//        Node inputNode = nep.getInputNode();
//
//        localWords = new Vector<>();
//        localWords.add(new Word(request.getInput(), inputNode.getId(), 0));
//        remoteWords = new Vector<>();
//
//        while (nep.getNepOutput().size() < request.getMaxOutputs()
//                && nep.getConfiguration() < request.getMaxConfiguration()) {
//
//            nep.putWords(localWords);
//
//            List<Word> nextConfig = nep.step();
//            for (Word word : nextConfig) {
//                if (nep.hasNode(word.getDestinyNode()))
//                    localWords.add(word);
//                else
//                    remoteWords.add(word);
//            }
//        }
//        return nep.getNepOutput();

    }

    private void cleanUp() {
        remoteQueues.forEach(rabbitAdmin::deleteQueue);
        rabbitAdmin.deleteQueue(localQueueName);
    }

    private void init() {
        nepRepository.registerActiveNep(this);
        startListening();
        rabbit.send(
                AppConfiguration.BROADCAST_EXCHANGE,
                "",
                new ComputationStartedEvent(nep.getId(), computationId).toMessage()
        );
    }

    public void startListening() {
        Exchange nepExchange = new DirectExchange(nep.getId(), false, true);
        rabbitAdmin.declareExchange(nepExchange);
        logger.info("Created exchange for Nep: " + nep.getId());

        Queue queue = new Queue(localQueueName);
        rabbitAdmin.declareQueue(queue);

        Binding binding = BindingBuilder
                .bind(queue)
                .to(nepExchange)
                .with(localQueueName)
                .noargs();

        rabbitAdmin.declareBinding(binding);

        messageListenerContainer = new SimpleMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        messageListenerContainer.setQueueNames(queue.getName());
        messageListenerContainer.setMessageListener(this);
        messageListenerContainer.start();

    }

    public Nep getNep() {
        return nep;
    }

    @Override public void onMessage(Message message) {
        String replyTo = (String) message.getMessageProperties().getHeaders().getOrDefault("reply-to", "");
        if (replyTo.isEmpty()) {
            sync.release();
            logger.info("Worker finished");
        } else {
            logger.info("Worker working");
            //Do the computation and send
            try {
                Thread.sleep((long) (Math.random() % 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rabbit.send(nep.getId(), replyTo, MessageBuilder.withBody("Done!".getBytes()).build());
        }
    }
}
