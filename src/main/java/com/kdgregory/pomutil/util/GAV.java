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


/**
 *  Holds a group-artifact-version specification. Instances of this class
 *  are comparable (alpha by fields at present), and may be used as map keys.
 */
public class GAV
implements Comparable<GAV>
{
    public String groupId;
    public String artifactId;
    public String version;


    public GAV(String groupId, String artifactId, String version)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    @Override
    public int hashCode()
    {
        return groupId.hashCode() * 37 + artifactId.hashCode();
    }


    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof GAV)
        {
            GAV that = (GAV)obj;
            return this.groupId.equals(that.groupId)
                && this.artifactId.equals(that.artifactId)
                && this.version.equals(that.version);
        }

        return false;
    }


    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }


    @Override
    public int compareTo(GAV that)
    {
        int cmp = this.groupId.compareTo(that.groupId);
        if (cmp == 0)
            cmp = this.artifactId.compareTo(that.artifactId);
        if (cmp == 0)
            cmp = this.version.compareTo(that.version);
        return cmp;
    }
}