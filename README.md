pomutil
=======

Utilities to clean, organize, and restructure Maven POMs. 

* `Cleaner [OPTIONS] [ INFILE [ OUTFILE ]]`

These generally behave as UNIX filters, reading StdIn or a file, writing StdOut or another file, and reporting any problems to StdErr. The target JAR contains all dependencies.

Cleaner
-------

Cleaner takes a single POM and cleans it up according to your options. This is the default app (you can type `java -jar target/pomutil.jar` and it runs Cleaner). 

As with modern Unix utilities, options are signalled by a double-dash. Each option has a default value; you must explicitly turn off options that you don't want.
Options are executed in the order listed here: the result of one option may be transformed by a subsequent option.

*   `--versionprops` (default)
    `--noversionprops`

    Finds all explicit dependency versions and converts them into properties of the form `GROUPID.version`, where `GROUPID` is the artifact's group ID.
    multiple artifacts have the same group ID, the second (and subsequent) artifacts are named `GROUPID.ARTIFACTID.version`, and a warning is written 
    the log.
    
    The original dependencies are updated to use the new properties, and the properties are added into the POM. If there's already a `<properties>` section,
    they'll be added to the end of it in alphabetical order. If there isn't a `<properties>`, one will be added before the first dependency consumer.
   