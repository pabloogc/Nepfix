package com.nepfix.sim.request;


import com.google.gson.annotations.Expose;

public class ComputationRequest {
    @Expose private String networkId;
    @Expose private String input;
    @Expose private int maxResults;
    @Expose private long timeoutMillis;

    public ComputationRequest(String input, String networkId, long timeoutMillis) {
        this(input, networkId, timeoutMillis, Integer.MAX_VALUE);
    }

    public ComputationRequest(String input, String networkId, long timeoutMillis, int maxResults) {
        this.networkId = networkId;
        this.input = input;
        this.maxResults = maxResults;
        this.timeoutMillis = timeoutMillis;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getInput() {
        return input;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}
