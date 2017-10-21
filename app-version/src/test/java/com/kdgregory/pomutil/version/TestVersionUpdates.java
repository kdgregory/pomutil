package com.kdgregory.pomutil.version;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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


    private List<String> createTestPomsInDirectory(String resourceName, int numCopies)
    throws IOException
    {
        List<String> result = new ArrayList<String>(numCopies);
        File systemTmpdir = new File(System.getProperty("java.io.tmpdir"));
        for (int ii = 0 ; ii < numCopies ; ii++)
        {
            // this is a hack but should work almost all of the time
            String testDirName = getClass().getName() + "-" + System.currentTimeMillis() + "-" + ii;
            File testDir = new File(systemTmpdir, testDirName);
            testDir.mkdir();
            testDir.deleteOnExit();
            File testFile = new File(testDir, "pom.xml");
            testFile.deleteOnExit();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            FileOutputStream out = new FileOutputStream(testFile);
            IOUtil.copy(in, out);
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(out);
            result.add(testDir.getAbsolutePath());
        }
        return result;
    }


    private void updateVersion(File file, String path, String newVersion)
    throws Exception
    {
        PomWrapper updateWrapper = new PomWrapper(file);
        DomUtil.setText(updateWrapper.selectElement(path), newVersion);
        FileOutputStream out = new FileOutputStream(file);
        OutputUtil.compactStream(updateWrapper.getDom(), out);
        out.close();

    }


    @Test
    public void testBasicOperation() throws Exception
    {
        List<String> poms = createTestPoms("basepom.xml", 2);

        String newVersion = "1.1.0";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, false, null, null, poms).run();

        for (String pom : poms)
        {
            PomWrapper check = new PomWrapper(new File(pom));
            assertEquals(newVersion, check.getGAV().getVersion());
        }
    }


    @Test
    public void testOnlyMatchingVersionsUpdated() throws Exception
    {
        List<String> poms = createTestPoms("basepom.xml", 2);
        File pomToUpdate = new File(poms.get(0));
        File pomToIgnore = new File(poms.get(1));

        String testVersion = "1.2.3-SNAPSHOT";
        updateVersion(pomToIgnore, PomPaths.PROJECT_VERSION, testVersion);

        String newVersion = "1.0.1-SNAPSHOT";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, false, null, null, poms).run();

        PomWrapper check1 = new PomWrapper(pomToUpdate);
        assertEquals("expected updated version", newVersion, check1.getGAV().getVersion());

        PomWrapper check2 = new PomWrapper(pomToIgnore);
        assertEquals("expected original version", testVersion, check2.getGAV().getVersion());
    }


    @Test
    public void testDirectory() throws Exception
    {
        List<String> dirs = createTestPomsInDirectory("basepom.xml", 2);

        String newVersion = "1.1.0";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, false, null, null, dirs).run();

        for (String dir : dirs)
        {
            PomWrapper check = new PomWrapper(new File(dir, "pom.xml"));
            assertEquals(newVersion, check.getGAV().getVersion());
        }
    }


    @Test
    public void testChildNoUpdate() throws Exception
    {
        List<String> poms = createTestPoms("childpom.xml", 1);

        String newVersion = "1.0.1-SNAPSHOT";
        new VersionUpdater(TESTPOM_VERSION, newVersion, false, false, null, null, poms).run();

        PomWrapper check = new PomWrapper(new File(poms.get(0)));
        assertEquals(TESTPOM_VERSION, check.selectValue(PomPaths.PARENT_VERSION));
    }


    @Test
    public void testChildUpdate() throws Exception
    {
        List<String> poms = createTestPoms("childpom.xml", 1);

        String newVersion = "1.0.1-SNAPSHOT";
        new VersionUpdater(TESTPOM_VERSION, newVersion, true, false, null, null, poms).run();

        PomWrapper check = new PomWrapper(new File(poms.get(0)));
        assertEquals(newVersion, check.selectValue(PomPaths.PARENT_VERSION));
    }


    @Test
    public void testAutomaticVersionDetectionSnapshotToRegular() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("basepom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PROJECT_VERSION, oldVersion);

        new VersionUpdater(null, null, false, false, null, null, poms).run();
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.getGAV().getVersion());
    }


    @Test
    public void testAutomaticVersionDetectionRegulaToSnapshot() throws Exception
    {
        String oldVersion = "1.0.1";
        String newVersion = "1.0.2-SNAPSHOT";

        List<String> poms = createTestPoms("basepom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PROJECT_VERSION, oldVersion);

        new VersionUpdater(null, null, false, false, null, null, poms).run();
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.getGAV().getVersion());
    }


    @Test
    public void testAutomaticVersionDetectionParentSnapshotToRegular() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("childpom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PARENT_VERSION, oldVersion);

        new VersionUpdater(null, null, true, false, null, null, poms).run();
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.selectValue(PomPaths.PARENT_VERSION));
    }


    @Test
    public void testAutomaticVersionDetectionParentRegularToSnapshot() throws Exception
    {
        String oldVersion = "1.0.1";
        String newVersion = "1.0.2-SNAPSHOT";

        List<String> poms = createTestPoms("childpom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PARENT_VERSION, oldVersion);

        new VersionUpdater(null, null, true, false, null, null, poms).run();
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.selectValue(PomPaths.PARENT_VERSION));
    }


    @Test
    public void testUpdateExplicitDependencies() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("explicitDependencyPom.xml", 1);
        File pom = new File(poms.get(0));

        new VersionUpdater(oldVersion, newVersion, false, true, "com.example.pomutil.test", "updated-dependency", poms).run();

        PomWrapper check = new PomWrapper(pom);
        assertEquals("updated dependency version",          newVersion, check.selectValue(PomPaths.PROJECT_DEPENDENCIES + "[mvn:artifactId='updated-dependency']/mvn:version"));
        assertEquals("nonupdated dependency version",       oldVersion, check.selectValue(PomPaths.PROJECT_DEPENDENCIES + "[mvn:artifactId='non-updated-dependency']/mvn:version"));
        assertEquals("updated dependency mgmt version",     newVersion, check.selectValue(PomPaths.MANAGED_DEPENDENCIES + "[mvn:artifactId='updated-dependency']/mvn:version"));
        assertEquals("non-updated dependency mgmt version", oldVersion, check.selectValue(PomPaths.MANAGED_DEPENDENCIES + "[mvn:artifactId='non-updated-dependency']/mvn:version"));
    }


    @Test
    public void testUpdatePropertyDependencies() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("propertyDependencyPom.xml", 1);
        File pom = new File(poms.get(0));

        new VersionUpdater(oldVersion, newVersion, false, true, "com.example.pomutil.test", "updated-dependency", poms).run();

        PomWrapper check = new PomWrapper(pom);
        assertEquals("updated dependency version",      newVersion, check.getProperty("expectUpdate.version"));
        assertEquals("non-updated dependency version",  oldVersion, check.getProperty("expectNoUpdate.version"));
        assertTrue("version still uses property, updated dependency",
                   check.selectValue(PomPaths.PROJECT_DEPENDENCIES + "[mvn:artifactId='updated-dependency']/mvn:version").startsWith("${"));
        assertTrue("version still uses property, non-updated dependency",
                   check.selectValue(PomPaths.PROJECT_DEPENDENCIES + "[mvn:artifactId='non-updated-dependency']/mvn:version").startsWith("${"));
        assertTrue("version still uses property, updated dependency mgmt",
                   check.selectValue(PomPaths.MANAGED_DEPENDENCIES + "[mvn:artifactId='updated-dependency']/mvn:version").startsWith("${"));
        assertTrue("version still uses property, nonupdated dependency mgmt",
                   check.selectValue(PomPaths.MANAGED_DEPENDENCIES + "[mvn:artifactId='non-updated-dependency']/mvn:version").startsWith("${"));
    }


    @Test
    public void testBogusFile() throws Exception
    {
        File file = IOUtil.createTempFile(new ByteArrayInputStream("test".getBytes()),
                                          getClass().getName());

        // we'll verify properties of the file to make sure it wasn't written
        long originalModificationTime = file.lastModified();
        long originalSize = file.length();

        new VersionUpdater(TESTPOM_VERSION, "1.0", true, false, null, null, Arrays.asList(file.getAbsolutePath())).run();

        assertEquals("modification time unchanged", originalModificationTime, file.lastModified());
        assertEquals("size unchanged",              originalSize,             file.length());
    }
}
