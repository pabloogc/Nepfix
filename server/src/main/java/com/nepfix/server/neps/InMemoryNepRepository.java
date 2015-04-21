package com.nepfix.server.neps;

import com.google.gson.annotations.Expose;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.sim.nep.NepUtils;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class InMemoryNepRepository implements NepRepository {

    @Expose private final HashMap<String, NepBlueprint> nepBlueprintHashMap = new HashMap<>();
    @Expose private final HashMap<String, RemoteNepExecutor> activeNepHashMap = new HashMap<>();
    @Expose private final HashMap<String, Set<String>> activeServersForNep = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override public NepBlueprint findBlueprint(String id) {
        lock.readLock().lock();
        try {
            return nepBlueprintHashMap.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public void registerBlueprint(NepBlueprint blueprint) {
        lock.writeLock().lock();
        try {
            nepBlueprintHashMap.put(blueprint.getNepId(), blueprint);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public RemoteNepExecutor getActiveNep(String id, long computationId) {
        lock.readLock().lock();
        try {
            return activeNepHashMap.get(RemoteNepExecutor.executorId(id, computationId));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public void registerActiveNep(RemoteNepExecutor executor) {
        lock.writeLock().lock();
        try {
            activeNepHashMap.put(executor.getExecutorId(), executor);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public void unregisterActiveNep(RemoteNepExecutor executor) {
        lock.writeLock().lock();
        try {
            activeNepHashMap.remove(executor.getExecutorId(), executor);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public void registerRemoteQueue(RemoteNepInfo remoteNep) {
        lock.writeLock().lock();
        try {
            NepUtils.putInSetHashMap(remoteNep.getId(), remoteNep.getSeverQueue(), activeServersForNep);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public List<String> getRemoteQueues(String nepId) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(activeServersForNep.getOrDefault(nepId, Collections.emptySet()));
        } finally {
            lock.readLock().unlock();
        }
    }
}
