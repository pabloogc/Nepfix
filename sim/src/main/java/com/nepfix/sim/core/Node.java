package com.nepfix.sim.core;


import com.google.gson.annotations.Expose;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.request.Instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
    @Expose protected String processorId;
    @Expose protected String id;
    @Expose protected boolean input;
    @Expose protected List<Connection> connections;
    protected Filter filter;
    protected Processor processor;
    @Expose private String filterId;
    @Expose private boolean remote;

    public List<Instruction> compute(String input) {
        if (accept(input)) {
            return process(input);
        } else {
            return Collections.emptyList();
        }
    }

    protected List<Instruction> process(String input) {
        ArrayList<Instruction> result = new ArrayList<>(connections.size());
        String output = processor.process(input);
        connections.stream()
                .filter(connection -> connection.getFilter().accept(output, false))
                .forEach(connection -> result.add(new Instruction(output, connection.getDestiny())));
        return result;
    }

    protected boolean accept(String input) {
        return connections != null && filter.accept(input, true);
    }


    public void link(NepBlueprint nepBlueprint) {
        filter = nepBlueprint.findFilter(filterId);
        if (!remote)
            processor = nepBlueprint.findProcessor(processorId);

        if (connections == null || connections.isEmpty())
            throw new IllegalStateException(String.format("Node with id '%s' is not connected and it's not an output", id));

        for (Connection connection : connections) {
            if (!connection.isOutput())
                connection.setDestiny(nepBlueprint.findNode(connection.getNodeId()));
            connection.setFilter(nepBlueprint.findFilter(connection.getFilterId()));
        }

    }

    public boolean isInput() {
        return input;
    }

    public String getId() {
        return id;
    }
}
