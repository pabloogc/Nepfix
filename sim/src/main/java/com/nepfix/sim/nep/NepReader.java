package com.nepfix.sim.nep;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.core.NepElement;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.core.RemoteNode;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NepReader {

    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    private static final Gson CUSTOM_GSON = new GsonBuilder()
            .registerTypeAdapter(Processor.class, new ProcessorAdapter())
            .registerTypeAdapter(Filter.class, new FilterAdapter())
            .registerTypeAdapter(Node.class, new NodeAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static NepBlueprint load(Reader stream) {
        NepBlueprint nepBlueprint = read(stream);
        nepBlueprint.link();
        return nepBlueprint;
    }

    private static NepBlueprint read(Reader reader){
        return CUSTOM_GSON.fromJson(reader, NepBlueprint.class);
    }

    private static class NodeAdapter implements JsonDeserializer<Node> {
        public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            if(jsonObject.has("remote") && jsonObject.get("remote").getAsBoolean()){
                return GSON.fromJson(json, RemoteNode.class);
            } else {
                return GSON.fromJson(json, Node.class);
            }
        }
    }

    private static class ProcessorAdapter implements JsonDeserializer<Processor> {
        public Processor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return readAndInit(json);
        }
    }

    private static class FilterAdapter implements JsonDeserializer<Filter> {
        public Filter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return readAndInit(json);
        }
    }


    @SuppressWarnings("unchecked")
    private static <T extends NepElement> T readAndInit(JsonElement json) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get("class").getAsString();
        String id = jsonObject.get("id").getAsString();
        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            T element = clazz.newInstance();
            Map<String, String> args = new HashMap<>();
            if (jsonObject.has("args")) {
                args = GSON.fromJson(jsonObject.get("args"),
                        new TypeToken<Map<String, String>>() {
                        }.getType());
            }
            element.init(id, args);
            return element;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new JsonParseException(e);
        }
    }
}
