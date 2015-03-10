package com.nepfix.sim.core;


public interface Filter extends NepElement {

    boolean accept(String input, boolean isInput);
}
