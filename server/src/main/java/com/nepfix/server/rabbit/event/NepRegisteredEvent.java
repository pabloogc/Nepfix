package com.nepfix.server.rabbit.event;


import com.nepfix.server.neps.RemoteNepInfo;

public class NepRegisteredEvent extends GenericMessage<RemoteNepInfo> {

    public NepRegisteredEvent(String nepId, String serverQueue) {
        super(Kind.NEW_NEP, new RemoteNepInfo(nepId, serverQueue));
    }

}

