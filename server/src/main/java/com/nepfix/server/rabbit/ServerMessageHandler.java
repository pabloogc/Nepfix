package com.nepfix.server.rabbit;

import com.nepfix.server.AppConfiguration;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.server.executor.RemoteNepExecutorFactory;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveServersRepository;
import com.nepfix.server.rabbit.event.*;
import com.nepfix.sim.nep.NepBlueprint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.nepfix.server.AppConfiguration.BROADCAST_EXCHANGE;
import static com.nepfix.server.AppConfiguration.SERVER_QUEUE;
import static com.nepfix.server.rabbit.event.GenericMessage.getKind;
import static com.nepfix.server.rabbit.event.GenericMessage.read;

@Component
public class ServerMessageHandler implements MessageListener {

    private static final Log logger = LogFactory.getLog(ServerMessageHandler.class);
    @Autowired private RabbitTemplate rabbit;
    @Autowired private ActiveServersRepository activeQueuesRepository;
    @Autowired private NepRepository nepRepository;
    @Autowired private RemoteNepExecutorFactory nepExecutorFactory;

    @Override public void onMessage(Message message) {
        Kind kind = getKind(message);
        if (kind == null) {
            logger.error("Message not handled: " + "Kind is NULL!!");
            return;
        }
        logger.debug("Message received with kind: " + kind.name());
        switch (kind) {
            case SERVER_READY:
                handleNewServerReady(message);
                break;
            case SERVER_READY_REPLY:
                handleNewServerReply(message);
                break;
            case NEW_NEP:
                handleNewServerForNep(message);
                break;
            case NEW_NEP_REPLY:
                handelNewServerForNepReply(message);
                break;
            case COMPUTATION_STARTED:
                handleComputationStarted(message);
                break;
            case COMPUTATION_FINISHED:
                handleComputationFinished(message);
                break;
            case STOP:
                return;
            case UNKNOWN:
            default:
                logger.warn("Message not handled: " + kind.name());
        }
    }

    private void handleNewServerReady(Message message) {
        if (message.getMessageProperties().getReplyTo().equals(SERVER_QUEUE))
            return; //Broadcast came from this server

        activeQueuesRepository.registerServerQueue(message.getMessageProperties().getReplyTo());
        rabbit.send(
                "", //Default exchange
                message.getMessageProperties().getReplyTo(),
                new ServerReadyReplyEvent(SERVER_QUEUE).toMessage());

    }

    private void handleNewServerReply(Message message) {
        ServerReadyEvent event = read(message, ServerReadyEvent.class);
        activeQueuesRepository.registerServerQueue(event.getValue());
    }

    /**
     * {@link com.nepfix.server.NepController#registerNep(String)}
     */
    private void handleNewServerForNep(Message message) {
        if (message.getMessageProperties().getReplyTo().equals(SERVER_QUEUE))
            return; //Broadcast came from this server

        NepRegisteredEvent event = read(message, NepRegisteredEvent.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(event.getValue().getId());
        if (blueprint != null) { //The new nep its related to one already stored
            //This operation is duplicated in the broadcast reply but has no effect, left here for clarity
            nepRepository.registerRemoteQueue(event.getValue());
            NepRegisteredEventReply reply = new NepRegisteredEventReply(blueprint.getNepId(), SERVER_QUEUE);
            //Reply rpc call
            rabbit.send(message.getMessageProperties().getReplyTo(), reply.toMessage());
        }
    }

    private void handelNewServerForNepReply(Message message) {
        NepRegisteredEventReply event = read(message, NepRegisteredEventReply.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(event.getValue().getId());
        if (blueprint != null) {
            nepRepository.registerRemoteQueue(event.getValue());
        }
    }

    private void handleComputationStarted(Message message) {
        ComputationStartedEvent event = read(message, ComputationStartedEvent.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(event.getValue().nepId);
        if (blueprint == null) return; //Nep is not on this machine
        RemoteNepExecutor activeNep = nepRepository.findActiveNep(event.getValue().nepId, event.getValue().compId);
        if (activeNep != null) return; //This nep is already active
        RemoteNepExecutor nepExecutor = nepExecutorFactory.create(blueprint, event.getValue().compId);
        nepExecutor.startListening();
        nepRepository.registerActiveNep(nepExecutor);
        //Reply rpc call
        rabbit.send(message.getMessageProperties().getReplyTo(), RabbitUtil.emptyMessage());
    }

    private void handleComputationFinished(Message message) {
        ComputationFinishedEvent event = read(message, ComputationFinishedEvent.class);
        RemoteNepExecutor executor = nepRepository.findActiveNep(event.getValue().nepId, event.getValue().compId);
        if (executor != null) {
            executor.stopListening();
            nepRepository.unregisterActiveNep(executor);
        }
        //Reply rpc call
        rabbit.send(message.getMessageProperties().getReplyTo(), RabbitUtil.emptyMessage());
    }

    public void broadcastServerReady() {
        rabbit.send(
                AppConfiguration.BROADCAST_EXCHANGE,
                "",
                new ServerReadyEvent()
                        .toMessageBuilder()
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.debug("Broadcasting server queue: " + SERVER_QUEUE);
    }

    public void broadcastNewServerForNep(NepBlueprint nepBlueprint) {
        rabbit.send(
                BROADCAST_EXCHANGE,
                "", //no routing
                new NepRegisteredEvent(nepBlueprint.getNepId(), SERVER_QUEUE)
                        .toMessageBuilder()
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.debug("Broadcasting new nep: " + nepBlueprint.getNepId());
    }
}
