package com.nepfix.sim.core;


import com.google.gson.annotations.Expose;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.request.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Node implements NepElement {

    public static final String OUTPUT_CONNECTION = "__output__";
    private Nep nep;
    @Expose private String id;
    @Expose private boolean input;
    @Expose private boolean output;
    @Expose private List<String> connections = new ArrayList<>(1);
    private Processor processor;
    private Filter filterIn;
    private Filter filterOut;

    public Node(Nep nep) {
        this.nep = nep;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isOutput() {
        return output;
    }

    @Override public String getId() {
        return id;
    }

    public List<Word> process(List<Word> words) {

        List<String> filteredInput = words.stream()
                .map(Word::getValue)
                .filter(w -> filterIn.accept(w, true))
                .collect(Collectors.toList());

        List<String> out = processor.process(filteredInput);

        List<String> sendTo;
        if (isOutput())
            sendTo = Arrays.asList(OUTPUT_CONNECTION);
        else
            sendTo = connections;

        List<Word> result = new ArrayList<>(out.size() * sendTo.size());
        for (String connection : sendTo) {
            out.stream()
                    .filter(w -> filterOut.accept(w, false))
                    .map(s -> new Word(s, connection, nep.getConfiguration() + 1))
                    .forEach(result::add);
        }
        return result;
    }

    public void setNep(Nep nep) {
        this.nep = nep;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public void setFilterIn(Filter filterIn) {
        this.filterIn = filterIn;
    }

    public void setFilterOut(Filter filterOut) {
        this.filterOut = filterOut;
    }
}
