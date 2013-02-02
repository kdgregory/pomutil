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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.util.PomWrapper;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Organizes the POM according to the prototype. Disabled by default.
 */
public class OrganizePom
extends AbstractTransformer
{


//----------------------------------------------------------------------------
//  Instance variables and constructors
//----------------------------------------------------------------------------

    private boolean enabled;


    /**
     *  Base constructor.
     */
    public OrganizePom(PomWrapper pom, CommandLine args)
    {
        super(pom, args);
        enabled = args.isOptionEnabled(CommandLine.Options.ORGANIZE_POM);
    }


//----------------------------------------------------------------------------
//  Transformer
//----------------------------------------------------------------------------

    @Override
    public void transform()
    {
        if (!enabled)
            return;

        String[] expectedElements = loadPrototype();

        Element root = pom.getDom().getDocumentElement();
        Map<String,Element> children = selectElements(root);
        Utils.reconstruct(root, children, expectedElements);
    }


//----------------------------------------------------------------------------
//  Implementation
//----------------------------------------------------------------------------

    private String[] loadPrototype()
    {
        List<String> result = new ArrayList<String>();
        Document proto = ParseUtil.parseFromClasspath("proto-pom.xml");
        for (Element elem : DomUtil.getChildren(proto.getDocumentElement()))
        {
            result.add(DomUtil.getLocalName(elem));
        }
        return result.toArray(new String[result.size()]);
    }


    private Map<String,Element> selectElements(Element root)
    {
        Map<String,Element> result = new LinkedHashMap<String,Element>();
        for (Element elem : DomUtil.getChildren(root))
        {
            result.put(DomUtil.getLocalName(elem), elem);
        }
        return result;
    }
}
