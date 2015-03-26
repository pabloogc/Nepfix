package com.nepfix.sim.nep;

import com.google.gson.*;
import com.nepfix.sim.elements.util.Functions;

import java.io.Reader;

public class NepReader {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Functions.IntComp.class, new Functions.IntComp.Adapter())
            .registerTypeAdapter(Functions.IntervalComp.class, new Functions.IntervalComp.Adapter())
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static NepBlueprint loadBlueprint(Reader stream) {
        return read(stream);
    }

    private static NepBlueprint read(Reader reader) {
        return GSON.fromJson(reader, NepBlueprint.class);
    }
}
