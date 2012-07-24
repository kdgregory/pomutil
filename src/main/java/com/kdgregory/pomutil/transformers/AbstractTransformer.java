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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import net.sf.practicalxml.xpath.XPathWrapper;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;


/**
 *  Base class for operations that transform a single POM.
 */
public abstract class AbstractTransformer
{
    private XPathWrapperFactory xpFact = new XPathWrapperFactory(CacheType.SIMPLE)
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");


//----------------------------------------------------------------------------
//  Services for subclasses
//----------------------------------------------------------------------------

    /**
     *  Returns an XPath that with the Maven namespace bound to the prefix
     *  "mvn". XPath objects maybe stored in a single-threaded cache.
     */
    public XPathWrapper newXPath(String xpath)
    {
        return xpFact.newXPath(xpath);
    }


    /**
     *  Removes all children from the passed element.
     *
     *  FIXME - this belongs in PracticalXml
     */
    protected void removeAllChildren(Node node)
    {
        Node child = node.getFirstChild();
        while (child != null)
        {
            Node nextChild = child.getNextSibling();
            node.removeChild(child);
            child = nextChild;
        }
    }


//----------------------------------------------------------------------------
//  Subclasses must implement this method
//----------------------------------------------------------------------------

    /**
     *  Transforms the source POM, returning the result (which may be the same
     *  Document instance of a different one).
     */
    public abstract Document transform(Document pom);
}
