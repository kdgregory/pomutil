package com.kdgregory.pomutil.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Normalizes <code>&lt;dependency&gt;</code> entries, ordering the child elements
 *  and removing any children that represent default values.
 */
public class DependencyNormalize
extends AbstractTransformer     // FIXME - create AbstractDependencyTransformer
{
    private static final String[] STANDARD_CHILDREN = new String[] { "groupId", "artifactId", "version",
                                                   "classifier", "type", "scope",
                                                   "systemPath", "exclusions", "optional" };
    private final static String[] DEPENDENCY_LOCATIONS = new String[]
            {
            "/mvn:project/mvn:dependencies/mvn:dependency",
            "/mvn:project/mvn:dependencyManagement/mvn:dependencies/mvn:dependency"
            };


//----------------------------------------------------------------------------
//  Constuctors
//----------------------------------------------------------------------------

    /**
     *  Base constructor.
     */
    public DependencyNormalize(PomWrapper pom, InvocationArgs options)
    {
        super(pom, options);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public DependencyNormalize(PomWrapper pom)
    {
        this(pom, new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        for (Element dependency : selectDependencies())
        {
            Map<String,Element> children = extractChildren(dependency);
            removeMatchingChild(children, "scope", "compile");
            removeMatchingChild(children, "type", "jar");
            reconstructDependency(dependency, children);
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private List<Element> selectDependencies()
    {
        List<Element> ret = new ArrayList<Element>();
        for (String xpath : DEPENDENCY_LOCATIONS)
        {
            ret.addAll(pom.selectElements(xpath));
        }
        return ret;
    }


    private Map<String,Element> extractChildren(Element dependency)
    {
        Map<String,Element> children = new HashMap<String,Element>();
        for (Element child : DomUtil.getChildren(dependency))
        {
            children.put(DomUtil.getLocalName(child), child);
        }
        return children;
    }


    private void removeMatchingChild(Map<String,Element> children, String childName, String defaultValue)
    {
        Element elem = children.get(childName);
        if (elem == null)
            return;

        String curVal = DomUtil.getText(elem).trim();
        if (curVal.equals(defaultValue))
            children.remove(childName);
    }


    private void reconstructDependency(Element dependency, Map<String,Element> children)
    {
        DomUtil.removeAllChildren(dependency);
        for (String localName : STANDARD_CHILDREN)
        {
            Element child = children.remove(localName);
            if (child != null)
                dependency.appendChild(child);
        }

        for (Element child : children.values())
        {
                dependency.appendChild(child);
        }
    }


}
