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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapper;
import net.sf.practicalxml.xpath.XPathWrapperFactory;

import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Base class for testcases. Provides a shared method for loading and applying
 *  transformers, some assertions against the transformed DOM, and an XPath
 *  factory with the maven namespace already bound.
 */
public abstract class AbstractTransformerTest
{

    private XPathWrapperFactory xpFact = new XPathWrapperFactory()
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");

    protected PomWrapper pom;


//----------------------------------------------------------------------------
//  POM integeration
//----------------------------------------------------------------------------

    /**
     *  Loads the specified POM from the resource path, stashing the wrapper
     *  for future use.
     */
    protected PomWrapper loadPom(String pomName)
    throws Exception
    {
        Document initialDom = ParseUtil.parseFromClasspath(pomName);
        pom = new PomWrapper(initialDom);
        return pom;
    }


    /**
     *  Returns the current DOM from the PomWrapper
     */
    public Document dom()
    {
        return pom.getDom();
    }


//----------------------------------------------------------------------------
//  Asserts
//----------------------------------------------------------------------------

    /**
     *  Asserts that there is a <code>property</code> entry with the given
     *  name and value.
     */
    protected void assertProperty(String propName, String expected)
    {
        XPathWrapper xpath = xpFact.newXPath("/mvn:project/mvn:properties/mvn:" + propName);
        if (expected == null)
            assertNull("property " + propName, xpath.evaluateAsElement(dom()));
        else
            assertEquals("property " + propName, expected, xpath.evaluateAsString(dom()));
    }


    /**
     *  Asserts that there is a dependency reference somewhere in the POM with the
     *  expected group ID, artifact ID, and version number. Note that, since this
     *  performs an unrestricted search, it's only useful if you know that there
     *  will be one and only one such reference.
     */
    protected void assertDependencyReference(String groupId, String artifactId, String expected)
    {
        String xpath = "//mvn:groupId[text()='" + groupId + "']/"
                     + "../mvn:artifactId[text()='" + artifactId + "']/"
                     + "../mvn:version";
        String actual = xpFact.newXPath(xpath).evaluateAsString(dom());
        assertEquals("version for " + groupId + ":" + artifactId, expected, actual);
    }


    /**
     *  Asserts that there is no dependency reference anywhere in the POM with the
     *  expected group ID, artifact ID, and version number. This is used to ensure
     *  that existing dependencies have been modified.
     */
    protected void assertNoDependencyReference(String groupId, String artifactId, String version)
    {
        String xpath = "//mvn:groupId[text()='" + groupId + "']/"
                     + "../mvn:artifactId[text()='" + artifactId + "']/"
                     + "../mvn:version[text()='" + version + "']";
        Element elem = xpFact.newXPath(xpath).evaluateAsElement(dom());
        assertNull("should not find dependency with GAV " + groupId + ":" + artifactId + ":" + version, elem);
    }


    /**
     *  Asserts that the passed element is a <code>dependency</code>, and that
     *  its GAV matches the expected values.
     */
    protected void assertDependencySpec(
            String msg, Element elem,
            String expectedGroupId, String expectedArtifactId, String expectedVersion)
    {
        assertEquals(msg + ": element",    "dependency",       DomUtil.getLocalName(elem));
        assertEquals(msg + ": groupId",    expectedGroupId,    xpFact.newXPath("mvn:groupId").evaluateAsString(elem));
        assertEquals(msg + ": artifactId", expectedArtifactId, xpFact.newXPath("mvn:artifactId").evaluateAsString(elem));
        assertEquals(msg + ": version",    expectedVersion,    xpFact.newXPath("mvn:version").evaluateAsString(elem));
    }


//----------------------------------------------------------------------------
//  Utiltiies
//----------------------------------------------------------------------------

    /**
     *  Returns an XPath from the factory.
     */
    protected XPathWrapper newXPath(String xpath)
    {
        return xpFact.newXPath(xpath);
    }
}
