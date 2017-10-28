package com.kdgregory.pomutil.version;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.collections.CollectionUtil;

import com.kdgregory.pomutil.version.CommandLine.Options;


public class TestCommandLine {

    @Test
    public void testBasicInvocation() throws Exception
    {
        CommandLine c = new CommandLine("--groupId", "com.example", "--artifactId", "ix", "--fromVersion", "1.0", "--toVersion", "2.0", "foo.xml", "bar.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("groupId",             Arrays.asList("com.example"),   c.getOptionValues(Options.GROUP_ID));
        assertEquals("artifactId",          Arrays.asList("ix"),            c.getOptionValues(Options.ARTIFACT_ID));
        assertEquals("fromVersion",         Arrays.asList("1.0"),           c.getOptionValues(Options.OLD_VERSION));
        assertEquals("toVersion",           Arrays.asList("2.0"),           c.getOptionValues(Options.NEW_VERSION));
        assertFalse("auto-version",         c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent",        c.isOptionEnabled(Options.UPDATE_PARENT));
        assertFalse("update dependencies",  c.isOptionEnabled(Options.UPDATE_DEPENDENCIES));
        assertEquals("filenames",           Arrays.asList("foo.xml", "bar.xml"), c.getParameters());
    }


    @Test
    public void testMissingGroupId() throws Exception
    {
        CommandLine c = new CommandLine("--artifactId", "ix", "--fromVersion", "1.0", "--toVersion", "2.0", "foo.xml", "bar.xml");

        assertFalse("command line is valid", c.isValid());
    }

    @Test
    public void testMissingArtifactId() throws Exception
    {
        CommandLine c = new CommandLine("--groupId", "com.example", "--fromVersion", "1.0", "--toVersion", "2.0", "foo.xml", "bar.xml");

        assertTrue("command line is valid", c.isValid());
        assertTrue("artifactId",            CollectionUtil.isEmpty(c.getOptionValues(Options.ARTIFACT_ID)));
    }


    @Test
    public void testMissingVersionsAndNoAutoVersion() throws Exception
    {
        CommandLine c1 = new CommandLine("--groupId", "com.example", "--fromVersion", "1.0", "pom.xml");
        assertFalse("command line is valid", c1.isValid());

        CommandLine c2 = new CommandLine("--groupId", "com.example", "--toVersion", "1.0", "pom.xml");
        assertFalse("command line is valid", c2.isValid());

        CommandLine c3 = new CommandLine("--groupId", "com.example", "pom.xml");
        assertFalse("command line is valid", c3.isValid());
    }


    @Test
    public void testAutoversion() throws Exception
    {
        CommandLine c = new CommandLine("--groupId", "com.example", "--artifactId", "ix", "--autoVersion", "foo.xml", "bar.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("groupId",             Arrays.asList("com.example"),   c.getOptionValues(Options.GROUP_ID));
        assertEquals("artifactId",          Arrays.asList("ix"),            c.getOptionValues(Options.ARTIFACT_ID));
        assertTrue("from-version",          CollectionUtil.isEmpty(c.getOptionValues(Options.OLD_VERSION)));
        assertTrue("from-version",          CollectionUtil.isEmpty(c.getOptionValues(Options.NEW_VERSION)));
        assertTrue("auto-version",          c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent",        c.isOptionEnabled(Options.UPDATE_PARENT));
        assertFalse("update dependencies",  c.isOptionEnabled(Options.UPDATE_DEPENDENCIES));
        assertEquals("filenames",           Arrays.asList("foo.xml", "bar.xml"), c.getParameters());
    }


    @Test
    public void testParentUpdate() throws Exception
    {
        CommandLine c = new CommandLine("--groupId", "com.example", "--fromVersion", "1.0", "--toVersion", "2.0", "--updateParent", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertFalse("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertTrue("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testDependencyUpdate() throws Exception
    {
        CommandLine c = new CommandLine("--groupId", "com.example", "--fromVersion", "1.0", "--toVersion", "2.0", "--updateDependencies", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertFalse("auto-version",         c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent",        c.isOptionEnabled(Options.UPDATE_PARENT));
        assertTrue("update parent",         c.isOptionEnabled(Options.UPDATE_DEPENDENCIES));
        assertEquals("filenames",           Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testMissingFiles() throws Exception
    {
        CommandLine c = new CommandLine("--fromVersion", "1.0", "--toVersion", "2.0");
        assertFalse("command line is valid", c.isValid());
    }
}
