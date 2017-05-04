package com.kdgregory.pomutil.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;

import com.kdgregory.pomutil.util.PomPaths;
import com.kdgregory.pomutil.util.PomWrapper;


public class TestVersionUpdates
{
    private static String TESTPOM_VERSION = "1.0.0";


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
        List<String> poms = createTestPoms("basepom.xml", 2);

        String newVersion = "1.1.0";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, poms).run();

        for (String pom : poms)
        {
            PomWrapper wrapped = new PomWrapper(new File(pom));
            assertEquals(newVersion, wrapped.getGAV().getVersion());
        }
    }


    @Test
    public void testOnlyMatchingVersionsUpdated() throws Exception
    {
        List<String> poms = createTestPoms("basepom.xml", 2);
        File pomToUpdate = new File(poms.get(0));
        File pomToIgnore = new File(poms.get(1));

        String testVersion = "1.2.3-SNAPSHOT";
        PomWrapper updateWrapper = new PomWrapper(pomToIgnore);
        DomUtil.setText(updateWrapper.selectElement(PomPaths.PROJECT_VERSION), testVersion);
        FileOutputStream out = new FileOutputStream(pomToIgnore);
        OutputUtil.compactStream(updateWrapper.getDom(), out);
        out.close();

        String newVersion = "1.0.1-SNAPSHOT";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, poms).run();

        PomWrapper check1 = new PomWrapper(pomToUpdate);
        assertEquals("expected updated version", newVersion, check1.getGAV().getVersion());

        PomWrapper check2 = new PomWrapper(pomToIgnore);
        assertEquals("expected original version", testVersion, check2.getGAV().getVersion());
    }


}
