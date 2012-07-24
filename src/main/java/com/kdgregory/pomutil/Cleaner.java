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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;
import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.transformers.DependencySort;
import com.kdgregory.pomutil.transformers.VersionProps;
import com.kdgregory.pomutil.util.InvocationArgs;


/**
 *  Driver program for single-file cleanup. See README for invocation instructions.
 *  <p>
 *  Successful execution results in a 0 return code. Any exception will be written
 *  to StdErr, and the program will terminate with a non-zero return code.
 */
public class Cleaner
{

    public static void main(String[] argv)
    {
        try
        {
            InvocationArgs args = new InvocationArgs(argv);
            Document dom = readDocument(args);

            if (! args.hasOption(Options.NO_DEPENDENCY_SORT))
                dom = new DependencySort(args).transform(dom);

            if (! args.hasOption(Options.NO_VERSION_PROPS))
                dom = new VersionProps(args).transform(dom);

            writeOutput(dom, args);
            System.exit(0);
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Creates the DOM, either by reading the first entry in the passed list
     *  (which is then removed), or by reading StdIn (if there aren't any
     *  entries in the list).
     */
    private static Document readDocument(InvocationArgs args)
    throws Exception
    {
        String filename = args.shift();
        if (filename != null)
        {
            return ParseUtil.parse(new File(filename));
        }
        else
        {
            return ParseUtil.parse(new InputSource(System.in));
        }
    }


    /**
     *  Output generation code.
     */
    private static void writeOutput(Document dom, InvocationArgs args)
    throws Exception
    {
        String filename = args.shift();

        // note: must use writer due to JDK Bug 6337981
        Writer out = (filename != null)
                   ? new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")
                   : new OutputStreamWriter(System.out, "UTF-8");
        out = new BufferedWriter(out);

        try
        {
            if (args.hasOption(Options.NO_PRETTY_PRINT))
            {
                OutputUtil.compact(new DOMSource(dom), new StreamResult(out));
            }
            else
            {
                DomUtil.removeEmptyTextRecursive(dom.getDocumentElement());

                Integer indent0 = args.getNumericOptionValue(Options.PRETTY_PRINT);
                int indent = (indent0 != null) ? indent0.intValue() : 4;
                OutputUtil.indented(new DOMSource(dom), new StreamResult(out), indent);
            }
        }
        finally
        {
            IOUtil.closeQuietly(out);
        }
    }
}
