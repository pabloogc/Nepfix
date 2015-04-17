package com.nepfix.sim.elements.util;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.nep.Nep;
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

    public static boolean intersect(String word, String container) {
        for (int i = 0; i < word.toCharArray().length; i++) {
            if (container.indexOf(word.charAt(i)) != -1)
                return true;
        }
        return false;
    }

    public static int[] intersectCount(String word, String container){
        int[] result = new int[word.length()];
        for (int i = 0; i < word.length(); i++) {
            for (int j = 0; j < container.length(); j++) {
                if(word.charAt(i) == container.charAt(j))
                    result[i]++;
            }
        }
        return result;
    }

}
