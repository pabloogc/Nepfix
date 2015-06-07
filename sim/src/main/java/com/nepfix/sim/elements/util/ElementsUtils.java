package com.nepfix.sim.elements.util;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.nep.NepReader;

import java.util.Collections;
import java.util.List;

public class ElementsUtils {

    public static <T extends List<?>> T readList(JsonElement list, TypeToken<T> token) {
        return NepReader.GSON.fromJson(list, token.getType());
    }

    public static List<String> readAsListString(JsonElement list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return NepReader.GSON.fromJson(list, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * @return true if any symbols of word1 are in word2
     */
    public static boolean anyIntersect(String word1, String word2) {
        String[] split = word1.split("\\.");
        for (String s1 : split) {
            if (containsSymbol(word2, s1)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return true if all symbols of word1 are in word2
     */
    public static boolean allIntersect(String word1, String word2) {
        String[] split = word2.split("\\.");
        for (String s1 : split) {
            if (!containsSymbol(word1, s1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsSymbol(String word, String symbol) {
        String[] split = word.split("\\.");
        for (String s : split) {
            if (s.equals(symbol)) {
                return true;
            }
        }
        return false;
    }


    public static int timesContained(String word, String symbol) {
        int count = 0;
        String[] split = word.split("\\.");
        for (String symbol2 : split) {
            if (symbol.equals(symbol2)) count++;
        }
        return count;
    }

    public static String concat(String... words) {
        String out = "";
        for (String word : words) {
            out = concat(out, word);
        }
        return out;
    }


    public static String concat(String word1, String word2) {
        if (word1.isEmpty()) return word2;
        if (word2.isEmpty()) return word1;
        return word1 + (word1.endsWith(".") ? word2 : "." + word2);
    }

    public static String[] splitAt(String word, int i) {
        String[] out = {"", ""};
        String[] split = word.split("\\.");
        for (int j = 0; j < i; j++) {
            out[0] = concat(out[0], split[j]);
        }
        for (int j = i; j < split.length; j++) {
            out[1] = concat(out[1], split[j]);
        }
        return out;
    }

    public static int symbolCount(String word) {
        return word.split("\\.").length;
    }

    public static int firstIndexOf(String word, String symbol) {
        return circularIndexOf(word, symbol, 0);
    }

    public static int lastIndexOf(String word, String symbol) {
        return circularIndexOf(word, symbol, symbolCount(word) - 1);
    }

    public static int circularIndexOf(String word, String symbol, int startIndex) {
        String[] symbols = word.split("\\.");
        int max = symbols.length;
        for (int i = startIndex, j = 0; j < max; i++, j++) {
            if (i == symbols.length)
                i = 0;
            if (symbols[i].equals(symbol)) {
                return i;
            }
        }
        return -1;
    }
}
