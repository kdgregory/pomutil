// Copyright (c) Keith D Gregory
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

public class TestVersion
{
    @Test
    public void testEqualVersions() throws Exception
    {
        Version v1 = new Version("1.2.3.A1");
        Version v2 = new Version("1.2.3.A1");

        assertTrue("comparison, forward", v1.compareTo(v2) == 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) == 0);
        assertTrue("equality",   v1.equals(v2));
        assertEquals("hashcode", v1.hashCode(), v2.hashCode());
    }


    @Test
    public void testMajorVersion() throws Exception
    {
        Version v1 = new Version("3.2.1");
        Version v2 = new Version("1.2.3");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testMinorVersion() throws Exception
    {
        Version v1 = new Version("1.3.3");
        Version v2 = new Version("1.2.3");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testPatchVersion() throws Exception
    {
        Version v1 = new Version("1.2.4");
        Version v2 = new Version("1.2.3");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testFinalAlphaComponent() throws Exception
    {
        Version v1 = new Version("1.2.3.R2");
        Version v2 = new Version("1.2.3.R1");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testNonFinalAlphaComponent() throws Exception
    {
        // example from "Maven: The Complete Reference"

        Version v1 = new Version("1.2.3-alpha-2");
        Version v2 = new Version("1.2.3-alpha-10");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testSnapshotIsLessThanRelease() throws Exception
    {
        Version v1 = new Version("1.2.3");
        Version v2 = new Version("1.2.3-SNAPSHOT");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }


    @Test
    public void testSnapshotIsGreaterThanPreviousRelease() throws Exception
    {
        Version v1 = new Version("1.2.3-SNAPSHOT");
        Version v2 = new Version("1.2.2");

        assertTrue("comparison, forward", v1.compareTo(v2) > 0);
        assertTrue("comparison, reverse", v2.compareTo(v1) < 0);
        assertFalse("equality", v1.equals(v2));
    }

}
