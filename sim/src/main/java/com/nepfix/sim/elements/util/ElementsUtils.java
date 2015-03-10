package com.nepfix.sim.elements.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ElementsUtils {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Functions.IntComp.class, new Functions.IntComp.Adapter())
            .registerTypeAdapter(Functions.IntervalComp.class, new Functions.IntervalComp.Adapter())
            .create();


    public static <T> T readAsList(TypeToken<T> token, String list) {
        return readAsList(token, list, GSON);
    }

    public static <T> T readAsList(TypeToken<T> token, String list, Gson gson) {
        return gson.fromJson(list, token.getType());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void putInListHashMap(K key, V element, HashMap<K, List<V>> map) {
        List tList = map.get(key);
        if (tList != null) {
            tList.add(element);
        } else {
            tList = new ArrayList<>();
            tList.add(element);
            map.put(key, tList);
        }
    }
}
