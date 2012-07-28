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


/**
 *  Constants for invocation options.
 */
public class Options
{
    /**
     *  Disables addition of common properties.
     */
    public final static String  NO_COMMON_PROPS         = "--noCommonProps";

    /**
     *  Disables sorting dependencies.
     */
    public final static String  NO_DEPENDENCY_SORT      = "--noDependencySort";

    /**
     *  Modifies dependency sort to order dependencies by scope.
     */
    public final static String  GROUP_DEPCY_BY_SCOPE    = "--groupDependenciesByScope";


    /**
     *  Disables substitution of properties for explicit version numbers.
     */
    public final static String  NO_VERSION_PROPS        = "--noVersionProps";

    /**
     *  For version properties, specifies group IDs that should always have
     *  artifact ID appended to form property name.
     */
    public final static String  VP_ADD_ARTIFACT_GROUP   = "--addArtifactIdToProp";

    /**
     *  For version properties, specifies that existing properties should be
     *  replaced by "standardized" properties.
     */
    public final static String  VP_REPLACE_EXISTING     = "--replaceExistingProps";

    /**
     *  Disables pretty-printing of output.
     */
    public final static String  NO_PRETTY_PRINT         = "--noPrettyPrint";

    /**
     *  Controls spacing of pretty-printed output.
     */
    public final static String  PRETTY_PRINT            = "--prettyPrint";
}
