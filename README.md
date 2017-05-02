Utilities to clean, organize, and restructure Maven POMs.

* [POM Cleaner](docs/AppCleaner.md)

    "Cleans up" a single POM, normalizing plugin and dependency specifications, converting hardcoded versions
    to properties, consitently ordering top-level elements, and pretty-printing the output.

    There is also a version of this tool that [[WebCleaner|runs as a web-app]]. 

* [Dependency Check](docs/AppDependency.md) (in process)

    Examines a project to find dependencies that are specified but unused, and those that are used but
    unspecified (ie, transitive dependencies that should be direct).

* [Parent POM Builder](docs/AppParent.md) (not currently implemented)

    Examines a collection of project POMs, extracting dependency and plugin information, and producing
    a parent POM that is referenced by those projects.


# Building and Running

This project is built with Maven:

    mvn clean install

Each of the modules is in its own "app" directory. The build process produces a "shaded" executable JAR
(one that contains all dependencies needed to run), so you can invoke a given app like so:

    java -jar target/application.jar APP_SPECIFIC_ARGUMENTS

Documentation for each app can be found on the Wiki, following links shown above.
