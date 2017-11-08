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

import java.util.HashMap;
import java.util.Map;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.util.SimpleCLIParser;


/**
 *  Command-line processor and option definitions.
 *  <p>
 *  In order to simplify usage from the web application, this class manages its
 *  option definitions as static data, retrievable by key without an instance.
 */
public class CommandLine
extends SimpleCLIParser
{

//----------------------------------------------------------------------------
//  Configuration
//----------------------------------------------------------------------------

    public enum Options
    {
        ORGANIZE_POM, PRETTY_PRINT, COMMON_PROPS,
        DEPENDENCY_NORMALIZE, DEPENDENCY_SORT, DEPENDENCY_SORT_BY_SCOPE,
        VERSION_PROPS, VP_REPLACE_EXISTING, VP_ARTIFACT_ID,
        VP_CONVERT_PLUGINS, PLUGIN_NORMALIZE
    }


    private static OptionDefinition[] optionDefs = new OptionDefinition[]
    {
        new OptionDefinition(
                Options.ORGANIZE_POM,
                "--organizePom", "--noOrganizePom", false,
                "Restructure the entire POM to follow the order in the Maven documentation"
                + " (as a side-effect, removes any comments between top-level sections)."),
        new OptionDefinition(
                Options.PRETTY_PRINT,
                "--prettyPrint", "--noPrettyPrint", true,
                "Pretty-print the cleaned POM. By default, indentation is four spaces;"
                + " you can control this with \"--prettyPrint=VALUE\"."),
        new OptionDefinition(
                Options.COMMON_PROPS,
                "--commonProps", "--noCommonProps", true,
                "Insert common build properties (such as source encoding) if not already present."),
        new OptionDefinition(
                Options.VERSION_PROPS,
                "--versionProps", "--noVersionProps", true,
                "Replace hardcoded dependency versions with properties. These properties normally"
                + " take the form \"GROUPID.version\"; where there are multiple artifacts with the"
                + " same group but different versions, they take the form \"GROUPID.ARTIFACTID.version\"."),
        new OptionDefinition(
                Options.VP_REPLACE_EXISTING,
                "--replaceExistingProps", "--noReplaceExistingProps", false,
                "Replace existing properties used as dependency versions (assures that all"
                + " version properties follow same form). Do not use if you inherit properties"
                + " from a parent POM"),
        new OptionDefinition(
                Options.VP_ARTIFACT_ID,
                "--addArtifactIdToProp", 1,
                "For artifacts in the specified group, always construct version properties named"
                + " \"GROUPID.ARTIFACTID.version\" (often used for organization-local artifacts)."),
        new OptionDefinition(
                Options.VP_CONVERT_PLUGINS,
                "--convertPluginVersions", "--noConvertPluginVersions", true,
                "Create properties for plugins as well as normal dependencies. These properties"
                + " take the form \"plugin.ARTIFACTID.version\"."),
        new OptionDefinition(
                Options.DEPENDENCY_NORMALIZE,
                "--dependencyNormalize", "--noDependencyNormalize", true,
                "Ensure that the children of a <dependency> element follow the order shown"
                + " in the Maven POM documentation."),
        new OptionDefinition(
                Options.DEPENDENCY_SORT,
                "--dependencySort", "--noDependencySort", true,
                "Sort <dependency> elements by groupId and artifactId."),
        new OptionDefinition(
                Options.DEPENDENCY_SORT_BY_SCOPE,
                "--groupDependenciesByScope", "--noGroupDependenciesByScope", false,
                "When sorting <dependency> elements, sort first by scope."),
        new OptionDefinition(
                Options.PLUGIN_NORMALIZE,
                "--pluginNormalize", "--noPluginNormalize", true,
                "Ensure that the children of a <plugin> element follow the order shown"
                + " in the Maven POM documentation, and adds an explicit <groupId> if"
                + " the specification is relying on the default.")
    };

    private static Map<Object,OptionDefinition> optionDefsByKey = new HashMap<Object,OptionDefinition>();
    static
    {
        for (OptionDefinition optionDef : optionDefs)
        {
            optionDefsByKey.put(optionDef.getKey(), optionDef);
        }
    }


    /**
     *  Retrieves an option definition by its key.
     */
    public static OptionDefinition getDefinition(Options key)
    {
        return optionDefsByKey.get(key);
    }

//----------------------------------------------------------------------------
//  Operation
//----------------------------------------------------------------------------

    public CommandLine(String... argv)
    {
        super(argv, optionDefs);
    }


    public boolean isValid()
    {
        return ! CollectionUtil.isEmpty(getParameters());
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);

        for (OptionDefinition optionDef : optionDefs)
        {
            if (optionDef.getType() == OptionDefinition.Type.BINARY)
            {
                sb.append((sb.length() > 0) ? " " : "");
                sb.append(isOptionEnabled(optionDef.getKey())
                          ? optionDef.getEnableVal()
                          : optionDef.getDisableVal());
            }
            else if (! getOptionValues(optionDef.getKey()).isEmpty())
            {
                sb.append((sb.length() > 0) ? " " : "");
                sb.append(optionDef.getEnableVal());
                for (String value : getOptionValues(optionDef.getKey()))
                {
                    sb.append(" ").append(value);
                }
            }
        }

        for (String param : getParameters())
        {
            sb.append(" ").append(param);
        }

        return sb.toString();
    }
}
