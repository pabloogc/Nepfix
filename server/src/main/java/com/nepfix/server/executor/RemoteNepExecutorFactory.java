package com.nepfix.server.executor;

import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteNepExecutorFactory {

    @Autowired AutowireCapableBeanFactory autowireCapableBeanFactory;

    public RemoteNepExecutor create(NepBlueprint blueprint, long computationId) {
        RemoteNepExecutor executor = new RemoteNepExecutor(
                computationId,
                blueprint
        );
        autowireCapableBeanFactory.autowireBean(executor);
        executor.init();
        return executor;
    }
}
