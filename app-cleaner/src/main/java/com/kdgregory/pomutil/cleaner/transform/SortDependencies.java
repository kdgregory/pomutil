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

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.Options;
import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Sorts <code>&lt;dependency&gt;</code> elements based on GAV, optionally
 *  including scope.
 */
public class SortDependencies
extends AbstractTransformer
{

//----------------------------------------------------------------------------
//  Instance variables and constructors
//----------------------------------------------------------------------------

    private boolean disabled;
    private boolean orderByGroup;


    /**
     *  Base constructor.
     */
    public SortDependencies(PomWrapper pom, InvocationArgs args)
    {
        super(pom,args);
        disabled = args.hasOption(Options.NO_DEPENDENCY_SORT);
        orderByGroup = args.hasOption(Options.GROUP_DEPCY_BY_SCOPE);
    }


    /**
     *  Convenience constructor, for no-options testing.
     */
    public SortDependencies(PomWrapper pom)
    {
        this(pom, new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        if (disabled)
            return;

        processGroup(SELECT_DIRECT_DEPENDENCIES);
        processGroup(SELECT_MANAGED_DEPENDENCIES);
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processGroup(String selectionPath)
    {
        TreeMap<Artifact,Element> dependencies
                = orderByGroup
                ? new TreeMap<Artifact,Element>(new Artifact.ScopedComparator())
                : new TreeMap<Artifact,Element>();

        for (Element dependency : pom.selectElements(selectionPath))
        {
            Artifact artifact = new Artifact(dependency);
            dependencies.put(artifact, dependency);
        }

        if (dependencies.size() == 0)
            return;

        Element container = pom.selectElement(selectionPath + "/..");
        DomUtil.removeAllChildren(container);
        for (Map.Entry<Artifact,Element> dependency : dependencies.entrySet())
        {
            container.appendChild(dependency.getValue());
        }
    }
}
