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

import org.w3c.dom.Element;

import com.kdgregory.pomutil.util.GAV;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Sorts <code>&lt;dependency&gt;</code> elements based on GAV.
 */
public class DependencySort
extends AbstractTransformer
{
    /**
     *  Base constructor.
     */
    public DependencySort(PomWrapper pom, InvocationArgs args)
    {
        super(pom,args);
    }


    /**
     *  Convenience constructor, for no-options testing.
     */
    public DependencySort(PomWrapper pom)
    {
        this(pom, new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        processGroup("/mvn:project/mvn:dependencies");
        processGroup("/mvn:project/mvn:dependencyManagement/mvn:dependencies");
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processGroup(String parentPath)
    {
        TreeMap<GAV,Element> dependencies = new TreeMap<GAV,Element>();

        // note: any exact dupes will disappear in this step
        String selectionPath = parentPath + "/mvn:dependency";
        for (Element dependency : pom.selectElements(selectionPath))
        {
            GAV gav = pom.extractGAV(dependency);
            dependencies.put(gav, dependency);
        }

        if (dependencies.size() == 0)
            return;

        Element container = pom.selectElement(parentPath);
        pom.clear(container);
        for (Map.Entry<GAV,Element> dependency : dependencies.entrySet())
        {
            container.appendChild(dependency.getValue());
        }
    }
}
