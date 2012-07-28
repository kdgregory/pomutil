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

package com.kdgregory.pomutil.transformers;

import java.util.List;

import org.w3c.dom.Element;

import org.junit.Test;

import static org.junit.Assert.*;

import com.kdgregory.pomutil.transformers.DependencySort;
import com.kdgregory.pomutil.util.InvocationArgs;


public class TestDependencySort
extends AbstractTransformerTest
{
    @Test
    public void testBasicOperation() throws Exception
    {
        new DependencySort(loadPom("DependencySort1.xml")).transform();

        List<Element> dependencies = newXPath("/mvn:project/mvn:dependencies/*").evaluate(dom(), Element.class);
        assertEquals("number of dependencies in <dependencies>", 3, dependencies.size());
        assertDependencySpec("dependencies(0)", dependencies.get(0), "commons-io",      "commons-io",      "2.4");
        assertDependencySpec("dependencies(1)", dependencies.get(1), "commons-logging", "commons-logging", "1.1");
        assertDependencySpec("dependencies(2)", dependencies.get(2), "junit",           "junit",           "4.10");

        List<Element> dependencyMgmt = newXPath("/mvn:project/mvn:dependencyManagement/mvn:dependencies/*").evaluate(dom(), Element.class);
        assertEquals("number of dependencies in <dependencyManagement>", 4, dependencyMgmt.size());
        assertDependencySpec("dependencyMgmt(0)", dependencyMgmt.get(0), "org.springframework", "spring-context", "3.1.2.RELEASE");
        assertDependencySpec("dependencyMgmt(1)", dependencyMgmt.get(1), "org.springframework", "spring-core",    "3.1.2.RELEASE");
        assertDependencySpec("dependencyMgmt(2)", dependencyMgmt.get(2), "org.springframework", "spring-orm",     "3.1.2.RELEASE");
        assertDependencySpec("dependencyMgmt(3)", dependencyMgmt.get(3), "org.springframework", "spring-tx",      "3.1.2.RELEASE");
    }


    @Test
    public void testGroupByScope() throws Exception
    {
        InvocationArgs args = new InvocationArgs("--groupDependenciesByScope");
        new DependencySort(loadPom("DependencySort2.xml"), args).transform();

        List<Element> dependencies = newXPath("/mvn:project/mvn:dependencies/*").evaluate(dom(), Element.class);
        assertEquals("number of dependencies in <dependencies>", 6, dependencies.size());
        assertDependencySpec("dependencies(0)", dependencies.get(0), "commons-io",      "commons-io",       "2.4");
        assertDependencySpec("dependencies(1)", dependencies.get(1), "commons-lang",    "commons-lang",     "2.3");
        assertDependencySpec("dependencies(2)", dependencies.get(2), "junit",           "junit",            "4.10");
        assertDependencySpec("dependencies(3)", dependencies.get(3), "org.slf4j",       "slf4j-log4j12",    "1.6.6");
        assertDependencySpec("dependencies(4)", dependencies.get(4), "commons-logging", "commons-logging",  "1.1");
        assertDependencySpec("dependencies(5)", dependencies.get(5), "javax.sql",       "jdbc-stdext",      "2.0");
    }
}
