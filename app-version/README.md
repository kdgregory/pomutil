# VersionUpdate

Takes a set of POMS/directories and updates the version numbers for a specified
group/artifact. This is used as part of a release process, when you want to update
the project being released along with all projects that depend on it.

You can either specify explicit version numbers or use an "auto-version" feature:

* If the current version is a snapshot then "-SNAPSHOT" is removed, turning it into
  a release.
* If the current version is a release, using a dot-delimited numeric value, the lowest
  component of the version number is incremented, and "-SNAPSHOT" is added to the end.


## Invocation

You can invoke with individual POM files, or a directory; in the latter case the
tool will traverse the directory hierarchy and find all files names "pom.xml".

    java -jar app-version.jar [OPTIONS] FILE_OR_DIRECTORY [...]


### Options

* `--groupId GROUP_ID`

  _Required._ Limits the updated projects/dependencies to those having the specified
  Maven `groupId`.

* `--artifactId ARTIFACT_ID`

  _Optional._ If used, only projects/dependencies associated with the specified Maven
  `artifactId` will be updated. You may find it useful to omit this parameter when
  updating a set of related projects.

* `--autoVersion`

  If used, the version for all selected projects will be updated as described above.
  This is useful when updating a single project and all projects that depend on it,
  but should be used with care: if you don't restrict the artifact ID or have
  dependencies on multiple versions, this might not do what you want.

* `--fromVersion VERSION`

  Specifies the desired version to update; if omitted, all versions that match the
  group and artifact test will be updated. May be used with `--autoVersion`.

* `--toVersion VERSION`

  Specifies the version number that selected projects/dependencies will be updated to.
  May not be used with `--autoVersion`, and is required if that parameter is not used.

* `--updateParent`

  If used, the version number of a `<parent>` section will be updated (provided that
  it matches the desired group, artifact, and version).
  
* `--updateDependencies`

  If used, the version number of any `<dependency>` element that matches the specified
  group / artifact / version will be updated. This affects both direct dependencies
  and those within `<dependencyManagement>` sections.

  This flag will make a best attempt to update properties that are used to specify
  dependency versions. Such properties must be simple properties (ie, no embedded
  property expansions), and must not be shared between dependencies that are not
  otherwise selected (eg, if you specify `--groupId com.example.foo` and there's
  a property that's shared between it and a dependency on `com.example.bar`, that
  property will not be updated. You will be warned if this happens so that you
  can manually update the dependency.


### Examples

> Note: these examples refer to the application JAR as `target/app-version-*.jar`.
  As long as you use an operating system that pre-globs filenames this should work.
  If not, you must explicitly identify the JAR version.

Bulk update of dependencies: update all references to `com.example:foo:1.2.3` to 
version `1.2.4`, in any POM found under the `workspace` directoriy.

```
java -jar target/app-version-*.jar --groupId com.example --artifactId foo --fromVersion 1.2.3 --toVersion 1.2.4 --updateDependencies
```

Prepare current multi-artifact project for release: matches on group ID and version,
uses auto-versioning to change that version, and includes the references to a parent
POM.

```
java -jar target/app-version-*.jar --groupId com.example --fromVersion 1.2.3-SNAPSHOT --autoVersion --updateParent
```
