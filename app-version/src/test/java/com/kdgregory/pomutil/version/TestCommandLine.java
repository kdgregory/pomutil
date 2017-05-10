package com.kdgregory.pomutil.version;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pomutil.version.CommandLine.Options;


public class TestCommandLine {

    @Test
    public void testNormalInvocation() throws Exception
    {
        CommandLine c = new CommandLine("--fromVersion", "1.0", "--toVersion", "2.0", "foo.xml", "bar.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("from-versions", Arrays.asList("1.0"), c.getOptionValues(Options.OLD_VERSION));
        assertEquals("from-versions", Arrays.asList("2.0"), c.getOptionValues(Options.NEW_VERSION));
        assertFalse("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("foo.xml", "bar.xml"), c.getParameters());
    }


    @Test
    public void testParentInvocation() throws Exception
    {
        CommandLine c = new CommandLine("--fromVersion", "1.0", "--toVersion", "2.0", "--updateParentRef", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("from-versions", Arrays.asList("1.0"), c.getOptionValues(Options.OLD_VERSION));
        assertEquals("from-versions", Arrays.asList("2.0"), c.getOptionValues(Options.NEW_VERSION));
        assertFalse("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertTrue("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testAutoversionFull() throws Exception
    {
        CommandLine c = new CommandLine("--autoVersion", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("from-versions", Collections.emptyList(), c.getOptionValues(Options.OLD_VERSION));
        assertEquals("from-versions", Collections.emptyList(), c.getOptionValues(Options.NEW_VERSION));
        assertTrue("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testAutoversionFrom() throws Exception
    {
        CommandLine c = new CommandLine("--autoVersion", "--toVersion", "2.0", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("from-versions", Collections.emptyList(), c.getOptionValues(Options.OLD_VERSION));
        assertEquals("from-versions", Arrays.asList("2.0"), c.getOptionValues(Options.NEW_VERSION));
        assertTrue("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testAutoversionTo() throws Exception
    {
        CommandLine c = new CommandLine("--autoVersion", "--fromVersion", "1.0", "pom.xml");

        assertTrue("command line is valid", c.isValid());
        assertEquals("from-versions", Arrays.asList("1.0"), c.getOptionValues(Options.OLD_VERSION));
        assertEquals("from-versions", Collections.emptyList(), c.getOptionValues(Options.NEW_VERSION));
        assertTrue("auto-version", c.isOptionEnabled(Options.AUTO_VERSION));
        assertFalse("update parent", c.isOptionEnabled(Options.UPDATE_PARENT));
        assertEquals("filenames", Arrays.asList("pom.xml"), c.getParameters());
    }


    @Test
    public void testMissingVersionsAndNoAutoVersion() throws Exception
    {
        CommandLine c1 = new CommandLine("--fromVersion", "1.0", "pom.xml");
        assertFalse("command line is valid", c1.isValid());

        CommandLine c2 = new CommandLine("--toVersion", "1.0", "pom.xml");
        assertFalse("command line is valid", c2.isValid());

        CommandLine c3 = new CommandLine("pom.xml");
        assertFalse("command line is valid", c3.isValid());
    }


    @Test
    public void testMissingFiles() throws Exception
    {
        CommandLine c = new CommandLine("--fromVersion", "1.0", "--toVersion", "2.0");
        assertFalse("command line is valid", c.isValid());
    }
}
