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

import java.util.Map;
import java.util.TreeMap;

import net.sf.kdgcommons.collections.MapBuilder;
import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pomutil.Options;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Inserts common properties: those that Maven expects to find or it complains.
 */
public class CommonProps
extends AbstractTransformer
{
    private static Map<String,String> COMMON_PROPS = new MapBuilder<String,String>(new TreeMap<String,String>())
                                                     .put("project.build.sourceEncoding",       "UTF-8")
                                                     .put("project.reporting.outputEncoding",   "UTF-8")
                                                     .toMap();


//----------------------------------------------------------------------------
//  Instance variables and Constuctors
//----------------------------------------------------------------------------

    private boolean disabled;

    /**
     *  Base constructor.
     */
    public CommonProps(PomWrapper pom, InvocationArgs options)
    {
        super(pom, options);
        disabled = args.hasOption(Options.NO_COMMON_PROPS);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public CommonProps(PomWrapper pom)
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

        for (Map.Entry<String,String> prop : COMMON_PROPS.entrySet())
        {
            if (StringUtil.isBlank(pom.getProperty(prop.getKey())))
                pom.setProperty(prop.getKey(), prop.getValue());
        }
    }
}