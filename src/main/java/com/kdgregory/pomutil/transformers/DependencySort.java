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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import net.sf.kdgcommons.collections.MapBuilder;

import com.kdgregory.pomutil.Options;
import com.kdgregory.pomutil.util.GAV;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Sorts <code>&lt;dependency&gt;</code> elements based on GAV, optionally
 *  including scope.
 */
public class DependencySort
extends AbstractTransformer
{
    private static Map<String,Integer> SCOPE_GROUPS
        = new MapBuilder<String,Integer>(new HashMap<String,Integer>())
          .put("compile",  Integer.valueOf(1))
          .put("test",     Integer.valueOf(2))
          .put("runtime",  Integer.valueOf(3))
          .put("provided", Integer.valueOf(4))
          .put("system",   Integer.valueOf(5))
          .toMap();

    private boolean orderByGroup;

    /**
     *  Base constructor.
     */
    public DependencySort(PomWrapper pom, InvocationArgs args)
    {
        super(pom,args);
        orderByGroup = args.hasOption(Options.GROUP_DEPCY_BY_SCOPE);
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
            if (orderByGroup)
            {
                String scope = pom.selectValue(dependency, "mvn:scope");
                gav = new GAVWithScope(gav.groupId, gav.artifactId, gav.version, scope);
            }
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


    private static class GAVWithScope
    extends GAV
    {
        public String scope;

        public GAVWithScope(String groupId, String artifactId, String version, String scope)
        {
            super(groupId, artifactId, version);
            this.scope = StringUtils.defaultIfBlank(scope, "compile");
        }

        @Override
        public int compareTo(GAV obj)
        {
            GAVWithScope that = (GAVWithScope)obj;
            Integer thisGroup = ObjectUtils.defaultIfNull(SCOPE_GROUPS.get(this.scope), Integer.valueOf(99));
            Integer thatGroup = ObjectUtils.defaultIfNull(SCOPE_GROUPS.get(that.scope), Integer.valueOf(99));

            int cmp = thisGroup.compareTo(thatGroup);
            if (cmp == 0)
                cmp = super.compareTo(that);

            return cmp;
        }
    }
}
