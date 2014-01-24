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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pomutil.util.Artifact;


public class TestDependencyCheck
{

//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    private Set<String> extractArtifactIds(Collection<Artifact> artifacts)
    {
        Set<String> result = new TreeSet<String>();
        for (Artifact artifact : artifacts)
            result.add(artifact.getArtifactId());
        return result;
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testBasicOperation() throws Exception
    {
        CommandLine args = new CommandLine("../test-dependency");
        DependencyCheck checker = new DependencyCheck(args);
        checker.run();

        assertTrue("unsupported mainline class",                checker.getUnsupportedMainlineClasses().contains("org.apache.bcel.classfile.ClassParser"));
        assertTrue("unsupported mainline package",              checker.getUnsupportedMainlinePackages().contains("org.apache.bcel.classfile"));

        assertTrue("expected missing test class",               checker.getUnsupportedTestClasses().contains("org.apache.bcel.classfile.ConstantPool"));
        assertTrue("expected missing test package",             checker.getUnsupportedTestPackages().contains("org.apache.bcel.classfile"));

        assertFalse("doesn't contain in-project reference",     checker.getUnsupportedMainlineClasses().contains("com.kdgregory.pomutil.testdata.AnInterface"));

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        2, unusedMainlineDependencies.size());
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("bcelx"));
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("commons-io"));

        Set<String> incorrectMainlineDependencies = extractArtifactIds(checker.getIncorrectMainlineDependencies());
        assertEquals("incorrect mainline dependency count",     1, incorrectMainlineDependencies.size());
        assertTrue("incorrect mainline dependency",             incorrectMainlineDependencies.contains("commons-codec"));

        Set<String> unusedTestDependencies = extractArtifactIds(checker.getUnusedTestDependencies());
        assertEquals("unused test-only dependency count",       2, unusedTestDependencies.size());
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-core"));
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-test"));
    }


    @Test
    public void testIgnoreUnusedDepenencies() throws Exception
    {
        CommandLine args = new CommandLine("../test-dependency",
                                           "--ignoreUnusedDependency=commons-io",
                                           "--ignoreUnusedDependency=org.springframework:spring-core");
        DependencyCheck checker = new DependencyCheck(args);
        checker.run();

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        1, unusedMainlineDependencies.size());
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("bcelx"));

        Set<String> unusedTestDependencies = extractArtifactIds(checker.getUnusedTestDependencies());
        assertEquals("unused test-only dependency count",       1, unusedTestDependencies.size());
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-test"));
    }


    @Test
    public void testParentPom() throws Exception
    {
        CommandLine args = new CommandLine("../test-dependency-child");
        DependencyCheck checker = new DependencyCheck(args);
        checker.run();

        assertTrue("unsupported mainline class",                checker.getUnsupportedMainlineClasses().contains("org.apache.bcel.classfile.ClassParser"));
        assertTrue("unsupported mainline package",              checker.getUnsupportedMainlinePackages().contains("org.apache.bcel.classfile"));

        assertTrue("expected missing test class",               checker.getUnsupportedTestClasses().contains("org.apache.bcel.classfile.ConstantPool"));
        assertTrue("expected missing test package",             checker.getUnsupportedTestPackages().contains("org.apache.bcel.classfile"));

        assertFalse("doesn't contain in-project reference",     checker.getUnsupportedMainlineClasses().contains("com.kdgregory.pomutil.testdata.AnInterface"));

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        3, unusedMainlineDependencies.size());
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("bcelx"));
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("commons-io"));
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("commons-lang"));

        Set<String> incorrectMainlineDependencies = extractArtifactIds(checker.getIncorrectMainlineDependencies());
        assertEquals("incorrect mainline dependency count",     1, incorrectMainlineDependencies.size());
        assertTrue("incorrect mainline dependency",             incorrectMainlineDependencies.contains("commons-codec"));

        Set<String> unusedTestDependencies = extractArtifactIds(checker.getUnusedTestDependencies());
        assertEquals("unused test-only dependency count",       2, unusedTestDependencies.size());
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-core"));
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-test"));
    }


    @Test
    public void testReportUnusedRuntimeDependencies() throws Exception
    {
        CommandLine args = new CommandLine("../test-dependency",
                                                 "--reportUnusedRuntimeDependencies");
        DependencyCheck checker = new DependencyCheck(args);
        checker.run();

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        3, unusedMainlineDependencies.size());
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("bcelx"));
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("commons-io"));
        assertTrue("unused mainline dependency",                unusedMainlineDependencies.contains("commons-logging"));

    }


    public void testSelf() throws Exception
    {
        // running Main without a project directory will use cwd
        DependencyCheck checker = new DependencyCheck();
        checker.run();

        assertTrue("expected unsupported mainline package",     checker.getUnsupportedMainlinePackages().contains("org.apache.bcel.classfile"));
        assertEquals("no unused mainline dependencies",         0, checker.getUnusedMainlineDependencies().size());
        assertEquals("no unused test dependencies",             0, checker.getUnusedTestDependencies().size());
        assertEquals("no mis-scoped dependencies",              0, checker.getIncorrectMainlineDependencies().size());
    }
}
