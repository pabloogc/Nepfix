package com.nepfix.sim.elements;

import com.nepfix.sim.core.Processor;

import java.util.Map;

public class ReplaceProcessor implements Processor {

    private String target;
    private String replacement;
    private String id;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
        this.target = args.get("target");
        this.replacement = args.get("replacement");
    }

    @Override public String process(String input) {
        return input.replaceFirst(target, replacement);
    }

    @Override public String getId() {
        return id;
    }
}
