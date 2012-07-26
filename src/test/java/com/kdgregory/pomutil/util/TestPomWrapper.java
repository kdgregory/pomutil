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

package com.kdgregory.pomutil.util;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.junit.Test;

import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;


public class TestPomWrapper
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  Loads the named POM as a classpath resource.
     */
    private static Document loadPom(String path)
    {
        InputStream in = TestPomWrapper.class.getClassLoader().getResourceAsStream(path);
        return ParseUtil.parse(new InputSource(in));
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testSelectAgainstPom() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));

        assertEquals("selectValue()",
                     "A simple POM with a few sections",
                     wrapper.selectValue("/mvn:project/mvn:description").trim());

        Element propElem = wrapper.selectElement("/mvn:project/mvn:properties");
        assertEquals("selectElement()",
                     "properties", DomUtil.getLocalName(propElem));

        List<Element> properties = wrapper.selectElements("/mvn:project/mvn:properties/*");
        assertEquals("selectElements()",
                     2, properties.size());
    }


    @Test
    public void testSelectAgainstNode() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));

        Element root = wrapper.getDom().getDocumentElement();

        Element propElem = wrapper.selectElement(root, "mvn:properties");
        assertEquals("selectElement(Node,path)", "properties", DomUtil.getLocalName(propElem));

        List<Element> allProps = wrapper.selectElements(propElem, "*");
        assertEquals("selectElements(Node,path)", 2, allProps.size());

        String value = wrapper.selectValue(propElem, "*[1]");
        assertEquals("selectValue(Node,path)", DomUtil.getText(allProps.get(0)), value);
    }


    @Test
    public void testSelectOrCreate() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));

        String path = "/mvn:project/mvn:name";
        assertNull("element doesn't exist at start", wrapper.selectElement(path));

        Element newElem = wrapper.selectOrCreateElement(path);
        assertNotNull("element was created", newElem);
        assertEquals("element created with correct namespace", "http://maven.apache.org/POM/4.0.0", newElem.getNamespaceURI());
        assertEquals("element was created with correct name",  "name", DomUtil.getLocalName(newElem));

        Element check = wrapper.selectElement(path);
        assertSame("element was selectable via path", newElem, check);
    }


    @Test
    public void testSelectOrCreateRecursive() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));

        String path = "/mvn:project/mvn:argle/mvn:bargle";
        assertNull("element doesn't exist at start", wrapper.selectElement(path));

        Element newElem = wrapper.selectOrCreateElement(path);
        assertNotNull("element was created", newElem);
        assertEquals("element created with correct namespace", "http://maven.apache.org/POM/4.0.0", newElem.getNamespaceURI());
        assertEquals("element was created with correct name",  "bargle", DomUtil.getLocalName(newElem));

        Element check = wrapper.selectElement(path);
        assertSame("element was selectable via path", newElem, check);
    }
    
    
    @Test
    public void testExtractGAV() throws Exception 
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));
        
        Element elem = wrapper.selectElement("/mvn:project/mvn:dependencies/mvn:dependency[1]");
        GAV gav = wrapper.extractGAV(elem);
        
        assertEquals("groupId",     "junit",    gav.groupId);
        assertEquals("artifactId",  "junit",    gav.artifactId);
        assertEquals("version",     "4.10",     gav.version);
    }
    

    @Test
    public void testClear() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(loadPom("PomWrapper1.xml"));

        Element propElem = wrapper.clear("/mvn:project/mvn:properties");
        assertEquals("element name", "properties", DomUtil.getLocalName(propElem));
        assertEquals("number of children", 0, propElem.getChildNodes().getLength());
    }

}
