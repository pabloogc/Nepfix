package com.nepfix.sim.elements.util;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.Word;

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
            if (containsSymbol(s1, word2)) {
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
            if (!containsSymbol(s1, word1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsSymbol(String symbol, String word){
        String[] split = word.split("\\.");
        for (String s : split) {
            if (s.equals(symbol)) {
                return true;
            }
        }
        return false;
    }


    public static int timesContained(String symbol, String word) {
        int count = 0;
        String[] split = word.split("\\.");
        for (String symbol2 : split) {
            if (symbol.equals(symbol2)) count++;
        }
        return count;
    }

}
