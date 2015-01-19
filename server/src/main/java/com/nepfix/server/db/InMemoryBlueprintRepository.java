package com.nepfix.server.db;

import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class InMemoryBlueprintRepository implements BlueprintRepository {

    private final HashMap<String, NepBlueprint> nepBlueprintHashMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override public NepBlueprint findBlueprint(String id) {
        lock.readLock().lock();
        try {
            NepBlueprint nepBlueprint = nepBlueprintHashMap.get(id);
            if (nepBlueprint == null) {
                throw new BlueprintNotFoundException();
            }
            return nepBlueprint;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public List<NepBlueprint> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(nepBlueprintHashMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public void registerBlueprint(NepBlueprint blueprint) {
        lock.writeLock().lock();
        try {
            nepBlueprintHashMap.put(blueprint.getNetworkId(), blueprint);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
