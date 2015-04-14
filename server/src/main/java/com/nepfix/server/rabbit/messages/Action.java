package com.nepfix.server.rabbit.messages;


import com.google.gson.reflect.TypeToken;
import com.nepfix.server.neps.RemoteNepInfo;
import com.nepfix.sim.request.Word;

import java.lang.reflect.Type;
import java.util.List;

public enum Action {
    //Server messages
    S_NEW_SERVER(String.class),
    S_NEW_SERVER_REPLY(String.class),
    S_NEW_NEP(RemoteNepInfo.class),
    S_NEW_NEP_REPLY(RemoteNepInfo.class),
    S_COMPUTATION_STARTED(NepComputationInfo.class),
    S_COMPUTATION_FINISHED(NepComputationInfo.class),
    S_GET_LOAD(Void.class),
    S_STOP(Void.class),
    //Nep messages
    N_STEP(Void.class),
    N_STEP_REPLY(Void.class),
    N_ADD_WORDS(new TypeToken<List<Word>>(){}.getType()),
    UNKNOWN(Void.class);

    private final Type type;

    Action(Type bodyType) {
        this.type = bodyType;
    }

    public Type getType() {
        return type;
    }
}
