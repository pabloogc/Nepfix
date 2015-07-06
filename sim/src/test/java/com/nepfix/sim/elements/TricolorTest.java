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


public class TricolorTest {
    private static final NepBlueprint blueprint = NepReader.loadBlueprintResource("6x6.json");

    @Test public void testTricolor() {
        final Nep nep = blueprint.create(0);
        nep.getId();
        LocalNepExecutor executor = new LocalNepExecutor(blueprint);

        ArrayList<String> input = new ArrayList<>(729);
        int[] combinations = new int[]{0, 0, 0, 0, 0, 0};
        char[] sub = new char[]{'r', 'g', 'b'};
        String patter = "T5.e12.e14.e23.e36.e45.e65.%c1.%c2.%c3.%c4.%c5.%c6";

        for (int k = 0; k < 729; k++) {
            input.add(String.format(patter,
                    sub[combinations[0]],
                    sub[combinations[1]],
                    sub[combinations[2]],
                    sub[combinations[3]],
                    sub[combinations[4]],
                    sub[combinations[5]]));
            for (int i = 0; i < 6; i++) {
                if (combinations[i] == 2) {
                    combinations[i] = 0;
                } else {
                    combinations[i]++;
                    break;
                }
            }
        }
        List<Word> out = executor.execute(
                new ComputationRequest("0", "T5.e12.e14.e23.e36.e45.e65.r1.g2.b3.r4.g5.b6", -1, 1, 0));
        System.out.println("OUT" + new GsonBuilder().setPrettyPrinting().create().toJson(out));
    }

}