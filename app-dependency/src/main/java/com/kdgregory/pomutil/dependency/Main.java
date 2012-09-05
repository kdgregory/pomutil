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
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.lang.ObjectUtil;
import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pomutil.util.Artifact;
import com.kdgregory.pomutil.util.Artifact.Scope;
import com.kdgregory.pomutil.util.InvocationArgs;
import com.kdgregory.pomutil.util.Utils;


/**
 *  Examines the current project or a specified directory, to compare the actual
 *  dependencies (referenced classes) against those specified in the POM.
 */
public class Main
{
    public static void main(String[] argv)
    throws Exception
    {
        InvocationArgs args = new InvocationArgs(argv);
        Main checker = new Main(args).run();
        new Reporter(args, checker).output(System.out);
    }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private Logger logger = Logger.getLogger(getClass());

    private File cwd;
    private DependencyScanner dependencyScanner;
    private Collection<String> ignoredDependencies;
    private boolean reportUnusedRuntimeDependences;

    private SortedSet<String> mainlineReferencedClasses = new TreeSet<String>();
    private SortedSet<String> testReferencedClasses = new TreeSet<String>();

    private SortedSet<String> mainlineUnsupportedClasses = new TreeSet<String>();
    private SortedSet<String> testUnsupportedClasses = new TreeSet<String>();

    private SortedSet<String> mainlineUnsupportedPackages = new TreeSet<String>();
    private SortedSet<String> testUnsupportedPackages = new TreeSet<String>();

    private SortedSet<Artifact> mainlineReferencedDependencies = new TreeSet<Artifact>();
    private SortedSet<Artifact> testReferencedDependencies = new TreeSet<Artifact>();

    private SortedSet<Artifact> mainlineUnusedDependencies = new TreeSet<Artifact>();
    private SortedSet<Artifact> mainlineIncorrectDependencies = new TreeSet<Artifact>();
    private SortedSet<Artifact> testUnusedDependencies = new TreeSet<Artifact>();


    /**
     *  Standard constructor, which processes invocation arguments.
     */
    public Main(InvocationArgs args)
    {
        ignoredDependencies = args.getOptionValues(Options.IGNORE_UNUSED_DPCY);
        reportUnusedRuntimeDependences = args.hasOption(Options.REPORT_UNUSED_RUNTIME_DPCY);
        String projectDir = args.getOptionValue(Options.TARGET_DIRECTORY);
        cwd = StringUtil.isBlank(projectDir)
            ? new File(System.getProperty("user.dir"))
            : new File(projectDir);
    }


    /**
     *  Convenience constructor, for testing without invocation arguments.
     */
    public Main()
    {
        this(new InvocationArgs());
    }


//----------------------------------------------------------------------------
//  Operational methods
//----------------------------------------------------------------------------

    /**
     *  Executes the dependency check, compiling information but not writing
     *  output. Returns itself as a convenience for chained calls.
     */
    public Main run()
    throws IOException
    {
        dependencyScanner = new DependencyScanner(new File(cwd, "pom.xml"));

        selectReferencedClasses();
        removeJDKClasses();

        findReferencedDependencies(mainlineReferencedClasses, mainlineReferencedDependencies, mainlineUnsupportedClasses,
                                  Scope.COMPILE, Scope.SYSTEM, Scope.PROVIDED);
        findReferencedDependencies(testReferencedClasses, testReferencedDependencies, testUnsupportedClasses,
                                  Scope.COMPILE, Scope.SYSTEM, Scope.PROVIDED, Scope.TEST);

        findUnusedDependencies(dependencyScanner.getDependencies(Scope.COMPILE, Scope.SYSTEM, Scope.RUNTIME, Scope.PROVIDED),
                                    mainlineReferencedDependencies,
                                    mainlineUnusedDependencies);

        findUnusedDependencies(dependencyScanner.getDependencies(Scope.TEST),
                                    CollectionUtil.combine(new TreeSet<Artifact>(), mainlineReferencedDependencies, testReferencedDependencies),
                                    testUnusedDependencies);

        moveImproperlyScopedDependencies();
        removeRuntimeScopedUnusedDependencies();
        removeIgnoredUnusedDependencies(mainlineUnusedDependencies, testUnusedDependencies);
        convertUnsupportedClassesToPackages(mainlineUnsupportedClasses, mainlineUnsupportedPackages);
        convertUnsupportedClassesToPackages(testUnsupportedClasses, testUnsupportedPackages);

        return this;
    }


//----------------------------------------------------------------------------
//  Methods to return the results
//----------------------------------------------------------------------------

    /**
     *  Returns the names of classes referenced by mainline code but not found in
     *  any direct dependency.
     */
    public SortedSet<String> getUnsupportedMainlineClasses()
    {
        return mainlineUnsupportedClasses;
    }


    /**
     *  Returns the names of classes referenced by test code but not found in any
     *  direct dependency.
     */
    public SortedSet<String> getUnsupportedTestClasses()
    {
        return testUnsupportedClasses;
    }


