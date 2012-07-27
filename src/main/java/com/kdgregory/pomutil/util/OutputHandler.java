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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;

import com.kdgregory.pomutil.Options;


/**
 *  Responsible for generating output. This object
 */
public class OutputHandler
{

    /**
     *  Post-processes the passed DOM as specified by the invocation
     *  arguments, and writes it to the destination. If there is at
     *  least one non-option argument, it is taken as the name of the
     *  output file. If not, output is written to StdOut.
     */
    public void writeOutput(Document dom, InvocationArgs args)
    throws IOException
    {
        dom = postProcess(dom, args);
        Writer out = null;
        try
        {
            out = openWriter(args);
            writeOutput(dom, args, out);
        }
        finally
        {
            IOUtil.closeQuietly(out);
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Performs any final transformations on the DOM. Currently, this consists
     *  of removing any existing inter-element whitespace (which messes up the
     *  pretty-printing).
     */
    private Document postProcess(Document dom, InvocationArgs args)
    {
        if (args.hasOption(Options.NO_PRETTY_PRINT))
            return dom;

        DomUtil.removeEmptyTextRecursive(dom.getDocumentElement());
        return dom;
    }


    /**
     *  Opens a writer for the output. If there's a non-option argument in the
     *  passed arguments, it will be taken as a filename. Otherwise, uses StdOut.
     *  <p>
     *  This method returns a writer, rather than an OutputStream, to work around
     *  JDK bug #XXX, which doesn't indent when writing to a stream. The returned
     *  writer will perform UTF-8 encoding.
     */
    public Writer openWriter(InvocationArgs args)
    throws IOException
    {
        FileOutputStream fos = null;
        OutputStream out = null;
        try
        {
            String filename = args.shift();
            if (filename != null)
            {
                out = fos = new FileOutputStream(filename);
            }
            else
            {
                out = System.out;
            }
            return new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }
        catch (IOException ex)
        {
            IOUtil.closeQuietly(fos);
            throw ex;
        }
    }


    /**
     *  The actual output routine.
     */
    private void writeOutput(Document dom, InvocationArgs args, Writer out)
    throws IOException
    {
        if (args.hasOption(Options.NO_PRETTY_PRINT))
        {
            OutputUtil.compact(new DOMSource(dom), new StreamResult(out));
        }
        else
        {
            Integer indent0 = args.getNumericOptionValue(Options.PRETTY_PRINT);
            int indent = (indent0 != null) ? indent0.intValue() : 4;
            OutputUtil.indented(new DOMSource(dom), new StreamResult(out),
                    indent);
        }
    }
}
