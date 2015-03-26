package com.nepfix.server.neps;


import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NepRepository {
    //Blueprint
    NepBlueprint findBlueprint(String id);

    void registerBlueprint(NepBlueprint blueprint);

    //Local Nep
    RemoteNepExecutor findActiveNep(String id, long computationId);

    void registerActiveNep(RemoteNepExecutor nep);

    //Remote Nep
    public List<String> getAllRemoteQueues(String nepId);

    public String findRemoteNode(String nepId, String nodeId);

    void registerRemoteNodes(RemoteNepInfo nep);

}
