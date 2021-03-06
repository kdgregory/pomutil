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
import java.util.List;

import com.kdgregory.pomutil.util.Utils;


/**
 *  Driver program for POM cleanup.
 *  <p>
 *  Successful execution results in a 0 return code. Any exception will be written
 *  to StdErr, and the program will terminate with a non-zero return code.
 */
public class Main
{
    public static void main(String[] argv)
    throws Exception
    {
        CommandLine commandLine = new CommandLine(argv);
        List<File> files = Utils.buildFileListFromStringList(commandLine.getParameters());
        
        if (! commandLine.isValid())
        {
            System.err.println("usage: java -jar target/app-cleaner-*.jar OPTIONS FILES_OR_DIRECTORIES...");
            System.err.println();
            System.err.println("where OPTIONS are:");
            System.err.println(commandLine.getHelp());
            System.exit(1);
        }

        new Cleaner(commandLine).run(files);
    }
}
