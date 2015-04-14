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
    S_STOP(VoidMessage.class),
    //Nep messages
    N_STEP(VoidMessage.class),
    N_STEP_REPLY(VoidMessage.class),
    N_ADD_WORDS(new TypeToken<List<Word>>(){}.getType()),
    UNKNOWN(VoidMessage.class);

    private final Type type;

    Action(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
