package com.nepfix.sim.core;

import com.google.gson.JsonElement;

import java.util.Map;

public interface NepElement {
    void init(String id, JsonElement args);

    String getId();
}
