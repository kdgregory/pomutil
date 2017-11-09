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
import java.util.Arrays;
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
     *  Builds a list of files from the provided list of files and/or directories.
     *  The provided files are added to this list without change; directories are
     *  recursively examined, and any file named "pom.xml" is added to the list.
     */
    public static List<File> buildFileListFromStringList(List<String> sources)
    {
        List<File> sourceFiles = new ArrayList<File>(sources.size());
        for (String source : sources)
        {
            sourceFiles.add(new File(source));
        }
        return buildFileList(sourceFiles);
    }


    /**
     *  Builds a list of files from the provided list of files and/or directories.
     *  The provided files are added to this list without change; directories are
     *  recursively examined, and any file named "pom.xml" is added to the list.
     */
    public static List<File> buildFileList(List<File> sources)
    {
        List<File> result = new ArrayList<File>();
        for (File source : sources)
        {
            if (source.isDirectory())
            {
                for (File child : source.listFiles())
                {
                    if (child.isDirectory())
                    {
                        result.addAll(buildFileList(Arrays.asList(child)));
                    }
                    else if (child.getName().equals("pom.xml"))
                    {
                        result.add(child);
                    }
                }
            }
            else
            {
                result.add(source);
            }
        }
        return result;
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


    /**
     *  Removes all children from the passed element, storing them in an
     *  order-preserving map keyed by the child's localName.
     *
     *  @throws IllegalStateException if there's more than one child with the
     *          same localName (this should be considered an unrecoverable
     *          error, as the element will already have been mutated).
     */
    public static Map<String,Element> removeChildrenToMap(Element elem)
    {
        Map<String,Element> children = new LinkedHashMap<String,Element>();
        for (Element child : DomUtil.getChildren(elem))
        {
            String localName = DomUtil.getLocalName(child);
            Element prev = children.put(localName, child);
            if (prev != null)
                throw new IllegalStateException("duplicate child name: " + localName);
            elem.removeChild(child);
        }
        return children;
    }


    /**
     *  Reconstructs the passed element, ordering its children according to their
     *  localNames as specified by the <code>childOrder</code> array. Any children
     *  whose names are not in the array are appended to the end of the element.
     *  <p>
     *  WARNING: only one child must exist with a given name!
     */
    public static Element reconstruct(Element elem, String... childOrder)
    {
        return reconstruct(elem, removeChildrenToMap(elem), childOrder);
    }


    /**
     *  Updates the specified element with children from the map, in the specified
     *  order. Any elements in the map that do not correspond to specified order
     *  will be appended at the end of the element.
     */
    public static Element reconstruct(Element elem, Map<String,Element> children, String... childOrder)
    {
        for (String name : childOrder)
        {
            Element child = children.remove(name);
            if (child != null)
                elem.appendChild(child);
        }

        // rebuild anything left over
        for (Element child : children.values())
        {
            elem.appendChild(child);
        }

        return elem;
    }
}