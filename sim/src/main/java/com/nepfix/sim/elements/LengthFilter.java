package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.nepfix.sim.core.Filter;

import java.util.function.BiFunction;

public class LengthFilter implements Filter {


    private int value;

    private BiFunction<Integer, Integer, Boolean> function;

    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        this.value = Integer.parseInt(args.getAsJsonObject().get("value").getAsString());
        this.function = ElementsUtils.readBinaryOperator(args.getAsJsonObject().get("operator").getAsString());
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String input) {
        return function.apply(input.length(), value);
    }

}
