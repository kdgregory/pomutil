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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;


/**
 *  Static utility methods that don't have a better home.
 */
public class Utils
{
    /**
     *  Selects and concatenates the elements from multiple paths.
     */
    public static List<Element> multiSelect(PomWrapper pom, String... paths)
    {

        List<Element> ret = new ArrayList<Element>();
        for (String xpath : paths)
        {
            ret.addAll(pom.selectElements(xpath));
        }
        return ret;
    }


    /**
     *  Creates a <code>Map</code> from the children of the passed element, with
     *  the child's localName used as key.
     */
    public static Map<String,Element> getChildrenAsMap(Element elem)
    {
        Map<String,Element> children = new LinkedHashMap<String,Element>();
        for (Element child : DomUtil.getChildren(elem))
        {
            children.put(DomUtil.getLocalName(child), child);
        }
        return children;
    }


    /**
     *  Reconstructs the passed element, ordering its children according to their
     *  localNames as specified by the <code>childOrder</code> array. Any children
     *  whose names are not in the array are appended to the end of the element.
     *  Will remove any comments or other non-element children.
     */
    public static Element reconstruct(Element elem, Map<String,Element> children, String... childOrder)
    {
        DomUtil.removeAllChildren(elem);
        for (String name : childOrder)
        {
            Element child = children.remove(name);
            if (child != null)
                elem.appendChild(child);
        }

        // pick up anything left over
        for (Element child : children.values())
        {
            elem.appendChild(child);
        }

        return elem;
    }


    /**
     *  Given a JAR, finds all entries that represent classes and converts them to classnames.
     */
    public static List<String> extractClassesFromJar(File jarFile)
    throws IOException
    {
        List<String> result = new ArrayList<String>();
        JarFile jar = null;
        try
        {
            jar = new JarFile(jarFile);
            for (Enumeration<JarEntry> entryItx = jar.entries() ; entryItx.hasMoreElements() ; )
            {
                JarEntry entry = entryItx.nextElement();
                String filename = entry.getName();
                if (! filename.endsWith(".class"))
                    continue;
                filename = filename.substring(0, filename.length() - 6);
                filename = filename.replace('/', '.');
                filename = filename.replace('$', '.');
                result.add(filename);
            }
            return result;
        }
        finally
        {
            if (jar != null)
            {
                try
                {
                    jar.close();
                }
                catch (IOException ignored)
                {
                    // ignored
                }
            }
        }
    }
}