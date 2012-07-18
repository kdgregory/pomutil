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

package com.kdgregory.pomutil.util;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestInvocationArgs
{
    @Test
    public void testEmptyConstructor() throws Exception
    {
        // a basic test to make sure nothing blows up
        InvocationArgs args = new InvocationArgs(new String[0]);

        assertFalse("hasOption()",  args.hasOption("--foo"));
        assertNull("shift()",       args.shift());
    }


    @Test
    public void testHasOption() throws Exception
    {
        InvocationArgs args = new InvocationArgs(new String[] { "foo", "--foo", "--bar=baz" });

        assertTrue("option without parameter",                      args.hasOption("--foo"));
        assertTrue("option with parameter specified",               args.hasOption("--bar=baz"));
        assertTrue("option with parameter, not specified",          args.hasOption("--bar"));

        assertFalse("option with parameter, different specified",   args.hasOption("--bar=foo"));
        assertFalse("no match for partial option",                  args.hasOption("--b"));
        assertFalse("shouldn't look at non-option params",          args.hasOption("foo"));
    }


    @Test
    public void testShift() throws Exception
    {
        InvocationArgs args = new InvocationArgs(new String[] { "foo", "--foo", "--bar=baz", "argle" });

        assertEquals("first shift",  "foo", args.shift());
        assertEquals("second shift", "argle", args.shift());
        assertNull("third shift",    args.shift());
    }

}
