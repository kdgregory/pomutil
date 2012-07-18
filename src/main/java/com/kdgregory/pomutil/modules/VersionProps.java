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

package com.kdgregory.pomutil.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;


/**
 *  Finds explicit dependency version numbers and converts them to properties.
 *  <p>
 *  Examines <code>&lt;dependencies&gt;</code> and <code>&lt;dependencyManagement&gt;</code>,
 *  sections.
 *  <p>
 *  The generated properties will be named <code>GROUPID.version</code>, where
 *  <code>GROUPID</code> is the group ID of the dependency. If multiple dependencies
 *  have the same group ID but different versions, they are logged and the second
 *  (and subsequent) properties are named <code>GROUPID.ARTIFACTID.version</code>.
 *  <p>
 *  If the POM already has a <code>&lt;properties&gt;</code> section, the generated
 *  properties will be appended to it. Otherwise, a new <code>&lt;properties&gt;</code>
 *  section will be appended to the POM.
 */
public class VersionProps 
extends AbstractTransformer
{
    private final static String[] DEPENDENCY_LOCATIONS = new String[]
            {
            "/mvn:project/mvn:dependencies/mvn:dependency",
            "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency"
            };

    private XPathWrapperFactory xpFact = new XPathWrapperFactory(CacheType.SIMPLE)
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");

//----------------------------------------------------------------------------
//  InplaceTransform
//----------------------------------------------------------------------------

    @Override
    public Document transform(Document dom)
    {
        TreeMap<String,String> versionProps = new TreeMap<String,String>();
        for (Element dependency : findDependencyDefinitions(dom))
        {
            updateDependency(dependency, versionProps);
        }
        updateOrAddProperties(dom, versionProps);
        return dom;
    }


//----------------------------------------------------------------------------
//  Implementation
//----------------------------------------------------------------------------

    private List<Element> findDependencyDefinitions(Document dom)
    {
        List<Element> ret = new ArrayList<Element>();
        for (String xpath : DEPENDENCY_LOCATIONS)
        {
            ret.addAll(xpFact.newXPath(xpath).evaluate(dom, Element.class));
        }
        return ret;
    }


    private void updateDependency(Element dependency, Map<String,String> versionProps)
    {
        String groupId = xpFact.newXPath("mvn:groupId").evaluateAsString(dependency);
        String artifactId = xpFact.newXPath("mvn:artifactId").evaluateAsString(dependency);
        String version = xpFact.newXPath("mvn:version").evaluateAsString(dependency);

        if (version.startsWith("${"))
            return;

        String propName = groupId + ".version";
        if (versionProps.containsKey(propName))
        {
            propName = groupId + "." + artifactId + ".version";
            // FIXME - log this
        }

        versionProps.put(propName, version);

        Element versionElem = xpFact.newXPath("mvn:version").evaluateAsElement(dependency);
        DomUtil.setText(versionElem, "${" + propName + "}");
    }


    private void updateOrAddProperties(Document dom, Map<String,String> versionProps)
    {
        Element props = xpFact.newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        if (props == null)
        {
            props = DomUtil.appendChildInheritNamespace(dom.getDocumentElement(), "properties");
        }

        for (Map.Entry<String,String> entry : versionProps.entrySet())
        {
            Element prop = DomUtil.appendChildInheritNamespace(props, entry.getKey());
            DomUtil.setText(prop, entry.getValue());
        }
    }
}
