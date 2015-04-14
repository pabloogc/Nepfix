package com.nepfix.server.executor;

import com.nepfix.server.neps.NepRepository;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NepRPC implements InitializingBean {

    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbit;
    @Autowired private NepRepository nepRepository;
    @Autowired private RemoteNepExecutorFactory nepExecutorFactory;

    @Override public void afterPropertiesSet() throws Exception {

    }

}
