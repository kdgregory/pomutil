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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import net.sf.kdgcommons.collections.MapBuilder;
import net.sf.practicalxml.OutputUtil;
import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.modules.InplaceTransform;
import com.kdgregory.pomutil.modules.VersionProps;


/**
 *  Driver program for single-file cleanup. See README for invocation instructions.
 *  <p>
 *  Successful execution results in a 0 return code. Any exception will be written
 *  to StdErr, and the program will terminate with a non-zero return code.
 */
public class Cleaner
{
    public final static String  OPT_VERSION_PROPS       = "--versionprops";
    public final static String  OPT_NO_VERSION_PROPS    = "--noversionprops";

    public static void main(String[] argv)
    {
        try
        {
            LinkedList<String> args = new LinkedList<String>(Arrays.asList(argv));

            List<InplaceTransform> modules = processOptions(args);
            Document dom = readDocument(args);
            for (InplaceTransform module : modules)
            {
                module.transform(dom);
            }
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
     *  Examines the passed argument list for options, and builds the module
     *  list. Options are removed once they're processed.
     *
     *  @throws IllegalArgumentException if an unrecognized option is in the
     *          list.
     */
    private static List<InplaceTransform> processOptions(LinkedList<String> args)
    {
        Map<String,InplaceTransform> transforms = new MapBuilder<String,InplaceTransform>(
                                                    new HashMap<String,InplaceTransform>())
                                                    .put(OPT_VERSION_PROPS, new VersionProps())
                                                    .toMap();

        for (Iterator<String> itx = args.iterator() ; itx.hasNext() ; )
        {
            String arg = itx.next();
            if (!arg.startsWith("--"))
                continue;

            itx.remove();
            if (arg.equals(OPT_VERSION_PROPS))
                ; // do nothing, this is the default
            else if (arg.equals(OPT_NO_VERSION_PROPS))
                transforms.remove(OPT_VERSION_PROPS);
            else
                throw new IllegalArgumentException("invalid option: " + arg);
        }

        return new ArrayList<InplaceTransform>(transforms.values());
    }


    /**
     *  Creates the DOM, either by reading the first entry in the passed list
     *  (which is then removed), or by reading StdIn (if there aren't any
     *  entries in the list).
     */
    private static Document readDocument(LinkedList<String> args)
    throws Exception
    {
        if (args.size() > 0)
        {
            File file = new File(args.removeFirst());
            return ParseUtil.parse(file);
        }
        else
        {
            return ParseUtil.parse(new InputSource(System.in));
        }
    }


    /**
     *  Pretty-prints the provided DOM, writing it to either a file whose name is
     *  the first (and only) entry in the passed list, or to StdOut (if there are
     *  no entries in the list).
     */
    private static void writeOutput(Document dom, LinkedList<String> args)
    throws Exception
    {
        if (args.size() > 0)
        {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(args.removeFirst()));
            OutputUtil.indented(new DOMSource(dom), new StreamResult(out), 4);
            out.flush();
            out.close();
        }
        else
        {
            OutputUtil.indented(new DOMSource(dom), new StreamResult(System.out), 4);
        }
    }
}
