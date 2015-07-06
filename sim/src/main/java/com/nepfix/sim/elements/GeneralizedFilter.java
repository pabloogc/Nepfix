package com.nepfix.sim.elements;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.elements.util.Functions;
import com.nepfix.sim.nep.Nep;

import java.util.Collections;
import java.util.List;

public class GeneralizedFilter extends ComputationElement implements Filter {

    private List<Rule> rules;

    public GeneralizedFilter(Nep nep, JsonObject json) {
        super(nep, json);
        rules = ElementsUtils.readList(
                json.get("intervals"), new TypeToken<List<Rule>>() {
                });
        if (rules == null) {
            rules = Collections.emptyList();
        }
    }

    @Override public boolean accept(String input, boolean isInput) {
        if (isInput) {
            return rules.stream().anyMatch(r -> phiInInterval(input, r));
        } else {
            return rules.stream().allMatch(r -> !phiInInterval(input, r));
        }
    }

    private static class Rule {
        @Expose public String symbols = "";
        @Expose public Functions.IntervalComp interval;
    }

    public boolean phiInInterval(String word, Rule rule) {
        int sum = 0;
        String[] split = word.split("\\.");
        for (String symbol : split) {
            sum += ElementsUtils.timesContained(rule.symbols, symbol) * weightForSymbol(symbol);
        }
        return rule.interval.contains(sum);
    }

    public int weightForSymbol(String symbol) {
        JsonElement element = getConfigFor("symbolsWeights").get(symbol);
        if (element == null) {
            return 0;
        }
        return element.getAsInt();
    }

    public List<Rule> getRules() {
        return rules;
    }
}
