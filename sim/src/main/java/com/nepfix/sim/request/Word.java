package com.nepfix.sim.request;


import com.google.gson.annotations.Expose;
import com.nepfix.sim.core.Node;

public class Word {

    @Expose private final String value;
    @Expose private final long configuration;
    @Expose private final String destinyNode;

    public Word(String value, String destinyNode, long configuration) {
        this.value = value;
        this.destinyNode = destinyNode;
        this.configuration = configuration;
    }

    public String getValue() {
        return value;
    }

    public long getConfiguration() {
        return configuration;
    }

    public String getDestinyNode() {
        return destinyNode;
    }
}
