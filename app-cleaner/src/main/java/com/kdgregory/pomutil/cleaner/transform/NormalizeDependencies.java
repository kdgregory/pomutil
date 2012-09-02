package com.kdgregory.pomutil.cleaner.transform;

import java.util.Map;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.Options;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Normalizes <code>&lt;dependency&gt;</code> entries, ordering the child elements
 *  and removing any children that represent default values.
 */
public class NormalizeDependencies
extends AbstractTransformer
{
    private static final String[] STANDARD_CHILDREN = new String[] {
           "groupId", "artifactId", "version",
           "classifier", "type", "scope",
           "systemPath", "exclusions", "optional" };


//----------------------------------------------------------------------------
//  Instance variables and Constuctors
//----------------------------------------------------------------------------

    private boolean disabled;


    /**
     *  Base constructor.
     */
    public NormalizeDependencies(PomWrapper pom, InvocationArgs args)
    {
        super(pom, args);
        disabled = args.hasOption(Options.NO_DEPENDENCY_NORMALIZE);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public NormalizeDependencies(PomWrapper pom)
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

        for (Element dependency : selectAllDependencies())
        {
            Map<String,Element> children = Utils.getChildrenAsMap(dependency);
            removeMatchingChild(children, "scope", "compile");
            removeMatchingChild(children, "type", "jar");
            Utils.reconstruct(dependency, children, STANDARD_CHILDREN);
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void removeMatchingChild(Map<String,Element> children, String childName, String defaultValue)
    {
        Element elem = children.get(childName);
        if (elem == null)
            return;

        String curVal = DomUtil.getText(elem).trim();
        if (curVal.equals(defaultValue))
            children.remove(childName);
    }
}
