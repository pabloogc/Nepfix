package com.nepfix.sim.nep;

import com.nepfix.sim.core.Node;
import com.nepfix.sim.request.ComputationRequest;
import com.nepfix.sim.request.Instruction;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Nep {

    private BlockingQueue<Instruction> instructionQueue;
    private ExecutorService executor;
    private Node inputNode;

    Nep() {
    }

    public List<String> compute(ComputationRequest request) {
        Vector<String> result = new Vector<>();
        AtomicInteger completed = new AtomicInteger();
        instructionQueue.add(new Instruction(request.getInput(), inputNode));

        long elapsed = 0;

        try {
            while (true) {
                long now = System.nanoTime();
                Instruction instruction = instructionQueue.poll(request.getTimeoutMillis() * 1000000 - elapsed, TimeUnit.NANOSECONDS);
                elapsed = elapsed + System.nanoTime() - now;
                if (instruction == null || instruction.getCode() == Instruction.TERMINATE) break; //Timed out (null) or terminated

                executor.execute(() -> {
                    List<Instruction> newInstructions = executeInstruction(instruction);
                    newInstructions.stream().forEach((newInstruction) -> {
                        if (newInstruction.getDestintyNode() == null) {
                            int count = completed.addAndGet(1);
                            result.add(newInstruction.getInput());
                            if (count == request.getMaxResults()) {
                                Instruction terminateInstruction = new Instruction(null, null);
                                terminateInstruction.setCode(Instruction.TERMINATE);
                                instructionQueue.add(terminateInstruction);
                            }
                        } else {
                            instructionQueue.add(newInstruction);
                        }
                    });
                });
            }

        } catch (InterruptedException e) {
            //Just exit, Spring will interrupt the thread if the client disconnects.
        }

        executor.shutdown();
        return result;
    }

    private Instruction takeInstruction() {
        try {
            return instructionQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();//Should never happen
            throw new RuntimeException(e);
        }
    }

    private List<Instruction> executeInstruction(Instruction instruction) {
        return instruction.getDestintyNode().compute(instruction.getInput());
    }


    void setInstructionQueue(BlockingQueue<Instruction> instructionQueue) {
        this.instructionQueue = instructionQueue;
    }

    void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    void setInputNode(Node inputNode) {
        this.inputNode = inputNode;
    }
}
