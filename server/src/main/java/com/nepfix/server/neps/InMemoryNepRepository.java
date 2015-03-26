package com.nepfix.server.neps;

import com.google.gson.annotations.Expose;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.sim.elements.util.Misc;
import com.nepfix.sim.nep.NepBlueprint;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class InMemoryNepRepository implements NepRepository {

    @Expose private final HashMap<String, NepBlueprint> nepBlueprintHashMap = new HashMap<>();
    @Expose private final HashMap<String, RemoteNepExecutor> activeNepHashMap = new HashMap<>();
    @Expose private final HashMap<String, HashMap<String, String>> remoteNepHashMap = new HashMap<>();
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

    @Override public RemoteNepExecutor findActiveNep(String id, long computationId) {
        lock.readLock().lock();
        try {
            return activeNepHashMap.get(id + "-" + computationId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public void registerActiveNep(RemoteNepExecutor executor) {
        lock.writeLock().lock();
        try {
            executor.getNep();
            activeNepHashMap.put(executor.getNep().getId() + "-" + executor.getNep().getComputationId(), executor);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public String findRemoteNode(String nepId, String nodeId) {
        lock.readLock().lock();
        try {
            HashMap<String, String> nodeToQueueMap = remoteNepHashMap.get(nepId);
            if (nodeToQueueMap != null) {
                return nodeToQueueMap.get(nodeId);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override public void registerRemoteNodes(RemoteNepInfo remoteNep) {
        lock.writeLock().lock();
        try {
            String nepHash = Misc.getNepMd5(remoteNep.getId(), remoteNep.getNodes());
            HashMap<String, String> nodeToQueueMap = new HashMap<>();
            remoteNepHashMap.putIfAbsent(remoteNep.getId(), nodeToQueueMap);
            nodeToQueueMap = remoteNepHashMap.get(remoteNep.getId());
            for (String nodeId : remoteNep.getNodes()) {
                nodeToQueueMap.put(nodeId, nepHash);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override public List<String> getAllRemoteQueues(String nepId) {
        lock.readLock().lock();
        try {
            return remoteNepHashMap.get(nepId)
                    .values()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
