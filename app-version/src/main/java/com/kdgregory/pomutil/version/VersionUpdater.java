package com.kdgregory.pomutil.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.io.IOUtil;
import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.OutputUtil;

import com.kdgregory.pomutil.util.PomPaths;
import com.kdgregory.pomutil.util.PomWrapper;


/**
 *  Version updater: identifies POMs that match a specified version and updates
 *  them to a new version.
 */
public class VersionUpdater {

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
        List<File> pomFiles = findValidPoms(files);
        for (File file : pomFiles)
        {
            PomWrapper wrapped = new PomWrapper(file);
            boolean changed = possiblyUpdateVersion(wrapped);
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


    private List<File> findValidPoms(List<String> filenames)
    throws IOException
    {
        List<File> result = new ArrayList(filenames.size());
        for (String filename : filenames)
        {
            File file = new File(filename);
            if (file.isDirectory())
            {
                List<String> subdirs = new ArrayList<String>();
                for (String child : file.list())
                {
                    File childFile = new File(file, child);
                    if (childFile.isDirectory())
                    {
                        subdirs.add(child);
                    }
                    if (childFile.getName().equals("pom.xml"))
                    {
                        result.add(childFile);
                    }
                }
                result.addAll(findValidPoms(subdirs));
            }
            else
            {
                result.add(file);
            }
        }
        // TODO - open files, verify that they're POMS, remove those that aren't
        return result;
    }


    private boolean possiblyUpdateVersion(PomWrapper wrapped)
    {
        Element pomVersionElement = wrapped.selectElement(PomPaths.PROJECT_VERSION);
        if (ObjectUtil.equals(fromVersion, DomUtil.getText(pomVersionElement)))
        {
            pomVersionElement.setTextContent(toVersion);
            return true;
        }
        return false;
    }

}
