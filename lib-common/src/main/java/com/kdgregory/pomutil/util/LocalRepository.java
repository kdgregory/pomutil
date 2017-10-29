// Copyright (c) Keith D Gregory
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


/**
 *  Provides access to artifacts in a local Maven repository.
 */
public class LocalRepository
{
    private File repository;

    /**
     *  References the user's default repository.
     *  <p>
     *  At present this just looks in the default location, <code>$HOME/.m2/repository</code>.
     *  In the future it will also examine the user's <code>settings.xml</code>, as well as
     *  any other location specifications.
     *
     *  @throws IllegalArgumentException if the default repository doesn't exist.
     */
    public LocalRepository()
    throws IOException
    {
        this(defaultRepository());
    }


    /**
     *  Creates an instance that references a non-default repository.
     *
     *  @throws IllegalArgumentException if the specified repository doesn't exist.
     */
    public LocalRepository(File repo)
    throws IOException
    {
        if (!repo.exists() || !repo.isDirectory())
            throw new IllegalArgumentException("invalid local repository: " + repo);

        repository = repo;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Converts the passed artifact into a path relative to repository root.
     *  This is used internally to reference files in the local repository, and
     *  may be used to construct URLs against a remote repository.
     */
    public static String relativePath(Artifact artifact)
    {
        return artifact.groupId.replace('.', '/')
             + "/" + artifact.artifactId
             + "/" + artifact.version
             + "/" + artifact.artifactId + "-" + artifact.version + "." + artifact.packaging;
    }

    /**
     *  Resolves the specified artifact, returning <code>null</code> if it is
     *  not found in the repository.
     */
    public File resolve(Artifact artifact)
    throws IOException
    {
        File file = new File(repository, relativePath(artifact));
        return file.exists() ? file : null;
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Returns user's home directory as a <code>File</code>. Required because
     *  <code>this()</code> must be the first thing in a constructor.
     */
    private static File defaultRepository()
    {
        File userHome = new File(System.getProperty("user.home"));
        File m2 = new File(userHome, ".m2");
        File repo = new File(m2, "repository");
        return repo;
    }
}
