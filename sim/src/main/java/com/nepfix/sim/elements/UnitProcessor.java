package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.nepfix.sim.core.Processor;

public class UnitProcessor implements Processor {

    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
    }

    @Override public String process(String input) {
        return input;
    }

    @Override public String getId() {
        return id;
    }
}
