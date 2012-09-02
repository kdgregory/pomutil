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

package com.kdgregory.pomutil.testdata;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

public class SomeClass
implements AnInterface
{

    @SuppressWarnings("unused")
    private JavaClass me;

    @Override
    public void foo()
    {
        String myName = this.getClass().getName() + ".class";
        try
        {
            me = new ClassParser(this.getClass().getResourceAsStream(myName), myName).parse();
        }
        catch (Exception e)
        {
            throw new RuntimeException("unable to parse self", e);
        }
    }

}
