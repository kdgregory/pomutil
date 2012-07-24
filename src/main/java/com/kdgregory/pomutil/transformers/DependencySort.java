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

package com.kdgregory.pomutil.transformers;

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.kdgregory.pomutil.util.InvocationArgs;


/**
 *  Sorts <code>&lt;dependency&gt;</code> elements based on GAV.
 */
public class DependencySort
extends AbstractTransformer
{
    /**
     *  Base constructor.
     */
    public DependencySort(InvocationArgs args)
    {
        // nothing here yet
    }


    /**
     *  Convenience constructor, for testing without options.
     */
    public DependencySort()
    {
        this(new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public Document transform(Document dom)
    {
        processGroup(dom, "/mvn:project/mvn:dependencies");
        processGroup(dom, "/mvn:project/mvn:dependencyManagement/mvn:dependencies");
        return dom;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processGroup(Document dom, String parentPath)
    {
        TreeMap<String,Element> dependencies = new TreeMap<String,Element>();

        // note: any exact dupes will disappear in this step
        String selectionPath = parentPath + "/mvn:dependency";
        for (Element dependency : newXPath(selectionPath).evaluate(dom, Element.class))
        {
            String groupId = newXPath("mvn:groupId").evaluateAsString(dependency);
            String artifactId = newXPath("mvn:artifactId").evaluateAsString(dependency);
            String version = newXPath("mvn:version").evaluateAsString(dependency);
            dependencies.put(groupId + ":" + artifactId + ":" + version, dependency);
        }

        if (dependencies.size() == 0)
            return;

        Element container = newXPath(parentPath).evaluateAsElement(dom);
        removeAllChildren(container);
        for (Map.Entry<String,Element> dependency : dependencies.entrySet())
        {
            container.appendChild(dependency.getValue());
        }
    }
}
