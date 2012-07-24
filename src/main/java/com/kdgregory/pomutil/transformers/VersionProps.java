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

package com.kdgregory.pomutil.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.log4j.Logger;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.Options;
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
    private boolean replaceExisting;


    /**
     *  Base constructor.
     */
    public VersionProps(InvocationArgs args)
    {
        groupsToAppendArtifactId = args.getOptionValues(Options.VP_ADD_ARTIFACT_GROUP);
        replaceExisting = args.hasOption(Options.VP_REPLACE_EXISTING);
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
        List<Element> dependencies = selectDependencies(dom);
        Map<String,String> allProps = selectProperties(dom);

        if (replaceExisting)
        {
            Set<String> oldProps = replaceExistingDependencyProps(dependencies, allProps);
            removeReplacedProperties(dom, oldProps);
        }

        Set<String> newProps = updateDependencies(dependencies, allProps);
        addNewProperties(dom, allProps, newProps);
        return dom;
    }


//----------------------------------------------------------------------------
//  Implementation
//----------------------------------------------------------------------------

    private List<Element> selectDependencies(Document dom)
    {
        List<Element> ret = new ArrayList<Element>();
        for (String xpath : DEPENDENCY_LOCATIONS)
        {
            ret.addAll(newXPath(xpath).evaluate(dom, Element.class));
        }
        return ret;
    }


    private Map<String,String> selectProperties(Document dom)
    {
        Map<String,String> result = new TreeMap<String,String>();   // TreeMap is easier to debug
        for (Element prop : newXPath("/mvn:project/mvn:properties/*").evaluate(dom, Element.class))
        {
            result.put(DomUtil.getLocalName(prop), DomUtil.getText(prop));
        }
        return result;
    }


    private Set<String> replaceExistingDependencyProps(List<Element> dependencies, Map<String,String> properties)
    {
        Set<String> result = new TreeSet<String>();

        for (Element dependency : dependencies)
        {
            String version = newXPath("mvn:version").evaluateAsString(dependency);
            if (! version.startsWith("${"))
                continue;

            version = version.substring(2);
            version = version.substring(0, version.length() - 1);
            if (! properties.containsKey(version))
                continue;

            updateDependency(dependency, properties.get(version).trim());
            result.add(version);
        }

        return result;
    }


    private void removeReplacedProperties(Document dom, Set<String> propNames)
    {
        Element containerElem = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        for (String propName : propNames)
        {
            Element propElem = newXPath("mvn:" + propName).evaluateAsElement(containerElem);
            containerElem.removeChild(propElem);
        }
    }


    private Set<String> updateDependencies(List<Element> dependencies, Map<String,String> props)
    {
        Set<String> newProps = new TreeSet<String>();
        for (Element dependency : dependencies)
        {
            String groupId = newXPath("mvn:groupId").evaluateAsString(dependency);
            String artifactId = newXPath("mvn:artifactId").evaluateAsString(dependency);
            String version = newXPath("mvn:version").evaluateAsString(dependency);

            if (version.startsWith("${"))
                continue;

            String propName = generatePropertyName(props, groupId, artifactId, version);
            updateDependency(dependency, "${" + propName + "}");
            newProps.add(propName);
        }
        return newProps;
    }


    private String generatePropertyName(Map<String,String> props, String groupId, String artifactId, String version)
    {
        String propName = groupId + ".version";
        if (props.containsKey(propName) && !version.equals(props.get(propName)))
        {
            String existingVersion = props.get(propName);
            String newPropName = groupId + "." + artifactId + ".version";
            logger.warn("property \"" + propName + "\" already exists with version " + existingVersion
                        + "; creating \"" + newPropName + "\" for version " + version);
            propName = newPropName;
        }

        if (groupsToAppendArtifactId.contains(groupId))
        {
            propName = groupId + "." + artifactId + ".version";
        }

        props.put(propName, version);
        return propName;
    }


    private void updateDependency(Element dependency, String version)
    {
        Element versionElem = newXPath("mvn:version").evaluateAsElement(dependency);
        DomUtil.setText(versionElem, version);
    }


    private void addNewProperties(Document dom, Map<String,String> props, Set<String> newProps)
    {
        Element propElem = newXPath("/mvn:project/mvn:properties").evaluateAsElement(dom);
        if (propElem == null)
        {
            propElem = DomUtil.appendChildInheritNamespace(dom.getDocumentElement(), "properties");
        }

        for (String propName : newProps)
        {
            Element prop = DomUtil.appendChildInheritNamespace(propElem, propName);
            DomUtil.setText(prop, props.get(propName));
        }
    }
}
