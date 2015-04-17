package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.nep.Nep;

public class AcceptAllFilter extends ComputationElement implements Filter {

    public AcceptAllFilter(Nep nep, JsonObject element) {
        super(nep, element);
    }

    @Override public boolean accept(String input, boolean isInput) {
        return true;
    }
}
