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

package com.kdgregory.pomutil.dependency;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import net.sf.kdgcommons.collections.CollectionUtil;

import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.InvocationArgs;


/**
 *  Invoked by DependencyCheck to write the report to StdOut.
 */
public class Reporter
{
    private final static String OUTPUT_FORMAT = "%-32s %s%n";

    private InvocationArgs args;
    private Main checker;

    public Reporter(InvocationArgs args, Main checker)
    {
        this.args = args;
        this.checker = checker;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  This method does the writing.
     */
    public void output(OutputStream out0)
    throws IOException
    {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(out0, "UTF-8"));

        outputMissingDependencies(out);
        outputUnusedDependencies(out);
        outputIncorrectDependencies(out);

        out.flush();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void outputMissingDependencies(PrintWriter out)
    {
        Set<String> missingDependencies
                    = CollectionUtil.combine(new TreeSet<String>(),
                            checker.getUnsupportedMainlinePackages(),
                            checker.getUnsupportedTestPackages());

        for (String pkg : missingDependencies)
        {
            out.format(OUTPUT_FORMAT, "PACKAGE_MISSING_DEPENDENCY", pkg);
        }
    }


    private void outputUnusedDependencies(PrintWriter out)
    {
        for (Artifact artifact : checker.getUnusedMainlineDependencies())
        {
            out.format(OUTPUT_FORMAT, "UNUSED_MAINLINE_DEPENDENCY", artifact.artifactId);
        }

        for (Artifact artifact : checker.getUnusedTestDependencies())
        {
            out.format(OUTPUT_FORMAT, "UNUSED_TEST_DEPENDENCY", artifact.artifactId);
        }
    }


    private void outputIncorrectDependencies(PrintWriter out)
    {

        for (Artifact artifact : checker.getIncorrectMainlineDependencies())
        {
            out.format(OUTPUT_FORMAT, "UNSCOPED_TEST_DEPENDENCY", artifact.artifactId);
        }
    }
}
