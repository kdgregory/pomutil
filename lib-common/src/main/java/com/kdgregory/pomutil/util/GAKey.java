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

import net.sf.kdgcommons.tuple.ComparableTuple2;


/**
 *  A combination of group ID and artifact ID, used to key dependency maps. Exists
 *  because we need to resolve between different versions of the same artifact.
 */
public class GAKey
extends ComparableTuple2<String,String>
{
    public GAKey(String groupId, String artifactId)
    {
        super(groupId, artifactId);
    }
}
