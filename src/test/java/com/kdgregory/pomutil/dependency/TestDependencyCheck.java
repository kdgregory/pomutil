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

import org.apache.bcel.classfile.ConstantPool;
import org.apache.commons.codec.binary.Base32;

import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.InvocationArgs;


public class TestDependencyCheck
{
    // this variable creates a reference to an "unused" mainline dependency,
    // is used to identify dependencies that should have test scope; it's
    // marked "protected" to keep Eclipse from complaining that it's not used

    protected Base32 ignoreMe = new Base32();

    // this variable creates a dependency that is only satisfied transitively
    // (it's also only found via field analysis); like above, it's protected
    // to keep Eclipse from complaining

    protected ConstantPool ignoreMe2;


//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    private Set<String> extractArtifactIds(Collection<Artifact> artifacts)
    {
        Set<String> result = new TreeSet<String>();
        for (Artifact artifact : artifacts)
            result.add(artifact.artifactId);
        return result;
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testBasicOperation() throws Exception
    {
        DependencyCheck checker = new DependencyCheck();
        checker.run();

        assertTrue("expected missing mainline class",            checker.getUnsupportedMainlineClasses().contains("org.apache.bcel.classfile.ClassParser"));
        assertTrue("expected missing mainline package",          checker.getUnsupportedMainlinePackages().contains("org.apache.bcel.classfile"));
        assertFalse("expected present mainline class",           checker.getUnsupportedMainlineClasses().contains("net.sf.kdgcommons.io.IOUtil"));
        assertFalse("mainline JDK references should be ignored", checker.getUnsupportedMainlineClasses().contains("java.lang.String"));

        assertTrue("expected missing test class",               checker.getUnsupportedTestClasses().contains("org.apache.bcel.classfile.ConstantPool"));
        assertTrue("expected missing test package",             checker.getUnsupportedTestPackages().contains("org.apache.bcel.classfile"));
        assertFalse("expected present test class",              checker.getUnsupportedTestClasses().contains("org.junit.Test"));
        assertFalse("test JDK references should be ignored",    checker.getUnsupportedTestClasses().contains("java.lang.String"));

        assertFalse("doesn't contain in-project reference",     checker.getUnsupportedMainlineClasses().contains("com.kdgregory.pomutil.dependency.util.ClassScanner"));

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        1, unusedMainlineDependencies.size());
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
        InvocationArgs args = new InvocationArgs("--ignoreUnusedDependency=commons-io", "--ignoreUnusedDependency=org.springframework:spring-core");
        DependencyCheck checker = new DependencyCheck(args);
        checker.run();

        Set<String> unusedMainlineDependencies = extractArtifactIds(checker.getUnusedMainlineDependencies());
        assertEquals("unused mainline dependency count",        0, unusedMainlineDependencies.size());

        Set<String> unusedTestDependencies = extractArtifactIds(checker.getUnusedTestDependencies());
        assertEquals("unused test-only dependency count",       1, unusedTestDependencies.size());
        assertTrue("unused test dependency",                    unusedTestDependencies.contains("spring-test"));
    }

}
