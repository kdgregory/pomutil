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

import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapper;
import net.sf.practicalxml.xpath.XPathWrapperFactory;


/**
 *  Base class for testcases. Provides a shared method for loading and applying
 *  transformers, some assertions against the transformed DOM, and an XPath
 *  factory with the maven namespace already bound.
 */
public abstract class AbstractTransformerTest
{

    private XPathWrapperFactory xpFact = new XPathWrapperFactory()
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");

    protected Document dom;


//----------------------------------------------------------------------------
//  POM integeration
//----------------------------------------------------------------------------

    /**
     *  Loads the specified POM from the resource path, and applies one or more
     *  transformations to it. The result is stored in the <code>dom</code>
     *  member variable.
     */
    protected void loadAndApply(String pomName, AbstractTransformer... transforms)
    throws Exception
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream(pomName);
        dom = ParseUtil.parse(new InputSource(in));

        for (AbstractTransformer transform : transforms)
        {
            dom = transform.transform(dom);
        }
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
        String value = xpFact.newXPath("/mvn:project/mvn:properties/mvn:" + propName)
                       .evaluateAsString(dom);
        assertEquals("property " + propName, expected, value);
    }


    /**
     *  Asserts that there is some GAV reference with the expected version number.
     *  Note that this is not limited to dependencies; it can be used anywhere there
     *  are sibling elements <code>groupId</code>, <code>artifactId</code>, and
     *  <code>version</code>.
     */
    protected void assertReference(String groupId, String artifactId, String expected)
    {
        String xpath = "//mvn:groupId[text()='" + groupId + "']/"
                     + "../mvn:artifactId[text()='" + artifactId + "']/"
                     + "../mvn:version";
        String actual = xpFact.newXPath(xpath).evaluateAsString(dom);
        assertEquals("version for " + groupId + ":" + artifactId, expected, actual);
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
