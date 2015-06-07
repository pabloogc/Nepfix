package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import org.junit.Assert;
import org.junit.Test;


public class EvolutionaryProcessorTest {
    private static final NepBlueprint blueprint = NepReader.loadBlueprintResource("evolutionary_test_file.json");
    private static final Nep nep = blueprint.create(0);

    @Test public void testAdd() {
        EvolutionaryProcessor processor = (EvolutionaryProcessor) nep.findProcessor("0");
        Assert.assertEquals("a.b", processor.processOne("b"));
    }

    @Test public void testAddRandom() {
        EvolutionaryProcessor processor = (EvolutionaryProcessor) nep.findProcessor("1");
        Assert.assertTrue(processor.processOne("b").matches("(a|b|c)\\.b"));
    }

    @Test public void testReplace() {
        EvolutionaryProcessor processor = (EvolutionaryProcessor) nep.findProcessor("2");
        Assert.assertEquals("b", processor.processOne("a"));
    }

    @Test public void testReplaceRandom() {
        EvolutionaryProcessor processor = (EvolutionaryProcessor) nep.findProcessor("3");
        Assert.assertTrue(processor.processOne("a").matches("b|c"));
    }
}