package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.elements.util.ElementsUtils;

import java.util.List;

public class AppendProcessor implements Processor {

    private String id;
    private List<Rule> rules;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        rules = ElementsUtils.readAsList(new TypeToken<List<Rule>>() {
        }, args.getAsJsonArray().toString());
    }

    @Override public String process(String input) {
        for (Rule rule : rules) {
            if (rule.tail)
                input = input + rule.append;
            if (rule.head)
                input = rule.append + input;
        }

        return input;
    }

    @Override public String getId() {
        return id;
    }

    private static class Rule {
        public boolean tail;
        public boolean head;
        public String append;
    }
}
