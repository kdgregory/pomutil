Utilities to clean, organize, and restructure Maven POMs.

These generally behave as UNIX filters, reading StdIn or a file, writing StdOut or another file, and reporting any problems to StdErr. The target JAR contains all dependencies.


Cleaner
-------

Cleaner takes a single POM and cleans it up according to your options. This is the default app (you can invoke with `java -jar target/pomutil.jar`).

`Cleaner [OPTIONS] [ INFILE [ OUTFILE ]]`

The cleaning process consists of multiple steps, shown here. Some are enabled by default, some are disabled. For each step,
you can specify an option to do something other than the default. Some steps also take options to control their behavior.

* Order dependencies

    Disable with: `--noDependencySort`

    Sorts the `<dependency>` entries within `<dependencies>` and `<dependencyManagement>` top-level sections. Sort
    is alphabetic, by combining group ID, artifact ID, and version (although version should never come into play).

    Note that this step completely rebuilds the container elements, and will remove any blank lines or comments
    between entries.


* Convert dependency versions to properties

    Disable with: `--noVersionProps`

    Modify with: `--addArtifactIdToProp=GROUPID`, `--replaceExistingProps`

    Finds all `<dependency>` entries that use explicit numeric versions, and converts those dependencies to use a property.
    Will append version properties to the end of an existing `<properties>` section, or create a new `<properties>` section
    at the end of the POM.

    Properties are named by appending ".version" to the dependency's group ID (eg: "`com.example.verson`"). If the same
    group ID is associated with two version numbers, then the property for *the second and subsequent* instances will be
    constructed with the artifact ID (eg: first dependency is "`com.example.verson`" second is "`com.example.foo.verson`").
    These collisions will be logged.

    If you know that you have multiple artifacts with the same group ID and different versions, you can provide one or
    more "`--addArtifactIdToProp=GROUPID`" options, where `GROUPID` is a group ID that should not appear alone.

    Existing dependency properties will be checked for collisions but otherwise ignored. To make all of your dependencies
    use the same format, use the `--replaceExistingProps` option.


* Pretty-print output

    Disable with: `--noPrettyPrint`

    Removes all "ignorable whitespace" from the POM, then writes the output using an indenting serializer. Note that this
    will remove any blank lines between sections.

    By default, the indentation is 4 spaces per level. You can change this with the option `--prettyPrint=NUM`, where
    `NUM` is the number of spaces you want.
