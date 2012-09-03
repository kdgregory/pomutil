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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.w3c.dom.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.cleaner.OutputHandler;


public class TestOutputHandler
{
//----------------------------------------------------------------------------
//  The OutputHandler writes to StdOut, so we need to capture it
//----------------------------------------------------------------------------

    private PrintStream savedStdOut;
    private ByteArrayOutputStream capture = new ByteArrayOutputStream();

    @Before
    public void setUp()
    {
        savedStdOut = System.out;
        System.setOut(new PrintStream(capture));
    }


    @After
    public void tearDown()
    {
        System.setOut(savedStdOut);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  Replaces all platform-dependent line separators in the source string
     *  with a newline, and trims any whitespace from the ends of the string.
     *  This will hopefully let us build on Windows and Mac as well as Linux.
     */
    private static String transformNewlines(String src)
    {
        String lineSep = System.getProperty("line.separator");
        if (StringUtil.isEmpty(lineSep))
            lineSep = "\n";

        StringBuilder sb = new StringBuilder(src);
        int ii = 0;
        while ((ii = sb.indexOf(lineSep, ii)) >= 0)
        {
            sb.insert(ii++, '\n');
            sb.delete(ii, ii + lineSep.length());
        }
        return sb.toString().trim();
    }


    /**
     *  Extracts the captured output stream and prepares it for use.
     */
    private String getOutput()
    throws Exception
    {
        String output = new String(capture.toByteArray(), "UTF-8");
        return transformNewlines(output);
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testNoPrettyPrint() throws Exception
    {
        // note: we don't have to work with a Maven POM for most (all?) of these tests

        String src = "<root>              <child>value</child> \n </root>";
        Document dom = ParseUtil.parse(src);

        InvocationArgs args = new InvocationArgs("--noPrettyPrint");
        new OutputHandler().writeOutput(dom, args);

        assertEquals("no transform", src, getOutput());
    }


    @Test
    public void testPrettyPrint() throws Exception
    {
        // note: we don't have to work with a Maven POM for most (all?) of these tests

        String src = "<root>         <child>value</child> \n </root>";
        Document dom = ParseUtil.parse(src);

        InvocationArgs args = new InvocationArgs("--prettyPrint=3");
        new OutputHandler().writeOutput(dom, args);

        assertEquals("<root>\n   <child>value</child>\n</root>", getOutput());
    }

}
