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

package com.kdgregory.pomutil.cleaner;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.w3c.dom.Document;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.ParseUtil;


public class TestOutputHandler
{

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  Converts the passed stream to a string, replaces all platform-dependent
     *  line separaters with a newline, and trims whitespace. This will hopefully
     *  let us build on Windows and Mac as well as Linux.
     */
    private String getOutput(ByteArrayOutputStream out)
    throws Exception
    {
        String lineSep = System.getProperty("line.separator");
        if (StringUtil.isEmpty(lineSep))
            lineSep = "\n";

        String output = new String(out.toByteArray(), "UTF-8");
        StringBuilder sb = new StringBuilder(output);
        int ii = 0;
        while ((ii = sb.indexOf(lineSep, ii)) >= 0)
        {
            sb.insert(ii++, '\n');
            sb.delete(ii, ii + lineSep.length());
        }
        return sb.toString().trim();
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    // note: we don't have to work with a Maven POM for any of these tests

    @Test
    public void testNoPrettyPrint() throws Exception
    {
        CommandLine args = new CommandLine("--noPrettyPrint");

        String src = "<root>              <child>value</child> \n </root>";
        Document dom = ParseUtil.parse(src);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new OutputHandler(args).writeOutput(dom, out);

        assertEquals("no transform", src, getOutput(out));
    }


    @Test
    public void testPrettyPrint() throws Exception
    {
        CommandLine args = new CommandLine("--prettyPrint=3");

        String src = "<root>         <child>value</child> \n </root>";
        Document dom = ParseUtil.parse(src);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new OutputHandler(args).writeOutput(dom, out);

        assertEquals("<root>\n   <child>value</child>\n</root>", getOutput(out));
    }


    @Test
    public void testWriteToFile() throws Exception
    {
        CommandLine args = new CommandLine("--noPrettyPrint");

        String src = "<root><child>value</child></root>";
        Document dom = ParseUtil.parse(src);

        File dest = IOUtil.createTempFile("testWriteToFile", 0);
        new OutputHandler(args).writeOutput(dom, dest);

        assertEquals(src.length(), dest.length());
    }

}
