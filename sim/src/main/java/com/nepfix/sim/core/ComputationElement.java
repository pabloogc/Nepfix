package com.nepfix.sim.core;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nepfix.sim.nep.Nep;

public abstract class ComputationElement implements NepElement {

    private final Nep nep;
    private final String id;
    private final JsonObject json;

    public ComputationElement(Nep nep, JsonObject json) {
        this.json = json;
        this.nep = nep;
        JsonElement id = json.get("id");
        if (id == null)
            this.id = "__anonymous__";
        else
            this.id = id.getAsString();

    }

    public JsonObject getJson() {
        return json;
    }

    @Override public String getId() {
        return id;
    }

    public JsonObject getConfigFor(Object object) {
        return nep.getDefinition().get(object.getClass().getSimpleName()).getAsJsonObject();
    }

    public JsonObject getConfigFor(String name) {
        return nep.getDefinition().get(name).getAsJsonObject();
    }

    public JsonObject getConfig(){
        return nep.getDefinition();
    }
}
