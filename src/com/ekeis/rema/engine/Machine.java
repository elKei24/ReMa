/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

import com.ekeis.rema.engine.exceptions.InternalException;
import com.ekeis.rema.engine.exceptions.RemaException;
import com.ekeis.rema.engine.exceptions.runtime.RegisterNotFoundException;
import com.ekeis.rema.engine.exceptions.syntax.SyntaxException;
import com.ekeis.rema.engine.log.LogMessage;
import com.ekeis.rema.prefs.Prefs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Machine {
    private static final Logger log = Logger.getLogger(Machine.class.getName());
    private List<Register> registers;

    private Set<MachineListener> listeners = new LinkedHashSet<>();

    public enum Flag {ZERO, NEGATIVE, OVERFLOW};

    private Register counter;
    private Register akku;
    private Program program = new Program(this);
    private boolean running, isEnd;

    public Machine() {
        setNumRegisters(0);
        reset();
    }
    public Machine(int numRegisters) {
        setNumRegisters(numRegisters);
        reset();
    }

    public synchronized void reset() {
        log.fine("Resetting machine ...");

        setRunning(false);
        isEnd = false;

        counter.setValue(1);
        akku.setValue(0);
        for (Register r : registers) {
            r.setValue(0);
        }

        for (MachineListener l : new ArrayList<>(listeners)) l.onReset(this);
    }

    public void step() {
        if (!running && !isEnd) {
            log.fine("Machine performing step ...");
            setRunning(true);
            stepThreadsafe();
            setRunning(false);
        }
    }
    private synchronized void stepThreadsafe() {
        try {
            program.execute((int) counter.getValue());
        } catch (RemaException re) {
            log(re);
            pause();
        }
    }
    public synchronized void run() {
        final long waittime = Prefs.getInstance().getRunWaittime();
        if (!running && !isEnd) {
            log.fine("Machine performing run ...");
            setRunning(true);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isEnd && running) {
                        try {
                            stepThreadsafe();
                            Thread.sleep(waittime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }, "CodeRunner");
            thread.start();
        }
    }

    public synchronized void pause() {
        setRunning(false);
    }
    public synchronized void end() {
        isEnd = true;
        pause();
    }

    //getters/setters
    public List<Register> getRegisters() {
        return registers;
    }
    public Register getCounter() {
        return counter;
    }

    public Register getAkku() {
        return akku;
    }

    public void increaseIP() {
        Register ip = counter;
        ip.setValue(ip.getValue() + 1);
    }

    public Register getRegister(int curLine, int regIndex) {
        try {
            return registers.get(regIndex);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new RegisterNotFoundException(curLine, regIndex);
        }
    }

    public Program getProgram() {
        return program;
    }

    /**
     * Compiles the program and sets it
     * @param code the code for the program
     * @return true if it was a success, false if there was a syntax error (will be logged)
     */
    public boolean setProgram(final String code) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = false;
                    try {
                        setProgramThreadsafe(new Program(Machine.this, code));
                        success = true;
                    } catch (RemaException re) {
                        log(re);
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Could not compile", ex);
                        log(new InternalException(ex));
                    }
                    for (MachineListener l : new ArrayList<>(listeners)) l.onCompileTried(Machine.this, success);
                }
            }, "ProgramCompiler");
            thread.start();
            return true;
        } catch (SyntaxException se) {
            log(se);
        }
        return false;
    }
    private synchronized void setProgramThreadsafe(Program program) {
        this.program = program;
    }
    public void log(LogMessage msg) {
        for (MachineListener l : new ArrayList<>(listeners)) l.onLogMessage(this, msg);
    }
    public boolean canStartRun() {
        return !isEnd && !running;
    }
    private void setRunning(boolean running) {
        if (this.running != running) {
            this.running = running;
            for (MachineListener l : new ArrayList<>(listeners)) l.onRunningChanged(this, running);
        }
    }
    public boolean isEnd() {
        return isEnd;
    }
    public void setNumRegisters(int numRegisters) {
        long min = Prefs.getInstance().getRegisterMin();
        long max = Prefs.getInstance().getRegisterMax();

        counter = new Register(min, max, 1);
        akku = new Register(min, max);
        registers = new ArrayList<>(numRegisters);
        for (int i = 0; i < numRegisters; i++) {
            Register r = new Register(min, max);
            registers.add(r);
        }
        for (MachineListener l : new ArrayList<>(listeners)) l.onRegistersChanged(this);
    }

    //listener
    public interface MachineListener {
        void onLogMessage(Machine machine, LogMessage msg);
        void onCompileTried(Machine machine, boolean success);
        void onRunningChanged(Machine machine, boolean running);
        void onRegistersChanged(Machine machine);
        void onReset(Machine machine);
    }
    public void addListener(MachineListener l) {
        listeners.add(l);
    }
    public void removeListener(MachineListener l) {
        listeners.remove(l);
    }
}
