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

import org.w3c.dom.Document;


/**
 *  Base class for operations that transform a single POM in some way. The
 *  parsed POM is passed to {@link #transform}, which returns the same or
 *  a different parsed POM. 
 */
public abstract class AbstractTransformer
{
    public abstract Document transform(Document dom);
}
