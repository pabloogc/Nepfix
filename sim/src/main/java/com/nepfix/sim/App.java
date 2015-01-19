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
        NepBlueprint blueprint = NepReader.load(new InputStreamReader(App.class.getClassLoader().getResourceAsStream("nep_def2.json")));
        Nep nep = blueprint.create();
        ComputationRequest request = new ComputationRequest("aa", "1", 5000, 2);
        List<String> result = nep.compute(request);
        System.out.println(new Gson().toJson(result));
    }
}
