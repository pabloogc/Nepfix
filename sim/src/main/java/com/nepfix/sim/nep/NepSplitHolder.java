package com.nepfix.sim.nep;

import com.google.gson.annotations.Expose;
import com.nepfix.sim.request.Word;

import java.util.HashMap;
import java.util.List;

public class NepSplitHolder {
    @Expose public NepBlueprint blueprint;
    @Expose private final HashMap<Long, List<Word>> activeConfigurations = new HashMap<>();
    @Expose private long configuration;
}
