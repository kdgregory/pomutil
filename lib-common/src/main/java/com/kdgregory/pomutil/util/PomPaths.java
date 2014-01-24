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

package com.kdgregory.pomutil.util;


/**
 *  Common XPaths for retrieving data from a POM. These all use a namespace binding
 *  prefix of "mvn".
 */
public class PomPaths
{
    public final static String  PROJECT_GROUP        = "/mvn:project/mvn:groupId";
    public final static String  PROJECT_ARTIFACT     = "/mvn:project/mvn:artifactId";
    public final static String  PROJECT_VERSION      = "/mvn:project/mvn:version";
    public final static String  PROJECT_PACKAGING    = "/mvn:project/mvn:packaging";

    public final static String  PARENT               = "/mvn:project/mvn:parent";
    public final static String  PARENT_GROUP         = "/mvn:project/mvn:parent/mvn:groupId";
    public final static String  PARENT_ARTIFACT      = "/mvn:project/mvn:parent/mvn:artifactId";
    public final static String  PARENT_VERSION       = "/mvn:project/mvn:parent/mvn:version";

    public final static String  PROPERTIES_BASE      = "/mvn:project/mvn:properties";
    public final static String  PROJECT_PROPERTIES   = PROPERTIES_BASE + "/*";

    public final static String  PROJECT_DEPENDENCIES = "/mvn:project/mvn:dependencies/mvn:dependency";
    public final static String  MANAGED_DEPENDENCIES = "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency";

    public final static String  BUILD_PLUGINS        = "/mvn:project/mvn:build/mvn:plugins/mvn:plugin";
    public final static String  REPORTING_PLUGINS    = "/mvn:project/mvn:reporting/mvn:plugins/mvn:plugin";
    public final static String  MANAGED_PLUGINS      = "/mvn:project/mvn:build/mvn:pluginManagement/mvn:plugins/mvn:plugin";
}
