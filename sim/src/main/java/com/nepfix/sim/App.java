package com.nepfix.sim;


import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class App {

    public static void main(String[] args) {
        String nepFile = "fizzbuzz.json";
        NepBlueprint blueprint = NepReader.load(new InputStreamReader(App.class.getClassLoader().getResourceAsStream(nepFile)));
        Nep nep = blueprint.create();
        String input = "a";
        for (int i = 0; i < 3000; i++) {
            input += "a";
        }
        ComputationRequest request = new ComputationRequest(input, "1", 60*1000, 3000);
        List<String> result = nep.compute(request);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
}
