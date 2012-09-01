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


/**
 *  Options to control the behavior of {@link DependencyCheck}.
 */
public class Options
{
    /**
     *  Selects the target directory for the dependency check. By default, checks the
     *  current working directory.
     */
    public final static String  TARGET_DIRECTORY    = "--projectDirectory";


    /**
     *  Ignores the specified depdendency when flagging unused dependencies.
     */
    public final static String  IGNORE_UNUSED_DPCY   = "--ignoreUnusedDependency";
}
