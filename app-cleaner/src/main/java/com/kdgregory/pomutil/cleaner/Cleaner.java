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

package com.kdgregory.pomutil.cleaner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.ParseUtil;

import com.kdgregory.pomutil.cleaner.transform.InsertCommonProperties;
import com.kdgregory.pomutil.cleaner.transform.NormalizeDependencies;
import com.kdgregory.pomutil.cleaner.transform.ReplaceExplicitVersionsWithProperties;
import com.kdgregory.pomutil.cleaner.transform.SortDependencies;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  The cleaner, responsible for applying the desired set of transformations
 *  to the input POM.
 */
public class Cleaner
{
    Logger logger = LoggerFactory.getLogger(getClass());

    private CommandLine args;

    public Cleaner(CommandLine args)
    {
        this.args = args;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Invokes the selected transformations on the specified list of files.
     */
    public void run(List<File> files)
    throws Exception
    {
        for (File file : files)
        {
            logger.info("processing: " + file.getPath());
            PomWrapper pom = openFile(file);  
            if (pom != null)
            {
                applyTransformations(pom);
                writeOutput(pom, file);
            }
        }
    }
    
    
    /**
     *  Invokes the selected transformations on an input stream, writing the
     *  output to the provided output stream. This exists for the web cleaner.
     */
    public void run(InputStream in, OutputStream out)
    throws Exception
    {
        PomWrapper pom = new PomWrapper(ParseUtil.parse(in));
        applyTransformations(pom);
        new OutputHandler(args, out).writeOutput(pom.getDom());
    }
    
    
//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------
    
    private PomWrapper openFile(File file)
    {
        try
        {
            return new PomWrapper(file);
        }
        catch (Exception ex)
        {
            logger.warn("unable to parse file: " + file);
            return null;
        }
    }
    
    private void applyTransformations(PomWrapper pom)
    throws Exception
    {
        new InsertCommonProperties(pom, args).transform();
        new NormalizeDependencies(pom, args).transform();
        new SortDependencies(pom, args).transform();
        new ReplaceExplicitVersionsWithProperties(pom, args).transform();  
    }
    
    
    private void writeOutput(PomWrapper pom, File file)
    throws Exception
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            new OutputHandler(args, out).writeOutput(pom.getDom());
        }
        catch (Exception ex)
        {
            logger.error("failed to successfully write: " + file);
        }
        finally
        {
            IOUtil.closeQuietly(out);
        }
    }
}
