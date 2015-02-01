package com.nepfix.sim.elements;

import com.nepfix.sim.core.Filter;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsFilter implements Filter {


    private String value;
    private int times;

    private BiFunction<Integer, Integer, Boolean> function;

    private String id;
    private Pattern pattern;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
        this.value = args.get("value");
        this.times = Integer.parseInt(args.get("times"));
        this.function = getOperator(args.get("operator"));
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

    private final BiFunction<Integer, Integer, Boolean> getOperator(String op) {
        switch (op) {
            case "!=":
                return (a, b) -> !Objects.equals(a, b);
            case "==":
                return Objects::equals;
            case ">=":
                return (a, b) -> a >= b;
            case ">":
                return (a, b) -> a > b;
            case "<":
                return (a, b) -> a < b;
            case "<=":
                return (a, b) -> a <= b;
            default:
                throw new IllegalArgumentException("Unknown operator " + op);
        }
    }
}
