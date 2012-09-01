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

import java.util.Comparator;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;


/**
 *  Holds the data identifying an artifact, either stand-alone or in the context
 *  of a dependency. Instances may be used as map keys, and may be sorted. As this
 *  is intended as a data holder, there are no accessor methods; all members are
 *  public.
 */
public class Artifact
implements Comparable<Artifact>
{
    /**
     *  For artifacts that represent dependencies, identifies the scope to which the
     *  dependency applies. The order of these elements defines the outermost sort
     *  order for {@link #ScopedComparator}.
     */
    public enum Scope
    {
        COMPILE, RUNTIME, TEST, SYSTEM, PROVIDED
    }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    public String groupId;
    public String artifactId;
    public String version;
    public String classifier;
    public String packaging;
    public Scope scope;


    /**
     *  Base constructor, allowing explicit specification of all fields.
     *  The following defaults will be applied:
     *  <ul>
     *  <li> packaging: "jar"
     *  <li> scope: "compile"
     *  </ul>
     *  @throws IllegalArgumentException if passed a scope that does not match
     *          one of the enumerated values.
     */
    public Artifact(String groupId, String artifactId, String version,
                    String classifier, String packaging, String scope)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.packaging = StringUtil.isBlank(packaging) ? "jar" : packaging;
        this.scope = lookupScope(scope);
    }


    /**
     *  Simplified constructor, used when all you care about is the GAV.
     */
    public Artifact(String groupId, String artifactId, String version)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    /**
     *  Dependency constructor, which extracts all values from XML.
     */
    public Artifact(Element dependency)
    {
        this("", "", "", "", "", "");
        for (Element child : DomUtil.getChildren(dependency))
        {
            String localName = DomUtil.getLocalName(child);
            String value = DomUtil.getText(child).trim();
            if (localName.equals("groupId"))
                this.groupId = value;
            else if (localName.equals("artifactId"))
                this.artifactId = value;
            else if (localName.equals("version"))
                this.version = value;
            else if (localName.equals("type"))
                this.packaging = value;
            else if (localName.equals("classifier"))
                this.classifier = value;
            else if (localName.equals("scope"))
                this.scope = lookupScope(value);
        }
    }


    private static Scope lookupScope(String scope)
    {
        if (StringUtil.isBlank(scope))
        {
            return Scope.COMPILE;
        }
        else
        {
            return Scope.valueOf(scope.toUpperCase());
        }
    }


//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    /**
     *  Two instances are equal if all of their fields are equal.
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof Artifact)
        {
            Artifact that = (Artifact)obj;
            return this.groupId.equals(that.groupId)
                && this.artifactId.equals(that.artifactId)
                && this.version.equals(that.version);
        }

        return false;
    }


    @Override
    public int hashCode()
    {
        return artifactId.hashCode();
    }


    /**
     *  Returns a string that is useful for debugging.
     */
    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version + ":" + packaging;
    }


    /**
     *  Compares artifacts based on their groupId, artifactId, and version (in that order).
     *  At present, version comparison is textual; we do not interpret bounded ranges.
     */
    @Override
    public int compareTo(Artifact that)
    {
        int cmp = this.groupId.compareTo(that.groupId);
        if (cmp == 0)
            cmp = this.artifactId.compareTo(that.artifactId);
        if (cmp == 0)
            cmp = this.version.compareTo(that.version);
        return cmp;
    }


//----------------------------------------------------------------------------
//  Additional utility classes
//----------------------------------------------------------------------------

    /**
     *  A comparator that does a first-order comparison based on scope, then
     *  falls back to the built-in comparator.
     */
    public static class ScopedComparator
    implements Comparator<Artifact>
    {
        @Override
        public int compare(Artifact o1, Artifact o2)
        {
            int cmp = o1.scope.compareTo(o2.scope);
            return (cmp != 0)
                 ? cmp
                 : o1.compareTo(o2);
        }
    }
}