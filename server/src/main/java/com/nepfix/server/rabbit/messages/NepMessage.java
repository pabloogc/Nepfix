package com.nepfix.server.rabbit.messages;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;

public abstract class NepMessage {

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    protected NepMessage(){
        //No instances
    }

    public static <T> T parse(Message message) {
        Action action = getAction(message);
        return GSON.fromJson(new String(message.getBody()), action.getType());
    }

    public static Action getAction(Message message){
        String action = (String) message.getMessageProperties().getHeaders().getOrDefault("action", Action.UNKNOWN.name());
        //Enum.valueOf does not work because a != b but a.equals(b) == true in string comparations
        Optional<Action> optional = Enums.getIfPresent(Action.class, action);
        if(optional.isPresent())
            return optional.get();
        return Action.UNKNOWN;
    }

    private static byte[] bodyBytes(Object value) {
        return GSON.toJson(value).getBytes();
    }

    public static Message toMessage(Action action){
        return toMessage(action, null);
    }

    public static Message toMessage(Action action, Object body){
        return toBuilder(action, body).build();
    }

    public static MessageBuilderSupport<Message> toBuilder(Action action){
        return toBuilder(action, null);
    }

    public static MessageBuilderSupport<Message> toBuilder(Action action, Object body){
        return MessageBuilder
                .withBody(bodyBytes(body))
                .setHeader("action", action.name());
    }
}
