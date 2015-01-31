package com.nepfix.sim.elements;

import com.nepfix.sim.core.Filter;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class LengthFilter implements Filter {


    private int value;

    private BiFunction<Integer, Integer, Boolean> function;

    private String id;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
        this.value = Integer.parseInt(args.get("value"));
        this.function = getOperator(args.get("operator"));
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String input) {
        return function.apply(input.length(), value);
    }

    private final BiFunction<Integer, Integer, Boolean> getOperator(String op) {
        switch (op) {
            case "!=":
                return (a,b) -> !Objects.equals(a, b);
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
