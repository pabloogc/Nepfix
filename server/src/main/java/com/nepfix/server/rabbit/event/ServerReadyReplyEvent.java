package com.nepfix.server.rabbit.event;


public class ServerReadyReplyEvent extends GenericMessage.StringMessage {
    public ServerReadyReplyEvent(String value) {
        super(Kind.SERVER_READY_REPLY, value);
    }
}
