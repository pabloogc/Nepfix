package com.nepfix.server.executor;

import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteNepExecutorFactory {

    @Autowired AutowireCapableBeanFactory autowireCapableBeanFactory;

    public RemoteNepExecutor create(NepBlueprint blueprint, long computationId) {
        RemoteNepExecutor executor = new RemoteNepExecutor(
                autowireCapableBeanFactory,
                computationId,
                blueprint
        );
        return executor;
    }
}
