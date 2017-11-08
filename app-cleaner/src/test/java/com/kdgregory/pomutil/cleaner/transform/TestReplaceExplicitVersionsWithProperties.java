// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.pomutil.cleaner.transform;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.cleaner.transform.ReplaceExplicitVersionsWithProperties;


public class TestReplaceExplicitVersionsWithProperties
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps1.xml")).transform();

        assertProperty("commons-io.version",        "2.1");
        assertProperty("junit.version",             "4.10");

        assertDependencyReference("junit",          "junit",      "${junit.version}");
        assertDependencyReference("commons-io",     "commons-io", "${commons-io.version}");

        // verify that we removed the previous dependencies
        assertNoDependencyReference("junit",        "junit",      "4.10");
        assertNoDependencyReference("commons-io",   "commons-io", "2.1");

        // verify that we didn't damage the existing properties section

        String existingProp = newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding")
                              .evaluateAsString(dom());
        assertEquals("existing property still exists", "UTF-8", existingProp);

        // finally, verify that the new properties are in sorted order

        String lastPropValue = "";
        for (Element prop : newXPath("/mvn:project/mvn:properties/*").evaluate(dom(), Element.class))
        {
            String value = DomUtil.getLocalName(prop);
            if (!value.endsWith(".version"))
                continue;
            if (value.compareTo(lastPropValue) < 0)
                fail("found property " + value + " after " + lastPropValue);
            lastPropValue = value;
        }
    }


    @Test
    public void testAdditionOfPropertiesSection() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps2.xml")).transform();

        Element props = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom());
        assertNotNull("should find properties section", props);
        assertEquals("<properties> should have two children", 2, DomUtil.getChildren(props).size());

        assertProperty("junit.version",                     "4.10");
        assertProperty("net.sf.kdgcommons.version",         "1.0.6");

        assertDependencyReference("junit",             "junit",      "${junit.version}");
        assertDependencyReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");
    }


    @Test
    public void testExistingVersionPropertiesLeftAlone() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps3.xml")).transform();

        assertProperty("junit.version",                     "4.10");
        assertProperty("kdgcommons.version",                "1.0.6");

        assertDependencyReference("junit",             "junit",      "${junit.version}");
        assertDependencyReference("net.sf.kdgcommons", "kdgcommons", "${kdgcommons.version}");

        // ensure that we haven't added a property

        Element props = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom());
        assertNotNull("should find properties section", props);
        assertEquals("<properties> should have two children", 2, DomUtil.getChildren(props).size());
    }


    @Test
    public void testSameGroupDifferentVersion() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps4.xml")).transform();

        // note that the first dependency gets the regular property name, the second gets
        // the second is the one that has artifactId appended
        assertProperty("com.example.version",               "1.2.3");
        assertProperty("com.example.bar.version",           "4.5.6");

        assertDependencyReference("com.example", "foo",               "${com.example.version}");
        assertDependencyReference("com.example", "bar",               "${com.example.bar.version}");

        // FIXME - need to examine log output
    }


    @Test
    public void testAlwaysCombineGroupAndArtifact() throws Exception
    {
        CommandLine args = new CommandLine("--addArtifactIdToProp=com.example");
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps4.xml"), args).transform();

        // note that the first dependency gets the regular property name, the second gets
        // the second is the one that has artifactId appended
        assertProperty("com.example.foo.version",           "1.2.3");
        assertProperty("com.example.bar.version",           "4.5.6");

        assertDependencyReference("com.example", "foo",               "${com.example.foo.version}");
        assertDependencyReference("com.example", "bar",               "${com.example.bar.version}");
    }


    @Test
    public void testSameGroupSameVersion() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps5.xml")).transform();

        assertEquals("should only be one property added", 1, newXPath("/mvn:project/mvn:properties/*").evaluate(dom()).size());

        assertProperty("org.springframework.version",                       "3.1.2.RELEASE");

        assertDependencyReference("org.springframework", "spring-tx",       "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-core",     "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-context",  "${org.springframework.version}");
    }


    @Test
    public void testReplaceExistingVersionProperties() throws Exception
    {
        CommandLine args = new CommandLine("--replaceExistingProps");
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps6.xml"), args).transform();

        assertEquals("post-transform property count", 3, newXPath("/mvn:project/mvn:properties/*").evaluate(dom()).size());

        assertProperty("com.example.version",               "1.2.3");
        assertProperty("com.other.version",                 "4.5.6");

        assertDependencyReference("com.example", "foo",     "${com.example.version}");
        assertDependencyReference("com.other",   "bar",     "${com.other.version}");

        // verify that we did not damage to existing non-version property

        assertProperty("some.innocuous.propery",            "foo");
    }


    @Test
    public void testReplacePluginVersions() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps7.xml")).transform();

        assertProperty("commons-io.version",                    "2.1");
        assertProperty("storm.version",                         "0.7.0");
        assertProperty("plugin.maven-antrun-plugin.version",    "1.3");
        assertProperty("plugin.maven-compiler-plugin.version",  "2.3.2");
        assertProperty("plugin.cobertura-maven-plugin.version", "2.5.1");

        // because not all plugins have groupIds, we can't use assertDependencyReference(), but must
        // instead use exact paths ... relies on the POM having one plugin per section

        assertEquals("build plugin", "${plugin.maven-antrun-plugin.version}",
                                     newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "${plugin.maven-compiler-plugin.version}",
                                     newXPath("/mvn:project/mvn:build/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "${plugin.cobertura-maven-plugin.version}",
                                     newXPath("/mvn:project/mvn:reporting/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));

        // verify that plugins are at the end of the property list
        String lastPluginProp = null;
        for (Element prop : newXPath("/mvn:project/mvn:properties/*").evaluate(dom(), Element.class))
        {
            String value = DomUtil.getLocalName(prop);
            if (value.startsWith("plugin."))
                lastPluginProp = value;
            else if (lastPluginProp != null)
                fail("found property " + value + " after " + lastPluginProp);
        }
    }


    @Test
    public void testLeaveExistingPluginVersions() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps8.xml")).transform();

        assertProperty("commons-io.version",                    "2.1");
        assertProperty("storm.version",                         "0.7.0");
        assertProperty("plugins.maven-antrun-plugin",           "1.3");     // note "plugins", not ".version"
        assertProperty("plugin.maven-compiler-plugin.version",  "2.3.2");

        assertEquals("build plugin", "${plugins.maven-antrun-plugin}",
                                     newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "${plugin.maven-compiler-plugin.version}",
                                     newXPath("/mvn:project/mvn:build/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
    }


    @Test
    public void testPluginsWithSameArtifactId() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps9.xml")).transform();

        assertProperty("plugin.example-plugin.version",        "1.1");
        assertProperty("plugin.example-plugin-1.2.version",    "1.2");

        assertEquals("build plugin", "${plugin.example-plugin.version}",
                                     newXPath("//mvn:plugin/mvn:groupId[text()='com.example']/../mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "${plugin.example-plugin-1.2.version}",
                                     newXPath("//mvn:plugin/mvn:groupId[text()='com.example.other']/../mvn:version")
                                     .evaluateAsString(dom()));

        // FIXME - check log
    }


    @Test
    public void testReplaceExistingPluginVersionProperties() throws Exception
    {
        CommandLine args = new CommandLine("--replaceExistingProps");
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps8.xml"), args).transform();

        assertProperty("commons-io.version",                    "2.1");
        assertProperty("storm.version",                         "0.7.0");
        assertProperty("plugin.maven-antrun-plugin.version",    "1.3");
        assertProperty("plugin.maven-compiler-plugin.version",  "2.3.2");

        // because not all plugins have groupIds, we can't use assertDependencyReference(), but must
        // instead use exact paths ... relies on the POM having one plugin per section

        assertEquals("build plugin", "${plugin.maven-antrun-plugin.version}",
                                     newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "${plugin.maven-compiler-plugin.version}",
                                     newXPath("/mvn:project/mvn:build/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
    }


    @Test
    public void testIgnorePluginsWithoutVersions() throws Exception
    {
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps10.xml")).transform();

        // note: there's only one plugin defined
        assertEquals("plugin group",    "com.example",
                                        newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:groupId")
                                        .evaluateAsString(dom()));
        assertEquals("plugin artifact", "example-plugin",
                                        newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:artifactId")
                                        .evaluateAsString(dom()));
        assertEquals("plugin version",  "",
                                        newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:version")
                                        .evaluateAsString(dom()));

    }



    @Test
    public void testDisabled() throws Exception
    {
        CommandLine args = new CommandLine("--noVersionProps");
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps1.xml"), args).transform();

        assertNull("property should not be added",
                   newXPath("/mvn:project/mvn:properties/mvn:junit.version").evaluateAsElement(dom()));

        assertDependencyReference("junit", "junit", "4.10");
    }


    @Test
    public void testDisablePluginConversion() throws Exception
    {
        CommandLine args = new CommandLine("--noConvertPluginVersions");
        new ReplaceExplicitVersionsWithProperties(loadPom("cleaner/VersionProps7.xml"), args).transform();

        assertProperty("commons-io.version",                    "2.1");
        assertProperty("storm.version",                         "0.7.0");
        assertProperty("plugin.maven-antrun-plugin.version",    "");
        assertProperty("plugin.maven-compiler-plugin.version",  "");
        assertProperty("plugin.cobertura-maven-plugin.version", "");

        assertEquals("build plugin", "1.3",
                                     newXPath("/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "2.3.2",
                                     newXPath("/mvn:project/mvn:build/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
        assertEquals("build plugin", "2.5.1",
                                     newXPath("/mvn:project/mvn:reporting/mvn:plugins/mvn:plugin/mvn:version")
                                     .evaluateAsString(dom()));
    }
}
