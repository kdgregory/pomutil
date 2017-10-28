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
 *  Version updater: identifies POMs or dependencies that match a
 *  specified version and updates them to a new version.
 */
public class VersionUpdater {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String groupId;
    private String artifactId;
    private String fromVersion;
    private String toVersion;
    private boolean autoVersion;
    private boolean updateParent;
    private boolean updateDependencies;


    /**
     * @param groupId               If not-null, updates are restricted to POMs/dependencies that
     *                              have a matching group ID.
     * @param artifactId            If not-null, updates are restricted to POMs/dependencies that
     *                              have a matching artifact ID.
     * @param fromVersion           If not-null, updates are restricted to POMs/dependencies that
     *                              have a matching version ID.
     * @param toVersion             The desired new version.
     * @param autoVersion           Flag to indicate that versions should be automatically updated.
     * @param updateParent          Flag to indicate that parent references should be updated.
     * @param updateDependencies    Flag to indicate that dependency references should be updated.
     */
    public VersionUpdater(
        String groupId, String artifactId, String fromVersion, String toVersion,
        boolean autoVersion, boolean updateParent, boolean updateDependencies)
    {
        // these checks simplify logic further down
        if (StringUtil.isEmpty(groupId))
            throw new IllegalArgumentException("groupId must be specified");
        if ((fromVersion == null) && ! autoVersion)
            throw new IllegalArgumentException("fromVersion must be specified if autoVersion not true");
        if ((toVersion == null) && ! autoVersion)
            throw new IllegalArgumentException("fromVersion must be specified if autoVersion not true");

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.autoVersion = autoVersion;
        this.updateParent = updateParent;
        this.updateDependencies = updateDependencies;
    }


    public void run(List<String> filenames)
    throws Exception
    {
        Map<File,PomWrapper> pomFiles = findValidPoms(filenames);
        for (File file : pomFiles.keySet())
        {
            PomWrapper wrapped = pomFiles.get(file);
            boolean changed = possiblyUpdateProjectVersion(wrapped)
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


    private boolean groupAndArtifactMatches(Element reference)
    {
        Element groupElement = DomUtil.getChild(reference, "groupId");
        if (groupElement == null)
            return false;
        if (! groupId.equals(DomUtil.getText(groupElement)))
            return false;

        if (artifactId == null)
            return true;

        Element artifactElement = DomUtil.getChild(reference, "artifactId");
        if (artifactElement == null)
            return false;
        if (! artifactId.equals(DomUtil.getText(artifactElement)))
            return false;

        return true;
    }


    private boolean oldVersionMatches(Element container)
    {
        // this is a bogus file
        if (container == null)
            return false;

        Element versionElement = DomUtil.getChild(container, "version");
        if (versionElement == null)
            return false;

        // auto-version doesn't need a from-version
        if (fromVersion == null)
            return autoVersion;

        String oldVersion = DomUtil.getText(versionElement).trim();
        return ObjectUtil.equals(fromVersion, oldVersion);
    }


    private void updateVersionElement(Element container)
    {
        Element versionElement = DomUtil.getChild(container, "version");

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


    private boolean possiblyUpdateProjectVersion(PomWrapper wrapped)
    {
        Element projectElement = wrapped.selectElement(PomPaths.PROJECT);
        if (groupAndArtifactMatches(projectElement) && oldVersionMatches(projectElement))
        {
            updateVersionElement(projectElement);
            return true;
        }
        else
        {
            return false;
        }
    }


    private boolean possiblyUpdateParentVersion(PomWrapper wrapped)
    {
        if (! updateParent)
            return false;

        Element parentElement = wrapped.selectElement(PomPaths.PARENT);
        if (groupAndArtifactMatches(parentElement) && oldVersionMatches(parentElement))
        {
            updateVersionElement(parentElement);
            return true;
        }
        else
        {
            return false;
        }
    }


    private boolean possiblyUpdateDependencies(PomWrapper wrapped)
    {
        if (! updateDependencies)
            return false;

        boolean result = false;

        Set<Element> targetDependencies = new HashSet<Element>(wrapped.selectDependenciesByGroupAndArtifact(groupId, artifactId));
        Set<String> targetProperties = new HashSet<String>();
        for (Element dependencyElement : targetDependencies)
        {
            if (groupAndArtifactMatches(dependencyElement) && oldVersionMatches(dependencyElement))
            {
                updateVersionElement(dependencyElement);
                result = true;
            }
            else
            {
                String dependencyVersion = wrapped.selectValue(dependencyElement, "mvn:version").trim();
                if (dependencyVersion.startsWith("${"))
                {
                    String propertyName = dependencyVersion.substring(2, dependencyVersion.length() - 1);
                    targetProperties.add(propertyName);
                }
            }
        }

        return result || possiblyUpdateProperties(wrapped, targetDependencies, targetProperties);
    }


    private boolean possiblyUpdateProperties(PomWrapper wrapped, Set<Element> targetDependencies, Set<String> targetProperties)
    {
        boolean result = false;

        for (String property : targetProperties)
        {
            if (! fromVersion.equals(wrapped.getProperty(property)))
            {
                continue;
            }

            Set<Element> elementsWithProperty = new HashSet<Element>();
            elementsWithProperty.addAll(wrapped.selectElements(PomPaths.PROJECT_DEPENDENCIES + "[mvn:version='${" + property + "}']"));
            elementsWithProperty.addAll(wrapped.selectElements(PomPaths.MANAGED_DEPENDENCIES + "[mvn:version='${" + property + "}']"));

            if (! elementsWithProperty.equals(targetDependencies))
            {
                continue;
            }

            wrapped.setProperty(property, toVersion);
            result = true;
        }
        return result;
    }
}
