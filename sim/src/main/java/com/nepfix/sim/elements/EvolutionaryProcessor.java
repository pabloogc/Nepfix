package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.nep.Nep;

public class EvolutionaryProcessor extends UnitProcessor {

    public EvolutionaryProcessor(Nep nep, JsonObject element) {
        super(nep, element);
    }
}
