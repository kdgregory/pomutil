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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;


/**
 *  Holds a parsed POM and provides access to its various sections.
 */
public class PomWrapper
{
    private Document dom;

    private XPathWrapperFactory xpFact = new XPathWrapperFactory(CacheType.SIMPLE)
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");
    private boolean pomWithoutNamespace;

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;


    /**
     *  Constructs an instance from an already-parsed DOM.
     */
    public PomWrapper(Document dom)
    {
        this.dom = dom;

        pomWithoutNamespace = StringUtil.isBlank(dom.getDocumentElement().getNamespaceURI());

        groupId = selectValue(dom, PomPaths.PROJECT_GROUP);
        if (StringUtil.isBlank(groupId))
            groupId = selectValue(dom, PomPaths.PARENT_GROUP);

        artifactId = selectValue(dom, PomPaths.PROJECT_ARTIFACT);

        version = selectValue(dom, PomPaths.PROJECT_VERSION);
        if (StringUtil.isBlank(version))
            version = selectValue(dom, PomPaths.PARENT_VERSION);

        packaging = selectValue(dom, PomPaths.PROJECT_PACKAGING);
        if (StringUtil.isBlank(version))
            packaging = "jar";
    }


    /**
     *  Constructs an instance from a file.
     *
     *  @throws XmlException if unable to parse (for any reason).
     */
    public PomWrapper(File file)
    throws FileNotFoundException
    {
        this(ParseUtil.parse(file));
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the DOM wrapped by this object.
     */
    public Document getDom()
    {
        return dom;
    }


    /**
     *  Replaces the DOM wrapped by this object.
     */
    public void setDom(Document dom)
    {
        this.dom = dom;
    }


    /**
     *  Executes the passed XPath against the entire POM, and returns its string
     *  value. Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public String selectValue(String xpath)
    {
        return selectValue(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node, and returns its string
     *  value. Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public String selectValue(Node node, String xpath)
    {
        xpath = mungePath(xpath);
        return xpFact.newXPath(xpath).evaluateAsString(node);
    }


    /**
     *  Executes the passed XPath against the POM and returns the single element
     *  that it selects, <code>null</code> if it doesn't select anything. Path
     *  components must be prefixed with "mvn" to use the Maven namespace.
     */
    public Element selectElement(String xpath)
    {
        return selectElement(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node and returns the single
     *  element that it selects, <code>null</code> if it doesn't select anything.
     *  Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public Element selectElement(Node node, String xpath)
    {
        xpath = mungePath(xpath);
        return xpFact.newXPath(xpath).evaluateAsElement(node);
    }


    /**
     *  Executes the passed XPath against the POM and returns the elements that
     *  it selects. Path components must be prefixed with "mvn" to use the Maven
     *  namespace.
     */
    public List<Element> selectElements(String xpath)
    {
        return selectElements(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node and returns the elements
     *  that it selects. Path components must be prefixed with "mvn" to use the Maven
     *  namespace.
     */
    public List<Element> selectElements(Node node, String xpath)
    {
        xpath = mungePath(xpath);
        return xpFact.newXPath(xpath).evaluate(node, Element.class);
    }


    /**
     *  Selects the element specified by the given xpath, if it exists. If it doesn't
     *  exist, selects the parent element (recursively) and appends an element with
     *  the desired name (which is extracted from the path). Steps in the path must
     *  be prefixed with "mvn" to use the Maven namespace.
     *  <p>
     *  To properly create the child element, the last step in the path must be a
     *  simple node selector, of the form "mvn:NAME".
     */
    public Element selectOrCreateElement(String xpath)
    {
        xpath = mungePath(xpath);
        Element elem = selectElement(xpath);
        if (elem != null)
            return elem;

        String parentPath = StringUtil.extractLeftOfLast(xpath, "/");
        Element parent = selectOrCreateElement(parentPath);

        String childPath = StringUtil.extractRightOfLast(xpath, "/");
        if (!childPath.startsWith("mvn:"))
            throw new IllegalArgumentException("last element of path must have \"mvn\" prefix: " + childPath);

        String childName = childPath.substring(4);

        return DomUtil.appendChildInheritNamespace(parent, childName);
    }


    /**
     *  Selects the element identified by the given XPath, and removes all of its
     *  children. Acts as a no-op if the path does not select an element.
     */
    public Element clear(String xpath)
    {
        Element elem = selectElement(xpath);
        if (elem != null)
            DomUtil.removeAllChildren(elem);
        return elem;
    }


    /**
     *  Returns all properties currently in the POM.
     */
    public Map<String,String> getProperties()
    {
        Map<String,String> properties = new TreeMap<String,String>();
        for (Element propElem : selectElements(PomPaths.PROJECT_PROPERTIES + "/*"))
        {
            properties.put(DomUtil.getLocalName(propElem), DomUtil.getText(propElem));
        }
        return properties;
    }


    /**
     *  Returns the value of the named property, an empty string if it does not
     *  exist.
     */
    public String getProperty(String name)
    {
        return selectValue(pathToProp(name));
    }


    /**
     *  Sets the value of the named property, appending a <code>&lt;properties&gt;</code>
     *  section to the POM if one does not already exist.
     */
    public void setProperty(String name, String value)
    {
        Element elem = selectOrCreateElement(pathToProp(name));
        DomUtil.setText(elem, value);
    }


    /**
     *  Removes the named property if it exists; no-op if it doesn't.
     */
    public void deleteProperty(String name)
    {
        Element elem = selectElement(pathToProp(name));
        if (elem == null)
            return;

        elem.getParentNode().removeChild(elem);
    }


    /**
     *  Performs property substitution on the passed string. Will first look to
     *  user-defined properties, then a select set of Maven-defined properties.
     *  <p>
     *  If passed <code>null</code>, returns an empty string.
     */
    public String resolveProperties(String src)
    {
        if (src == null)
            return "";

        StringBuilder dst = new StringBuilder(256).append(src);

        int propIdx = 0;
        while ((propIdx = dst.indexOf("${", propIdx)) >= 0)
        {
            int endPropIdx = dst.indexOf("}", propIdx);
            if (endPropIdx < 0)
                break;  // unterminated propname
            String propName = dst.substring(propIdx+2, endPropIdx);
            if (propName.contains("{"))
                break;  // unterminated propname that causes problems with XPath
            String propValue = lookupPropertyValue(propName);
            if (! StringUtil.isBlank(propValue))
            {
                dst.delete(propIdx, endPropIdx + 1);
                if (propIdx < dst.length())
                    dst.insert(propIdx, propValue);
                else
                    dst.append(propValue);
            }
            else
            {
                // leave the unresolved property in place
                propIdx = endPropIdx;
            }
        }

        return dst.toString();
    }


    /**
     *  Returns this POM's GAV. The group and version will be taken from the POM's
     *  <code>parent</code> reference, if not specified in the POM itself.
     */
    public Artifact getGAV()
    {
        return new Artifact(groupId, artifactId, version, packaging);
    }


    /**
     *  Returns this POM's parent info, <code>null</code> if the POM does not have
     *  a parent.
     */
    public Artifact getParent()
    {
        Element parentElem = selectElement(dom, PomPaths.PARENT);
        if (parentElem == null)
            return null;

        String parentGroupId = selectValue(dom, PomPaths.PARENT_GROUP);
        String parentArtifactId = selectValue(dom, PomPaths.PARENT_ARTIFACT);
        String parentVersion = selectValue(dom, PomPaths.PARENT_VERSION);
        return new Artifact(parentGroupId, parentArtifactId, parentVersion, "pom");
    }


    /**
     *  Returns this POM's GAV, formatted "group:artifact:version".
     */
    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }


    /**
     *  Finds all dependencies (including those inside dependencyManagement)
     *  that have the specified group and optional artifact IDs.
     *
     *  @param  withGroupId     The dependency's groupId; must be specified.
     *  @param  withArtifactId  The dependency's artifactId; if null, selects
     *                          all dependencies for the specified group.
     */
    public List<Element> selectDependenciesByGroupAndArtifact(String withGroupId, String withArtifactId)
    {
        List<Element> result = new ArrayList<Element>();

        List<Element> allDependencies = new ArrayList<Element>();
        allDependencies.addAll(selectElements(PomPaths.PROJECT_DEPENDENCIES));
        allDependencies.addAll(selectElements(PomPaths.MANAGED_DEPENDENCIES));

        for (Element dependency : allDependencies)
        {
            String dependencyGroupId    = selectValue(dependency, "mvn:groupId");
            String dependencyArtifactId = selectValue(dependency, "mvn:artifactId");
            if (withGroupId.equals(dependencyGroupId)
                && ((withArtifactId == null) || withArtifactId.equals(dependencyArtifactId)))
            {
                result.add(dependency);
            }
        }

        return result;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private String mungePath(String xpath)
    {
        if (pomWithoutNamespace) xpath = xpath.replace("mvn:", "");
        return xpath;
    }


    private String lookupPropertyValue(String propName)
    {
        // try user-defined properties first
        String propValue = getProperty(propName);
        if (! StringUtil.isBlank(propValue))
            return propValue;

        // try to resolve project properties via XPath
        if (propName.startsWith("project."))
        {
            StringBuilder xpath = new StringBuilder(1024);
            for (String component : propName.split("\\."))
                xpath.append("/mvn:").append(component);

            return selectValue(dom, xpath.toString());
        }

        // and fall back to system property
        return ObjectUtil.defaultValue(System.getProperty(propName), "");
    }


    private static String pathToProp(String propName)
    {
        return PomPaths.PROJECT_PROPERTIES + "/mvn:" + propName;
    }
}
