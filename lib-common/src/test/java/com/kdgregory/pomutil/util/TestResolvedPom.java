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
import java.util.Collection;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.ParseUtil;


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
        Collection<Artifact> dependencies = pom.getDirectDependencies().values();

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
    public void testImportedPom() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(ParseUtil.parseFromClasspath("Importer.xml"));
        ResolvedPom pom = new ResolvedPom(wrapper, new LocalRepository());

        // direct dependencies should not contain the imported PO
        Collection<Artifact> directDependencies = pom.getDirectDependencies().values();
        assertEquals("number of direct dependencies", 1, directDependencies.size());
        assertTrue("direct dependency contains practicalxml",
                   directDependencies.contains(new Artifact("net.sf.practicalxml", "practicalxml", "1.1.13")));
        assertFalse("direct dependency does not contain imported POM",
                   directDependencies.contains(new Artifact("com.kdgregory.pomutil", "test-dependency-imported", "0.0.0-SNAPSHOT")));

        assertEquals("number of imported POMs", 1, pom.getImportedPoms().size());
        ResolvedPom imported = pom.getImportedPoms().iterator().next();
        assertEquals("imported POM GAV",
                     new Artifact("com.kdgregory.pomutil", "test-dependency-imported", "0.0.0-SNAPSHOT"),
                     imported.getGAV());
    }


    @Test
    public void testOptionalDependency() throws Exception
    {
        PomWrapper wrapper = new PomWrapper(ParseUtil.parseFromClasspath("OptionalDependency.xml"));
        ResolvedPom pom = new ResolvedPom(wrapper, new LocalRepository());

        Artifact a1 = pom.getDirectDependencies().get(new GAKey("net.sf.practicalxml", "practicalxml"));
        assertFalse("practicalxml: default non-optional", a1.optional);

        Artifact a2 = pom.getDirectDependencies().get(new GAKey("net.sf.kdgcommons", "kdgcommons"));
        assertTrue("kdgcommons: explicit optional", a2.optional);

        Artifact a3 = pom.getDirectDependencies().get(new GAKey("junit", "junit"));
        assertFalse("junit: explicit non-optional", a3.optional);
    }
}
