package com.kdgregory.pomutil.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;

import com.kdgregory.pomutil.util.PomPaths;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Version updater: identifies POMs that match a specified version and updates
 *  them to a new version.
 */
public class VersionUpdater {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String fromVersion;
    private String toVersion;
    private boolean updateParentRef;
    private boolean updateDependencies;
    private String dependencyGroupId;
    private String dependencyArtifactId;
    List<String> files;


    /**
     *  @param fromVersion          The version to update; anything with a different version is ignored.
     *  @param toVersion            The desired new version.
     *  @param updateParentRef      Flag to indicate that parent references should be updated as well.
     *  @param updateDependencyRef  Flag to indicate that dependency references should be updated as well.
     *  @param dependencyGroupId    The group ID of the dependency to update (ignored if null).
     *  @param dependencyArtifactId The artifact ID of the dependency to update (ignored if null).
     *  @param files                The list of POM files to update.
     */
    public VersionUpdater(
        String fromVersion, String toVersion,
        boolean updateParentRef, boolean updateDependencyRef, String dependencyGroupId, String dependencyArtifactId,
        List<String> files)
    {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.updateParentRef = updateParentRef;
        this.updateDependencies = updateDependencyRef && (dependencyGroupId != null) && (dependencyArtifactId != null);
        this.dependencyGroupId = dependencyGroupId;
        this.dependencyArtifactId = dependencyArtifactId;
        this.files = files;
    }


    public void run()
    throws Exception
    {
        Map<File,PomWrapper> pomFiles = findValidPoms(files);
        for (File file : pomFiles.keySet())
        {
            PomWrapper wrapped = pomFiles.get(file);
            boolean changed = possiblyUpdateVersion(wrapped)
                            | possiblyUpdateParentVersion(wrapped)
                            | possiblyUpdateDependencies(wrapped);
            if (changed)
            {
                FileOutputStream out = new FileOutputStream(file);
                try
                {
                    OutputUtil.compactStream(wrapped.getDom(), out);
                }
                finally
                {
                    IOUtil.closeQuietly(out);
                }
            }
        }
    }


    private Map<File,PomWrapper> findValidPoms(List<String> filenames)
    throws IOException
    {
        Map<File,PomWrapper> result = new HashMap<File,PomWrapper>();
        for (String filename : filenames)
        {
            for (File file : recursivelyLookForPoms(new File(filename)))
            {
                try
                {
                    result.put(file, new PomWrapper(file));
                }
                catch (Exception ex)
                {
                    logger.warn("unable to process file: " + file, ex);
                }
            }
        }
        return result;
    }


    private List<File> recursivelyLookForPoms(File file)
    throws IOException
    {
        List<File> result = new ArrayList<File>();
        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                if (child.isDirectory())
                {
                    result.addAll(recursivelyLookForPoms(child));
                }
                else if (child.getName().equals("pom.xml"))
                {
                    result.add(child);
                }
            }
        }
        else
        {
            // an explicit file can have any name
            result.add(file);
        }

        return result;
    }


    private boolean oldVersionMatches(Element versionElement)
    {
        // this is a bogus file
        if (versionElement == null)
            return false;

        // we update whatever is there
        if (fromVersion == null)
            return true;

        String oldVersion = DomUtil.getText(versionElement).trim();
        return ObjectUtil.equals(fromVersion, oldVersion);
    }


    private void updateVersionElement(Element versionElement)
    {
        if (toVersion != null)
        {
            DomUtil.setText(versionElement, toVersion);
            return;
        }

        String existingVersion = DomUtil.getText(versionElement);
        if (existingVersion.endsWith("-SNAPSHOT"))
        {
            String newVersion = StringUtil.extractLeft(existingVersion, "-SNAPSHOT");
            DomUtil.setText(versionElement, newVersion);
            return;
        }

        String preservedPart = StringUtil.extractLeftOfLast(existingVersion, ".");
        String updatedPart = StringUtil.extractRightOfLast(existingVersion, ".");
        try
        {
            int oldValue = Integer.parseInt(updatedPart);
            String newVersion = preservedPart + "." + (oldValue + 1) + "-SNAPSHOT";
            DomUtil.setText(versionElement, newVersion);
            return;
        }
        catch (NumberFormatException ex)
        {
            logger.error("unable to update version: " + existingVersion);
        }
    }


    private boolean possiblyUpdateVersion(PomWrapper wrapped)
    {
        Element pomVersionElement = wrapped.selectElement(PomPaths.PROJECT_VERSION);
        if (oldVersionMatches(pomVersionElement))
        {
            updateVersionElement(pomVersionElement);
            return true;
        }

        return false;
    }


    private boolean possiblyUpdateParentVersion(PomWrapper wrapped)
    {
        if (! updateParentRef)
            return false;

        Element parentVersionElement = wrapped.selectElement(PomPaths.PARENT_VERSION);
        if (oldVersionMatches(parentVersionElement))
        {
            updateVersionElement(parentVersionElement);
            return true;
        }

        return false;
    }


    private boolean possiblyUpdateDependencies(PomWrapper wrapped)
    {
        if (! updateDependencies)
            return false;

        boolean result = false;

        List<Element> targetDependencies = wrapped.selectDependenciesByGroupAndArtifact(dependencyGroupId, dependencyArtifactId);
        Set<String> targetProperties = new HashSet<String>();
        for (Element dependencyElement : targetDependencies)
        {
            Element dependencyVersionElement = wrapped.selectElement(dependencyElement, "mvn:version");
            String dependencyVersion = DomUtil.getText(dependencyVersionElement).trim();
            if (dependencyVersion.equals(fromVersion))
            {
                updateVersionElement(dependencyVersionElement);
                result = true;
            }
            else if (dependencyVersion.startsWith("${"))
            {
                targetProperties.add(dependencyVersion.substring(2, dependencyVersion.length() - 1));
            }
        }

        return result || possiblyUpdateProperties(wrapped, targetProperties);
    }


    private boolean possiblyUpdateProperties(PomWrapper wrapped, Set<String> targetProperties)
    {
        boolean result = false;

        for (String property : targetProperties)
        {
            if (fromVersion.equals(wrapped.getProperty(property)))
            {
                wrapped.setProperty(property, toVersion);
                result = true;
            }
        }
        return result;
    }
}
