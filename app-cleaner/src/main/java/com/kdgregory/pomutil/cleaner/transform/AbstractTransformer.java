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

import java.util.List;

import org.w3c.dom.Element;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.util.PomPaths;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Base class for transformers; exists to (1) provide a place to stash invocation
 *  arguments and helper methods, and (2) enforce the <code>Transformer</code>
 *  interface.
 *  <p>
 *  Transformers perform some operation on a single POM: it is constructed around
 *  the POM, and some time later its {@link #transform} method is called.
 */
public abstract class AbstractTransformer
{
    protected PomWrapper pom;
    protected CommandLine args;

    public AbstractTransformer(PomWrapper pom, CommandLine args)
    {
        this.pom = pom;
        this.args = args;
    }


//----------------------------------------------------------------------------
//  Methods for subclases to use
//----------------------------------------------------------------------------

    /**
     *  Selects all dependencies, both direct and managed.
     */
    protected List<Element> selectAllDependencies()
    {
        return Utils.multiSelect(pom, PomPaths.PROJECT_DEPENDENCIES, PomPaths.MANAGED_DEPENDENCIES);
    }


    /**
     *  Selects all plugins: biild, reporting, and and managed.
     */
    protected List<Element> selectAllPlugins()
    {
        return Utils.multiSelect(pom, PomPaths.BUILD_PLUGINS, PomPaths.REPORTING_PLUGINS, PomPaths.MANAGED_PLUGINS);
    }


//----------------------------------------------------------------------------
//  Methods for subclases to implement
//----------------------------------------------------------------------------

    public abstract void transform();
}
