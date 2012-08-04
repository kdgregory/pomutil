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

import com.kdgregory.pomutil.cleaner.transform.OrganizePom;
import com.kdgregory.pomutil.util.InvocationArgs;


public class TestOrganizePom
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        loadPom("cleaner/OrganizePom1.xml");
        Element root = dom().getDocumentElement();

        List<Element> initialChildren = DomUtil.getChildren(root);
        assertEquals("number of children before organize", 9, initialChildren.size());

        InvocationArgs args = new InvocationArgs("--organizePom");
        new OrganizePom(pom, args).transform();

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("number of children after organize", 9, children.size());

        assertEquals("child 0 name", "modelVersion",    children.get(0).getLocalName());
        assertEquals("child 0 text", "4.0.0",           DomUtil.getText(children.get(0)));

        assertEquals("child 1 name", "groupId",         children.get(1).getLocalName());
        assertEquals("child 1 text", "com.example.pom", DomUtil.getText(children.get(1)));

        assertEquals("child 2 name", "artifactId",      children.get(2).getLocalName());
        assertEquals("child 2 text", "pomorg1",         DomUtil.getText(children.get(2)));

        assertEquals("child 3 name", "version",         children.get(3).getLocalName());
        assertEquals("child 3 text", "1.0-SNAPSHOT",    DomUtil.getText(children.get(3)));

        assertEquals("child 4 name", "packaging",       children.get(4).getLocalName());
        assertEquals("child 4 text", "jar",             DomUtil.getText(children.get(4)));

        // at this point I trust that we're moving entire child trees

        assertEquals("child 5 name", "description",         children.get(5).getLocalName());
        assertEquals("child 6 name", "properties",          children.get(6).getLocalName());
        assertEquals("child 7 name", "dependencyManagement",children.get(7).getLocalName());
        assertEquals("child 8 name", "dependencies",        children.get(8).getLocalName());
    }


    @Test
    public void testDisabledByDefault() throws Exception
    {
        loadPom("cleaner/OrganizePom1.xml");
        Element root = dom().getDocumentElement();

        InvocationArgs args = new InvocationArgs();
        new OrganizePom(pom, args).transform();

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("number of children", 9, children.size());

        // checking first and last child should be sufficient
        assertEquals("first child name", "description",         children.get(0).getLocalName());
        assertEquals("last child name", "modelVersion",         children.get(8).getLocalName());
    }


    @Test
    public void testUnrecognizedChildrenAreRetained() throws Exception
    {
        loadPom("cleaner/OrganizePom2.xml");
        Element root = dom().getDocumentElement();

        List<Element> initialChildren = DomUtil.getChildren(root);
        assertEquals("number of children before organize", 7, initialChildren.size());

        InvocationArgs args = new InvocationArgs("--organizePom");
        new OrganizePom(pom, args).transform();

        List<Element> children = DomUtil.getChildren(root);
        assertEquals("number of children after organize", 7, children.size());

        assertEquals("child 0 name", "modelVersion",    children.get(0).getLocalName());
        assertEquals("child 1 name", "groupId",         children.get(1).getLocalName());
        assertEquals("child 2 name", "artifactId",      children.get(2).getLocalName());
        assertEquals("child 3 name", "version",         children.get(3).getLocalName());
        assertEquals("child 4 name", "packaging",       children.get(4).getLocalName());
        assertEquals("child 5 name", "description",     children.get(5).getLocalName());
        assertEquals("child 6 name", "argle",           children.get(6).getLocalName());
    }
}
