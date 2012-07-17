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
 *  Operations that do an in-place transform of a single POM implement this interface.
 *  <p>
 *  Instances are allowed to maintain state. However, a new instance will be created
 *  for each POM, so state will not persist.
 */
public interface InplaceTransform
{
    public void transform(Document dom);
}
