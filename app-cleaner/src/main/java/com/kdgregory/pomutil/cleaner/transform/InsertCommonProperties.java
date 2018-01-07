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

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.kdgcommons.collections.MapBuilder;
import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Inserts common properties: those that Maven expects to find or it complains.
 */
public class InsertCommonProperties
extends AbstractTransformer
{
    Logger logger = LoggerFactory.getLogger(getClass());

    private static Map<String,String> COMMON_PROPS = new MapBuilder<String,String>(new TreeMap<String,String>())
                                                     .put("project.build.sourceEncoding",       "UTF-8")
                                                     .put("project.reporting.outputEncoding",   "UTF-8")
                                                     .toMap();

    private boolean disabled;


    /**
     *  Base constructor.
     */
    public InsertCommonProperties(PomWrapper pom, CommandLine options)
    {
        super(pom, options);
        disabled = ! options.isOptionEnabled(CommandLine.Options.COMMON_PROPS);
    }


    /**
     *  Convenience constructor with no arguments (primarily used for testing).
     */
    public InsertCommonProperties(PomWrapper pom)
    {
        this(pom, new CommandLine());
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
            {
                logger.info("adding property: " + prop.getKey());
                pom.setProperty(prop.getKey(), prop.getValue());
            }
        }
    }
}