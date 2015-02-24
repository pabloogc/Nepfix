package com.nepfix.sim.nep;


import com.google.gson.annotations.Expose;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.core.Processor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class NepBlueprint {
    private boolean linked = false;
    private Node inputNode;
    @Expose private String networkId;
    @Expose private List<Processor> processorDefinitions;
    @Expose private List<Filter> filterDefinitions;
    @Expose private List<Node> network;

    public String getNetworkId() {
        return networkId;
    }

    public Nep create() {
        Nep nep = new Nep();
        nep.setInputNode(inputNode);
        nep.setExecutor(Executors.newFixedThreadPool(5));
        nep.setInstructionQueue(new LinkedBlockingQueue<>());
        return nep;
    }

    public void link() {
        if (linked) return;
        linked = true;
        for (Node node : network) {
            try {
                node.link(this);
            } catch (Exception ex) {
                throw new RuntimeException("Error linking the Nep graph at node with id: \"" + node.getId() + "\"", ex);
            }
            if (node.isInput()) inputNode = node;
        }
    }

    public Processor findProcessor(String id) {
        for (Processor processor : processorDefinitions) {
            if (processor.getId().equals(id))
                return processor;
        }
        throw new IllegalArgumentException(String.format("Processor with id '%s' not found", id));
    }


    public Filter findFilter(String id) {
        for (Filter filter : filterDefinitions) {
            if (filter.getId().equals(id))
                return filter;
        }
        throw new IllegalArgumentException(String.format("Filter with id '%s' not found", id));
    }

    public Node findNode(String id) {
        for (Node node : network) {
            if (node.getId().equals(id))
                return node;
        }
        throw new IllegalArgumentException(String.format("Node with id '%s' not found", id));
    }
}
