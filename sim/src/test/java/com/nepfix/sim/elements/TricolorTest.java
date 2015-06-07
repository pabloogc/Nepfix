package com.nepfix.sim.elements;

import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import org.junit.Test;


public class TricolorTest {
    private static final NepBlueprint blueprint = NepReader.loadBlueprintResource("6x6.json");

    @Test public void testTricolor() {
        final Nep nep = blueprint.create(0);
        nep.getId();
    }

}