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

package com.kdgregory.pomutil.cleaner.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;

import org.apache.log4j.Logger;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.Options;
import com.kdgregory.pomutil.util.GAV;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Finds explicit dependency version numbers and converts them to properties. See
 *  README for full specification.
 */
public class ReplaceExplicitVersionsWithProperties
extends AbstractDependencyTransformer
{
    Logger logger = Logger.getLogger(getClass());


//----------------------------------------------------------------------------
//  Instance variables and constructors
//----------------------------------------------------------------------------

    private boolean disabled;
    private boolean replaceExisting;
    private Set<String> groupsToAppendArtifactId;


    /**
     *  Base constructor.
     */
    public ReplaceExplicitVersionsWithProperties(PomWrapper pom, InvocationArgs args)
    {
        super(pom, args);
        disabled = args.hasOption(Options.NO_VERSION_PROPS);
        replaceExisting = args.hasOption(Options.VP_REPLACE_EXISTING);
        groupsToAppendArtifactId = args.getOptionValues(Options.VP_ADD_ARTIFACT_GROUP);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public ReplaceExplicitVersionsWithProperties(PomWrapper pom)
    {
        this(pom, new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        if (disabled)
            return;

        Map<String,String> allProps = pom.getProperties();
        List<Element> dependencies = selectAllDependencies();

        if (replaceExisting)
        {
            Set<String> oldProps = replaceExistingDependencyProps(dependencies, allProps);
            removeReplacedProperties(oldProps);
        }

        Set<String> newProps = updateDependencies(dependencies, allProps);
        addNewProperties(allProps, newProps);
    }


//----------------------------------------------------------------------------
//  Implementation
//----------------------------------------------------------------------------

    private Set<String> replaceExistingDependencyProps(List<Element> dependencies, Map<String,String> properties)
    {
        Set<String> result = new TreeSet<String>();

        for (Element dependency : dependencies)
        {
            String version = pom.selectValue(dependency, "mvn:version");
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


    private void removeReplacedProperties(Set<String> propNames)
    {
        for (String propName : propNames)
        {
            pom.deleteProperty(propName);
        }
    }


    private Set<String> updateDependencies(List<Element> dependencies, Map<String,String> props)
    {
        Set<String> newProps = new TreeSet<String>();
        for (Element dependency : dependencies)
        {
            GAV gav = pom.extractGAV(dependency);
            if (gav.version.startsWith("${"))
                continue;

            String propName = generatePropertyName(props, gav);
            updateDependency(dependency, "${" + propName + "}");
            newProps.add(propName);
        }
        return newProps;
    }


    private String generatePropertyName(Map<String,String> props, GAV gav)
    {
        String propName = gav.groupId + ".version";
        if (props.containsKey(propName) && !gav.version.equals(props.get(propName)))
        {
            String existingVersion = props.get(propName);
            String newPropName = gav.groupId + "." + gav.artifactId + ".version";
            logger.warn("property \"" + propName + "\" already exists with version " + existingVersion
                        + "; creating \"" + newPropName + "\" for version " + gav.version);
            propName = newPropName;
        }

        if (groupsToAppendArtifactId.contains(gav.groupId))
        {
            propName = gav.groupId + "." + gav.artifactId + ".version";
        }

        props.put(propName, gav.version);
        return propName;
    }


    private void updateDependency(Element dependency, String version)
    {
        Element versionElem = pom.selectElement(dependency, "mvn:version");
        DomUtil.setText(versionElem, version);
    }


    private void addNewProperties(Map<String,String> props, Set<String> newProps)
    {
        for (String propName : newProps)
        {
            pom.setProperty(propName, props.get(propName));
        }
    }
}
