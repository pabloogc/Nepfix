package com.nepfix.sim.elements.util;

import org.junit.Assert;
import org.junit.Test;

public class ElementsUtilsTest {
    @Test public void testIntersection() {
        Assert.assertTrue(ElementsUtils.intersect("abb", "ccc__a__ccc"));
        Assert.assertFalse(ElementsUtils.intersect("ddd", "ccc__b__ccc"));
    }

    @Test public void testIntersectionCount() {
        Assert.assertArrayEquals(new int[]{2, 2, 2, 0}, ElementsUtils.intersectCount("abce", "aabbcc"));
    }
}