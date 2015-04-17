package com.nepfix.sim.elements;


import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.elements.util.Functions;
import com.nepfix.sim.nep.Nep;

import java.util.List;

public class GeneralizedProcessor extends ComputationElement implements Processor {

    /**
     * *, l, r
     */
    private String mode;
    /**
     * w [weak], s [strong]
     */
    private String inputMode;

    /**
     * w [weak], s [strong]
     */
    private String outputMode;

    private List<Rule> rules;

    public GeneralizedProcessor(Nep nep, JsonObject json) {
        super(nep, json);
        mode = json.get("mode").getAsString();
        inputMode = json.get("inputMode").getAsString();
        outputMode = json.get("outputMode").getAsString();
        rules = ElementsUtils.readAsList(new TypeToken<List<Rule>>() {
        }, json.get("rules"));
    }

    @Override public List<String> process(List<String> input) {
        return input;
    }

    private String processOne(String input){
        return input;
    }

    private static class Rule {
        public List<String> symbols;
        public Functions.IntervalComp interval;
    }

    private boolean phi(int rule){
        return true;
    }
}
