package com.nepfix.sim.elements;

import com.nepfix.sim.core.Filter;

import java.util.Map;

public class AcceptAllFilter implements Filter {


    private String id;

    @Override public void init(String id, Map<String, String> args) {
        this.id = id;
    }

    @Override public String getId() {
        return id;
    }

    @Override public boolean accept(String input) {
        return true;
    }
}
