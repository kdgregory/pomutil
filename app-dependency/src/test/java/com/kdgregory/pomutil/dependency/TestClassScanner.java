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

package com.kdgregory.pomutil.dependency;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import static org.junit.Assert.*;


//----------------------------------------------------------------------------
//  To get full coverage of referenced annotations, we need to apply them at
//  class, method, parameter, and field level. The method annotation is easy:
//  we're already marking the methods for JUnit. The class annotation is a
//  random annotation that should be available (JDK 1.6 or above). For field
//  and parameter annotations, create our own below (along with a method that
//  uses them)
//----------------------------------------------------------------------------
@Resource
public class TestClassScanner
{
//----------------------------------------------------------------------------
//  Test data
//----------------------------------------------------------------------------

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.CLASS)
    public @interface MyFieldAnnotation
    {
        // nothing here
    }


    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyParamAnnotation
    {
        // nothing here either
    }

    @MyFieldAnnotation
    private String foo;

    public String doSomethingWithFoo(@MyParamAnnotation String bar)
    {
        return foo + bar;
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testSingleFile() throws Exception
    {
        // assumes a standard Maven layout; will break if class is repackaged
        File file = new File("target/test-classes/com/kdgregory/pomutil/dependency/TestClassScanner.class");
        assertTrue("verify classfile exists", file.exists());

        ClassScanner scanner = new ClassScanner(file);

        Set<String> processedClasses = scanner.getProcessedClasses();
        Set<String> referencedClasses = scanner.getReferencedClasses();

        assertEquals("#/classes processed",      1, processedClasses.size());
        assertTrue("processed class",            processedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner"));

        assertTrue("normal class",               referencedClasses.contains("java.io.File"));
        assertTrue("class-level annotation",     referencedClasses.contains("javax.annotation.Resource"));
        assertTrue("method-level annotation",    referencedClasses.contains("org.junit.Test"));
        assertTrue("parameter-level annotation", referencedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner.MyParamAnnotation"));
        assertTrue("field-level annotation",     referencedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner.MyFieldAnnotation"));
    }


    @Test
    public void testDirectoryTree() throws Exception
    {
        File file = new File("target/classes");
        assertTrue("verify target directory exists", file.exists());

        ClassScanner scanner = new ClassScanner(file);

        Set<String> processedClasses = scanner.getProcessedClasses();
        Set<String> referencedClasses = scanner.getReferencedClasses();

        assertTrue("#/classes processed",               processedClasses.size() > 1);
        assertTrue("processed class",                   processedClasses.contains("com.kdgregory.pomutil.dependency.ClassScanner"));
        assertTrue("known referenced class found",      referencedClasses.contains("java.io.File"));

        assertFalse("did not process test-only class",  processedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner"));
        assertFalse("test-only reference",              referencedClasses.contains("org.junit.Assert"));
    }


    @Test
    public void testInnerClassesProcessed() throws Exception
    {
        File file = new File("target/test-classes");
        assertTrue("verify target directory exists", file.exists());

        ClassScanner scanner = new ClassScanner(file);

        Set<String> processedClasses = scanner.getProcessedClasses();
        Set<String> referencedClasses = scanner.getReferencedClasses();

        assertTrue("processed class",   processedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner.MyFieldAnnotation"));
        assertTrue("referenced class",  referencedClasses.contains("com.kdgregory.pomutil.dependency.TestClassScanner.MyFieldAnnotation"));
    }
}
