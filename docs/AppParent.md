# BuildParent

    Constructs a parent POM from a set of project POMs.


## Invocation

## Options

## Roadmap

*   Move all version properties into the parent, keeping the highest version number (option: warning where different
    children have different versions).
*   Move all non-execution plugin definitions into the parent. This will probably use a configuration file of some
    sort, as it's unclear to me whether there's any automated way to resolve discrepancies. Might need some sort of
    hardcoded logic to warn when one POM's configuration differs from the parent.
*   Optionally add a `<dependencyManagement>` section, containing all dependencies in all children.
