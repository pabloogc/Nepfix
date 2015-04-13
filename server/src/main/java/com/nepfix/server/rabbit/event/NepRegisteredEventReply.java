package com.nepfix.server.rabbit.event;


import com.nepfix.server.neps.RemoteNepInfo;

import java.util.List;

public class NepRegisteredEventReply extends GenericMessage<RemoteNepInfo> {

    public NepRegisteredEventReply(String nepId, String serverQueue) {
        super(Kind.NEW_NEP_REPLY, new RemoteNepInfo(nepId, serverQueue));
    }

}

