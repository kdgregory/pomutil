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

package com.kdgregory.pomutil.dependency;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.Artifact.Scope;
import com.kdgregory.pomutil.util.ResolvedPom;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Examines all direct dependencies referenced by the POM, to
 *  extract their contained classes.
 */
public class DependencyScanner
{

//----------------------------------------------------------------------------
//  Instance Variables and Constructors
//----------------------------------------------------------------------------

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ResolvedPom pom;

    private Set<Artifact> dependencies = new TreeSet<Artifact>();
    private Map<String,Artifact> dependencyLookup = new HashMap<String,Artifact>();


    public DependencyScanner(File pomFile)
    throws IOException
    {
        pom = new ResolvedPom(pomFile);
        dependencies = pom.getDependencies();
        buildDependencyLookup();
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns all dependencies.
     */
    public Collection<Artifact> getDependencies()
    {
        return dependencies;
    }


    /**
     *  Returns all dependencies in the specified scope(s).
     */
    public Collection<Artifact> getDependencies(Scope... scopes)
    {
        Set<Artifact> result = new TreeSet<Artifact>();
        for (Artifact artifact : dependencies)
        {
            for (Scope scope : scopes)
            {
                if (artifact.scope == scope)
                {
                    result.add(artifact);
                    break;
                }
            }
        }
        return result;
    }


    /**
     *  Returns the artifact ID of the dependency that provides the specified
     *  class and scope. Returns <code>null</code> if the class is not provided
     *  by a dependency of this POM.
     */
    public Artifact getDependency(String className, Scope scope)
    {
        Artifact dependency = dependencyLookup.get(className);
        if ((dependency == null) || (dependency.scope != scope))
            return null;
        return dependency;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void buildDependencyLookup()
    throws IOException
    {
        for (Artifact dependency : dependencies)
        {
            File jarFile = Utils.getLocalRepositoryFile(dependency);
            if (jarFile == null)
                throw new IOException("dependency not in repository: " + dependency);
            logger.debug("processing {} from {}", dependency, jarFile);
            for (String className : Utils.extractClassesFromJar(jarFile))
            {
                dependencyLookup.put(className, dependency);
            }
        }
    }
}
