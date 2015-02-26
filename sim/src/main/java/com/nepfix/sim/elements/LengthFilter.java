package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.Functions;

public class LengthFilter implements Filter {


    private int value;
    private Functions.IntComp function;
    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        this.value = Integer.parseInt(args.getAsJsonObject().get("value").getAsString());
        this.function = Functions.readIntComparatorFunc(args.getAsJsonObject().get("operator").getAsString());
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String word, boolean in) {
        return function.apply(word.length(), value);
    }

}
