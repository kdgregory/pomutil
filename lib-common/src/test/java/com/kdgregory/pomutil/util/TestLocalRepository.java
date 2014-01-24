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

import org.junit.Test;
import static org.junit.Assert.*;

public class TestLocalRepository
{
    @Test
    public void testPathToJar() throws Exception
    {
        String path = LocalRepository.relativePath(new Artifact("junit", "junit", "4.10"));
        assertEquals("junit/junit/4.10/junit-4.10.jar", path);
    }


    @Test
    public void testPathToPom() throws Exception
    {
        String path = LocalRepository.relativePath(new Artifact("junit", "junit", "4.10", "pom"));
        assertEquals("junit/junit/4.10/junit-4.10.pom", path);
    }


    @Test
    public void testResolveJar() throws Exception
    {
        LocalRepository repo = new LocalRepository();

        // update artifact version if/when POM changes
        File jar = repo.resolve(new Artifact("junit", "junit", "4.10"));
        assertNotNull("file found in repository", jar);
    }

    @Test
    public void testUnresolvableArtifact() throws Exception
    {
        LocalRepository repo = new LocalRepository();

        File jar = repo.resolve(new Artifact("com.example", "example", "1.2.3"));
        assertNull("artifact should not be in repository", jar);
    }


}
