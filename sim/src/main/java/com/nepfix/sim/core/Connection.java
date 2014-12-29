package com.nepfix.sim.core;


import com.google.gson.annotations.Expose;

public class Connection {
    @Expose private String nodeId;
    @Expose private String filterId;
    @Expose private boolean output;

    private Node destiny;
    private Filter filter;

    public Node getDestiny() {
        return destiny;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setDestiny(Node destiny) {
        this.destiny = destiny;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getNodeId() {

        return nodeId;
    }

    public boolean isOutput() {
        return output;
    }

    public String getFilterId() {
        return filterId;
    }
}
