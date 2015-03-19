package com.nepfix.sim.nep;

import com.google.gson.JsonObject;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.core.NepElement;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Word;

import java.util.*;

import static com.nepfix.sim.elements.util.ElementsUtils.putInListHashMap;

public class Nep {

    private final JsonObject nepConfig;
    private final HashMap<String, Node> nodes = new HashMap<>();
    private final HashMap<String, Processor> processors = new HashMap<>();
    private final HashMap<String, Filter> filters = new HashMap<>();
    private final HashMap<Long, List<Word>> activeConfigurations = new HashMap<>(); //Words of the current configuration
    private final List<Word> nepOutput = new ArrayList<>();
    private long configuration;

    public Nep(JsonObject nepConfig) {
        this.nepConfig = nepConfig;
        this.configuration = 0;
    }

    public List<Word> step() {
        List<Word> words = activeConfigurations.remove(configuration);
        HashMap<Node, List<Word>> classifiedWords = new HashMap<>();

        for (Word word : words) {
            putInListHashMap(findNode(word.getDestinyNode()), word, classifiedWords);
        }

        List<Word> newConfiguration = new Vector<>(words.size());

        classifiedWords.entrySet()
                .parallelStream()
                .forEach(entry -> {
                    Node node = entry.getKey();
                    List<Word> processedWords = node.process(entry.getValue());
                    if (node.isOutput()) {
                        nepOutput.addAll(processedWords);
                    } else {
                        newConfiguration.addAll(processedWords);
                    }
                });
        configuration++;
        return newConfiguration;
    }

    public void putWords(List<Word> words) {
        words.forEach(this::putWord);
    }

    private void putWord(Word word) {
        putInListHashMap(word.getConfiguration(), word, activeConfigurations);
    }

    public Node findNode(String id) {
        return (Node) findElement(id, nodes);
    }

    public Processor findProcessor(String id) {
        return (Processor) findElement(id, processors);
    }

    public Filter findFilter(String id) {
        return (Filter) findElement(id, filters);
    }

    private NepElement findElement(String id, HashMap<String, ? extends NepElement> collection) {
        NepElement node = collection.get(id);
        if (node == null) {
            throw new IllegalArgumentException("Element with id: " + id + " not found");
        }
        return node;
    }

    public JsonObject getNepConfig() {
        return nepConfig;
    }


    public List<String> compute(ComputationRequest request) {
        return null;
    }

    public void putNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public void putProcessor(Processor processor) {
        processors.put(processor.getId(), processor);
    }

    public Collection<Processor> getProcessors() {
        return processors.values();
    }

    public void putFilter(Filter filter) {
        filters.put(filter.getId(), filter);
    }

    public Collection<Filter> getFilters() {
        return filters.values();
    }

    public long getConfiguration() {
        return configuration;
    }

    public List<Word> getNepOutput() {
        return nepOutput;
    }

    public Node getInputNode() {
        for (Node node : nodes.values()) {
            if (node.isInput()) return node;
        }
        throw new IllegalStateException("Nep does not have an input node");
    }
}
