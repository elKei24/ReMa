/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.prefs;

import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Elias Keis (29.05.2016)
 */
public class Prefs {
    private static final Logger log = Logger.getLogger(Prefs.class.getName());

    private static final String PREF_SETTINGS = "settings.";
    private static final String KEY_REGISTERS = PREF_SETTINGS + "registers";
    private static final String KEY_REGISTER_MAX = PREF_SETTINGS + "registerMax";
    private static final String KEY_REGISTER_MIN = PREF_SETTINGS + "registerMin";
    private static final String KEY_LIFE_COMPILE = PREF_SETTINGS + "lifeCompilation";
    private static final String KEY_STYLE_CODE = PREF_SETTINGS + "styleCode";
    private static final String KEY_IGNORE_AUTOLINES = "ignoreAutolinesWarning";

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

    public void sync() {
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
            log.warning("Could not sync preferences");
        }
    }

    public int getNumberRegisters() {
        return prefs.getInt(KEY_REGISTERS, 15);
    }
    public void setNumberRegisters(int val) {
        prefs.putInt(KEY_REGISTERS, val);
    }
    public long getRegisterMax() {
        return prefs.getLong(KEY_REGISTER_MAX, Integer.MAX_VALUE);
    }
    public void setRegisterMax(long val) {
        prefs.putLong(KEY_REGISTER_MAX, val);
    }
    public long getRegisterMin() {
        return prefs.getLong(KEY_REGISTER_MIN, Integer.MIN_VALUE);
    }
    public void setRegisterMin(long val) {
        prefs.putLong(KEY_REGISTER_MIN, val);
    }
    public boolean getIgnoreAutolinesWarning() {
        return prefs.getBoolean(KEY_IGNORE_AUTOLINES, false);
    }
    public void setIgnoreAutolinesWarning(boolean ignore) {
        prefs.putBoolean(KEY_IGNORE_AUTOLINES, ignore);
    }
    public boolean getLifeCompileEnabled() {
        return prefs.getBoolean(KEY_LIFE_COMPILE, true);
    }
    public void setLifeCompileEnabled(boolean enabled) {
        prefs.putBoolean(KEY_LIFE_COMPILE, enabled);
    }
    public boolean getStyleCode() {
        return prefs.getBoolean(KEY_STYLE_CODE, true);
    }
    public void setStyleCode(boolean enabled) {
        prefs.putBoolean(KEY_STYLE_CODE, enabled);
    }
}
