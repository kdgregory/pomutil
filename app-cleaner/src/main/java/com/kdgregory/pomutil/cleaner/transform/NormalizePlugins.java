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

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Normalizes <code>&lt;plugin&gt;</code> entries, ordering the child elements
 *  and adding a missing <code>&lt;groupId&gt;</code>. element.
 */
public class NormalizePlugins
extends AbstractTransformer
{
    private static final String[] STANDARD_CHILDREN = new String[] {
            "groupId", "artifactId", "version",
            "extensions", "inherited", "configuration",
            "dependencies", "executions"
            };


//----------------------------------------------------------------------------
//  Instance variables and Constuctors
//----------------------------------------------------------------------------

    private boolean disabled;


    /**
     *  Base constructor.
     */
    public NormalizePlugins(PomWrapper pom, CommandLine args)
    {
        super(pom, args);
        disabled = ! args.isOptionEnabled(CommandLine.Options.PLUGIN_NORMALIZE);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public NormalizePlugins(PomWrapper pom)
    {
        this(pom, new CommandLine());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        if (disabled)
            return;

        for (Element plugin : selectAllPlugins())
        {
            if (DomUtil.getChild(plugin, "groupId") == null)
            {
                Element groupId = DomUtil.appendChildInheritNamespace(plugin, "groupId");
                DomUtil.setText(groupId, "org.apache.maven.plugins");
            }
            Utils.reconstruct(plugin, STANDARD_CHILDREN);
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------
}
