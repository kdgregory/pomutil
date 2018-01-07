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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;


/**
 *  Responsible for generating output, applying any transformations (such
 *  as pretty-printing) specified via command-line arguments.
 */
public class OutputHandler
{
    private CommandLine args;


    public OutputHandler(CommandLine args)
    {
        this.args = args;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Post-processes the passed DOM as specified by the invocation arguments
     *  and writes it to the specified file. Overwrites any previous contents.
     */
    public void writeOutput(Document dom, File file)
    throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            writeOutput(dom, out);
        }
        finally
        {
            IOUtil.closeQuietly(out);
        }
    }


    /**
     *  Post-processes the passed DOM as specified by the invocation arguments
     *  and writes it to the specified stream. Caller is responsible for closing
     *  the stream.
     */
    public void writeOutput(Document dom, OutputStream out0)
    throws IOException
    {
        dom = postProcess(dom);

        Writer out = new BufferedWriter(new OutputStreamWriter(out0, "UTF-8"));
        writeOutput(dom, out);
        out.flush();
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
        if (args.isOptionEnabled(CommandLine.Options.PRETTY_PRINT))
        {
            DomUtil.removeEmptyTextRecursive(dom.getDocumentElement());
        }
        return dom;
    }


    /**
     *  The actual output routine.
     */
    private void writeOutput(Document dom, Writer out)
    throws IOException
    {
        if (args.isOptionEnabled(CommandLine.Options.PRETTY_PRINT))
        {
            List<String> indentLevel = args.getOptionValues(CommandLine.Options.PRETTY_PRINT);
            int indent = indentLevel.size() > 0
                       ? Integer.parseInt(indentLevel.get(0), 10)
                       : 4;
            OutputUtil.indented(new DOMSource(dom), new StreamResult(out), indent);
        }
        else
        {
            OutputUtil.compact(new DOMSource(dom), new StreamResult(out));
        }
    }
}
