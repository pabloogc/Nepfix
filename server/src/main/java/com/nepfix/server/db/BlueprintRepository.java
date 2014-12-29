package com.nepfix.server.db;


import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlueprintRepository {
    NepBlueprint findBlueprint(String id);
    List<NepBlueprint> findAll();
    void registerBlueprint(NepBlueprint blueprint);
}
