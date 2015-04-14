package com.nepfix.server.network;

import com.google.gson.annotations.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ActiveServersRepository {
    private static final Logger logger = LoggerFactory.getLogger(ActiveServersRepository.class);
    @Expose private Set<String> serverQueues = Collections.synchronizedSet(new HashSet<>());

    /**
     * Thread safe
     */
    public List<String> getServerQueues() {
        return new ArrayList<>(serverQueues);
    }

    public void registerServerQueue(String queueName){
        if (queueName == null) {
            return;
        }
        serverQueues.add(queueName);
    }

}
