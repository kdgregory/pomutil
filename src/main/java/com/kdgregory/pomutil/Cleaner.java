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

import java.io.File;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.transformers.DependencySort;
import com.kdgregory.pomutil.transformers.VersionProps;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.OutputHandler;
import com.kdgregory.pomutil.util.PomWrapper;


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
            PomWrapper pom = new PomWrapper(readDocument(args));

            if (! args.hasOption(Options.NO_DEPENDENCY_SORT))
                new DependencySort(pom, args).transform();

            if (! args.hasOption(Options.NO_VERSION_PROPS))
                new VersionProps(pom, args).transform();

            new OutputHandler().writeOutput(pom.getDom(), args);
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
}
