package com.nepfix.sim.nep;

import com.google.gson.annotations.Expose;

public class NepStats {
    @Expose private long numberOfNodes;
    @Expose private long wordsPerSecond;
    @Expose private long averageProcessingTime;
    @Expose private long averageWordsInMemory;

    public long getNumberOfNodes() {
        return numberOfNodes;
    }

    public long getWordsPerSecond() {
        return wordsPerSecond;
    }

    public long getAverageProcessingTime() {
        return averageProcessingTime;
    }

    public long getAverageWordsInMemory() {
        return averageWordsInMemory;
    }
}
