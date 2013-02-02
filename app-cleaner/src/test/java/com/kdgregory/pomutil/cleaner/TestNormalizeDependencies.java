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

import java.util.List;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.transform.NormalizeDependencies;


public class TestNormalizeDependencies
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        new NormalizeDependencies(loadPom("cleaner/DependencyNormalize1.xml")).transform();

        Element dep1 = newXPath("//mvn:artifactId[text()='junit']/..").evaluateAsElement(dom());
        List<Element> children1 = DomUtil.getChildren(dep1);
        assertEquals("basic #/children", 4, children1.size());
        assertEquals("basic child 1", "groupId",    children1.get(0).getNodeName());
        assertEquals("basic child 2", "artifactId", children1.get(1).getNodeName());
        assertEquals("basic child 3", "version",    children1.get(2).getNodeName());
        assertEquals("basic child 4", "scope",      children1.get(3).getNodeName());

        Element dep2 = newXPath("//mvn:artifactId[text()='practicalxml']/..").evaluateAsElement(dom());
        List<Element> children2 = DomUtil.getChildren(dep2);
        assertEquals("defaults #/children", 3, children2.size());
        assertEquals("defaults child 1", "groupId",    children2.get(0).getNodeName());
        assertEquals("defaults child 2", "artifactId", children2.get(1).getNodeName());
        assertEquals("defaults child 3", "version",    children2.get(2).getNodeName());

        Element dep3 = newXPath("//mvn:artifactId[text()='kdgcommons']/..").evaluateAsElement(dom());
        List<Element> children3 = DomUtil.getChildren(dep3);
        assertEquals("mgmt #/children", 3, children3.size());
        assertEquals("mgmt child 1", "groupId",    children3.get(0).getNodeName());
        assertEquals("mgmt child 2", "artifactId", children3.get(1).getNodeName());
        assertEquals("mgmt child 3", "version",    children3.get(2).getNodeName());
    }


    @Test
    public void testUnexpectedElmementsArePreserved() throws Exception
    {
        new NormalizeDependencies(loadPom("cleaner/DependencyNormalize2.xml")).transform();

        Element dep = newXPath("//mvn:artifactId[text()='practicalxml']/..").evaluateAsElement(dom());
        List<Element> children = DomUtil.getChildren(dep);
        assertEquals("#/children", 4, children.size());
        assertEquals("child 1", "groupId",    children.get(0).getNodeName());
        assertEquals("child 2", "artifactId", children.get(1).getNodeName());
        assertEquals("child 3", "version",    children.get(2).getNodeName());
        assertEquals("child 4", "argle",      children.get(3).getNodeName());
    }


    @Test
    public void testDisabled() throws Exception
    {
        CommandLine args = new CommandLine("--noDependencyNormalize");
        new NormalizeDependencies(loadPom("cleaner/DependencyNormalize1.xml"), args).transform();

        // one check should be sufficient; we'll pick the one with the most happening
        Element dep = newXPath("//mvn:artifactId[text()='practicalxml']/..").evaluateAsElement(dom());
        List<Element> children = DomUtil.getChildren(dep);
        assertEquals("defaults #/children", 5, children.size());
        assertEquals("defaults child 1", "artifactId", children.get(0).getNodeName());
        assertEquals("defaults child 2", "groupId",    children.get(1).getNodeName());
        assertEquals("defaults child 3", "scope",      children.get(2).getNodeName());
        assertEquals("defaults child 4", "type",       children.get(3).getNodeName());
        assertEquals("defaults child 5", "version",    children.get(4).getNodeName());
    }
}
