package com.nepfix.sim.core;


import com.nepfix.sim.nep.Nep;

import java.util.Objects;

public class NameResolver {
    private final Nep nep;

    public NameResolver(Nep nep) {
        this.nep = nep;
    }

    public Connection resolve(String nodeId){
        Node local = findLocal(nodeId);
        if (local != null) {

        }
        return null;
    }

    private Node findLocal(String nodeId){
        for (Node node : nep.getNodes()) {
            if (Objects.equals(node.getId(), nodeId)) {
                return node;
            }
        }
        return null;
    }
}
