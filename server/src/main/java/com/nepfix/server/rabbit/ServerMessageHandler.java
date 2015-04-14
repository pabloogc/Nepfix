package com.nepfix.server.rabbit;

import com.nepfix.server.AppConfiguration;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.server.executor.RemoteNepExecutorFactory;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.neps.RemoteNepInfo;
import com.nepfix.server.network.ActiveServersRepository;
import com.nepfix.server.rabbit.messages.Action;
import com.nepfix.server.rabbit.messages.NepComputationInfo;
import com.nepfix.server.rabbit.messages.NepMessage;
import com.nepfix.sim.nep.NepBlueprint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.nepfix.server.AppConfiguration.BROADCAST_EXCHANGE;
import static com.nepfix.server.AppConfiguration.SERVER_QUEUE;
import static com.nepfix.server.rabbit.messages.NepMessage.getAction;
import static com.nepfix.server.rabbit.messages.NepMessage.parse;

@Component
public class ServerMessageHandler implements MessageListener {

    private static final Log logger = LogFactory.getLog(ServerMessageHandler.class);
    @Autowired private RabbitTemplate rabbit;
    @Autowired private ActiveServersRepository activeQueuesRepository;
    @Autowired private NepRepository nepRepository;
    @Autowired private RemoteNepExecutorFactory nepExecutorFactory;

    @Override public void onMessage(Message message) {
        Action action = getAction(message);
        if (action == null) {
            logger.error("Message not handled: " + "Kind is NULL!!");
            return;
        }

        logger.debug("Message received with action: " + action.name());
        switch (action) {
            case S_NEW_SERVER:
                handleNewServer(message);
                break;
            case S_NEW_SERVER_REPLY:
                handleNewServerReply(message);
                break;
            case S_NEW_NEP:
                handleNepRegistered(message);
                break;
            case S_NEW_NEP_REPLY:
                handleNepRegisteredReply(message);
                break;
            case S_COMPUTATION_STARTED:
                handleComputationStarted(message);
                break;
            case S_COMPUTATION_FINISHED:
                handleComputationFinished(message);
                break;
            case S_STOP:
                return;
            case UNKNOWN:
            default:
                logger.warn("Message not handled: " + action.name());
        }
    }

    private void handleNewServer(Message message) {
        activeQueuesRepository.registerServerQueue(message.getMessageProperties().getReplyTo());
        rabbit.send(
                "", //Default exchange
                message.getMessageProperties().getReplyTo(),
                NepMessage.toMessage(Action.S_NEW_SERVER_REPLY, SERVER_QUEUE));

    }

    private void handleNewServerReply(Message message) {
        String remoteServerQueue = parse(message);
        activeQueuesRepository.registerServerQueue(remoteServerQueue);
    }

    /**
     * {@link com.nepfix.server.NepController#registerNep(String)}
     */
    private void handleNepRegistered(Message message) {
        if (message.getMessageProperties().getReplyTo().equals(SERVER_QUEUE))
            return; //Broadcast came from this server

        RemoteNepInfo remoteNepInfo = parse(message);
        NepBlueprint blueprint = nepRepository.findBlueprint(remoteNepInfo.getId());
        if (blueprint != null) {
            nepRepository.registerRemoteQueue(remoteNepInfo);
            RemoteNepInfo reply = new RemoteNepInfo(blueprint.getNepId(), SERVER_QUEUE);
            //Reply rpc call
            rabbit.send(message.getMessageProperties().getReplyTo(), NepMessage.toMessage(Action.S_NEW_NEP_REPLY, reply));
        }
    }

    private void handleNepRegisteredReply(Message message) {
        RemoteNepInfo remoteNepInfo = parse(message);
        NepBlueprint blueprint = nepRepository.findBlueprint(remoteNepInfo.getId());
        if (blueprint != null) {
            nepRepository.registerRemoteQueue(remoteNepInfo);
        }
    }

    private void handleComputationStarted(Message message) {
        NepComputationInfo msg = parse(message);
        NepBlueprint blueprint = nepRepository.findBlueprint(msg.nepId);
        if (blueprint == null) return; //Nep is not on this machine
        RemoteNepExecutor activeNep = nepRepository.findActiveNep(msg.nepId, msg.computationId);
        if (activeNep != null) return; //This nep is already active
        RemoteNepExecutor nepExecutor = nepExecutorFactory.create(blueprint, msg.computationId);
        nepExecutor.startListening();
        nepRepository.registerActiveNep(nepExecutor);
        //Reply rpc call
        rabbit.send(message.getMessageProperties().getReplyTo(), RabbitUtil.emptyMessage());
    }

    private void handleComputationFinished(Message message) {
        NepComputationInfo msg = parse(message);
        RemoteNepExecutor executor = nepRepository.findActiveNep(msg.nepId, msg.computationId);
        if (executor != null) {
            executor.stopListening();
            nepRepository.unregisterActiveNep(executor);
        }
        //Reply rpc call
        rabbit.send(message.getMessageProperties().getReplyTo(), RabbitUtil.emptyMessage());
    }

    public void broadcastNewServer() {
        rabbit.send(
                AppConfiguration.BROADCAST_EXCHANGE,
                "",
                NepMessage.toBuilder(Action.S_NEW_SERVER)
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.debug("Broadcasting server queue: " + SERVER_QUEUE);
    }

    public void broadcastNewNep(NepBlueprint nepBlueprint) {
        rabbit.send(
                BROADCAST_EXCHANGE,
                "", //no routing
                NepMessage.toBuilder(Action.S_NEW_NEP,
                        new RemoteNepInfo(nepBlueprint.getNepId(), SERVER_QUEUE))
                        .setReplyTo(SERVER_QUEUE)
                        .build());
        logger.debug("Broadcasting new nep: " + nepBlueprint.getNepId());
    }

}
