package com.nepfix.server;

import com.google.common.eventbus.EventBus;
import com.nepfix.server.rabbit.ServerMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class AppConfiguration implements InitializingBean, DisposableBean {


    public static final String BROADCAST_EXCHANGE = "broadcast";
    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
    public static String SERVER_QUEUE = null;

    @Value("${rabbit.server}") private String server;
    @Autowired private RabbitTemplate rabbit;
    @Autowired private EventBus bus;
    @Autowired private ConnectionFactory connectionFactory;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private RabbitListenerEndpointRegistry registry;
    @Autowired private RabbitListenerContainerFactory containerFactory;
    @Autowired private ServerMessageHandler serverMessageHandler;
    private Binding binding;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(server);
    }

    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setReplyTimeout(5000); //5 seconds
        return template;
    }

    @Bean public EventBus getEventBus() {
        return new EventBus();
    }

    @Override public void afterPropertiesSet() throws Exception {
        //Declare broadcast channel (will do nothing if already created)
        logger.info("Server started");
        Exchange broadcastExchange = new FanoutExchange(BROADCAST_EXCHANGE);
        rabbitAdmin.declareExchange(broadcastExchange);

        Queue serverQueue = rabbitAdmin.declareQueue();
        SERVER_QUEUE = serverQueue.getName();

        binding = BindingBuilder
                .bind(serverQueue)
                .to(broadcastExchange)
                .with("")
                .noargs();
        rabbitAdmin.declareBinding(binding);

        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setQueueNames(SERVER_QUEUE);
        endpoint.setId(SERVER_QUEUE);
        endpoint.setMessageListener(serverMessageHandler);
        registry.registerListenerContainer(endpoint, containerFactory);
        logger.info("Server listening on: " + SERVER_QUEUE);

        serverMessageHandler.broadcastServerReady();
    }

    @Override public void destroy() throws Exception {
        rabbitAdmin.removeBinding(binding);
    }
}
