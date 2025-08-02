# Contributing guide

## Adding new features

If you want to add a new feature, check if it's already been discussed in the issues list.

Before you start working on an existing feature / bugfix, let us know you're taking it on in its comments :)

## Adding a new Scala version

### GitHub Workflow

Ask someone with permissions to run the `add-version` GitHub Workflow. Inputs:

- scala_version: The Scala version you want to add support for
- project_version: The version number of better-tostring that should be published. Most likely, you want to use an already existing version.

### Manually

This is the way to go in more complex situations, such as when adding the new version requires code changes.

1. Add it to `./scala-versions` (one version per line)
2. Run `sbt generateAll`
3. Run `sbt ++<your scala version> test`
4. Commit and open a pull request.
