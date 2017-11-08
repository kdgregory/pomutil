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

package com.kdgregory.pomutil.cleaner.transform;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.cleaner.transform.InsertCommonProperties;


public class TestInsertCommonProperties
extends AbstractTransformerTest
{
    @Test
    public void testAddPropsIfTheyDontExist() throws Exception
    {
        new InsertCommonProperties(loadPom("cleaner/CommonProps1.xml")).transform();

        assertEquals("number of properties", 2,
                     newXPath("/mvn:project/mvn:properties/*").evaluate(dom(), Element.class).size());
        assertEquals("project.build.sourceEncoding", "UTF-8",
                     newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding").evaluateAsString(dom()));
        assertEquals("project.reporting.outputEncoding", "UTF-8",
                     newXPath("/mvn:project/mvn:properties/mvn:project.reporting.outputEncoding").evaluateAsString(dom()));
    }


    @Test
    public void testLeaveExistingPropsAlone() throws Exception
    {
        new InsertCommonProperties(loadPom("cleaner/CommonProps2.xml")).transform();

        assertEquals("number of properties", 2,
                     newXPath("/mvn:project/mvn:properties/*").evaluate(dom(), Element.class).size());
        assertEquals("project.build.sourceEncoding", "US-ASCII",
                     newXPath("/mvn:project/mvn:properties/mvn:project.build.sourceEncoding").evaluateAsString(dom()));
        assertEquals("project.reporting.outputEncoding", "UTF-8",
                     newXPath("/mvn:project/mvn:properties/mvn:project.reporting.outputEncoding").evaluateAsString(dom()));
    }


    @Test
    public void testDisabled() throws Exception
    {
        CommandLine args = new CommandLine("--noCommonProps");
        new InsertCommonProperties(loadPom("cleaner/CommonProps1.xml"), args).transform();

        assertEquals("number of properties", 0,
                     newXPath("/mvn:project/mvn:properties/*").evaluate(dom(), Element.class).size());
    }

}
