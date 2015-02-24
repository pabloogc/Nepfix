package com.nepfix.sim.elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ElementsUtils {

    public static BiFunction<Integer, Integer, Boolean> readBinaryOperator(String op) {
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


    public static <T> T readAsList(TypeToken<T> token, String list) {
        return readAsList(token, list, new Gson());
    }

    public static <T> T readAsList(TypeToken<T> token, String list, Gson gson) {
        return gson.fromJson(list, token.getType());
    }
}
