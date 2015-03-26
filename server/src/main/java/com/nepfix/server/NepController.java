package com.nepfix.server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nepfix.server.executor.NepExecutorFactory;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.network.ActiveQueuesRepository;
import com.nepfix.server.rabbit.ServerMessageHandler;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class NepController {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String JSON_CT = "application/json; charset=utf-8";
    @Autowired private NepRepository nepRepository;
    @Autowired private ActiveQueuesRepository activeQueuesRepository;
    @Autowired private NepExecutorFactory factory;
    @Autowired private RabbitAdmin rabbitAdmin;
    @Autowired private ServerMessageHandler serverMessageHandler;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public void registerNep(@RequestBody String nepDefinition) {
        NepBlueprint nepBlueprint = NepReader.loadBlueprint(new StringReader(nepDefinition));
        nepRepository.registerBlueprint(nepBlueprint);
        serverMessageHandler.broadcastNewRegisteredNep(nepBlueprint);
    }

    @RequestMapping(value = "compute", method = RequestMethod.POST, produces = JSON_CT)
    public List<String> compute(@RequestBody String requestString) {
        ComputationRequest request = gson.fromJson(requestString, ComputationRequest.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(request.getNetworkId());
        RemoteNepExecutor executor = factory.create(blueprint, new Random().nextLong());
        List<Word> words = executor.execute(request);
        return words.stream().map(Word::getValue).collect(Collectors.toList());
    }

    @RequestMapping(value = "blueprints/{id}", method = RequestMethod.GET, produces = JSON_CT)
    public String find(@PathVariable("id") String id) {
        return gson.toJson(nepRepository.findBlueprint(id));
    }

    @RequestMapping(value = "dump", produces = JSON_CT)
    public String findAll() {
        JsonObject dump = new JsonObject();
        dump.add("nepRepository", gson.toJsonTree(nepRepository));
        dump.add("activeQueuesRepository", gson.toJsonTree(activeQueuesRepository));
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(dump);
    }

}
