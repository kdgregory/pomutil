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

import java.io.Serializable;
import java.util.Comparator;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;


/**
 *  Holds the data identifying an artifact, either stand-alone or in the context
 *  of a dependency. Instances may be used as map keys, and may be sorted. As this
 *  is intended as a data holder, there are no accessor methods; all members are
 *  public and mutable.
 */
public class Artifact
extends GAV
{
    /**
     *  For artifacts that represent dependencies, identifies the scope to which the
     *  dependency applies. The order of these elements defines the outermost sort
     *  order for {@link #ScopedComparator}.
     */
    public enum Scope
    {
        IMPORT, COMPILE, RUNTIME, TEST, SYSTEM, PROVIDED
    }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    public String classifier    = "";
    public String packaging     = "jar";
    public Scope scope          = Scope.COMPILE;
    public boolean optional;


    /**
     *  Base constructor, allowing explicit specification of all fields.
     *
     *  @throws IllegalArgumentException if passed a scope that does not match
     *          one of the enumerated values.
     */
    public Artifact(String groupId, String artifactId, String version,
                    String classifier, String packaging, String scope, boolean isOptional)
    {
        super(groupId, artifactId, version);
        this.classifier = classifier;
        this.packaging = packaging.toLowerCase();
        this.scope = lookupScope(scope);
        this.optional = isOptional;
    }


    /**
     *  Convenience constructor, used for arbitrary compile-scope artifacts.
     */
    public Artifact(String groupId, String artifactId, String version, String packaging)
    {
        super(groupId, artifactId, version);
        this.packaging = packaging;
    }


    /**
     *  Convenience constructor, used for compile-scope JARs.
     */
    public Artifact(String groupId, String artifactId, String version)
    {
        super(groupId, artifactId, version);
    }


    /**
     *  Dependency constructor, which extracts all values from XML.
     */
    public Artifact(Element dependency)
    {
        super(dependency);
        for (Element child : DomUtil.getChildren(dependency))
        {
            String localName = DomUtil.getLocalName(child);
            String value = StringUtil.trim(DomUtil.getText(child));

            if (localName.equals("type"))
                this.packaging = value.toLowerCase();
            else if (localName.equals("classifier"))
                this.classifier = value;
            else if (localName.equals("scope"))
                this.scope = lookupScope(value);
            else if (localName.equals("optional"))
                this.optional = value.equalsIgnoreCase("true");
        }
    }


    /**
     *  Internal constructor, used for the various copy operations.
     */
    public Artifact(Artifact that)
    {
        super(that.groupId, that.artifactId, that.version);
        this.classifier = that.classifier;
        this.packaging = that.packaging;
        this.scope = that.scope;
        this.optional = that.optional;
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
//  Other Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the groupId/artifactId of this artifact, to key a dependency
     *  map.
     */
    public GAKey toGAKey()
    {
        return new GAKey(groupId, artifactId);
    }


    /**
     *  Returns a new artifact, representing the POM for this artifact.
     */
    public Artifact toPom()
    {
        return new  Artifact(groupId, artifactId, version, "", "pom", "", optional);
    }


    /**
     *  Returns a copy of this artifact, with a different version.
     */
    public Artifact withVersion(String newVersion)
    {
        return new Artifact(groupId, artifactId, newVersion, classifier, packaging, scope.name(), optional);
    }

//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    /**
     *  Returns a string that is useful for debugging.
     */
    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version + ":" + packaging;
    }


//----------------------------------------------------------------------------
//  Additional utility classes
//----------------------------------------------------------------------------

    /**
     *  A comparator that does a first-order comparison based on scope, then
     *  falls back to the built-in comparator.
     */
    public static class ScopedComparator
    implements Comparator<Artifact>, Serializable
    {
        private static final long serialVersionUID = 1L;

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