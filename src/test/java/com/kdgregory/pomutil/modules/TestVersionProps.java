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

package com.kdgregory.pomutil.modules;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.util.InvocationArgs;


public class TestVersionProps
extends AbstractTransformerTest
{

    @Test
    public void testBasicOperation() throws Exception
    {
        loadAndApply("VersionProps1.xml", new VersionProps());

        assertProperty("junit.version",                     "4.10");
        assertProperty("net.sf.kdgcommons.version",         "1.0.6");

        assertDependencyReference("junit",             "junit",      "${junit.version}");
        assertDependencyReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");

        // verify that we removed the previous dependencies
        assertNoDependencyReference("junit",             "junit",      "4.10");
        assertNoDependencyReference("net.sf.kdgcommons", "kdgcommons", "1.0.6");

        // verify that we didn't damage the existing properties section
        String existingProp = newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding")
                              .evaluateAsString(dom);
        assertEquals("existing property still exists", "UTF-8", existingProp);
    }


    @Test
    public void testAdditionOfPropertiesSection() throws Exception
    {
        loadAndApply("VersionProps2.xml", new VersionProps());

        Element props = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
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
        loadAndApply("VersionProps3.xml", new VersionProps());

        assertProperty("junit.version",                     "4.10");
        assertProperty("kdgcommons.version",                "1.0.6");

        assertDependencyReference("junit",             "junit",      "${junit.version}");
        assertDependencyReference("net.sf.kdgcommons", "kdgcommons", "${kdgcommons.version}");

        // ensure that we haven't added a property

        Element props = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        assertNotNull("should find properties section", props);
        assertEquals("<properties> should have two children", 2, DomUtil.getChildren(props).size());
    }


    @Test
    public void testSameGroupDifferentVersion() throws Exception
    {
        loadAndApply("VersionProps4.xml", new VersionProps());

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
        loadAndApply("VersionProps4.xml", new VersionProps(args));

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
        loadAndApply("VersionProps5.xml", new VersionProps());
        
        assertEquals("should only be one property added", 1, newXPath("/mvn:project/mvn:properties/*").evaluate(dom).size());

        assertProperty("org.springframework.version",                       "3.1.2.RELEASE");

        assertDependencyReference("org.springframework", "spring-tx",       "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-core",     "${org.springframework.version}");
        assertDependencyReference("org.springframework", "spring-context",  "${org.springframework.version}");
    }
}
