package com.nepfix.sim.elements.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.nep.NepReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ElementsUtils {


    public static <T> T readAsList(TypeToken<T> token, JsonElement list) {
        return readAsList(token, list, NepReader.GSON);
    }

    public static <T> T readAsList(TypeToken<T> token, JsonElement list, Gson gson) {
        return gson.fromJson(list, token.getType());
    }

}
