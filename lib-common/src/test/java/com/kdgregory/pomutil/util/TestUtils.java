// Copyright (c) Keith D Gregory, all rights reserved
package com.kdgregory.pomutil.util;

import static net.sf.practicalxml.builder.XmlBuilder.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.junit.Test;

import static org.junit.Assert.*;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.util.NodeListIterable;


public class TestUtils
{
//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    private static List<String> getChildNames(Element elem)
    {
        // wouldn't it be nice to have Java8 here?
        List<String> childNames = new ArrayList<String>();
        for (Element child : DomUtil.getChildren(elem))
        {
            childNames.add(DomUtil.getLocalName(child));
        }
        return childNames;
    }

//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testRemoveChildrenToMap() throws Exception
    {
        Document dom = element("root",
                            element("c2"),
                            element("c1"),
                            element("c5"),
                            element("c4"),
                            element("c3"))
                       .toDOM();
        Element root = dom.getDocumentElement();

        Map<String,Element> map = Utils.removeChildrenToMap(root);

        assertTrue("root no longer has children", DomUtil.getChildren(root).isEmpty());
        assertEquals("child names, in order",
                     Arrays.asList("c2", "c1", "c5", "c4", "c3"),
                     new ArrayList<String>(map.keySet()));

        for (String name : map.keySet())
        {
            assertEquals("mapping refers to correct element (" + name + ")",
                         name,
                         DomUtil.getLocalName(map.get(name)));
        }
    }


    @Test
    public void testRemoveChildrenToMapWithNamespaces() throws Exception
    {
        Document dom = element("root",
                            element("x", "c2"),
                            element("x", "c1"),
                            element("x", "c5"),
                            element("x", "c4"),
                            element("x", "c3"))
                       .toDOM();
        Element root = dom.getDocumentElement();

        Map<String,Element> map = Utils.removeChildrenToMap(root);

        assertTrue("root no longer has children", DomUtil.getChildren(root).isEmpty());
        assertEquals("child names, in order",
                     Arrays.asList("c2", "c1", "c5", "c4", "c3"),
                     new ArrayList<String>(map.keySet()));

        for (String name : map.keySet())
        {
            assertEquals("mapping refers to correct element (" + name + ")",
                         name,
                         DomUtil.getLocalName(map.get(name)));
        }
    }


    @Test(expected=IllegalStateException.class)
    public void testRemoveChildrenToMapWithDuplicates() throws Exception
    {
        Document dom = element("root",
                            element("x", "c2"),
                            element("x", "c1"),
                            element("y", "c2"))
                       .toDOM();
        Element root = dom.getDocumentElement();

        Utils.removeChildrenToMap(root);
    }


    @Test
    public void testReconstructBasicOperation() throws Exception
    {
        Document dom = element("root",
                            element("c2"),
                            element("c1"),
                            element("c3"))
                       .toDOM();
        Element root = dom.getDocumentElement();
        Utils.reconstruct(root, "c3", "c1", "c2");

        assertEquals("elements have been reordered",
                     Arrays.asList("c3", "c1", "c2"),
                     getChildNames(root));
    }


    @Test
    public void testReconstructWithExtraElements() throws Exception
    {
        Document dom = element("root",
                            element("c4"),
                            element("c2"),
                            element("c1"),
                            element("c3"),
                            element("c5"))
                       .toDOM();
        Element root = dom.getDocumentElement();
        Utils.reconstruct(root, "c3", "c2");

        assertEquals("unspecified elements at end, in original order",
                     Arrays.asList("c3", "c2", "c4", "c1", "c5"),
                     getChildNames(root));
    }


    @Test
    public void testReconstructPreservesRootAttributes() throws Exception
    {
        Document dom = element("root",
                            attribute("argle", "bargle"),
                            element("c2"),
                            element("c1"),
                            element("c3"))
                       .toDOM();
        Element root = dom.getDocumentElement();
        Utils.reconstruct(root, "c3", "c1", "c2");

        assertEquals("attribute remains", "bargle", root.getAttribute("argle"));
    }


    @Test
    public void testReconstructPreservesComments() throws Exception
    {
        Document dom = element("root",
                            element("c2"),
                            comment("test"),
                            element("c1"),
                            element("c3"))
                       .toDOM();
        Element root = dom.getDocumentElement();
        Utils.reconstruct(root, "c3", "c1", "c2");

        boolean foundComment = false;
        for (Node node : new NodeListIterable(root.getChildNodes()))
        {
            if (node.getNodeType() == Node.COMMENT_NODE)
                foundComment = true;
        }
        assertTrue("found comment node", foundComment);
    }


}
