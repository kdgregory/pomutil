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

import java.util.List;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.cleaner.transform.NormalizePlugins;


public class TestNormalizePlugins
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        new NormalizePlugins(loadPom("cleaner/PluginNormalize1.xml")).transform();

        Element plug1 = newXPath("//mvn:artifactId[text()='maven-antrun-plugin']/..").evaluateAsElement(dom());
        List<Element> children1 = DomUtil.getChildren(plug1);
        assertEquals("plugin #1, #/children", 3, children1.size());
        assertEquals("plugin #1, child #1 name",  "groupId",                  DomUtil.getLocalName(children1.get(0)));
        assertEquals("plugin #1, child #1 value", "org.apache.maven.plugins", DomUtil.getText(children1.get(0)));
        assertEquals("plugin #1, child #2 name",  "artifactId",               DomUtil.getLocalName(children1.get(1)));
        assertEquals("plugin #1, child #2 value", "maven-antrun-plugin",      DomUtil.getText(children1.get(1)));
        assertEquals("plugin #1, child #3 name",  "version",                  DomUtil.getLocalName(children1.get(2)));
        assertEquals("plugin #1, child #3 value", "1.3",                      DomUtil.getText(children1.get(2)));

        Element plug2 = newXPath("//mvn:artifactId[text()='cobertura-maven-plugin']/..").evaluateAsElement(dom());
        List<Element> children2 = DomUtil.getChildren(plug2);
        assertEquals("plugin #2, #/children", 3, children2.size());
        assertEquals("plugin #2, child #1 name",  "groupId",                  DomUtil.getLocalName(children2.get(0)));
        assertEquals("plugin #2, child #1 value", "org.codehaus.mojo",        DomUtil.getText(children2.get(0)));
        assertEquals("plugin #2, child #2 name",  "artifactId",               DomUtil.getLocalName(children2.get(1)));
        assertEquals("plugin #2, child #2 value", "cobertura-maven-plugin",   DomUtil.getText(children2.get(1)));
        assertEquals("plugin #2, child #3 name",  "configuration",            DomUtil.getLocalName(children2.get(2)));

        Element plug3 = newXPath("//mvn:artifactId[text()='maven-site-plugin']/..").evaluateAsElement(dom());
        List<Element> children3 = DomUtil.getChildren(plug3);
        assertEquals("plugin #3, #/children", 3, children3.size());
        assertEquals("plugin #3, child #1 name",  "groupId",                  DomUtil.getLocalName(children3.get(0)));
        assertEquals("plugin #3, child #1 value", "org.apache.maven.plugins", DomUtil.getText(children3.get(0)));
        assertEquals("plugin #3, child #2 name",  "artifactId",               DomUtil.getLocalName(children3.get(1)));
        assertEquals("plugin #3, child #2 value", "maven-site-plugin",        DomUtil.getText(children3.get(1)));
        assertEquals("plugin #3, child #3 name",  "version",                  DomUtil.getLocalName(children3.get(2)));
        assertEquals("plugin #3, child #3 value", "2.0.1",                    DomUtil.getText(children3.get(2)));
    }


    @Test
    public void testDisabled() throws Exception
    {
        CommandLine args = new CommandLine("--noPluginNormalize");
        new NormalizePlugins(loadPom("cleaner/PluginNormalize1.xml"), args).transform();

        Element plug1 = newXPath("//mvn:artifactId[text()='maven-antrun-plugin']/..").evaluateAsElement(dom());
        List<Element> children1 = DomUtil.getChildren(plug1);
        assertEquals("plugin #1, #/children", 2, children1.size());
        assertEquals("plugin #1, child #1 name",  "version",                  DomUtil.getLocalName(children1.get(0)));
        assertEquals("plugin #1, child #1 value", "1.3",                      DomUtil.getText(children1.get(0)));
        assertEquals("plugin #1, child #2 name",  "artifactId",               DomUtil.getLocalName(children1.get(1)));
        assertEquals("plugin #1, child #2 value", "maven-antrun-plugin",      DomUtil.getText(children1.get(1)));
    }
}
