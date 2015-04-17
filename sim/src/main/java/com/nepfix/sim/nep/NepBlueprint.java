package com.nepfix.sim.nep;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.nepfix.sim.core.ComputationElement;
import com.nepfix.sim.core.Filter;
import com.nepfix.sim.core.Node;
import com.nepfix.sim.core.Processor;
import com.nepfix.sim.elements.AcceptAllFilter;
import com.nepfix.sim.elements.UnitProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class NepBlueprint {
    @Expose private String nepId;
    @Expose private JsonObject definition;
    @Expose private List<JsonObject> processorDefinitions;
    @Expose private List<JsonObject> filterDefinitions;
    @Expose private List<JsonObject> network;

    public Nep create(long computationId) {
        Nep nep = new Nep(nepId, definition, computationId);
        processorDefinitions.forEach(d -> nep.putProcessor(createElement(d, nep, Processor.class)));
        filterDefinitions.forEach(f -> nep.putFilter(createElement(f, nep, Filter.class)));
        network.forEach(n -> nep.putNode(createNode(n, nep)));
        return nep;
    }

    private Node createNode(JsonObject object, Nep nep) {
        Node node = NepReader.GSON.fromJson(object, Node.class);
        node.setNep(nep);

        JsonElement processor = object.get("processor");
        JsonElement filterIn = object.get("filterIn");
        JsonElement filterOut = object.get("filterOut");

        if (processor == null)
            node.setProcessor(new UnitProcessor(nep, object));
        else if (processor.isJsonObject())
            node.setProcessor(createElement(processor.getAsJsonObject(), nep, Processor.class));
        else
            node.setProcessor(nep.findProcessor(processor.getAsString()));

        if (filterIn == null)
            node.setFilterIn(new AcceptAllFilter(nep, object));
        else if (filterIn.isJsonObject())
            node.setFilterIn(createElement(filterIn.getAsJsonObject(), nep, Filter.class));
        else
            node.setFilterIn(nep.findFilter(filterIn.getAsString()));


        if (filterOut == null)
            node.setFilterOut(new AcceptAllFilter(nep, object));
        else if (filterOut.isJsonObject())
            node.setFilterOut(createElement(filterOut.getAsJsonObject(), nep, Filter.class));
        else
            node.setFilterOut(nep.findFilter(filterOut.getAsString()));

        return node;
    }


    @SuppressWarnings("unchecked")
    private <T> T createElement(JsonObject object, Nep nep, Class<T> kind) {
        Class<? extends ComputationElement> clazz = tryLoadClass(object.get("class").getAsString());
        Constructor<? extends ComputationElement> constructor = tryGetConstructor(clazz);
        return kind.cast(tryCreateInstance(nep, object, constructor));
    }

    private ComputationElement tryCreateInstance(Nep nep, JsonObject object, Constructor<? extends ComputationElement> constructor) {
        try {
            return constructor.newInstance(nep, object);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ComputationElement> tryLoadClass(String className) {
        try {
            return (Class<? extends ComputationElement>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    private Constructor<? extends ComputationElement> tryGetConstructor(Class<? extends ComputationElement> clazz) {
        try {
            return clazz.getDeclaredConstructor(Nep.class, JsonObject.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    public String getNepId() {
        return nepId;
    }

    public List<String> getNodesIds() {
        return network.stream()
                .map(o -> o.get("id").getAsString())
                .sorted()
                .collect(Collectors.toList());
    }

    public NepBlueprint duplicate(){
        return NepReader.GSON.fromJson(NepReader.GSON.toJson(this), NepBlueprint.class);
    }

    public NepBlueprint[] split(){
        return new NepBlueprint[0];
    }

    public void setDefinition(JsonObject definition) {
        this.definition = definition;
    }
}
