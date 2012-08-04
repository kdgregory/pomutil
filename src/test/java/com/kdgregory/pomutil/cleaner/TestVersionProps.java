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

package com.kdgregory.pomutil.cleaner;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.VersionProps;
import com.kdgregory.pomutil.util.InvocationArgs;


public class TestVersionProps
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        new VersionProps(loadPom("cleaner/VersionProps1.xml")).transform();

        assertProperty("junit.version",                     "4.10");
        assertProperty("net.sf.kdgcommons.version",         "1.0.6");

        assertDependencyReference("junit",             "junit",      "${junit.version}");
        assertDependencyReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");

        // verify that we removed the previous dependencies
        assertNoDependencyReference("junit",             "junit",      "4.10");
        assertNoDependencyReference("net.sf.kdgcommons", "kdgcommons", "1.0.6");

        // verify that we didn't damage the existing properties section
        String existingProp = newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding")
                              .evaluateAsString(dom());
        assertEquals("existing property still exists", "UTF-8", existingProp);
    }


    @Test
    public void testAdditionOfPropertiesSection() throws Exception
    {
        new VersionProps(loadPom("cleaner/VersionProps2.xml")).transform();

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
        new VersionProps(loadPom("cleaner/VersionProps3.xml")).transform();

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
        new VersionProps(loadPom("cleaner/VersionProps4.xml")).transform();

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
        InvocationArgs args = new InvocationArgs("--addArtifactIdToProp=com.example");
        new VersionProps(loadPom("cleaner/VersionProps4.xml"), args).transform();

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
        new VersionProps(loadPom("cleaner/VersionProps5.xml")).transform();

        assertEquals("should only be one property added", 1, newXPath("/mvn:project/mvn:properties/*").evaluate(dom()).size());

        assertProperty("org.springframework.version",                       "3.1.2.RELEASE");

        assertDependencyReference("org.springframework", "spring-tx",       "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-core",     "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-context",  "${org.springframework.version}");
    }


    @Test
    public void testReplaceExistingProperties() throws Exception
    {
        InvocationArgs args = new InvocationArgs("--replaceExistingProps");
        new VersionProps(loadPom("cleaner/VersionProps6.xml"), args).transform();

        assertEquals("post-transform property count", 3, newXPath("/mvn:project/mvn:properties/*").evaluate(dom()).size());

        assertProperty("com.example.version",               "1.2.3");
        assertProperty("com.other.version",                 "4.5.6");

        assertDependencyReference("com.example", "foo",     "${com.example.version}");
        assertDependencyReference("com.other",   "bar",     "${com.other.version}");

        // verify that we did not damage to existing non-version property

        assertProperty("some.innocuous.propery",            "foo");
    }


    @Test
    public void testDisabled() throws Exception
    {
        InvocationArgs args = new InvocationArgs("--noVersionProps");
        new VersionProps(loadPom("cleaner/VersionProps1.xml"), args).transform();

        assertNull("property should not be added",
                   newXPath("/mvn:project/mvn:properties/mvn:junit.version").evaluateAsElement(dom()));

        assertDependencyReference("junit", "junit", "4.10");
    }
}