    /**
     *  Returns the names of packages referenced by mainline code but not found in
     *  any direct dependency.
     */
    public SortedSet<String> getUnsupportedMainlinePackages()
    {
        return mainlineUnsupportedPackages;
    }


    /**
     *  Returns the names of packages referenced by test code but not found in any
     *  direct dependency.
     */
    public SortedSet<String> getUnsupportedTestPackages()
    {
        return testUnsupportedPackages;
    }


    /**
     *  Returns dependencies in mainline scope that are not referenced anywhere.
     *  Note that this does not overlap with {@link #getIncorrectMainlineDependencies}.
     */
    public SortedSet<Artifact> getUnusedMainlineDependencies()
    {
        return mainlineUnusedDependencies;
    }


    /**
     *  Returns dependencies in mainline scope that are only referenced by test code.
     */
    public SortedSet<Artifact> getIncorrectMainlineDependencies()
    {
        return mainlineIncorrectDependencies;
    }


    /**
     *  Returns the dependencies in test scope that are not referenced by test code.
     */
    public SortedSet<Artifact> getUnusedTestDependencies()
    {
        return testUnusedDependencies;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void selectReferencedClasses()
    throws IOException
    {
        ClassScanner mainlineScanner = new ClassScanner(new File(cwd, "target/classes"));
        mainlineReferencedClasses.addAll(mainlineScanner.getReferencedClasses());

        ClassScanner testScanner = new ClassScanner(new File(cwd, "target/test-classes"));
        testReferencedClasses.addAll(testScanner.getReferencedClasses());

        Set<String> projectClasses = new TreeSet<String>();
        projectClasses.addAll(mainlineScanner.getProcessedClasses());
        projectClasses.addAll(testScanner.getProcessedClasses());

        mainlineReferencedClasses.removeAll(projectClasses);
        testReferencedClasses.removeAll(projectClasses);
    }


    private void removeJDKClasses()
    throws IOException
    {
        File rtJarFile = new File(System.getProperty("java.home"), "lib/rt.jar");
        if (!rtJarFile.exists())
        {
            logger.warn("unable to find rt.jar; results will have Java classes");
            return;
        }

        for (String className : Utils.extractClassesFromJar(rtJarFile))
        {
            mainlineReferencedClasses.remove(className);
            testReferencedClasses.remove(className);
        }
    }


    private void findReferencedDependencies(
            Set<String> referencedClasses,
            Set<Artifact> referencedDependencies,
            Set<String> unsupportedClasses,
            Scope... allowedScopes)
    {
        Iterator<String> classItx = referencedClasses.iterator();
        while (classItx.hasNext())
        {
            String classname = classItx.next();

            Artifact artifact = null;
            for (Scope scope : allowedScopes)
            {
                artifact = dependencyScanner.getDependency(classname, scope);
                if (artifact != null)
                    break;
            }

            if (artifact == null)
            {
                unsupportedClasses.add(classname);
            }
            else
            {
                referencedDependencies.add(artifact);
            }
        }
    }


    private void findUnusedDependencies(
            Collection<Artifact> allDependenciesInScope,
            Set<Artifact> dependenciesToRemove,
            Set<Artifact> result)
    {
        for (Artifact artifact : allDependenciesInScope)
        {
            if (! dependenciesToRemove.contains(artifact))
            {
                result.add(artifact);
            }
        }
    }


    private void moveImproperlyScopedDependencies()
    {
        Iterator<Artifact> itx = mainlineUnusedDependencies.iterator();
        while (itx.hasNext())
        {
            Artifact artifact = itx.next();
            if (testReferencedDependencies.contains(artifact))
            {
                mainlineIncorrectDependencies.add(artifact);
                itx.remove();
            }
        }
    }


    private void removeRuntimeScopedUnusedDependencies()
    {
        if (reportUnusedRuntimeDependences)
            return;

        Iterator<Artifact> itx = mainlineUnusedDependencies.iterator();
        while (itx.hasNext())
        {
            Artifact artifact = itx.next();
            if (artifact.scope == Scope.RUNTIME)
                itx.remove();
        }
    }


    private void removeIgnoredUnusedDependencies(SortedSet<Artifact>... dependencyLists)
    {
        for (String ignoredDependency : ignoredDependencies)
        {
            String groupId = StringUtil.extractLeft(ignoredDependency, ":");
            String artifactId = StringUtil.extractRight(ignoredDependency, ":");
            for (SortedSet<Artifact> dependencyList : dependencyLists)
            {
                Iterator<Artifact> itx = dependencyList.iterator();
                while (itx.hasNext())
                {
                    Artifact dependency = itx.next();
                    if (!ObjectUtil.equals(groupId, dependency.groupId))
                        continue;
                    if (!StringUtil.isBlank(artifactId) && !ObjectUtil.equals(artifactId, dependency.artifactId))
                        continue;
                    itx.remove();
                }
            }
        }
    }


    private void convertUnsupportedClassesToPackages(SortedSet<String> classes, SortedSet<String> packages)
    {
        for (String cls : classes)
        {
            String pkg = StringUtil.extractLeftOfLast(cls, ".");
            packages.add(pkg);
        }
    }
}
