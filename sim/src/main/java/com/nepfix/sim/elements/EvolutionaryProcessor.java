package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.nep.Nep;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EvolutionaryProcessor extends ComputationElement implements Processor {


    private static final Random RAND = new Random();

    private enum Place {
        LEFT, RIGHT, RANDOM;
    }

    private enum Action {
        INSERT, DELETE, REPLACE;
    }

    private final Place place;
    private final Action action;
    private final List<ReplaceRule> replaceRules = new ArrayList<>();
    private final List<String> insertOrDeleteRules = new ArrayList<>();
    private final String insertSymbol;
    private final boolean isRandomInsert;
    private final String[] randomSymbolSplit;


    private static class ReplaceRule {
        public final String symbol;
        public final boolean isRandom;
        public final String[] replacementSplit;
        public final String replacement;

        private ReplaceRule(String symbol, String replacement, String[] symbolSplit, boolean isRandom) {
            this.symbol = symbol;
            this.replacement = replacement;
            this.isRandom = isRandom;
            this.replacementSplit = symbolSplit;
        }
    }

    public EvolutionaryProcessor(Nep nep, JsonObject element) {
        super(nep, element);

        if (element.get("place") == null) {
            throw new IllegalArgumentException("place is not defined");
        }
        if (element.get("action") == null) {
            throw new IllegalArgumentException("action is not defined");
        }

        switch (element.get("place").getAsString()) {
            case "l":
                this.place = Place.LEFT;
                break;
            case "r":
                this.place = Place.RIGHT;
                break;
            case "*":
                this.place = Place.RANDOM;
                break;
            default:
                throw new IllegalArgumentException("Place must be one of: l (left), r (right), * (random)");
        }


        switch (element.get("action").getAsString()) {
            case "i":
                this.action = Action.INSERT;
                break;
            case "d":
                this.action = Action.DELETE;
                break;
            case "r":
                this.action = Action.REPLACE;
                if (element.get("rules") == null) {
                    throw new IllegalArgumentException("rules are not defined");
                }

                JsonObject jsonRules = element.get("rules").getAsJsonObject();
                jsonRules.entrySet().forEach(e -> {
                    String replacement = e.getValue().getAsString();
                    String[] symbolSplit;
                    boolean isRandom = replacement.split(",").length > 1;
                    if (isRandom) {
                        symbolSplit = replacement.split(",");
                    } else {
                        symbolSplit = null;
                    }
                    replaceRules.add(new ReplaceRule(e.getKey(), replacement, symbolSplit, isRandom));
                });
                break;
            default:
                throw new IllegalArgumentException("Action must be one of: i (insertion), d (deletion), r (replace)");
        }

        if (action == Action.INSERT || action == Action.DELETE) {
            if (element.get("symbol") == null)
                throw new IllegalArgumentException("symbol is null");
            this.insertSymbol = element.get("symbol").getAsString();
            this.isRandomInsert = this.insertSymbol.split(",").length > 1;
            if (isRandomInsert) {
                this.randomSymbolSplit = this.insertSymbol.split(",");
            } else {
                this.randomSymbolSplit = null;
            }
        } else {
            insertSymbol = null;
            isRandomInsert = false;
            randomSymbolSplit = null;
        }
    }

    @Override public List<String> process(List<String> input) {
        return input.stream().map(this::processOne).collect(Collectors.toList());
    }

    public String processOne(String word) {
        switch (action) {
            case INSERT:
                return doInsert(word);
            case DELETE:
                return doDelete(word);
            case REPLACE:
                return doReplace(word);
        }
        throw new IllegalArgumentException(); //Should never happen
    }

    private String doInsert(String word) {
        String symbol;
        if (isRandomInsert) {
            symbol = randomSymbolSplit[RAND.nextInt(randomSymbolSplit.length)];
        } else {
            symbol = this.insertSymbol;
        }

        switch (place) {
            case LEFT:
                return ElementsUtils.concat(symbol, word);
            case RIGHT:
                return ElementsUtils.concat(word, symbol);
            case RANDOM:
                int position = RAND.nextInt(ElementsUtils.symbolCount(word));
                String[] split = ElementsUtils.splitAt(word, position);
                return ElementsUtils.concat(split[0], word, split[1]);
        }
        return null;
    }

    private String doReplace(String word) {
        String[] symbols = word.split("\\.");
        for (ReplaceRule rule : replaceRules) {
            int idx = -1;
            switch (place) {
                case LEFT:
                    idx = ElementsUtils.firstIndexOf(word, rule.symbol);
                    break;
                case RIGHT:
                    idx = ElementsUtils.lastIndexOf(word, rule.symbol);
                    break;
                case RANDOM:
                    idx = ElementsUtils.circularIndexOf(word, rule.symbol, RAND.nextInt(symbols.length));
                    break;
            }

            if (idx == -1) continue;
            if (rule.isRandom) {
                symbols[idx] = rule.replacementSplit[RAND.nextInt(rule.replacementSplit.length)];
            } else {
                symbols[idx] = rule.replacement;
            }
            word = ElementsUtils.concat(symbols);
        }
        return word;
    }

    private String doDelete(String word) {
        throw new NotImplementedException();
    }

}
