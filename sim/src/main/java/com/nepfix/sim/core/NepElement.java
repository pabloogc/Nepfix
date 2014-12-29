package com.nepfix.sim.core;

import java.util.Map;

public interface NepElement {
    void init(String id, Map<String, String> args);

    String getId();
}
