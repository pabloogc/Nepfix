package com.nepfix.sim.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nepfix.sim.nep.Nep;
import org.junit.Assert;
import org.junit.Test;


public class GeneralizedFilterTest {
    private static final JsonObject element;
    private static final JsonObject nepDef;
    private static final Nep nep;

    static {
        nepDef = new JsonObject();
        JsonObject weights = new JsonObject();
        weights.addProperty("a", 1);
        weights.addProperty("b", 5);
        weights.addProperty("c", 10);
        weights.addProperty("d", -1);
        weights.addProperty("e", -5);
        weights.addProperty("f", -10);
        nepDef.add(GeneralizedFilter.class.getSimpleName(), weights);

        element = new JsonObject();
        JsonArray rules = new JsonArray();

        JsonObject rule1 = new JsonObject();
        rule1.addProperty("symbols", "abc");
        rule1.addProperty("interval", "[0,10]");

        JsonObject rule2 = new JsonObject();
        rule2.addProperty("symbols", "def");
        rule2.addProperty("interval", "(10,100]");

        rules.add(rule1);
        rules.add(rule2);

        element.add("rules", rules);
        nep = new Nep("0", nepDef, 0);
    }

    @Test public void test1() {
        GeneralizedFilter filter = new GeneralizedFilter(nep, element);
        Assert.assertTrue(filter.accept("a", true)); //1
        Assert.assertTrue(filter.accept("a a a b", true)); //8
        Assert.assertTrue(filter.accept("e", true)); //-5
        Assert.assertFalse(filter.accept("c c c c", true)); //40
    }
    @Test public void test2() {
        GeneralizedFilter filter = new GeneralizedFilter(nep, element);
        Assert.assertFalse(filter.accept("a", false)); //1
        Assert.assertFalse(filter.accept("a a a b", false)); //8
        Assert.assertFalse(filter.accept("e", false)); //-5
        Assert.assertTrue(filter.accept("c c c c", false)); //40
    }

}