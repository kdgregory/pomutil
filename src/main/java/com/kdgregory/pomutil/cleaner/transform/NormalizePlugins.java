package com.kdgregory.pomutil.cleaner.transform;

import java.util.Map;

import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;

import com.kdgregory.pomutil.cleaner.Options;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Normalizes <code>&lt;plugin&gt;</code> entries, ordering the child elements
 *  and adding a missing <code>&lt;groupId&gt;</code>. element.
 */
public class NormalizePlugins
extends AbstractTransformer
{
    private static final String[] STANDARD_CHILDREN = new String[] {
            "groupId", "artifactId", "version",
            "extensions", "inherited", "configuration",
            "dependencies", "executions"
            };


//----------------------------------------------------------------------------
//  Instance variables and Constuctors
//----------------------------------------------------------------------------

    private boolean disabled;


    /**
     *  Base constructor.
     */
    public NormalizePlugins(PomWrapper pom, InvocationArgs args)
    {
        super(pom, args);
        disabled = args.hasOption(Options.NO_PLUGIN_NORMALIZE);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public NormalizePlugins(PomWrapper pom)
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

        for (Element dependency : selectAllPlugins())
        {
            Map<String,Element> children = Utils.getChildrenAsMap(dependency);
            if (! children.containsKey("groupId"))
            {
                Element groupId = DomUtil.appendChildInheritNamespace(dependency, "groupId");
                DomUtil.setText(groupId, "org.apache.maven.plugins");
                children = Utils.getChildrenAsMap(dependency);
            }
            Utils.reconstruct(dependency, children, STANDARD_CHILDREN);
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------
}
