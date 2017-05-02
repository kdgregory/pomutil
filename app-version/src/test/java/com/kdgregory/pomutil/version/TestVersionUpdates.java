package com.kdgregory.pomutil.version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;

import com.kdgregory.pomutil.util.PomWrapper;


public class TestVersionUpdates
{
    private List<String> createTestPoms(String resourceName, int numCopies)
    throws IOException
    {
        List<String> result = new ArrayList<String>(numCopies);
        for (int ii = 0 ; ii < numCopies ; ii++)
        {
            File file = IOUtil.createTempFile(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName),
                    getClass().getName() + "-");
            result.add(file.getCanonicalPath());
        }
        return result;
    }


    @Test
    public void testBasicOperation() throws Exception
    {
        String oldVersion = "1.0.0";
        String newVersion = "1.1.0";

        List<String> poms = createTestPoms("basepom.xml", 2);
        new VersionUpdater(oldVersion, newVersion, false, poms).run();

        for (String pom : poms)
        {
            PomWrapper wrapped = new PomWrapper(new File(pom));
            assertEquals(newVersion, wrapped.getGAV().getVersion());
        }
    }

}
