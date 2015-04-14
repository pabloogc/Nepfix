package com.nepfix.sim.nep;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.nepfix.sim.elements.util.Functions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NepReader {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Functions.IntComp.class, new Functions.IntComp.Adapter())
            .registerTypeAdapter(Functions.IntervalComp.class, new Functions.IntervalComp.Adapter())
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static NepBlueprint loadBlueprint(Reader stream) {
        try {
            String string = CharStreams.toString(stream);
            JsonObject blueprint = new ConfigReplacer().process(string);
            return GSON.fromJson(blueprint, NepBlueprint.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class ConfigReplacer {
        private static final Pattern regex = Pattern.compile("\\{\\{((\\w|\\.)*)\\}\\}");
        private Stack<String> stack = new Stack<>();
        private HashMap<String, String> paths = new HashMap<>();

        public ConfigReplacer() {
        }

        public JsonObject process(String string) throws IOException {
            traverse(new JsonReader(new StringReader(string)));
            Matcher matcher = regex.matcher(string);
            int lastMatch = 0;
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                sb.append(string.substring(lastMatch, matcher.start()));
                lastMatch = matcher.end();
                String key = matcher.group(1);
                sb.append(paths.get(key));
            }
            if (lastMatch == 0) {
                return GSON.fromJson(string, JsonObject.class);
            } else {
                sb.append(string.substring(lastMatch, string.length()));
                return GSON.fromJson(sb.toString(), JsonObject.class);
            }
        }

        private void traverse(JsonReader reader) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {
                JsonToken token = reader.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                    case END_ARRAY:
                    case END_OBJECT:
                        reader.skipValue();
                        stack.pop(); //pop the array name
                        break;
                    case BEGIN_OBJECT:
                        traverse(reader);
                        stack.pop(); //pop object name
                        break;
                    case NAME:
                        stack.push(reader.nextName());
                        break;
                    case STRING:
                    case NUMBER:
                    case BOOLEAN:
                    case NULL:
                        StringBuilder sb = new StringBuilder();
                        sb.append(stack.get(0));
                        for (int i = 1; i < stack.size(); i++) {
                            sb.append(".").append(stack.get(i));
                        }
                        paths.put(sb.toString(), reader.nextString());
                        stack.pop(); //pop value name
                        break;
                    case END_DOCUMENT:
                        break;
                }
            }
        }
    }
}
