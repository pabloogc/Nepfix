package com.nepfix.sim.elements.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class Functions {
    public static IntComp readIntComparatorFunc(String op) {
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

    public interface IntComp extends BiFunction<Integer, Integer, Boolean> {
        public static class Adapter implements JsonDeserializer<IntComp> {
            @Override
            public IntComp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return readIntComparatorFunc(json.getAsString());
            }
        }
    }

    public static class IntervalComp {

        private static final Pattern intervalRegex = Pattern.compile("(\\(|\\[)((\\+?|-)(inf|\\d+)),((\\+?|-)(inf|\\d+))(\\)|\\])");
        private int lowerBound;
        private boolean lowerInclusive;
        private int upperBound;
        private boolean upperInclusive;

        public IntervalComp(int lowerBound, boolean lowerInclusive, int upperBound, boolean upperInclusive) {
            this.lowerBound = lowerBound;
            this.lowerInclusive = lowerInclusive;
            this.upperBound = upperBound;
            this.upperInclusive = upperInclusive;
        }

        public boolean contains(int value) {
            boolean contained;
            if (lowerInclusive)
                contained = value >= lowerBound;
            else
                contained = value > lowerBound;

            if (upperInclusive)
                contained = contained && value <= upperBound;
            else
                contained = contained && value < upperBound;

            return contained;

        }

        public static class Adapter implements JsonDeserializer<IntervalComp> {
            @Override
            public IntervalComp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String intervalString = json.getAsString().replace(" ", "");
                Matcher matcher = intervalRegex.matcher(intervalString);
                if (!matcher.find())
                    throw new IllegalArgumentException("Invalid interval syntax: " + intervalString);

                boolean lowerInclusive = matcher.group(1).equals("[");
                boolean upperInclusive = matcher.group(8).equals("]");
                int lowerBound = parseBound(matcher.group(3), matcher.group(4));
                int upperBound = parseBound(matcher.group(6), matcher.group(5));

                IntervalComp intervalComp = new IntervalComp(lowerBound, lowerInclusive, upperBound, upperInclusive);
                return intervalComp;
            }

            private int parseBound(String sign, String value) {
                int s = sign.equals("-") ? -1 : 1;
                int v = value.equals("inf") ? Integer.MAX_VALUE : Integer.parseInt(value);
                return s * v;
            }

        }
    }
}
