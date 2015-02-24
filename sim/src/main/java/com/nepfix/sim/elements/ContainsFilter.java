package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.nepfix.sim.core.Filter;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsFilter implements Filter {


    private String value;
    private int times;

    private BiFunction<Integer, Integer, Boolean> function;

    private String id;
    private Pattern pattern;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
        this.value = args.getAsJsonObject().get("value").getAsString();
        this.times = Integer.parseInt(args.getAsJsonObject().get("times").getAsString());
        this.function = ElementsUtils.readBinaryOperator(args.getAsJsonObject().get("operator").getAsString());
        this.pattern = Pattern.compile(value);
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String input) {
        Matcher matcher = pattern.matcher(input);
        int count = 0;
        while (matcher.find())
            count++;
        Boolean apply = function.apply(count, times);
        return apply;
    }
}
