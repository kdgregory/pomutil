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

import com.kdgregory.pomutil.util.InvocationArgs;


/**
 *  Responsible for generating output. The output may be written to a file
 *  (if specified by command-line options), StdOut, or a passed stream (if
 *  invoked programmatically). Output is written as UTF-8 (which may cause
 *  problems with a terminal that expects something else).
 */
public class OutputHandler
{
    private InvocationArgs args;
    private OutputStream out0;


    /**
     *  Constructor for output to StdOut or file (specified via
     *  invocation arguments).
     */
    public OutputHandler(InvocationArgs args)
    {
        this(args, null);
    }


    /**
     *  Constructor for known output stream. Caller is responsible for
     *  closing this stream.
     */
    public OutputHandler(InvocationArgs args, OutputStream out)
    {
        this.args = args;
        this.out0 = out;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Post-processes the passed DOM as specified by the invocation
     *  arguments, and writes it to the destination. If there is at
     *  least one non-option argument, it is taken as the name of the
     *  output file. If not, output is written to StdOut.
     */
    public void writeOutput(Document dom)
    throws IOException
    {
        dom = postProcess(dom);
        Writer out = null;
        try
        {
            out = openWriter();
            writeOutput(dom, out);
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
    private Document postProcess(Document dom)
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
    public Writer openWriter()
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
                out = (out0 == null) ? System.out : out0;
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
    private void writeOutput(Document dom, Writer out)
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
            OutputUtil.indented(new DOMSource(dom), new StreamResult(out), indent);
        }
    }
}
