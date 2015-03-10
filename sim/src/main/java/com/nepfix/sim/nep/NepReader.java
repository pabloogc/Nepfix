package com.nepfix.sim.nep;

import com.google.gson.*;

import java.io.Reader;

public class NepReader {

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static NepBlueprint loadBlueprint(Reader stream) {
        return read(stream);
    }

    private static NepBlueprint read(Reader reader) {
        return GSON.fromJson(reader, NepBlueprint.class);
    }
}
