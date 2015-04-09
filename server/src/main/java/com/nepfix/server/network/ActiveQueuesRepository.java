package com.nepfix.server.network;

import com.google.gson.annotations.Expose;
import com.nepfix.server.rabbit.event.ServerReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Vector;

@Repository
public class ActiveQueuesRepository {
    private static final Logger logger = LoggerFactory.getLogger(ActiveQueuesRepository.class);
    @Expose private Vector<String> serverQueues = new Vector<>();

    /**
     * Thread safe
     */
    public List<String> getServerQueues() {
        return serverQueues;
    }

    public void registerServerQueue(String queueName){
        if (queueName == null) {
            return;
        }
        serverQueues.add(queueName);
    }

}
