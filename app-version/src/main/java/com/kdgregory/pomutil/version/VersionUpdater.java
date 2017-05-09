package com.kdgregory.pomutil.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.kdgcommons.collections.CollectionUtil;
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
    List<String> files;


    /**
     *  Constructor for command-line invocation.
     */
    public VersionUpdater(CommandLine commandLine)
    {
        this(
            CollectionUtil.first(commandLine.getOptionValues(CommandLine.Options.OLD_VERSION)),
            CollectionUtil.first(commandLine.getOptionValues(CommandLine.Options.NEW_VERSION)),
            commandLine.isOptionEnabled(CommandLine.Options.UPDATE_PARENT),
            commandLine.getParameters());
    }


    /**
     *  Constructor for programmatic invocation.
     */
    public VersionUpdater(String fromVersion, String toVersion, boolean updateParentRef, List<String> files)
    {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.updateParentRef = updateParentRef;
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
                            | possiblyUpdateParentVersion(wrapped);
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

        String oldVersion = DomUtil.getText(versionElement);
        return ObjectUtil.equals(fromVersion, oldVersion);
    }


    private void updateVersion(Element versionElement)
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
            updateVersion(pomVersionElement);
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
            updateVersion(parentVersionElement);
            return true;
        }

        return false;
    }
}
