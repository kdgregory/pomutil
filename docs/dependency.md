# DependencyCheck

Examines the bytecode of project classes to determine which third-party classes are invoked, then examines the
project dependencies to find those classes. Writes two lists to StdOut: those classes thata are not found in
any direct dependencies, and those direct dependencies that are not referenced by the application classes.

## Invocation

    java -jar app-dependency.jar [OPTIONS]

## Options

* `--projectDirectory=DIR`

    Selects a project directory for the check. By default, checks the current directory.

* `--ignoreUnusedDependency`=GROUPID[:ARTIFACTID]

    Removes dependencies from the "unused dependency" list. This is used if you load classes via
    reflection: since the dependency scanner looks for explicit references within the classfile,
    it will report a false positive if you use reflection to load classes (or call methods that
    do).

## Notes

* This utility must be run *after* building the project. It examines project classes in the `target` directory,
  and looks for dependencies in the local repository. It will not attempt to retrieve any missing dependencies.

## Roadmap

* Maintain a pre-configured set of "expected" unused dependencies. This is to cover cases (such as `spring-test` and `spring-core`)
    where the direct dependency has a runtime transitive dependency that is not specified in its own POM (and must
    therefore be specified as a direct dependency in the project POM).
* Add option to ignore unused dependencies where the scope is `provided` or `optional` (currently we consider these compile-time
    dependencies, so they're ignored by default).
* Handle dependencies with scope `system`.
* Support artifact qualifiers
