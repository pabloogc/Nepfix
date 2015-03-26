package com.nepfix.server.executor;

import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NepMessageHandler implements MessageListener, InitializingBean{

    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitTemplate rabbit;
    @Autowired private NepRepository nepRepository;
    @Autowired private NepExecutorFactory nepExecutorFactory;

    @Override public void afterPropertiesSet() throws Exception {

    }

    @Override public void onMessage(Message message) {
        System.out.println(new String(message.getBody()));
    }
}
