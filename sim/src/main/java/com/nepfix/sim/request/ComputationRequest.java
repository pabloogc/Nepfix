package com.nepfix.sim.request;


import com.google.gson.annotations.Expose;

public class ComputationRequest {
    @Expose private String networkId;
    @Expose private String input;
    @Expose private long maxConfiguration;
    @Expose private long maxOutputs;

    public ComputationRequest(String networkId, String input, long maxConfiguration, long maxOutputs) {
        this.networkId = networkId;
        this.input = input;
        this.maxConfiguration = maxConfiguration <= 0 ? Long.MAX_VALUE : maxConfiguration;
        this.maxOutputs = maxOutputs <= 0 ? Long.MAX_VALUE : maxOutputs;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getInput() {
        return input;
    }

    public long getMaxConfiguration() {
        return maxConfiguration;
    }

    public long getMaxOutputs() {
        return maxOutputs;
    }
}
