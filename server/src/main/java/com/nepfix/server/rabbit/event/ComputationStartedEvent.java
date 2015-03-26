package com.nepfix.server.rabbit.event;


import com.google.gson.annotations.Expose;

public class ComputationStartedEvent extends GenericMessage<ComputationStartedEvent.NepAndComId> {

    public ComputationStartedEvent(String nepId, long computationId) {
        super(Kind.COMPUTATION_STARTED, new NepAndComId(nepId, computationId));
    }

    public static class NepAndComId {
        @Expose public final String nepId;

        @Expose public final long compId;

        public NepAndComId(String nepId, long compId) {
            this.nepId = nepId;
            this.compId = compId;
        }
    }

}

