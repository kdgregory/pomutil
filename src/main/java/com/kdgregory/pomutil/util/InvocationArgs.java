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

import java.util.LinkedList;
import java.util.TreeSet;

/**
 *  Extracts options and arguments from <code>argv</code>. Options are denoted by
 *  leading double-dash, and may contain an embedded value, separated by an equals
 *  sign: "<code>--foo=bar</code>". Everything else is a parameter.
 */
public class InvocationArgs
{
    LinkedList<String> params = new LinkedList<String>();
    TreeSet<String> options = new TreeSet<String>();


    public InvocationArgs(String... argv)
    {
        for (String arg : argv)
        {
            if (arg.startsWith("--"))
                options.add(arg);
            else
                params.add(arg);
        }
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Determines whether the argument list contains the specified option, with or
     *  without value specified (ie, if the command contains "<code>--foo=bar</code>",
     *  then this method will return true for "<code>--foo</code>" or
     *  "<code>--foo=bar</code>", but not "<code>--for=baz</code>".
     */
    public boolean hasOption(String req)
    {
        boolean exactMatch = options.contains(req);
        if (exactMatch)
            return true;

        // if requested option includes a parameter value, it must be an exact match
        if (req.contains("="))
            return false;

        // otherwise, we can look for a partial match
        req += "=";
        for (String opt : options.tailSet(req))
        {
            return opt.startsWith(req);
        }

        // should have returned from within the loop, unless it was empty
        return false;
    }


    /**
     *  Returns the next non-option parameter and removes it from the list. Returns
     *  null if there are no parameters left.
     */
    public String shift()
    {
        return (params.size() == 0)
             ? null
             : params.removeFirst();
    }
}
