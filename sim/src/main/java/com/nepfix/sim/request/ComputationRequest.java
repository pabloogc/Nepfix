package com.nepfix.sim.request;


import com.google.gson.annotations.Expose;

public class ComputationRequest {
    @Expose private String networkId;
    @Expose private String input;
    @Expose private long maxConfigurations;
    @Expose private long maxOutputs;
    @Expose private long computationId = 0;

    public ComputationRequest(String networkId, String input, long maxConfigurations, long maxOutputs, long computationId) {
        this.networkId = networkId;
        this.input = input;
        this.maxConfigurations = maxConfigurations <= 0 ? Long.MAX_VALUE : maxConfigurations;
        this.maxOutputs = maxOutputs <= 0 ? Long.MAX_VALUE : maxOutputs;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getInput() {
        return input;
    }

    public long getMaxConfigurations() {
        return maxConfigurations;
    }

    public long getMaxOutputs() {
        return maxOutputs;
    }

    public long getComputationId() {
        return computationId;
    }
}

