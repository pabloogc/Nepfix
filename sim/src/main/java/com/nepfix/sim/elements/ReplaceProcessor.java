package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.Processor;

import java.util.List;

public class ReplaceProcessor implements Processor {

    private List<Rule> rules;
    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        rules = ElementsUtils.readAsList(new TypeToken<List<Rule>>(){}, args.getAsJsonArray().toString());
    }

    @Override public String process(String input) {
        for (Rule rule : rules) {
            input = input.replaceFirst(rule.target, rule.replacement);
        }
        return input;
    }

    @Override public String getId() {
        return id;
    }

    private static class Rule {
        public String target;
        public String replacement;
    }
}
