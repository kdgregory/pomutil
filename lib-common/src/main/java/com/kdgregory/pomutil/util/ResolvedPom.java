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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.ParseUtil;


/**
 *  Holds information abouta project and its dependencies, including the parent
 *  chain. This class may only be used after a project has been built, as it
 *  examines the local repository.
 */
public class ResolvedPom
{
    private File repo;
    private ArrayList<PomWrapper> poms = new ArrayList<PomWrapper>();
    private Set<Artifact> dependencies = new TreeSet<Artifact>();


    /**
     *  Creates an instance that resolves dependencies in the user's local repository.
     *
     *  @param  pom     The project's POM. This is presumed to reside at the root of
     *                  the project directory.
     */
    public ResolvedPom(File pom)
    throws IOException
    {
        this(pom, userRepo());
    }


    /**
     *  Creates an instance that resolves dependencies in the user's local repository.
     *
     *  @param  pom     The project's POM. This is presumed to reside at the root of
     *                  the project directory.
     *  @param  repo    The location of the Maven repository.
     */
    public ResolvedPom(File pom, File repo)
    throws IOException
    {
        if (!repo.isDirectory())
            throw new FileNotFoundException("invalid repository: " + repo);

        this.repo = repo;
        buildPomHierarchy(pom);
        resolveDependencies();
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns the project information for the base POM.
     */
    public Artifact getGAV()
    {
        return getPom(0).getGAV();
    }


    /**
     *  Returns the Nth POM in the parent hierarchy. Index 0 is the project POM,
     *  1 is the project's parent, and so on. This method exists primarily for
     *  testing; there is no method to find out how many POMs exist (although
     *  you could call <code>PomWrapper.getParent()</code>).
     */
    public PomWrapper getPom(int index)
    {
        return poms.get(index);
    }


    /**
     *  Returns all dependencies, including those defined by the parent POM.
     *  Where the same dependency is specified in multiple levels, the lowest
     *  level wins. At present, dependency management entries are not used.
     */
    public Set<Artifact> getDependencies()
    {
        return dependencies;
    }


    /**
     *  Resolves properties against all POMs in the chain.
     */
    public String resolveProperties(String value)
    {
        for (PomWrapper pom : poms)
        {
            value = pom.resolveProperties(value);
        }
        return value;
    }


//----------------------------------------------------------------------------
//  Internals (primarily called by constructor)
//----------------------------------------------------------------------------

    /**
     *  A helper class for extracting dependencies, used for lookups by group
     *  and artifact IDs.
     */
    private static class GAKey
    {
        private String groupId;
        private String artifactId;

        public GAKey(String groupId, String artifactId)
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public GAKey(Artifact artifact)
        {
            this(artifact.groupId, artifact.artifactId);
        }

        @Override
        public final boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            else if (obj instanceof ResolvedPom.GAKey)
            {
                ResolvedPom.GAKey that = (ResolvedPom.GAKey)obj;
                return this.groupId.equals(that.groupId)
                    && this.artifactId.equals(that.artifactId);
            }
            return false;
        }

        @Override
        public final int hashCode()
        {
            return artifactId.hashCode();
        }
    }


    private static File userRepo()
    {
        String homeDir = System.getProperty("user.home");
        return new File(new File(homeDir, ".m2"), "repository");
    }


    private void buildPomHierarchy(File pom)
    {
        while (pom != null)
        {
            PomWrapper wrapper = new PomWrapper(ParseUtil.parse(pom));
            poms.add(wrapper);
            Artifact parentRef = wrapper.getParent();
            pom = (parentRef != null) ? Utils.getLocalRepositoryFile(parentRef, repo) : null;
        }
    }


    private void resolveDependencies()
    throws IOException
    {
        Map<GAKey,Artifact> accumulator = new HashMap<GAKey,Artifact>();
        for (PomWrapper wrapper : poms)
        {
            extractDependencies(wrapper, accumulator);
        }
        dependencies.addAll(accumulator.values());
    }


    private void extractDependencies(PomWrapper wrapper, Map<GAKey,Artifact> accumulator)
    throws IOException
    {
        for (Element dependency : wrapper.selectElements("/mvn:project/mvn:dependencies/mvn:dependency"))
        {
            String groupId = wrapper.selectValue(dependency, "mvn:groupId").trim();
            String artifactId = wrapper.selectValue(dependency, "mvn:artifactId").trim();
            String version = wrapper.selectValue(dependency, "mvn:version").trim();
            String type = wrapper.selectValue(dependency, "mvn:type").trim();
            String scope = wrapper.selectValue(dependency, "mvn:scope").trim();

            GAKey key = new GAKey(groupId, artifactId);
            if (accumulator.containsKey(key))
                continue;

            if (type.equalsIgnoreCase("pom"))
            {
                // FIXME - warn if scope not specified
                resolveImportedPom(groupId, artifactId, version, accumulator);
                continue;
            }

            if (!StringUtil.isBlank(type) && !type.equalsIgnoreCase("jar"))
                continue;

            if (StringUtil.isBlank(version))
                version = getVersionFromDependencyManagement(groupId, artifactId);

            if (version.contains("${"))
                version = resolveProperties(version);

            if (StringUtil.isEmpty(version))
                continue;

            accumulator.put(key, new Artifact(groupId, artifactId, version, "", type, scope));
        }
    }


    private String getVersionFromDependencyManagement(String groupId, String artifactId)
    {
        // FIXME - deal with version ranges
        //          (maybe; I'm not sure if a valid POM can only use a range)

        String path = "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency"
                    + "/mvn:groupId[normalize-space(text())='" + groupId + "']"
                    + "/../mvn:artifactId[normalize-space(text())='" + artifactId + "']"
                    + "/../mvn:version";

        for (PomWrapper wrapper : poms)
        {
            String value = wrapper.selectValue(path);
            if (!StringUtil.isEmpty(value))
                return value;
        }

        return "";
    }


    private void resolveImportedPom(String groupId, String artifactId, String version,  Map<GAKey,Artifact> accumulator)
    throws IOException
    {
        File importedPom = Utils.getLocalRepositoryFile(new Artifact(groupId, artifactId, version, "", "pom", ""), repo);
        ResolvedPom resolved = new ResolvedPom(importedPom, repo);
        for (Artifact artifact : resolved.getDependencies())
        {
            accumulator.put(new GAKey(artifact), artifact);
        }
    }
}
