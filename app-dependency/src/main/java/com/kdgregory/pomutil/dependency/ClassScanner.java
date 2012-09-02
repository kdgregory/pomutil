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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

import net.sf.kdgcommons.io.IOUtil;

import com.kdgregory.bcelx.classfile.ClassfileUtil;


/**
 *  Extracts the referenced classes from a single classfile or directory tree.
 */
public class ClassScanner
{
    private Logger logger = Logger.getLogger(getClass());

    // we use TreeSet because it's easier to examine in a debugger, and the
    // performance difference vs HashSet doesn't matter here

    private Set<String> processedClasses = new TreeSet<String>();
    private Set<String> referencedClasses = new TreeSet<String>();


    /**
     *  Convenience constructor, to read classfile from filesystem. May be
     *  passed a single file or the root of a directory tree (which is processed
     *  recursively).
     */
    public ClassScanner(File file)
    throws IOException
    {
        processTreeOrFile(file);
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns all classes known to this scanner, as fully-qualified classnames.
     */
    public Set<String> getProcessedClasses()
    {
        return processedClasses;
    }


    /**
     *  Returns all referenced classes, as fully-qualified classnames.
     */
    public Set<String> getReferencedClasses()
    {
        return referencedClasses;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processTreeOrFile(File file)
    throws IOException
    {
        String filename = file.getName();
        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                processTreeOrFile(child);
            }
        }
        else if (filename.endsWith(".class"))
        {
            logger.debug("processing " + filename);
            processStream(new FileInputStream(file), filename);
        }
    }


    private void processStream(InputStream in, String name)
    throws IOException
    {
        try
        {
            JavaClass parsedClass = new ClassParser(in, name).parse();
            processedClasses.add(parsedClass.getClassName().replace('$', '.'));
            referencedClasses.addAll(ClassfileUtil.extractReferencedClasses(parsedClass));
        }
        finally
        {
            IOUtil.closeQuietly(in);
        }
    }
}
