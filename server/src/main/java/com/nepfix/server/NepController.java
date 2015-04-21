package com.nepfix.server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nepfix.server.executor.RemoteNepExecutor;
import com.nepfix.server.executor.RemoteNepExecutorFactory;
import com.nepfix.server.neps.BlueprintNotFoundException;
import com.nepfix.server.neps.NepRepository;
import com.nepfix.server.neps.RemoteNepInfo;
import com.nepfix.server.network.ActiveServersRepository;
import com.nepfix.server.rabbit.ServerMessageHandler;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.nep.NepStats;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class NepController {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String JSON_CT = "application/json; charset=utf-8";
    @Autowired private NepRepository nepRepository;
    @Autowired private ActiveServersRepository activeServersRepository;
    @Autowired private RemoteNepExecutorFactory factory;
    @Autowired private ServerMessageHandler serverMessageHandler;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public void registerNep(@RequestBody String nepDefinition) {
        NepBlueprint nepBlueprint = NepReader.loadBlueprint(new StringReader(nepDefinition));
        nepRepository.registerBlueprint(nepBlueprint);
        nepRepository.registerRemoteQueue(new RemoteNepInfo(nepBlueprint.getNepId(), AppConfiguration.SERVER_QUEUE));
        serverMessageHandler.broadcastNewNep(nepBlueprint);
    }

    @RequestMapping(value = "join/{nepId}")
    public void splitNep(@PathVariable String nepId) {
        List<Pair<String, NepStats>> stats = activeServersRepository.getServerQueues()
                .parallelStream()
                .filter(q -> !q.equals(AppConfiguration.SERVER_QUEUE))
                .map(q -> Pair.of(q, serverMessageHandler.getNepStats(q, nepId)))
                .filter(pair -> pair.getRight() != null)
                .collect(Collectors.toList());

        stats.sort((p1, p2) ->
                Long.compare(p1.getRight().getNumberOfNodes(), p2.getRight().getNumberOfNodes()));

        Pair<String, NepStats> toSplit = stats.get(0);

        for (Pair<String, NepStats> stat : stats) {
            nepRepository.registerRemoteQueue(new RemoteNepInfo(nepId, stat.getLeft()));
        }

        //serverMessageHandler.startSplit(nepId);
        //List<NepSplitHolder> splitHolders = serverMessageHandler.splitNep(toSplit, nepId);

        //serverMessageHandler.endSplit(nepId);
        nepRepository.registerRemoteQueue(new RemoteNepInfo(nepId, AppConfiguration.SERVER_QUEUE));

    }

    @RequestMapping(value = "leave/{nepId}")
    public void merge(@PathVariable String nepId) {
        //TODO: Implement this
    }

    @RequestMapping(value = "compute", method = RequestMethod.POST, produces = JSON_CT)
    public List<String> compute(@RequestBody String requestString) {
        ComputationRequest request = gson.fromJson(requestString, ComputationRequest.class);
        NepBlueprint blueprint = nepRepository.findBlueprint(request.getNetworkId());

        if (blueprint == null) throw new BlueprintNotFoundException();
        if(nepRepository.getActiveNep(request.getNetworkId(), request.getComputationId()) != null)
            throw new RuntimeException("Already active");

        RemoteNepExecutor executor = factory.create(blueprint, request.getComputationId());
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
        dump.add("activeServersRepository", gson.toJsonTree(activeServersRepository));
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(dump);
    }

}
