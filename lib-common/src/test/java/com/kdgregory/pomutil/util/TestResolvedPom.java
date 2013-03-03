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
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestResolvedPom
{
    @Test
    public void testGetPom() throws Exception
    {
        File pomFile = new File("../test-dependency-child/pom.xml");
        assertTrue("able to access POM", pomFile.exists());

        ResolvedPom pom = new ResolvedPom(pomFile);

        PomWrapper child = pom.getPom(0);
        assertEquals("com.kdgregory.pomutil:test-dependency-child:0.0.0-SNAPSHOT", child.toString());

        PomWrapper parent = pom.getPom(1);
        assertEquals("com.kdgregory.pomutil:test-dependency-parent:0.0.0-SNAPSHOT", parent.toString());
    }


    @Test
    public void testResolveProperties() throws Exception
    {
        ResolvedPom pom = new ResolvedPom(new File("../test-dependency-child/pom.xml"));

        assertEquals("property defined in child",   "foo",         pom.resolveProperties("${test.prop.child}"));
        assertEquals("property defined in parent",  "bar",         pom.resolveProperties("${test.prop.parent}"));
        assertEquals("property that involves both", "foo-bar-baz", pom.resolveProperties("${test.prop.combined}"));
    }


    @Test
    public void testResolveDependencies() throws Exception
    {
        ResolvedPom pom = new ResolvedPom(new File("../test-dependency-child/pom.xml"));
        Set<Artifact> dependencies = pom.getDependencies();

        // we'll spot-check the actual dependencies
        assertEquals("number of dependencies", 7, dependencies.size());

        // this one is defined by child
        assertTrue("com.kdgregory.bcelx:bcelx:1.0.0",
                   dependencies.contains(new Artifact("com.kdgregory.bcelx", "bcelx", "1.0.0")));

        // this one is defined by parent
        assertTrue("junit:junit:4.10",
                   dependencies.contains(new Artifact("junit", "junit", "4.10")));

        // this one has its version set by dependencyManagement
        assertTrue("commons-lang:commons-lang:2.3",
                   dependencies.contains(new Artifact("commons-lang", "commons-lang", "2.3")));
    }


    @Test
    public void testImportedPOM() throws Exception
    {
        ResolvedPom pom = new ResolvedPom(new File("src/test/resources/ImportingPom.xml"));
        Set<Artifact> dependencies = pom.getDependencies();

        // should not find the imported POM in the list of dependencies

        assertFalse("contains imported POM",
                   dependencies.contains(new Artifact("com.kdgregory.pomutil", "test-dependency-child", "0.0.0-SNAPSHOT")));

        // should find the dependencies that POM contains (and that resolution handles parent-child relationships)
        assertTrue("contains dependency defined by child (imported POM)",
                   dependencies.contains(new Artifact("com.kdgregory.bcelx", "bcelx", "1.0.0")));
        assertTrue("contains dependency defined by parent",
                   dependencies.contains(new Artifact("junit", "junit", "4.10")));

    }


    @Test
    public void testImportedPOMSansImportScope() throws Exception
    {
        ResolvedPom pom = new ResolvedPom(new File("src/test/resources/ImportingPomSansImportScope.xml"));
        Set<Artifact> dependencies = pom.getDependencies();

        // should not find the imported POM in the list of dependencies

        assertFalse("contains imported POM",
                   dependencies.contains(new Artifact("com.kdgregory.pomutil", "test-dependency-child", "0.0.0-SNAPSHOT")));

        // should find the dependencies that POM contains (and that resolution handles parent-child relationships)
        assertTrue("contains dependency defined by child (imported POM)",
                   dependencies.contains(new Artifact("com.kdgregory.bcelx", "bcelx", "1.0.0")));
        assertTrue("contains dependency defined by parent",
                   dependencies.contains(new Artifact("junit", "junit", "4.10")));
    }
}
