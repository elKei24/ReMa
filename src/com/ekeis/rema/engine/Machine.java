package com.ekeis.rema.engine;

import com.ekeis.rema.prefs.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Machine {
    private static final Logger log = Logger.getLogger(Machine.class.getName());
    private List<Register> registers;
    private Register counter;
    private Program program = new Program();

    public Machine() {
        reset();
    }

    public synchronized void reset(Program program) {
        reset();
        this.program = program;
    }
    public synchronized void reset(String code) {
        reset(new Program(code));
    }

    public synchronized void reset() {
        log.info("Resetting machine ...");
        counter = new Register(1);

        int numRegisters = Prefs.getInstance().getNumberRegisters();
        registers = new ArrayList<>(numRegisters);
        for (int i = 0; i < numRegisters; i++) {
            registers.add(new Register());
        }
    }

    public synchronized void step() {
        log.info("Machine performing step ...");
    }
    public synchronized void run() {
        log.info("Machine performing run ...");
    }

    //getters
    public List<Register> getRegisters() {
        return registers;
    }
    public Register getCounter() {
        return counter;
    }
}
