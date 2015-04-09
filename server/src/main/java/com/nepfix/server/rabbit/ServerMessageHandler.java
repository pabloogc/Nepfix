package com.nepfix.server.rabbit;

import com.nepfix.server.AppConfiguration;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.server.executor.RemoteNepExecutorFactory;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import com.nepfix.server.rabbit.event.*;
import com.nepfix.sim.nep.NepBlueprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);
    @Autowired private RabbitTemplate rabbit;
    @Autowired private ActiveQueuesRepository activeQueuesRepository;
    @Autowired private NepRepository nepRepository;
    @Autowired private RemoteNepExecutorFactory nepExecutorFactory;

    @Override public void onMessage(Message message) {
        Kind kind = getKind(message);
        if (kind == null) {
            logger.error("Message not handled: " + "Kind is NULL!!");
            return;
        }
        logger.info("Message received with kind: " + kind.name());
        switch (kind) {
            case SERVER_READY:
                handleServerReady(message);
                break;
            case SERVER_READY_REPLY:
                handleServerReply(message);
                break;
            case NEW_NEP:
                handleNewNep(message);
                break;
            case NEW_NEP_REPLY:
                handelNewNepReply(message);
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

    private void handleServerReady(Message message) {
        if (!message.getMessageProperties().getReplyTo().equals(SERVER_QUEUE)) {
            activeQueuesRepository.registerServerQueue(message.getMessageProperties().getReplyTo());
            rabbit.send(
                    "", //Default exchange
                    message.getMessageProperties().getReplyTo(),
                    new ServerReadyReplyEvent(SERVER_QUEUE).toMessage());
        }
    }

    private void handleServerReply(Message message) {
        ServerReadyEvent event = read(message, ServerReadyEvent.class);
        activeQueuesRepository.registerServerQueue(event.getValue());
    }

    private void handleNewNep(Message message) {
        NepRegisteredEvent event = read(message, NepRegisteredEvent.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(event.getValue().getId());
        if (blueprint != null) { //The new nep its related to one already stored
            //This operation is duplicated in the broadcast reply but has no effect, left here for clarity
            nepRepository.registerRemoteNodes(event.getValue());
            NepRegisteredEventReply reply = new NepRegisteredEventReply(blueprint.getNepId(), blueprint.getNodesIds());
            rabbit.send(
                    "", //Default exchange
                    message.getMessageProperties().getReplyTo(),
                    reply.toMessage());
        }
    }

    private void handelNewNepReply(Message message) {
        NepRegisteredEventReply event = read(message, NepRegisteredEventReply.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(event.getValue().getId());
        if (blueprint != null) {
            nepRepository.registerRemoteNodes(event.getValue());
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
        //Answer rpc call
        rabbit.send(message.getMessageProperties().getReplyTo(), Util.emptyMessage());
    }

    private void handleComputationFinished(Message message) {
        ComputationFinishedEvent event = read(message, ComputationFinishedEvent.class);
        RemoteNepExecutor activeNep = nepRepository.findActiveNep(event.getValue().nepId, event.getValue().compId);
        if (activeNep != null) {
            activeNep.stopListening();
        }
        rabbit.send(message.getMessageProperties().getReplyTo(), Util.emptyMessage());
    }

    public void broadcastServerReady() {
        rabbit.send(
                AppConfiguration.BROADCAST_EXCHANGE,
                "",
                new ServerReadyEvent()
                        .toMessageBuilder()
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.info("Broadcasting server queue: " + SERVER_QUEUE);
    }

    public void broadcastNewRegisteredNep(NepBlueprint nepBlueprint) {
        rabbit.send(
                BROADCAST_EXCHANGE,
                "", //no routing
                new NepRegisteredEvent(nepBlueprint.getNepId(), nepBlueprint.getNodesIds())
                        .toMessageBuilder()
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.info("Broadcasting new nep: " + nepBlueprint.getNepId());
    }
}
