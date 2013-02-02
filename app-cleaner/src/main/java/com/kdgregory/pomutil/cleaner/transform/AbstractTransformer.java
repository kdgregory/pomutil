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
//----------------------------------------------------------------------------
//  Common XPath selectors
//----------------------------------------------------------------------------

    protected final static String  SELECT_DIRECT_DEPENDENCIES   = "/mvn:project/mvn:dependencies/mvn:dependency";
    protected final static String  SELECT_MANAGED_DEPENDENCIES  = "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency";
    protected final static String  SELECT_BUILD_PLUGINS         = "/mvn:project/mvn:build/mvn:plugins/mvn:plugin";
    protected final static String  SELECT_REPORTING_PLUGINS     = "/mvn:project/mvn:reporting/mvn:plugins/mvn:plugin";
    protected final static String  SELECT_MANAGED_PLUGINS       = "/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin";

//----------------------------------------------------------------------------
//  Instance variables and Constructor
//----------------------------------------------------------------------------

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
        return Utils.multiSelect(pom, SELECT_DIRECT_DEPENDENCIES, SELECT_MANAGED_DEPENDENCIES);
    }


    /**
     *  Selects all plugins: biild, reporting, and and managed.
     */
    protected List<Element> selectAllPlugins()
    {
        return Utils.multiSelect(pom, SELECT_BUILD_PLUGINS, SELECT_REPORTING_PLUGINS, SELECT_MANAGED_PLUGINS);
    }


//----------------------------------------------------------------------------
//  Methods for subclases to implement
//----------------------------------------------------------------------------

    public abstract void transform();
}
