package com.nepfix.sim.elements;

import com.google.gson.JsonElement;
import com.nepfix.sim.core.Filter;

public class AcceptAllFilter implements Filter {


    private String id;

    @Override public void init(String id, JsonElement args) {
        this.id = id;
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String input, boolean in) {
        return true;
    }
}
