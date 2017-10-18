# VersionUpdate

Takes a set of POMs/directories and updates the version numbers, either to a specified
version or the next sequential version. This supports a Git-centric release cycle, in
which you create a long-lived integration branch with a snapshot version, then update
the version before merging into master.


## Invocation

    java -jar app-version.jar [OPTIONS] FILE_OR_DIRECTORY [...]


## Options

* Specified versions: `--fromVersion`, `--toVersion`

  These options take a single parameter and must be used together. When used, any
  POMs that don't specify the `fromVersion` are ignored; the others are updated to
  `toVersion`

  You can combine this flag with `--autoVersion`, to specify either the starting or
  ending version and let the other be automatically updated.

* Automatically determine old and new versions: `--autoVersion`

  Disabled by default.

  When this flag is present, the POM version is updated according to the following
  rules:

  * If the POM version ends in `-SNAPSHOT`, that is removed (this is prep for a
    release).
  * If the POM version ends in a number, the least-significant portion of the
    version is incremented and `-SNAPSHOT` is appended (this is prep for an
    integration branch).

  Note that different POMs may have different versions (although updating independent
  projects is generally a bad idea).

* Update parent reference: `--updateParentRef`
  
  Disabled by default.

  If used, parent references are updated along with the project version. The same
  rules apply: either the parent must match the specified `fromVersion` or you must
  enable auto-update.

* Update dependency reference: `--updateDependency`
  
  This flag is used to update references to a dependency, which is useful when
  propagating release versions to other projects. It takes two parameters, group ID
  and artifact ID; it will update either explicit dependencies (or dependency
  management specifications) or make a best effort to update properties referenced
  in dependencies (simple properties can be updated, properties that themselves
  depend on properties or use substitution logic cannot be updated).
