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

package com.kdgregory.pomutil.version;

import net.sf.kdgcommons.collections.CollectionUtil;

/**
 *  Driver program for POM version changes. See README for invocation instructions.
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
            CommandLine commandLine = new CommandLine(argv);
            if (! commandLine.isValid())
            {
                // TODO - print usage
                System.exit(1);
            }

            new VersionUpdater(
                CollectionUtil.first(commandLine.getOptionValues(CommandLine.Options.OLD_VERSION)),
                CollectionUtil.first(commandLine.getOptionValues(CommandLine.Options.NEW_VERSION)),
                commandLine.isOptionEnabled(CommandLine.Options.UPDATE_PARENT),
                commandLine.getParameters())
                .run();
            System.exit(0);
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
