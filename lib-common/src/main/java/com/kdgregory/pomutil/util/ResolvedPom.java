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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.ParseUtil;


/**
 *  Holds information about a project and its dependencies, including the parent
 *  chain. This class may only be used after a project has been built, as it
 *  examines the local repository.
 */
public class ResolvedPom
{
    private static Logger logger = LoggerFactory.getLogger(ResolvedPom.class);

    private LocalRepository repo;
    private ArrayList<PomWrapper> poms = new ArrayList<PomWrapper>();
    private Map<GAKey,Artifact> directDependencies = new TreeMap<GAKey,Artifact>();
    private List<ResolvedPom> importedPoms = new ArrayList<ResolvedPom>();


    /**
     *  Creates an instance that resolves dependencies in the user's default repository.
     *
     *  @param  pom     The project's POM. This is presumed to reside at the root of
     *                  the project directory.
     */
    public ResolvedPom(File pom)
    throws IOException
    {
        this(pom, new LocalRepository());
    }


    /**
     *  Creates an instance that resolves dependencies in the specified repository.
     *
     *  @param  pom     The project's POM. This is presumed to reside at the root of
     *                  the project directory.
     *  @param  repo    The location of the Maven repository.
     */
    public ResolvedPom(File pom, LocalRepository repo)
    throws IOException
    {
        this(new PomWrapper(ParseUtil.parse(pom)), repo);
    }


    /**
     *  Internal/testing constructor: takes pre-parsed POM.
     */
    public ResolvedPom(PomWrapper pom, LocalRepository repo)
    throws IOException
    {
        this.repo = repo;
        buildPomHierarchy(pom);
        for (PomWrapper wrapper : poms)
        {
            extractDependencies(wrapper);
        }
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
     *  Returns all direct dependencies, including those defined by ancestor POMs.
     *  Where the same dependency is specified in multiple levels, the lowest level
     *  (closest to the project POM) wins.
     */
    public Map<GAKey,Artifact> getDirectDependencies()
    {
        return directDependencies;
    }


    /**
     *  Returns all imported POMs.
     */
    public Collection<ResolvedPom> getImportedPoms()
    {
        return importedPoms;
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

    private void buildPomHierarchy(PomWrapper pom)
    throws IOException
    {
        while (pom != null)
        {
            logger.debug("adding {} to POM hierarchy", pom);
            poms.add(pom);
            Artifact parentRef = pom.getParent();
            pom = (parentRef == null) ? null : resolvePom(parentRef);
        }
    }


    /**
     *  Attempts to load a POM from the repository, returning null (and warning)
     *  if it doesn't exist.
     */
    private PomWrapper resolvePom(Artifact pomRef)
    throws IOException
    {
        File pomFile = repo.resolve(pomRef);
        if (pomFile == null)
        {
            logger.warn("unresolvable POM: {}", pomRef);
            return null;
        }
        return new PomWrapper(ParseUtil.parse(pomFile));
    }


    private void extractDependencies(PomWrapper wrapper)
    throws IOException
    {
        for (Element dependency : wrapper.selectElements(PomPaths.PROJECT_DEPENDENCIES))
        {
            Artifact artifact = new Artifact(dependency);
            GAKey key = artifact.toGAKey();
            if (directDependencies.containsKey(key))
                continue;

            String groupId = artifact.groupId;
            String artifactId = artifact.artifactId;
            String version = artifact.version;

            if (StringUtil.isBlank(version))
                version = getVersionFromDependencyManagement(groupId, artifactId);

            if (version.contains("${"))
                version = resolveProperties(version);

            if (StringUtil.isBlank(version))
            {
                logger.warn("unable to resolve dependency version: {}", artifact);
                continue;
            }

            if (! version.equals(artifact.version))
                artifact = artifact.withVersion(version);

            if (artifact.packaging.equals("pom"))
            {
                // FIXME - warn if scope not specified
                resolveImportedPom(groupId, artifactId, version);
            }

            if (! artifact.packaging.equals("jar"))
                continue;

            directDependencies.put(key, artifact);
        }
    }


    private String getVersionFromDependencyManagement(String groupId, String artifactId)
    {
        // FIXME - deal with version ranges
        //          (maybe; I'm not sure if a valid POM can only use a range)

        String path = PomPaths.MANAGED_DEPENDENCIES
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


    private void resolveImportedPom(String groupId, String artifactId, String version)
    throws IOException
    {
        File importedPom = repo.resolve(new Artifact(groupId, artifactId, version, "pom"));
        ResolvedPom resolved = new ResolvedPom(importedPom, repo);
        importedPoms.add(resolved);
    }
}
