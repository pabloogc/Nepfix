package com.nepfix.sim.request;


import com.nepfix.sim.core.Node;

public class Instruction {

    public static final int TERMINATE = 1;

    public int getCode() {
        return code;
    }

    private int code = 0;
    private final String input;
    private final Node destinyNode;

    public Instruction(String input, Node destinyNode) {
        this.input = input;
        this.destinyNode = destinyNode;
    }

    public String getInput() {
        return input;
    }

    public Node getDestintyNode() {
        return destinyNode;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
