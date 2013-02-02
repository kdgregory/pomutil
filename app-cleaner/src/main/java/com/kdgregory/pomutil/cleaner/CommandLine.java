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

package com.kdgregory.pomutil.cleaner;

import net.sf.kdgcommons.util.SimpleCLIParser;


public class CommandLine
extends SimpleCLIParser
{
    public enum Options
    {
        ORGANIZE_POM, PRETTY_PRINT, COMMON_PROPS,
        DEPENDENCY_NORMALIZE, DEPENDENCY_SORT, DEPENDENCY_SORT_BY_SCOPE,
        VERSION_PROPS, VP_REPLACE_EXISTING, VP_CONVERT_PLUGINS, VP_ARTIFACT_ID,
        PLUGIN_NORMALIZE
    }


    public CommandLine(String... argv)
    {
        super(argv,
            new OptionDefinition(
                    Options.ORGANIZE_POM,
                    "--organizePom", "--noOrganizePom", false,
                    "Restructures the entire POM to follow the order in the Maven documantion."
                    + " As a side-effect, removes any comments."),
            new OptionDefinition(
                    Options.PRETTY_PRINT,
                    "--prettyPrint", "--noPrettyPrint", true,
                    "Pretty-prints the cleaned POM. By default, indentation is four spaces;"
                    + " you can control this with \"--prettyPrint=VALUE\"."),
            new OptionDefinition(
                    Options.ORGANIZE_POM,
                    "--organizePom", "--noOrganizePom", false,
                    "Restructures the entire POM to follow the order in the Maven documantion."
                    + " As a side-effect, removes any comments."),
            new OptionDefinition(
                    Options.COMMON_PROPS,
                    "--commonProps", "--noCommonProps", true,
                    "Inserts common build properties (such as source encoding) if not already present."),
            new OptionDefinition(
                    Options.VERSION_PROPS,
                    "--versionProps", "--noVersionProps", true,
                    "Replaces hardcoded dependency versions with properties. These properties normally"
                    + " take the form \"GROUPID.version\"; where there are multiple artifacts with the"
                    + " same group but different versions, they take the form \"GROUPID.ARTIFACTID.version\"."),
            new OptionDefinition(
                    Options.VP_REPLACE_EXISTING,
                    "--replaceExistingProps", "--noReplaceExistingProps", false,
                    "Replaces existing properties used as dependency versions (assures that all"
                    + " version properties follow same form). Do not use if you inherit properties"
                    + " from a parent POM"),
            new OptionDefinition(
                    Options.VP_CONVERT_PLUGINS,
                    "--convertPluginVersions", "--noConvertPluginVersions", true,
                    "Creates properties for plugins as well as normal dependencies. These properties"
                    + " take the form \"plugin.ARTIFACTID.version\"."),
            new OptionDefinition(
                    Options.VP_ARTIFACT_ID,
                    "--addArtifactIdToProp", 1,
                    "For artifacts in the specified group, always construct version properties named"
                    + " \"GROUPID.ARTIFACTID.version\" (often used for organization-local artifacts)."),
            new OptionDefinition(
                    Options.DEPENDENCY_NORMALIZE,
                    "--dependencyNormalize", "--noDependencyNormalize", true,
                    "Ensures that the children of a <dependency> element follow the order shown"
                    + " in the Maven POM documentation."),
            new OptionDefinition(
                    Options.DEPENDENCY_SORT,
                    "--dependencySort", "--noDependencySort", true,
                    "Sorts <dependency> elements by groupId and artifactId."),
            new OptionDefinition(
                    Options.DEPENDENCY_SORT_BY_SCOPE,
                    "--groupDependenciesByScope", "--noGroupDependenciesByScope", false,
                    "When sorting <dependency> elements, sort first by scope."),
            new OptionDefinition(
                    Options.PLUGIN_NORMALIZE,
                    "--pluginNormalize", "--noPluginNormalize", true,
                    "Ensures that the children of a <plugin> element follow the order shown"
                    + " in the Maven POM documentation, and adds an explicit <groupId> if"
                    + " the specification is relying on the default.")
            );
    }
}
