package com.nepfix.server.db;

import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class InMemoryBlueprintRepository implements BlueprintRepository {

    private final HashMap<String, NepBlueprint> nepBlueprintHashMap = new HashMap<>();

    @Override public NepBlueprint findBlueprint(String id) {
        NepBlueprint nepBlueprint = nepBlueprintHashMap.get(id);
        if (nepBlueprint == null) {
            throw new BlueprintNotFoundException();
        }
        return nepBlueprint;
    }

    @Override public List<NepBlueprint> findAll() {
        return new ArrayList<>(nepBlueprintHashMap.values());
    }

    @Override public void registerBlueprint(NepBlueprint blueprint) {
        nepBlueprintHashMap.put(blueprint.getNetworkId(), blueprint);
    }
}
