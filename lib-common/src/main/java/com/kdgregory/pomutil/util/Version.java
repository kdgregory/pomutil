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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.kdgcommons.lang.StringUtil;


/**
 *  Encapsulates ordering of Maven versions according to the Maven 2/3 format:
 *  <code>major.minor.incremental-qualifier</code>, where major, minor, and
 *  incremental components are numeric, and the qualifier component is alpha.
 *  Version identifiers that do not follow this format use an alpha comparison
 *  (see @link http://books.sonatype.com/mvnref-book/reference/pom-relationships-sect-pom-syntax.html).
 */
public class Version
implements Comparable<Version>
{
    private final static String SNAPSHOT = "-SNAPSHOT";

    private String version;
    private List<VersionComponent> versionComponents;  // if null, use alpha comparison
    private boolean isSnapshot;

    public Version(String version)
    {
        this.version = version;

        if (version.endsWith(SNAPSHOT))
        {
            this.isSnapshot = true;
            version = StringUtil.extractLeftOfLast(version, SNAPSHOT);
        }

        this.versionComponents = decomposeVersion(version);
    }


    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof Version)
        {
            Version that = (Version)obj;
            return this.version == that.version;
        }

        return false;
    }


    @Override
    public final int hashCode()
    {
        return version.hashCode();
    }


    @Override
    public String toString()
    {
        return version;
    }


    @Override
    public int compareTo(Version that)
    {
        int cmp = ((this.versionComponents != null) && (that.versionComponents != null))
                ? CollectionUtil.compare(this.versionComponents, that.versionComponents)
                : this.version.compareTo(that.version);

        if (cmp == 0)
        {
            cmp = (this.isSnapshot && ! that.isSnapshot) ? -1
                : (that.isSnapshot && ! this.isSnapshot) ? 1
                : 0;
        }

        return cmp;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Holder for a component of the version string. Components may be
     *  numeric or alpha, and components of like types may be compared.
     */
    private static class VersionComponent
            implements Comparable<VersionComponent>
    {
        private String  _alphaValue;
        private Integer _numericValue;

        public VersionComponent(String value)
        {
            try
            {
                _numericValue = Integer.parseInt(value);
            }
            catch (NumberFormatException ex)
            {
                _alphaValue = value;
            }
        }

        public boolean isAlpha()
        {
            return _alphaValue != null;
        }

        @Override
        public final boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            else if (obj instanceof VersionComponent)
            {
                VersionComponent that = (VersionComponent)obj;
                return ObjectUtil.equals(this._alphaValue, that._alphaValue)
                    && ObjectUtil.equals(this._numericValue, that._numericValue);
            }
            return false;
        }

        @Override
        public final int hashCode()
        {
            return (_alphaValue != null) ? _alphaValue.hashCode() : _numericValue.hashCode();
        }

        @Override
        public int compareTo(VersionComponent that)
        {
            return (_alphaValue != null)
                 ? this._alphaValue.compareTo(that._alphaValue)
                 : this._numericValue.compareTo(that._numericValue);
        }
    }


    /**
     *  Decomposes a version into components, and determines whether it follows
     *  the standard format. Returns a list of the components if it does, null
     *  (as a flag) if it doesn't.
     */
    private static List<VersionComponent> decomposeVersion(String version)
    {
        List<VersionComponent> result = new ArrayList<VersionComponent>();
        for (String comp : version.split("[.-]"))
            result.add(new VersionComponent(comp));

        for (Iterator<VersionComponent> itx = result.iterator() ; itx.hasNext() ; )
        {
            if (itx.next().isAlpha() && itx.hasNext())
                return null;
        }

        return result;
    }
}
