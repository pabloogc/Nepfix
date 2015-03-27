package com.nepfix.server.executor;

import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NepExecutorFactory {

    @Autowired private NepRepository nepRepository;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private ConnectionFactory connectionFactory;
    @Autowired private ActiveQueuesRepository activeQueuesRepository;

    public RemoteNepExecutor create(NepBlueprint blueprint, long computationId) {
        return new RemoteNepExecutor(
                rabbitTemplate,
                rabbitAdmin,
                connectionFactory,
                activeQueuesRepository,
                computationId,
                nepRepository,
                blueprint
        );
    }
}
