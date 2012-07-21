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
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.log4j.Logger;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.util.InvocationArgs;


/**
 *  Finds explicit dependency version numbers and converts them to properties. See
 *  README for full specification.
 */
public class VersionProps
extends AbstractTransformer
{
    Logger logger = Logger.getLogger(getClass());

    private final static String[] DEPENDENCY_LOCATIONS = new String[]
            {
            "/mvn:project/mvn:dependencies/mvn:dependency",
            "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency"
            };

    private Set<String> groupsToAppendArtifactId;


    /**
     *  Base constructor.
     */
    public VersionProps(InvocationArgs args)
    {
        groupsToAppendArtifactId = args.getOptionValues("--addArtifactIdToProp");
    }


    /**
     *  Convenience constructor for argument-free invocation, used for testing.
     */
    public VersionProps()
    {
        this(new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
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
            ret.addAll(newXPath(xpath).evaluate(dom, Element.class));
        }
        return ret;
    }


    private void updateDependency(Element dependency, Map<String,String> versionProps)
    {
        String groupId = newXPath("mvn:groupId").evaluateAsString(dependency);
        String artifactId = newXPath("mvn:artifactId").evaluateAsString(dependency);
        String version = newXPath("mvn:version").evaluateAsString(dependency);

        if (version.startsWith("${"))
            return;

        String propName = groupId + ".version";
        if (versionProps.containsKey(propName) && !version.equals(versionProps.get(propName)))
        {
            String existingVersion = versionProps.get(propName);
            String newPropName = groupId + "." + artifactId + ".version";
            logger.warn("property \"" + propName + "\" already exists with version " + existingVersion
                        + "; creating \"" + newPropName + "\" for version " + version);
            propName = newPropName;
        }

        if (groupsToAppendArtifactId.contains(groupId))
        {
            propName = groupId + "." + artifactId + ".version";
        }

        versionProps.put(propName, version);

        Element versionElem = newXPath("mvn:version").evaluateAsElement(dependency);
        DomUtil.setText(versionElem, "${" + propName + "}");
    }


    private void updateOrAddProperties(Document dom, Map<String,String> versionProps)
    {
        Element props = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
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
