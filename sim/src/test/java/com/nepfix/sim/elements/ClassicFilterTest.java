package com.nepfix.sim.elements;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;


public class ClassicFilterTest {

    private static final JsonObject weak;
    private static final JsonObject strong;

    static {
        weak = new JsonObject();
        weak.addProperty("mode", "weak");
        weak.addProperty("pi", "ac");
        weak.addProperty("fi", "d");
        weak.addProperty("po", "ad");
        weak.addProperty("fo", "c");

        strong = new JsonObject();
        strong.addProperty("mode", "strong");
        strong.addProperty("pi", "ac");
        strong.addProperty("fi", "d");
        strong.addProperty("po", "ad");
        strong.addProperty("fo", "c");
    }

    @Test public void test1Strong() {
        ClassicFilter filter = new ClassicFilter(null, strong);
        Assert.assertFalse(filter.accept("ababa", true));
        Assert.assertTrue(filter.accept("ababac", true));
    }

    @Test public void test1Weak() {
        ClassicFilter filter = new ClassicFilter(null, weak);
        Assert.assertTrue(filter.accept("a", true));
    }

    @Test public void testOut(){
        ClassicFilter filter1 = new ClassicFilter(null, weak);
        ClassicFilter filter2 = new ClassicFilter(null, strong);
        Assert.assertEquals(filter1.accept("c", false), filter2.accept("c",false));
    }

}