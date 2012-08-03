Utilities to clean, organize, and restructure Maven POMs.

These generally behave as UNIX filters, reading StdIn or a file, writing StdOut or another file, and reporting any problems to StdErr. The target JAR contains all dependencies.


# Cleaner

Cleaner takes a single POM and cleans it up according to your options. This is the default app (you can invoke with `java -jar target/pomutil.jar`).

`Cleaner [OPTIONS] [ INFILE [ OUTFILE ]]`

The cleaning process consists of multiple steps, shown here. Some are enabled by default, some are disabled. For each step,
you can specify an option to do something other than the default. Some steps also take options to control their behavior.

* Add common properties

    Disable with: `--noCommonProps`

    There are some standard properties, such as build encoding, that Maven complains about if missing. This
    step adds them, creating a `<properties>` section if necessary.

* Normalize `dependency` specifications

    Disable with: `--noDependencyNormalize`

    Ensures that the children of the `dependency` element follow the order given in the
    [Maven docs](http://maven.apache.org/ref/3.0.4/maven-model/maven.html#class_dependency) (most important being that
    `groupId`, `artifactId`, and `version` come first and in that order), and removes any `<scope>compile</scope>`
    or `<type>jar</type>` elements (because these are the defaults).

* Order dependencies

    Disable with: `--noDependencySort`

    Modify with: `--groupDependenciesByScope`

    Sorts the `<dependency>` entries within `<dependencies>` and `<dependencyManagement>` top-level sections. Sort
    is alphabetic, by combining group ID, artifact ID, and version (although version should never come into play).

    If the `--groupDependenciesByScope` option is provided, dependencies are first sorted by their `<scope>` value,
    in the order `compile` (or blank), `test`, `runtime`, `provided`, `system`.

    Note that this step completely rebuilds the container elements, and will remove any blank lines or comments
    between entries.

* Convert explicit dependency versions to properties

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

----

# Roadmap

Features still to be implemented:

**Cleaner**

*   Remove duplicate dependencies (this already happens during sorting)
*   Version-property replacement for plugins. The algorithm for constructing property names is based on artifactID, not
    groupId, because most plugins have the same group.
*   Add explicit version numbers to plugins (disabled by default). This will be based on a configuration file that's
    irregularly updated from on [Maven documentation](http://maven.apache.org/plugins/index.html), and may be modified
    by the user.
*   Organize POM sections according to the [Maven documentation](http://maven.apache.org/ref/3.0.4/maven-model/maven.html).
*   Insert lines between sections in pretty-printed output. This will require abandoning the JAXP pretty-printer, as it
    gets confused when there's already inter-element whitespace.

**ExtractParent**

Utility to take a series of POMs, apply the *Cleaner* rules to them, and then generate a parent POM from them.

*   Move all version properties into the parent, keeping the highest version number (option: warning where different
    children have different versions).
*   Move all non-execution plugin definitions into the parent. This will probably use a configuration file of some
    sort, as it's unclear to me whether there's any automated way to resolve discrepancies. Might need some sort of
    hardcoded logic to warn when one POM's configuration differs from the parent.
*   Add a `<dependencyManagement>` section, containing all dependencies in all children.

**DependencyCheck**

Examines the bytecode of project classes to determine which third-party classes are invoked, then examines the
project dependencies to find those classes. Reports both on dependencies that aren't used, and those that are
missing (far too often I've used a class that's available through a transitive dependency, then wondered why
my build failed when I removed the direct dependency).
