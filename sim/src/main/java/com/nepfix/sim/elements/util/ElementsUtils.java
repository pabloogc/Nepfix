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

    public static boolean intersect(String word1, String word2) {
        for (int i = 0; i < word1.toCharArray().length; i++) {
            if (word2.indexOf(word1.charAt(i)) != -1)
                return true; //one found
        }
        return false;
    }


    /**
     * @return true if all symbols of word1 are in word2
     */
    public static boolean allIntersect(String word1, String word2) {
        for (int i = 0; i < word1.toCharArray().length; i++) {
            if (word2.indexOf(word1.charAt(i)) == -1)
                return false; //one not found
        }
        return true;
    }


    public static int timesContaied(char symbol, String word) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == symbol) count++;
        }
        return count;
    }

}
