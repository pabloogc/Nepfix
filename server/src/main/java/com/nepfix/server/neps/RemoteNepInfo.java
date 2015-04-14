package com.nepfix.server.neps;

import com.google.gson.annotations.Expose;
import com.nepfix.server.rabbit.messages.NepMessage;

import java.util.List;


public class RemoteNepInfo {
    @Expose private final String nepId;
    @Expose private final String severQueue;

    public RemoteNepInfo(String nepId, String serverQueue) {
        this.nepId = nepId;
        this.severQueue = serverQueue;
    }


    public String getId() {
        return nepId;
    }

    public String getSeverQueue() {
        return severQueue;
    }
}
