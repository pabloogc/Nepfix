package com.nepfix.sim.elements.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
}
