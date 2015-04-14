package com.nepfix.server.rabbit.messages;

import com.google.gson.annotations.Expose;

public class NepComputationInfo {
    @Expose public final String nepId;
    @Expose public final long computationId;

    public NepComputationInfo(String nepId, long computationId) {
        this.nepId = nepId;
        this.computationId = computationId;
    }
}