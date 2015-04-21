package com.nepfix.sim.elements;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.elements.util.ElementsUtils;
import com.nepfix.sim.nep.Nep;

import java.util.stream.IntStream;

public class ClassicFilter extends ComputationElement implements Filter {

    private final boolean isWeak;
    //Permitted input, forbidden input, permitted output, forbidden output
    private final String pi;
    private final String po;
    private final String fi;
    private final String fo;

    public ClassicFilter(Nep nep, JsonObject element) {
        super(nep, element);
        JsonElement mode = element.get("mode");
        if (mode == null || !mode.getAsString().matches("strong|weak")) {
            throw new IllegalArgumentException("Mode must be weak or strong");
        }
        this.isWeak = element.get("mode").getAsString().equals("weak");
        this.pi = element.get("pi") != null ? element.get("pi").getAsString() : "";
        this.fi = element.get("fi") != null ? element.get("fi").getAsString() : "";
        this.po = element.get("po") != null ? element.get("po").getAsString() : "";
        this.fo = element.get("fo") != null ? element.get("fo").getAsString() : "";
    }

    @Override public boolean accept(String input, boolean isInput) {
        String permittedSet, forbiddenSet;
        if (isInput) {
            permittedSet = pi;
            forbiddenSet = fi;
        } else {
            permittedSet = po;
            forbiddenSet = fo;
        }

        boolean permittedCondition;
        boolean forbiddenCondition;

        if(isWeak){
            permittedCondition = ElementsUtils.intersect(permittedSet, input);
        } else { //strong
            permittedCondition = ElementsUtils.allIntersect(permittedSet, input);
        }

        forbiddenCondition = !ElementsUtils.intersect(input, forbiddenSet);

        return permittedCondition && forbiddenCondition;
    }
}