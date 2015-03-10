package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.nep.Nep;

import java.util.List;

public class UnitProcessor extends ComputationElement implements Processor {

    public UnitProcessor(JsonObject element, Nep nep) {
        super(nep, element);
    }

    public List<String> process(List<String> input) {
        return input;
    }
}
