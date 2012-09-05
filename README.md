Utilities to clean, organize, and restructure Maven POMs.

* [Cleaner](docs/cleaner.md)

    "Cleans up" a single POM, normalizing plugin and dependency specifications, converting hardcoded versions
    to properties, consitently ordering top-level elements, and pretty-printing the output.

* [DependencyCheck](docs/dependency.md)

    Examines a project to find dependencies that are specified but unused, and those that are used but
    unspecified (ie, transitive dependencies that should be direct).

* [BuildParent](docs/parent.md) (not currently implemented)

    Examines a collection of project POMs, extracting dependency and plugin information, and producing
    a parent POM that is referenced by those projects.


# Building and Running

This project is built with Maven:

    `mvn clean install`

Each of the modules is in its own "app" directory. The build process produces a "shaded" executable JAR
(one that contains all dependencies needed to run), so you can invoke a given app like so:

    `java -jar target/application.jar APP_SPECIFIC_ARGUMENTS`

Documentation for each module can be found (as Markdown) in the "docs" directory. At some point I hope
to have the links above working, and/or generate HTML documentation as part of the build.
