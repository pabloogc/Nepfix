package com.nepfix.server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nepfix.server.db.BlueprintRepository;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.List;

@RestController
public class NepController {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String JSON_CT = "application/json; charset=utf-8";
    @Autowired private BlueprintRepository blueprintRepository;


    @RequestMapping(value = "register", method = RequestMethod.POST)
    public void registerNep(@RequestBody String nepDefinition) {
        NepBlueprint nepBlueprint = NepReader.loadBlueprint(new StringReader(nepDefinition));
        blueprintRepository.registerBlueprint(nepBlueprint);
    }

    @RequestMapping(value = "compute", method = RequestMethod.POST, produces = JSON_CT)
    public List<String> compute(@RequestBody String requestString) {
        ComputationRequest request = gson.fromJson(requestString, ComputationRequest.class);
        NepBlueprint blueprint = blueprintRepository.findBlueprint(request.getNetworkId());
        Nep nep = blueprint.create();
        return nep.compute(request);
    }

    @RequestMapping(value = "blueprints/{id}", method = RequestMethod.GET, produces = JSON_CT)
    public String find(@PathVariable("id") String id) {
        return gson.toJson(blueprintRepository.findBlueprint(id));
    }

    @RequestMapping(value = "blueprints", produces = JSON_CT)
    public String findAll() {
        return gson.toJson(blueprintRepository.findAll());
    }


}
