package com.nepfix.server.neps;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by pablo on 3/24/15.
 */
public class RemoteNepInfo {
    @Expose private final String nepId;
    @Expose private final List<String> nodes;

    public RemoteNepInfo(String nepId, List<String> nodes) {
        this.nepId = nepId;
        this.nodes = nodes;
    }


    public String getId() {
        return nepId;
    }

    public List<String> getNodes() {
        return nodes;
    }
}
