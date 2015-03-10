package com.nepfix.sim.nep;


import com.nepfix.sim.core.Node;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;

import java.util.Arrays;
import java.util.List;

public class LocalNepExecutor {

    private NepBlueprint nepBlueprint;

    public LocalNepExecutor(NepBlueprint nepBlueprint) {
        this.nepBlueprint = nepBlueprint;
    }

    public List<Word> execute(ComputationRequest request) {
        Nep nep = nepBlueprint.create();
        Node inputNode = nep.getInputNode();
        List<Word> words = Arrays.asList(new Word(request.getInput(), inputNode.getId(), 0));
        while (nep.getNepOutput().size() < request.getMaxOutputs()
                && nep.getConfiguration() < request.getMaxConfiguration()) {
            nep.putWords(words);
            words = nep.step();
            words.size();
        }
        return nep.getNepOutput();
    }

}
