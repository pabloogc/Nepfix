package com.nepfix.sim;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class App {

    public static void main(String[] args) throws IOException {

//        if (!(args.length == 2 || args.length == 4))
//            throw new IllegalArgumentException("Usage: sim nep_file.json computation_request.json [-o output_file]");
//
//        NepBlueprint blueprint = NepReader.loadBlueprint(new FileReader(args[0]));
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        ComputationRequest request = gson.fromJson(new FileReader(args[1]), ComputationRequest.class);
//
//        Nep nep = blueprint.create();
//        List<String> result = nep.compute(request);
//
//        if (args.length == 4 && args[2].equals("-o")) { //Output to file
//            FileWriter fileWriter = new FileWriter(args[3]);
//            fileWriter.write(gson.toJson(result));
//            fileWriter.flush();
//        } else {
//            System.out.println(gson.toJson(result));
//        }

    }
}
