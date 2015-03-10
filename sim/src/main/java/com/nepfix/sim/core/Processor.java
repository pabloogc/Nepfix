package com.nepfix.sim.core;


import java.util.List;

public interface Processor extends NepElement {

    List<String> process(List<String> input);
}
