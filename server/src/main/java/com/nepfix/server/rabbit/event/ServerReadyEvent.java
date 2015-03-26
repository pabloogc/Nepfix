package com.nepfix.server.rabbit.event;


public class ServerReadyEvent extends GenericMessage.StringMessage {
    public ServerReadyEvent(String value) {
        super(Kind.SERVER_READY, value);
    }
}
