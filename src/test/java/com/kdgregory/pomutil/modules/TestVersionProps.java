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

import org.xml.sax.InputSource;

import org.junit.Test;
import static org.junit.Assert.*;

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
    public void testSimplePOM() throws Exception
    {
        loadAndApply("props1.xml");

        assertProperty("junit.version",             "4.10");
        assertProperty("net.sf.kdgcommons.version", "1.0.6");

        assertReference("junit",             "junit",      "${junit.version}");
        assertReference("net.sf.kdgcommons", "kdgcommons", "${net.sf.kdgcommons.version}");
    }

}
