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

import org.junit.Ignore;
import org.junit.Test;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.commons.codec.binary.Base32;


@Ignore  // there's no reason to actually run this test
public class TestSomeClass
{
    // this variable creates a reference to an "unused" mainline dependency,
    // is used to identify dependencies that should have test scope; it's
    // marked "protected" to keep Eclipse from complaining that it's not used

    protected Base32 ignoreMe = new Base32();

    // this variable creates a dependency that is only satisfied transitively
    // (it's also only found via field analysis); like above, it's protected
    // to keep Eclipse from complaining

    protected ConstantPool ignoreMe2;


    @Test
    public void testInvokeFoo() throws Exception
    {
        SomeClass x = new SomeClass();
        x.foo();
    }
}
