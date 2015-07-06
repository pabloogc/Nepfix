package com.nepfix.sim.elements;

import com.google.gson.GsonBuilder;
import com.nepfix.sim.nep.LocalNepExecutor;
import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.nep.NepBlueprint;
import com.nepfix.sim.nep.NepReader;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class TricolorTest2x2 {
    private static final NepBlueprint blueprint = NepReader.loadBlueprintResource("2x2.json");

    @Test public void testTricolor2x2() {
        final Nep nep = blueprint.create(0);
        nep.getId();
        LocalNepExecutor executor = new LocalNepExecutor(blueprint);

        ArrayList<String> input = new ArrayList<>(729);
        int[] combinations = new int[]{0, 0, 0, 0, 0, 0};
        char[] sub = new char[]{'r', 'g', 'b'};
        String patter = "T2.e12.e23.%c1.%c2.%c3";

        for (int k = 0; k < Math.pow(3, 3); k++) {
            input.add(String.format(patter,
                    sub[combinations[0]],
                    sub[combinations[1]],
                    sub[combinations[2]]));
            for (int i = 0; i < 3; i++) {
                if (combinations[i] == 2) {
                    combinations[i] = 0;
                } else {
                    combinations[i]++;
                    break;
                }
            }
        }
        List<Word> out = executor.execute(
                new ComputationRequest("0", "T2.e12.e23.r1.g2.b3", -1, 1, 0));
        System.out.println("OUT" + new GsonBuilder().setPrettyPrinting().create().toJson(out));
    }

}