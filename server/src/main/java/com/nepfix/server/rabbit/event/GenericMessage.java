package com.nepfix.server.rabbit.event;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

public abstract class GenericMessage<T> {

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    @Expose private Kind kind;
    @Expose private T value;

    public GenericMessage(Kind kind, T value) {
        this.kind = kind;
        this.value = value;
    }

    public static <T> T read(Message message, Class<T> clazz) {
        return GSON.fromJson(new String(message.getBody()), clazz);
    }

    public static Kind getKind(Message message){
        JsonObject object = GSON.fromJson(new String(message.getBody()), JsonObject.class);
        //Enum.valueOf does not work because a != a but a.equals(a) == true
        Optional<Kind> optional = Enums.getIfPresent(Kind.class, object.get("kind").getAsString());
        if(optional.isPresent())
            return optional.get();
        return null;
    }

    public byte[] toBytes() {
        return GSON.toJson(this).getBytes();
    }

    public Message toMessage(){
        return MessageBuilder.withBody(toBytes()).build();
    }

    public Kind getKind() {
        return kind;
    }

    public T getValue() {
        return value;
    }

    public static class StringMessage extends GenericMessage<String> {
        public StringMessage(Kind kind, String value) {
            super(kind, value);
        }
    }
}
