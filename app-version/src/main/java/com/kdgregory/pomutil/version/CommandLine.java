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
        GROUP_ID, ARTIFACT_ID, OLD_VERSION, NEW_VERSION, AUTO_VERSION, UPDATE_PARENT, UPDATE_DEPENDENCIES
    }


    private static OptionDefinition[] optionDefs = new OptionDefinition[]
    {
        new OptionDefinition(
                Options.GROUP_ID, "--groupId", 1,
                "Only POMs/dependencies with this group ID will be updated (required)"),
        new OptionDefinition(
                Options.ARTIFACT_ID, "--artifactId", 1,
                "Only POMs/dependencies with this artifact ID will be updated (optional)"),
        new OptionDefinition(
                Options.OLD_VERSION, "--fromVersion", 1,
                "Only POMs/dependencies with this artifact ID will be updated (optional)"),
        new OptionDefinition(
                Options.NEW_VERSION, "--toVersion", 1,
                "The new version value (optional)"),
        new OptionDefinition(
                Options.AUTO_VERSION, "--autoVersion", "", false,
                "If enabled, automatically updates between release and development versions"),
        new OptionDefinition(
                Options.UPDATE_PARENT, "--updateParent", "", false,
                "If enabled, updates all parent references that match the selection criteria"),
        new OptionDefinition(
                Options.UPDATE_DEPENDENCIES, "--updateDependencies", "", false,
                "If enabled, updates all dependency references that match the selection criteria")
    };


    public CommandLine(String... argv)
    {
        super(argv, optionDefs);
    }


    public boolean isValid()
    {
        // must specify group ID
        List<String> groupId = getOptionValues(CommandLine.Options.GROUP_ID);
        if (CollectionUtil.isEmpty(groupId))
            return false;

        // must specify old and new version if not auto-versioning
        List<String> oldVersion = getOptionValues(CommandLine.Options.OLD_VERSION);
        List<String> newVersion = getOptionValues(CommandLine.Options.NEW_VERSION);
        boolean autoVersion = isOptionEnabled(CommandLine.Options.AUTO_VERSION);
        if ((! autoVersion) && (CollectionUtil.isEmpty(oldVersion) || CollectionUtil.isEmpty(newVersion)))
            return false;

        // must specify list of POMs/directories
        if (CollectionUtil.isEmpty(getParameters()))
            return false;

        return true;
    }
}
