package com.nepfix.sim.elements;

import com.nepfix.sim.core.Processor;

import java.util.Map;

public class UnitProcessor implements Processor {

    private String id;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
    }

    @Override public String process(String input) {
        return input;
    }

    @Override public String getId() {
        return id;
    }
}
