package com.nepfix.sim.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nepfix.sim.nep.Nep;
import org.junit.Assert;
import org.junit.Test;


public class GeneralizedFilterTest2 {
    private static final JsonObject element;
    private static final JsonObject nepDef;
    private static final Nep nep;

    static {
        nepDef = new JsonObject();
        JsonObject weights = new JsonObject();
        weights.addProperty("a", 1);
        weights.addProperty("b", -1);
        weights.addProperty("c", -1);
        weights.addProperty("d", 0);
        nepDef.add(GeneralizedFilter.class.getSimpleName(), weights);

        element = new JsonObject();
        JsonArray rules = new JsonArray();

        JsonObject rule1 = new JsonObject();
        rule1.addProperty("symbols", "a");
        rule1.addProperty("interval", "(2,+inf]");

        rules.add(rule1);

        element.add("rules", rules);
        nep = new Nep("0", nepDef, 0);
    }

    @Test public void test1() {
        GeneralizedFilter filter = new GeneralizedFilter(nep, element);
        Assert.assertEquals(1, filter.weightForSymbol('a'));
        Assert.assertTrue(filter.accept("ababa", true));
        Assert.assertFalse(filter.accept("abbba", true));
        Assert.assertTrue(filter.accept("abbba", false));
    }
}