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

    void unregisterActiveNep(RemoteNepExecutor nep);

    //Remote Nep
    public List<String> getRemoteQueues(String nepId);

    void registerRemoteQueue(RemoteNepInfo nep);

}
