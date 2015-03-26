package com.nepfix.server.rabbit.event;


import com.nepfix.server.neps.RemoteNepInfo;

import java.util.List;

public class NepRegisteredEvent extends GenericMessage<RemoteNepInfo> {

    public NepRegisteredEvent(String nepId, List<String> nodes) {
        super(Kind.NEW_NEP, new RemoteNepInfo(nepId, nodes));
    }

}

