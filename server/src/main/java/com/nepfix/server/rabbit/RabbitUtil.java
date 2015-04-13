package com.nepfix.server.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

public class RabbitUtil {

    public static Message emptyMessage(){
        return MessageBuilder.withBody(new byte[0]).build();
    }

    public static MessageBuilder emptyMessageBuilder(){
        return MessageBuilder.withBody(new byte[0]);
    }

    public static String routingKey(Object... args){
        StringBuilder sb = new StringBuilder();
        sb.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(".").append(args[i].toString());
        }
        return sb.toString();
    }

}
