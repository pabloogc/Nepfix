package com.nepfix.sim.core.impl;

import com.nepfix.sim.core.Processor;

import java.util.Map;

public class AppendProcessor implements Processor {

    private String id;
    private boolean tail;
    private boolean head;
    private String append;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
        if (args.containsKey("tail"))
            this.tail = Boolean.parseBoolean(args.get("tail"));
        if (args.containsKey("head"))
            this.head = Boolean.parseBoolean(args.get("head"));
        this.append = args.get("append");
    }

    @Override public String process(String input) {
        String out = input;
        if (tail)
            out = input + append;
        if (head)
            out = append + out;
        return out;
    }

    @Override public String getId() {
        return id;
    }
}
