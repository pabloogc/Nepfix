package com.nepfix.server.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

public class Util {

    public static Message emptyMessage(){
        return MessageBuilder.withBody(new byte[0]).build();
    }

    public static MessageBuilder emptyMessageBuilder(){
        return MessageBuilder.withBody(new byte[0]);
    }
}
