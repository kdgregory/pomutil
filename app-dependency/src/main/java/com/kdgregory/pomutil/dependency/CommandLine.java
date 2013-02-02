// Copyright (c) Keith D Gregory
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

import net.sf.kdgcommons.util.SimpleCLIParser;


public class CommandLine
extends SimpleCLIParser
{
    public enum Options
    {
        REPORT_UNUSED_RUNTIME, IGNORE_UNUSED
    }


    public CommandLine(String... argv)
    {
        super(argv,
            new OptionDefinition(
                    Options.REPORT_UNUSED_RUNTIME,
                    "--reportUnusedRuntimeDependencies", "--noReportUnusedRuntimeDependencies", false,
                    "Reports unused runtime-scoped dependencies. This doesn't usually make sense,"
                    + " but may be useful if you have a proliferation of dependencies and want to"
                    + " manually inspect them"),
            new OptionDefinition(
                    Options.IGNORE_UNUSED, "--ignoreUnusedDependency", 1,
                    "Ignores the specified dependency if it is not used by mainline code.")
            );
    }
}
