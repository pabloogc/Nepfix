package com.nepfix.sim;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.LocalNepExecutor;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;

import java.io.InputStreamReader;
import java.util.List;

public class LocalTest {
    public static void main(String[] args) {

        Gson gson = new Gson();
        String nepFile = "test.json";
        String input = "";

        int count = 1;

        for (int i = 0; i < count; i++) {
            input += "a";
        }

        ComputationRequest request = new ComputationRequest(input, "1", 4, 1);
        NepBlueprint blueprint = NepReader.loadBlueprint(new InputStreamReader(LocalTest.class.getClassLoader().getResourceAsStream(nepFile)));
        LocalNepExecutor executor = new LocalNepExecutor(blueprint);
        List<Word> output = executor.execute(request);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(output));
    }


}
