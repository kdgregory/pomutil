// Copyright (c) Keith D Gregory, all rights reserved
package com.kdgregory.pomutil.cleaner;

import org.junit.Test;
import static org.junit.Assert.*;

import static com.kdgregory.pomutil.cleaner.CommandLine.Options.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;


public class TestCommandLine
{
    private void assertToString(CommandLine cmd, String... expectedComponents)
    {
        String stringVal = cmd.toString(); // this variable is useful for debugging
        TreeSet<String> actualComponents = new TreeSet<String>(Arrays.asList(stringVal.split(" ")));
        for (String component : expectedComponents)
        {
            assertTrue("missing component: " + component, actualComponents.remove(component));
        }
        assertTrue("unexpected components: " + actualComponents, actualComponents.isEmpty());
    }


    @Test
    public void testDefaults() throws Exception
    {
        CommandLine cmd = new CommandLine("test.xml");

        assertFalse(ORGANIZE_POM.toString(),            cmd.isOptionEnabled(ORGANIZE_POM));
        assertTrue(PRETTY_PRINT.toString(),             cmd.isOptionEnabled(PRETTY_PRINT));
        assertTrue(COMMON_PROPS.toString(),             cmd.isOptionEnabled(COMMON_PROPS));
        assertTrue(VERSION_PROPS.toString(),            cmd.isOptionEnabled(VERSION_PROPS));
        assertFalse(VP_REPLACE_EXISTING.toString(),     cmd.isOptionEnabled(VP_REPLACE_EXISTING));
        assertTrue(VP_CONVERT_PLUGINS.toString(),       cmd.isOptionEnabled(VP_CONVERT_PLUGINS));
        assertTrue(DEPENDENCY_NORMALIZE.toString(),     cmd.isOptionEnabled(DEPENDENCY_NORMALIZE));
        assertTrue(DEPENDENCY_SORT.toString(),          cmd.isOptionEnabled(DEPENDENCY_SORT));
        assertFalse(DEPENDENCY_SORT_BY_SCOPE.toString(), cmd.isOptionEnabled(DEPENDENCY_SORT_BY_SCOPE));
        assertTrue(PLUGIN_NORMALIZE.toString(),         cmd.isOptionEnabled(PLUGIN_NORMALIZE));

        assertEquals(VP_ARTIFACT_ID.toString(),         Collections.emptyList(),
                                                        cmd.getOptionValues(VP_ARTIFACT_ID));

        assertEquals("arguments",                       Arrays.asList("test.xml"),
                                                        cmd.getParameters());

        assertToString(cmd,
            "--noOrganizePom",
            "--prettyPrint",
            "--commonProps",
            "--versionProps",
            "--noReplaceExistingProps",
            "--convertPluginVersions",
            "--dependencyNormalize",
            "--dependencySort",
            "--noGroupDependenciesByScope",
            "--pluginNormalize",
            "test.xml"
           );
    }


    @Test
    public void testAllExplicit() throws Exception
    {
        CommandLine cmd = new CommandLine("--organizePom", "--noPrettyPrint",
                                          "--noCommonProps", "--noVersionProps", "--replaceExistingProps",
                                          "--noConvertPluginVersions", "--noPluginNormalize",
                                          "--noDependencyNormalize", "--noDependencySort", "--groupDependenciesByScope",
                                          "test.xml");

        assertTrue(ORGANIZE_POM.toString(),             cmd.isOptionEnabled(ORGANIZE_POM));
        assertFalse(PRETTY_PRINT.toString(),            cmd.isOptionEnabled(PRETTY_PRINT));
        assertFalse(COMMON_PROPS.toString(),            cmd.isOptionEnabled(COMMON_PROPS));
        assertFalse(VERSION_PROPS.toString(),           cmd.isOptionEnabled(VERSION_PROPS));
        assertTrue(VP_REPLACE_EXISTING.toString(),      cmd.isOptionEnabled(VP_REPLACE_EXISTING));
        assertFalse(VP_CONVERT_PLUGINS.toString(),      cmd.isOptionEnabled(VP_CONVERT_PLUGINS));
        assertFalse(PLUGIN_NORMALIZE.toString(),        cmd.isOptionEnabled(PLUGIN_NORMALIZE));
        assertFalse(DEPENDENCY_NORMALIZE.toString(),    cmd.isOptionEnabled(DEPENDENCY_NORMALIZE));
        assertFalse(DEPENDENCY_SORT.toString(),         cmd.isOptionEnabled(DEPENDENCY_SORT));
        assertTrue(DEPENDENCY_SORT_BY_SCOPE.toString(), cmd.isOptionEnabled(DEPENDENCY_SORT_BY_SCOPE));

        assertEquals(VP_ARTIFACT_ID.toString(),         Collections.emptyList(),
                                                        cmd.getOptionValues(VP_ARTIFACT_ID));

        assertEquals("arguments",                       Arrays.asList("test.xml"),
                                                        cmd.getParameters());

        assertToString(cmd,
            "--organizePom",
            "--noPrettyPrint",
            "--noCommonProps",
            "--noVersionProps",
            "--replaceExistingProps",
            "--noConvertPluginVersions",
            "--noPluginNormalize",
            "--noDependencyNormalize",
            "--noDependencySort",
            "--groupDependenciesByScope",
            "test.xml"
           );
    }


    @Test
    public void testExplicitArtifactId() throws Exception
    {
        CommandLine cmd = new CommandLine("--addArtifactIdToProp", "com.example", "test.xml");

        assertFalse(ORGANIZE_POM.toString(),            cmd.isOptionEnabled(ORGANIZE_POM));
        assertTrue(PRETTY_PRINT.toString(),             cmd.isOptionEnabled(PRETTY_PRINT));
        assertTrue(COMMON_PROPS.toString(),             cmd.isOptionEnabled(COMMON_PROPS));
        assertTrue(VERSION_PROPS.toString(),            cmd.isOptionEnabled(VERSION_PROPS));
        assertFalse(VP_REPLACE_EXISTING.toString(),     cmd.isOptionEnabled(VP_REPLACE_EXISTING));
        assertTrue(VP_CONVERT_PLUGINS.toString(),       cmd.isOptionEnabled(VP_CONVERT_PLUGINS));
        assertTrue(DEPENDENCY_NORMALIZE.toString(),     cmd.isOptionEnabled(DEPENDENCY_NORMALIZE));
        assertTrue(DEPENDENCY_SORT.toString(),          cmd.isOptionEnabled(DEPENDENCY_SORT));
        assertFalse(DEPENDENCY_SORT_BY_SCOPE.toString(), cmd.isOptionEnabled(DEPENDENCY_SORT_BY_SCOPE));
        assertTrue(PLUGIN_NORMALIZE.toString(),         cmd.isOptionEnabled(PLUGIN_NORMALIZE));

        assertEquals(VP_ARTIFACT_ID.toString(),         Arrays.asList("com.example"),
                                                        cmd.getOptionValues(VP_ARTIFACT_ID));

        assertEquals("arguments",                       Arrays.asList("test.xml"),
                                                        cmd.getParameters());

        assertToString(cmd,
            "--noOrganizePom",
            "--prettyPrint",
            "--commonProps",
            "--versionProps",
            "--noReplaceExistingProps",
            "--convertPluginVersions",
            "--dependencyNormalize",
            "--dependencySort",
            "--noGroupDependenciesByScope",
            "--pluginNormalize",
            "--addArtifactIdToProp",
            "com.example",
            "test.xml"
           );
    }
}
