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
            throw new IllegalArgumentException("Element with params: \"" + json.toString() + "\" does not have an id ");

        this.id = id.getAsString();
    }

    public JsonObject getJson() {
        return json;
    }

    @Override public String getId() {
        return id;
    }
}
