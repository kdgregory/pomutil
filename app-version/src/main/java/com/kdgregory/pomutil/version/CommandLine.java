package com.kdgregory.pomutil.version;

import java.util.List;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.util.SimpleCLIParser;


/**
 *  Command-line processor and option definitions.
 */
public class CommandLine
extends SimpleCLIParser
{
    public enum Options
    {
        OLD_VERSION, NEW_VERSION, AUTO_VERSION, UPDATE_PARENT, UPDATE_DEPENDENCIES
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
                Options.AUTO_VERSION, "--autoVersion", "", false,
                "If enabled, automatically updates between release and development versions."
                + " You can combine with and explicit from- or to-version to apply those"
                + " options as filters"),
        new OptionDefinition(
                Options.UPDATE_PARENT, "--updateParentRef", "", false,
                "If enabled, updates all parent references that match the \"from\" version "
                    + "(this is needed for hierarchical projects).")
    };


    public CommandLine(String... argv)
    {
        super(argv, optionDefs);
    }


    public boolean isValid()
    {
        // the only validity problems are missing explicit versions without auto-version

        List<String> oldVersion = getOptionValues(CommandLine.Options.OLD_VERSION);
        List<String> newVersion = getOptionValues(CommandLine.Options.NEW_VERSION);
        boolean autoVersion = isOptionEnabled(CommandLine.Options.AUTO_VERSION);

        if ((! autoVersion) && (CollectionUtil.isEmpty(oldVersion) || CollectionUtil.isEmpty(newVersion)))
            return false;

        if (CollectionUtil.isEmpty(getParameters()))
            return false;

        return true;
    }
}
