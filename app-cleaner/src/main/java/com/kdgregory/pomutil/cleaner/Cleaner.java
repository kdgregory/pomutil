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
import java.io.InputStream;
import java.io.OutputStream;

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
 *  The cleaner, responsible for applying the desired set of transformations
 *  to the input POM.
 */
public class Cleaner
{
//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private InvocationArgs args;
    private PomWrapper pom;
    private OutputStream out;


    /**
     *  Constructor for command-line invocation. Will read POM from arguments.
     */
    public Cleaner(InvocationArgs args)
    throws Exception
    {
        this.args = args;
        this.pom = new PomWrapper(readDocument(args));
    }


    /**
     *  Constructor for programmatic invocation. Output stream is closed
     *  but input is not.
     */
    public Cleaner(InvocationArgs args, InputStream in, OutputStream out)
    {
        this.args = args;
        this.pom = new PomWrapper(ParseUtil.parse(new InputSource(in)));
        this.out = out;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    public void run()
    throws Exception
    {
        new InsertCommonProperties(pom, args).transform();
        new NormalizeDependencies(pom, args).transform();
        new SortDependencies(pom, args).transform();
        new ReplaceExplicitVersionsWithProperties(pom, args).transform();

        new OutputHandler(args, out).writeOutput(pom.getDom());
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
