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

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;


public class TestVersionProps
{

    private XPathWrapperFactory xpFact = new XPathWrapperFactory()
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");

    private Document dom;

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private void loadAndApply(String pomName)
    throws Exception
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream(pomName);
        dom = ParseUtil.parse(new InputSource(in));
        new VersionProps().transform(dom);
    }


    private void assertProperty(String propName, String expected)
    {
        String value = xpFact.newXPath("/mvn:project/mvn:properties/mvn:" + propName)
                       .evaluateAsString(dom);
        assertEquals("property " + propName, expected, value);
    }


    private void assertReference(String groupId, String artifactId, String expected)
    {
        String xpath = "//mvn:groupId[text()='" + groupId + "']/"
                     + "../mvn:artifactId[text()='" + artifactId + "']/"
                     + "../mvn:version";
        String actual = xpFact.newXPath(xpath).evaluateAsString(dom);
        assertEquals("version for " + groupId + ":" + artifactId, expected, actual);
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testBasicOperation() throws Exception
    {
        loadAndApply("VersionProps1.xml");

        assertProperty("junit.version",                     "4.10");
        assertProperty("net.sf.kdgcommons.version",         "1.0.6");

        assertReference("junit",             "junit",      "${junit.version}");
        assertReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");

        // verify that we didn't damage the existing properties section
        String existingProp = xpFact.newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding")
                              .evaluateAsString(dom);
        assertEquals("existing property still exists", "UTF-8", existingProp);
    }


    @Test
    public void testAdditionOfPropertiesSection() throws Exception
    {
        loadAndApply("VersionProps2.xml");

        Element props = xpFact.newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        assertNotNull("should find properties section", props);
        assertEquals("<properties> should have two children", 2, DomUtil.getChildren(props).size());

        assertProperty("junit.version",                     "4.10");
        assertProperty("net.sf.kdgcommons.version",         "1.0.6");

        assertReference("junit",             "junit",      "${junit.version}");
        assertReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");
    }


    @Test
    public void testExistingVersionPropertiesLeftAlone() throws Exception
    {
        loadAndApply("VersionProps3.xml");

        assertProperty("junit.version",                     "4.10");
        assertProperty("kdgcommons.version",                "1.0.6");

        assertReference("junit",             "junit",      "${junit.version}");
        assertReference("net.sf.kdgcommons", "kdgcommons", "${kdgcommons.version}");

        // ensure that we haven't added a property

        Element props = xpFact.newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        assertNotNull("should find properties section", props);
        assertEquals("<properties> should have two children", 2, DomUtil.getChildren(props).size());
    }


    @Test
    public void testSameGroupDifferentVersion() throws Exception
    {
        loadAndApply("VersionProps4.xml");

        // note that the first dependency gets the regular property name, the second gets
        // the second is the one that has artifactId appended
        assertProperty("com.example.version",               "1.2.3");
        assertProperty("com.example.bar.version",           "4.5.6");

        assertReference("com.example",  "foo",              "${com.example.version}");
        assertReference("com.example", "bar",               "${com.example.bar.version}");

        // FIXME - need to examine log output
    }
}
