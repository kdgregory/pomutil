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

import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Common superclass for transformers that work ith dependencies, providing constants
 *  and utility methods.
 */
public abstract class AbstractDependencyTransformer
extends AbstractTransformer
{
    /**
     *  Selects members of the top-level <code>&lt;dependencies&gt;</code> section.
     */
    public final static String  DIRECT_DEPENDENCIES = "/mvn:project/mvn:dependencies/mvn:dependency";


    /**
     *  Selects dependencies from the <code>&lt;dependencyManagement&gt;</code> section.
     */
    public final static String  MANAGED_DEPENDENCIES = "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency";


//----------------------------------------------------------------------------
//  Instance variables and constructors
//----------------------------------------------------------------------------

    /**
     *  Base constructor.
     */
    public AbstractDependencyTransformer(PomWrapper pom, InvocationArgs args)
    {
        super(pom,args);
    }


//----------------------------------------------------------------------------
//  Utility Methods
//----------------------------------------------------------------------------

    /**
     *  Selects all dependencies, both direct and managed.
     */
    protected List<Element> selectAllDependencies()
    {
        return Utils.multiSelect(pom, DIRECT_DEPENDENCIES, MANAGED_DEPENDENCIES);
    }
}
