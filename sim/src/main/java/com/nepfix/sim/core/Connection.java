package com.nepfix.sim.core;

import com.nepfix.sim.nep.Nep;
import com.nepfix.sim.request.Word;

import java.io.IOException;

public interface Connection {

    void forward(String nepId, Word word) throws IOException;

    static class Local implements Connection {

        private final Nep nep;

        public Local(Nep nep) {
            this.nep = nep;
        }

        @Override public void forward(String nepId, Word word) throws IOException {

        }
    }

}
