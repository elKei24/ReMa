package com.ekeis.rema.prefs;

import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Elias Keis (29.05.2016)
 */
public class Prefs {
    private static final Logger log = Logger.getLogger(Prefs.class.getName());

    private static final String KEY_PREFIX = "settings.";
    private static final String KEY_REGISTERS = KEY_PREFIX + "registers";
    private static final String KEY_REGISTER_MAX = KEY_PREFIX + "registerMax";
    private static final String KEY_REGISTER_MIN = KEY_PREFIX + "registerMin";

    private static final Prefs instance = new Prefs();
    public static Prefs getInstance() {return instance;}

    private Preferences prefs;

    private Prefs() {
        prefs = Preferences.userRoot().node("com/ekeis/rema");
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
            log.warning("Could not flush preferences");
        }
    }

    private void sync() {
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
            log.warning("Could not sync preferences");
        }
    }

    public int getNumberRegisters() {
        return prefs.getInt(KEY_REGISTERS, 5);
    }
    public void setNumberRegisters(int val) {
        prefs.putInt(KEY_REGISTERS, val);
        sync();
    }
    public long getRegisterMax() {
        return prefs.getLong(KEY_REGISTER_MAX, Byte.MAX_VALUE);
    }
    public void setRegisterMax(long val) {
        prefs.putLong(KEY_REGISTER_MAX, val);
        sync();
    }
    public long getRegisterMin() {
        return prefs.getLong(KEY_REGISTER_MIN, Byte.MIN_VALUE);
    }
    public void setRegisterMin(long val) {
        prefs.putLong(KEY_REGISTER_MIN, val);
        sync();
    }
}
