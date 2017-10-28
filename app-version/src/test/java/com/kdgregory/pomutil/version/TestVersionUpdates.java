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
    public void testExplicitUpdate() throws Exception
    {
        List<String> poms = createTestPoms("basepom.xml", 2);
        
        String oldVersion = "1.0.0";
        String newVersion = "1.1.0";

        new VersionUpdater("com.example.pomutil.test", "example", oldVersion, newVersion, false, false).run(poms);

        for (String pom : poms)
        {
            PomWrapper check = new PomWrapper(new File(pom));
            assertEquals(newVersion, check.getGAV().version);
        }
    }


    @Test
    public void testOnlyMatchingVersionsUpdated() throws Exception
    {
        List<String> poms = createTestPoms("basepom.xml", 2);
        File pomToUpdate = new File(poms.get(0));
        File pomToIgnore = new File(poms.get(1));
        
        String oldVersion = "1.0.0";
        String newVersion = "1.0.1-SNAPSHOT";
        
        String testVersion = "1.2.3-SNAPSHOT";
        updateVersion(pomToIgnore, PomPaths.PROJECT_VERSION, testVersion);

        new VersionUpdater("com.example.pomutil.test", "example", oldVersion, newVersion, false, false).run(poms);

        PomWrapper check1 = new PomWrapper(pomToUpdate);
        assertEquals("expected updated version", newVersion, check1.getGAV().version);

        PomWrapper check2 = new PomWrapper(pomToIgnore);
        assertEquals("expected original version", testVersion, check2.getGAV().version);
    }


    @Test
    public void testDirectory() throws Exception
    {
        List<String> dirs = createTestPomsInDirectory("basepom.xml", 2);
        
        String oldVersion = "1.0.0";
        String newVersion = "1.1.0";

        new VersionUpdater("com.example.pomutil.test", "example", oldVersion, newVersion, false, false).run(dirs);

        for (String dir : dirs)
        {
            PomWrapper check = new PomWrapper(new File(dir, "pom.xml"));
            assertEquals(newVersion, check.getGAV().version);
        }
    }


    @Test
    public void testChildNoUpdate() throws Exception
    {
        List<String> poms = createTestPoms("childpom.xml", 1);

        String oldVersion = "1.0.0";
        String newVersion = "1.0.1-SNAPSHOT";
        
        new VersionUpdater("com.example.pomutil.test", "example", oldVersion, newVersion, false, false).run(poms);

        PomWrapper check = new PomWrapper(new File(poms.get(0)));
        assertEquals(oldVersion, check.selectValue(PomPaths.PARENT_VERSION));
    }


    @Test
    public void testChildUpdate() throws Exception
    {
        List<String> poms = createTestPoms("childpom.xml", 1);

        String oldVersion = "1.0.0";
        String newVersion = "1.0.1-SNAPSHOT";
        
        new VersionUpdater("com.example.pomutil.test", "example", oldVersion, newVersion, true, false).run(poms);

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

        new VersionUpdater("com.example.pomutil.test", "example", null, null, false, false).run(poms);
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.getGAV().version);
    }


    @Test
    public void testAutomaticVersionDetectionRegulaToSnapshot() throws Exception
    {
        String oldVersion = "1.0.1";
        String newVersion = "1.0.2-SNAPSHOT";

        List<String> poms = createTestPoms("basepom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PROJECT_VERSION, oldVersion);

        new VersionUpdater("com.example.pomutil.test", "example", null, null, false, false).run(poms);
        PomWrapper check = new PomWrapper(pom);
        assertEquals(newVersion, check.getGAV().version);
    }


    @Test
    public void testAutomaticVersionDetectionParentSnapshotToRegular() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("childpom.xml", 1);
        File pom = new File(poms.get(0));
        updateVersion(pom, PomPaths.PARENT_VERSION, oldVersion);

        new VersionUpdater("com.example.pomutil.test", "example", null, null, true, false).run(poms);
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

        new VersionUpdater("com.example.pomutil.test", "example", null, null, true, false).run(poms);
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

        new VersionUpdater("com.example.pomutil.test", "updated-dependency", oldVersion, newVersion, false, true).run(poms);

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

        new VersionUpdater("com.example.pomutil.test", "updated-dependency", oldVersion, newVersion, false, true).run(poms);

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
    public void testUpdateSharedPropertyDependencies() throws Exception
    {
        String oldVersion = "1.0.1-SNAPSHOT";
        String newVersion = "1.0.1";

        List<String> poms = createTestPoms("sharedPropertyDependencyPom.xml", 1);
        File pom = new File(poms.get(0));

        new VersionUpdater("com.example.pomutil.test", "updated-dependency", oldVersion, newVersion, false, true).run(poms);

        PomWrapper check = new PomWrapper(pom);
        assertEquals("property was not updated", oldVersion, check.getProperty("expectNoUpdate.version"));
    }


    @Test
    public void testBogusFile() throws Exception
    {
        File file = IOUtil.createTempFile(new ByteArrayInputStream("test".getBytes()),
                                          getClass().getName());

        // we'll verify properties of the file to make sure it wasn't written
        long originalModificationTime = file.lastModified();
        long originalSize = file.length();

        new VersionUpdater("com.example.pomutil.test", "example", "0.1", "1.0", true, false).run(Arrays.asList(file.getAbsolutePath()));

        assertEquals("modification time unchanged", originalModificationTime, file.lastModified());
        assertEquals("size unchanged",              originalSize,             file.length());
    }
}
