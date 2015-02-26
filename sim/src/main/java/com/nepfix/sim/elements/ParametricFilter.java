package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.elements.util.Functions;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParametricFilter implements Filter {


    private HashMap<String, Integer> weights;
    private List<Rule> rules;
    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        this.rules = ElementsUtils.readAsList(new TypeToken<List<Rule>>() {
        }, args.getAsJsonArray().toString());
        if (rules != null) {
            rules.forEach(r -> r.pattern = Pattern.compile(r.value));
        }
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String word, boolean in) {
        Predicate<Rule> predicate = r -> {
            Matcher matcher = r.pattern.matcher(word);
            int totalWeight = 0;
            while (matcher.find())
                totalWeight += r.weight;
            return r.interval.contains(totalWeight);
        };

        if (in)
            return rules.stream().anyMatch(predicate); //Accept as input if inside one interval
        else
            return rules.stream().allMatch(predicate.negate()); //Accept as output if outside all intervals
    }

    private static class Rule {
        public Pattern pattern;
        public String value;
        public int weight;
        public Functions.IntervalComp interval;
    }
}
