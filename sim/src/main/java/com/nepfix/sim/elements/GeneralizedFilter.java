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

import java.util.List;

public class GeneralizedFilter extends ComputationElement implements Filter {

    private List<Rule> rules;

    public GeneralizedFilter(Nep nep, JsonObject json) {
        super(nep, json);
        rules = ElementsUtils.readList(
                json.get("rules"), new TypeToken<List<Rule>>() {
                });
    }

    @Override public boolean accept(String input, boolean isInput) {
        if (isInput) {
            return rules.parallelStream().anyMatch(r -> phi(input, r));
        } else {
            return rules.parallelStream().allMatch(r -> !phi(input, r));
        }
    }

    private static class Rule {
        @Expose public String symbols;
        @Expose public Functions.IntervalComp interval;
    }

    private boolean phi(String word, Rule rule) {
        int[] intersectCount = ElementsUtils.intersectCount(word, rule.symbols);
        int sum = 0;
        for (int i = 0; i < intersectCount.length; i++) {
            sum += intersectCount[i] * weightForSymbol(word.charAt(i));
        }
        return rule.interval.contains(sum);
    }

    private int weightForSymbol(char symbol) {
        JsonElement element = getConfigFor(this).get(Character.toString(symbol));
        if (element == null) {
            return 0;
        }
        return element.getAsInt();
    }
}
