pomutil
=======

Utilities to clean, organize, and restructure Maven POMs. 

* `Cleaner [OPTIONS] [ INFILE [ OUTFILE ]]`

These generally behave as UNIX filters, reading StdIn or a file, writing StdOut or another file, and reporting any problems to StdErr. The target JAR contains all dependencies.

Cleaner
-------

Cleaner takes a single POM and cleans it up according to your options. This is the default app (you can invoke with `java -jar target/pomutil.jar`).

The cleaning process consists of multiple steps, shown here. Some are enabled by default, some are disabled. For each step,
you can specify an option to do something other than the default. Some steps also take options to control their behavior.

* Convert dependency versions to properties (enabled by default)

    Disable with: `--noversionprops`

    Finds all `<dependency>` entries that use explicit numeric versions, and converts those dependencies to use a property.
    Will append version properties to the end of an existing `<properties>` section, or create a new `<properties>` section
    at the end of the POM.

    Properties are named by appending ".version" to the dependency's group ID (eg: "`com.example.verson`"). If the same
    group ID is associated with two version numbers, then the property for *the second and subsequent* instances will be
    constructed with the artifact ID (eg: first dependency is "`com.example.verson`" second is "`com.example.foo.verson`").
    These collisions will be logged.

    If you know that you have multiple artifacts with the same group ID and different versions, you can provide one or
    more "`--addArtifactIdToProp=GROUPID`" options, where `GROUPID` is a group ID that should not appear alone.
