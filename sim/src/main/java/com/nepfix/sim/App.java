package com.nepfix.sim;


import com.google.gson.Gson;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class App {

    public static void main(String[] args) {
        String nepFile = "nep_count.json";
        NepBlueprint blueprint = NepReader.load(new InputStreamReader(App.class.getClassLoader().getResourceAsStream(nepFile)));
        Nep nep = blueprint.create();
        ComputationRequest request = new ComputationRequest("aaaaaaaa", "1", 100, Integer.MAX_VALUE);
        List<String> result = nep.compute(request);
        System.out.println(new Gson().toJson(result));
    }
}
