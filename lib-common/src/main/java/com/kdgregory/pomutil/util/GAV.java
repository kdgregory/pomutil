// Copyright (c) Keith D Gregory, all rights reserved
package com.kdgregory.pomutil.util;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.practicalxml.DomUtil;

/**
 *  Holds the Maven groupId/artifactId/version.
 */
public class GAV
implements Comparable<GAV>
{
    public String groupId;
    public String artifactId;
    public String version;


    /**
     *  Initialize from explicit values.
     */
    public GAV(String groupId, String artifactId, String version)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    /**
     *  Initialize from XML, looking at the children of the passed element
     *  (and ignoring namespace).
     */
    public GAV(Element elem)
    {
        String localGroupId = "";
        String localArtifactId = "";
        String localVersion = "";

        for (Element child : DomUtil.getChildren(elem))
        {
            if (child.getLocalName().equals("groupId"))
                localGroupId = DomUtil.getText(child);
            else if (child.getLocalName().equals("artifactId"))
                localArtifactId = DomUtil.getText(child);
            else if (child.getLocalName().equals("version"))
                localVersion = DomUtil.getText(child);
        }

        this.groupId = localGroupId;
        this.artifactId = localArtifactId;
        this.version = localVersion;
    }


    @Override
    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;

        GAV that = (GAV)obj;
        return ObjectUtil.equals(this.groupId, that.groupId)
            && ObjectUtil.equals(this.artifactId, that.artifactId)
            && ObjectUtil.equals(this.version, that.version);
    }


    @Override
    public final int hashCode()
    {
        return (groupId.hashCode() * 37 + artifactId.hashCode()) * 37 + version.hashCode();
    }


    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }


    /**
     *  Compares artifacts based on their groupId, artifactId, and version (in that order).
     *  At present, version comparison is textual; we do not interpret bounded ranges.
     */
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
