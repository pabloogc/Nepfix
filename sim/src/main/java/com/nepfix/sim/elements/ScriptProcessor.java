package com.nepfix.sim.elements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.nep.Nep;

import javax.script.*;
import java.util.List;

public class ScriptProcessor extends ComputationElement implements Processor {

    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("jython");
    private final String code;
    private final LinkedTreeMap<? extends String, Object> configAsTree;

    public ScriptProcessor(Nep nep, JsonObject element) {
        super(nep, element);
        JsonArray codeArray = getJson().getAsJsonObject().get("code").getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (JsonElement jsonElement : codeArray) {
            sb.append(jsonElement.getAsString()).append("\n");
        }
        //TODO: Create an scope to reuse the same scriptEngine
        configAsTree = new Gson().fromJson(getConfig(),
                new TypeToken<LinkedTreeMap<? extends String, Object>>() {
                }.getType());
        code = sb.toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> process(List<String> input) {
        Bindings bindings = new SimpleBindings();
        bindings.put("input", input);
        bindings.putAll(configAsTree);
        try {
            scriptEngine.eval(code, bindings);
            Object output = bindings.get("output");
            if (output == null) {
                throw new NullPointerException("output not set in code: " + code);
            }
            return (List<String>) output;
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
