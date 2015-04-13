package com.nepfix.server.neps;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by pablo on 3/24/15.
 */
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
