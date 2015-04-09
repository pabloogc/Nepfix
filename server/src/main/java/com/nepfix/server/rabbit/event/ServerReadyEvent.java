package com.nepfix.server.rabbit.event;


public class ServerReadyEvent extends GenericMessage.StringMessage {
    public ServerReadyEvent() {
        super(Kind.SERVER_READY, null);
    }
}
