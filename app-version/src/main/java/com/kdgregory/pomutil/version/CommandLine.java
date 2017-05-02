package com.kdgregory.pomutil.version;

import net.sf.kdgcommons.util.SimpleCLIParser;


/**
 *  Command-line processor and option definitions.
 */
public class CommandLine
extends SimpleCLIParser
{
    public enum Options
    {
        OLD_VERSION, NEW_VERSION, UPDATE_PARENT
    }


    private static OptionDefinition[] optionDefs = new OptionDefinition[]
    {
        new OptionDefinition(
                Options.OLD_VERSION, "--fromVersion", 1,
                "The original version to update; POMs with a different version are ignored"),
        new OptionDefinition(
                Options.NEW_VERSION, "--toVersion", 1,
                "The new version value"),
        new OptionDefinition(
                Options.UPDATE_PARENT, "--updateParentRef", "--noUpdateParentRef", false,
                "If enabled, updates all parent references that match the \"from\" version "
                    + "(this is needed for hierarchical projects).")
    };

    public CommandLine(String[] argv) {
        super(argv, optionDefs);
    }
}
