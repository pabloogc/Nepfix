package com.nepfix.sim;


import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.InputStreamReader;
import java.util.List;

public class PumpTest {
    public static void main(String[] args) {
        ComputationRequest request = randomPumpInput();

        NepBlueprint blueprint = NepReader.load(new InputStreamReader(PumpTest.class.getClassLoader().getResourceAsStream("Pump.json")));
        Nep nep = blueprint.create();

        List<String> output = nep.compute(request);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(output));
    }

    public static ComputationRequest randomPumpInput() {
        String randomInput = "w";
        return new ComputationRequest(randomInput, "", 1000);
    }

}
