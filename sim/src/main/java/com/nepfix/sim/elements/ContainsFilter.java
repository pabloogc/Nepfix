package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.elements.util.Functions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsFilter implements Filter {


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
        return rules.stream().anyMatch(r -> {
            Matcher matcher = r.pattern.matcher(word);
            int count = 0;
            while (matcher.find())
                count++;
            return r.operator.apply(count, r.times);
        });
    }

    private static class Rule {
        public Pattern pattern;
        public String value;
        public int times;
        public Functions.IntComp operator;
    }
}
