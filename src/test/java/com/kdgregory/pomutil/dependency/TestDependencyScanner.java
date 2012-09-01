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
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.Artifact.Scope;


public class TestDependencyScanner
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  Converts a collection of Artifacts into a form easier to assert.
     */
    public Map<String,Scope> mapArtifacts(Collection<Artifact> artifacts)
    {
        Map<String,Scope> result = new TreeMap<String,Artifact.Scope>();
        for (Artifact artifact : artifacts)
            result.put(artifact.artifactId, artifact.scope);
        return result;
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testGetDependency() throws Exception
    {
        File pomFile = new File(System.getProperty("user.dir"),"pom.xml");
        assertTrue("running in directory with POM", pomFile.exists());

        DependencyScanner scanner = new DependencyScanner(pomFile);

        assertEquals("compile-scope dependency",
                     "practicalxml",
                     scanner.getDependency("net.sf.practicalxml.ParseUtil", Scope.COMPILE).artifactId);

        assertEquals("test-scope dependency",
                     "junit",
                     scanner.getDependency("org.junit.Test", Scope.TEST).artifactId);

        assertNull("transitive dependency",
                   scanner.getDependency("org.apache.bcel.classfile.ClassParser", Scope.TEST));
    }


    public void testGetDependencies() throws Exception
    {
        File pomFile = new File(System.getProperty("user.dir"),"pom.xml");
        assertTrue("running in directory with POM", pomFile.exists());

        DependencyScanner scanner = new DependencyScanner(pomFile);

        Map<String,Scope> allDependencies = mapArtifacts(scanner.getDependencies());
        assertEquals("count of dependencies, general select",           8, allDependencies.size());
        assertEquals("compile-scope dependency, general select",        Scope.COMPILE, allDependencies.get("practicalxml"));
        assertEquals("test-scope dependency, general select",           Scope.TEST,    allDependencies.get("junit"));
        assertNull("transitive dependency, general select",             allDependencies.get("bcel"));

        Map<String,Scope> allDependencies2 = mapArtifacts(scanner.getDependencies(Scope.COMPILE, Scope.TEST));
        assertEquals("count of dependencies, scope select",             8, allDependencies2.size());
        assertEquals("compile-scope dependency, scope select",          Scope.COMPILE, allDependencies2.get("practicalxml"));
        assertEquals("test-scope dependency, scope select",             Scope.TEST,    allDependencies2.get("junit"));
        assertNull("transitive dependency, scope select",               allDependencies2.get("bcel"));

        Map<String,Scope> compileDependencies = mapArtifacts(scanner.getDependencies(Scope.COMPILE));

        assertEquals("compile-scope select returned expected count",    6, compileDependencies.size());
        assertTrue("compile-scope select returned expected artifact",   compileDependencies.containsKey("practicalxml"));
        assertFalse("compile-scope select returned test artifact",      compileDependencies.containsKey("junit"));

        Map<String,Scope> testDependencies = mapArtifacts(scanner.getDependencies(Scope.TEST));

        assertEquals("compile-scope select returned expected count",    2, testDependencies.size());
        assertTrue("compile-scope select returned expected artifact",   testDependencies.containsKey("junit"));
        assertFalse("compile-scope select returned compile artifact",   testDependencies.containsKey("practicalxml"));
    }
}
