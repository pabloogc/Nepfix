package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.nep.Nep;

import java.util.List;
import java.util.stream.Collectors;

public class AppendProcessor extends ComputationElement implements Processor {

    private List<Rule> rules;

    public AppendProcessor(JsonObject element, Nep nep) {
        super(nep, element);
        rules = ElementsUtils.readAsList(new TypeToken<List<Rule>>() {
        }, getJson().get("rules").getAsJsonArray().toString());
    }

    public List<String> process(List<String> input) {
        return input.stream().map(this::processOne).collect(Collectors.toList());
    }

    private String processOne(String input) {
        for (Rule rule : rules) {
            if (rule.tail)
                input = input + rule.append;
            if (rule.head)
                input = rule.append + input;
        }

        return input;
    }

    private static class Rule {
        public boolean tail;
        public boolean head;
        public String append;
    }
}
