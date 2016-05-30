package com.ekeis.rema.engine.commands.descriptions;

import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class CommandDescription {
    private static final Logger log = Logger.getLogger(CommandDescription.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/commandDescriptions");

    private String id;

    public CommandDescription(String id) {
        this.id = id;
    }
    public String getTitle() {
        return res.getString(id + ".title");
    }
    public String getDescription() {
        return res.getString(id + ".text");
    }
    public String getId() {
        return id;
    }
    public String getKey() {
        return res.getString(id);
    }

    @Override
    public String toString() {
        return getKey();
    }
}
