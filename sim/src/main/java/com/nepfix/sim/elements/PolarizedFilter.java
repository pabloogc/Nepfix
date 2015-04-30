package com.nepfix.sim.elements;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.elements.util.Functions;
import com.nepfix.sim.nep.Nep;

import java.io.File;
import java.util.List;

public class PolarizedFilter extends ComputationElement implements Filter {

    private int polarity;

    public PolarizedFilter(Nep nep, JsonObject json) {
        super(nep, json);
        String polarityString = json.get("polarity").getAsString();
        if (polarityString == null) {
            polarityString = "";
        }
        if (polarityString.startsWith("+")) {
            this.polarity = +1;
        } else if (polarityString.startsWith("-")) {
            this.polarity = -1;
        } else if (polarityString.startsWith("0")) {
            this.polarity = 0;
        } else {
            throw new IllegalArgumentException("polarity must be one of: +, -, 0");
        }
    }

    @Override public boolean accept(String input, boolean isInput) {
        if (isInput) {
            return polarityForWord(input) == polarity;
        } else {
            return polarityForWord(input) != polarity;
        }
    }

    public int polarityForWord(String word) {
        int sum = 0;
        String[] split = word.split("\\.");
        for (String symbol : split) {
            sum += weightForSymbol(symbol);
        }
        if(sum > 0) return 1;
        if(sum < 0) return -1;
        return 0;
    }

    public int weightForSymbol(String symbol) {
        JsonElement element = getConfigFor(this).get(symbol);
        if (element == null) {
            return 0;
        }
        return element.getAsInt();
    }
}
