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
        weak.addProperty("pi", "a");
        weak.addProperty("fi", "b");
        weak.addProperty("po", "c");
        weak.addProperty("fo", "d");

        strong = new JsonObject();
        strong.addProperty("mode", "strong");
        strong.addProperty("pi", "a");
        strong.addProperty("fi", "b");
        strong.addProperty("po", "c");
        strong.addProperty("fo", "d");
    }

    @Test public void test1Weak() {
        ClassicFilter filter = new ClassicFilter(null, weak);
        Assert.assertTrue(filter.accept("a", true));
    }

    @Test public void test1Strong() {
        ClassicFilter filter = new ClassicFilter(null, strong);
        Assert.assertTrue(filter.accept("a", true));
    }

    @Test public void test2Weak() {
        ClassicFilter filter = new ClassicFilter(null, weak);
        Assert.assertTrue(filter.accept("ab", true));
    }

    @Test public void test2Strong() {
        ClassicFilter filter = new ClassicFilter(null, strong);
        Assert.assertFalse(filter.accept("ab", true));
    }

    @Test public void test3Weak() {
        ClassicFilter filter = new ClassicFilter(null, weak);
        Assert.assertFalse(filter.accept("b", true));
    }

    @Test public void test3Strong() {
        ClassicFilter filter = new ClassicFilter(null, strong);
        Assert.assertFalse(filter.accept("b", true));
    }


}