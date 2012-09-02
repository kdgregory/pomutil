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

import java.io.File;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.cleaner.transform.InsertCommonProperties;
import com.kdgregory.pomutil.cleaner.transform.NormalizeDependencies;
import com.kdgregory.pomutil.cleaner.transform.SortDependencies;
import com.kdgregory.pomutil.cleaner.transform.ReplaceExplicitVersionsWithProperties;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Driver program for single-file cleanup. See README for invocation instructions.
 *  <p>
 *  Successful execution results in a 0 return code. Any exception will be written
 *  to StdErr, and the program will terminate with a non-zero return code.
 */
public class Main
{
    public static void main(String[] argv)
    {
        try
        {
            new Main(new InvocationArgs(argv)).run();
            System.exit(0);
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private InvocationArgs args;

    public Main(InvocationArgs args)
    {
        this.args = args;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    public void run()
    throws Exception
    {
        PomWrapper pom = new PomWrapper(readDocument(args));

        new InsertCommonProperties(pom, args).transform();
        new NormalizeDependencies(pom, args).transform();
        new SortDependencies(pom, args).transform();
        new ReplaceExplicitVersionsWithProperties(pom, args).transform();

        new OutputHandler().writeOutput(pom.getDom(), args);
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
