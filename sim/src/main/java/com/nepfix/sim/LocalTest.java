package com.nepfix.sim;


import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class LocalTest {
    public static void main(String[] args) {

        String nepFile = "nep_diamond.json";
        String input = "aa";

        ComputationRequest request = new ComputationRequest(input, "1", 1000);

        NepBlueprint blueprint = NepReader.load(new InputStreamReader(LocalTest.class.getClassLoader().getResourceAsStream(nepFile)));
        Nep nep = blueprint.create();

        List<String> output = nep.compute(request);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(output));
    }


}
