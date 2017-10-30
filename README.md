Utilities to clean, organize, and restructure Maven POMs.

* [POM Cleaner](app-cleaner/README.md)

    "Cleans up" a single POM, normalizing plugin and dependency specifications, converting hardcoded versions
    to properties, consitently ordering top-level elements, and pretty-printing the output.

    There is also a version of this tool that [[WebCleaner|runs as a web-app]]. 

* [Version Updater](app-version/README.md)

    Updates the version for a set of POMs, either to a specified version or the next sequential version.

* [Dependency Check](app-dependency/README.md) (in process)

    Examines a project to find dependencies that are specified but unused, and those that are used but
    unspecified (ie, transitive dependencies that should be direct).

* [Parent POM Builder]() (not currently implemented)

    Examines a collection of project POMs, extracting dependency and plugin information, and producing
    a parent POM that is referenced by those projects.


# Building and Running

The minimum JDK version to build/run this project is 1.6.

This project is built with Maven; all dependencies are available from Maven Central.

    mvn clean install

Each of the modules is in its own "app" directory. The build process produces a "shaded" executable JAR
(one that contains all dependencies needed to run), so you can invoke a given app like so:

    java -jar target/APPLICATION.jar APP_SPECIFIC_ARGUMENTS

Documentation for each app can be found in the app's README, or by following the links above.
