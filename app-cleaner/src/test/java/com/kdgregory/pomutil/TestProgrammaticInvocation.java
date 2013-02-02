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

package com.kdgregory.pomutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.cleaner.Cleaner;
import com.kdgregory.pomutil.cleaner.CommandLine;


public class TestProgrammaticInvocation
{
    @Test
    public void testProgrammaticInvocation() throws Exception
    {
        InputStream in = Thread.currentThread().getContextClassLoader()
                         .getResourceAsStream("cleaner/OrganizePom1.xml");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new Cleaner(new CommandLine(), in, out).run();

        // the assertions are going to be minimal; we trust other tests

        Document cleanPom = ParseUtil.parse(new InputSource(
                                new ByteArrayInputStream(out.toByteArray())));

        assertEquals("created root element", "project", cleanPom.getDocumentElement().getLocalName());
    }
}
