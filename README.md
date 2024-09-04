# better-tostring

[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Latest version](https://index.scala-lang.org/polyvariant/better-tostring/better-tostring/latest.svg)](https://index.scala-lang.org/kubukoz/better-tostring/better-tostring)
[![Maven Central](https://img.shields.io/maven-central/v/org.polyvariant/better-tostring_2.13.5.svg)](http://search.maven.org/#search%7Cga%7C1%7Cbetter-tostring)

A Scala compiler plugin that replaces the default `toString` implementation of case classes with a more verbose one.

## Example

Without the plugin:

```scala
final case class User(name: String, age: Int)

User("Joe", 18).toString() // "User(Joe, 18)"
```

With the plugin:

```scala
User("Joe", 18).toString() // "User(name = Joe, age = 18)"
```

## Usage

In sbt:

```scala
libraryDependencies += compilerPlugin("org.polyvariant" % "better-tostring" % version cross CrossVersion.full)
```

In scala-cli:

```scala
//> using plugin "org.polyvariant:::better-tostring:version"
```

(note: versions before `0.3.8` were published under the `com.kubukoz` organization instead of `org.polyvariant`)

<!-- SCALA VERSIONS START -->
The plugin is currently published for the following 23 Scala versions:

- 2.12.18, 2.12.19, 2.12.20
- 2.13.10, 2.13.11, 2.13.12, 2.13.13, 2.13.14
- 3.1.3
- 3.2.2
- 3.3.0-RC6, 3.3.0, 3.3.1-RC1, 3.3.1, 3.3.2-RC1, 3.3.3
- 3.4.0-RC1, 3.4.0, 3.4.1-RC1, 3.4.1, 3.4.2
- 3.5.0-RC1, 3.5.0
<!-- SCALA VERSIONS END -->

For older Scala versions, see [previous versions of better-tostring](https://repo1.maven.org/maven2/org/polyvariant) ([or even older versions](https://repo1.maven.org/maven2/com/kubukoz)).

As a rule of thumb, active support will include _at least_ 3 latest stable versions of 2.12, 2.13 and 3.0 for the foreseeable future.

## What does the plugin actually do?

1. Only case classes located directly in `package`s or `object`s are changed. Nested classes and classes local to functions are currently ignored.
2. Only the fields in the first parameter list are shown.
3. If the class is nested in an object (but not a package object), the object's name and a dot are prepended.
4. If the class already overrides `toString` *directly*, it's not replaced.

## Roadmap

- Ignore classes that inherit `toString` from a type that isn't `Object` (#34)
- Potentially ignore value classes (#19)

If you have ideas for improving the plugin, feel free to create an issue and we'll consider making it happen :)

## Customization?

_tl;dr there is none._

The plugin makes certain assumptions about what is a _better_ `toString`. We aim for a useful and reasonably verbose description of the data type,
which could make it easier to find certain issues with your tests (mismatching values in a field) or see the labels in debug logs.

We also want the plugin to become minimal in the implementation and easy to use (plug & play), without lots of configuration options, so the representation of the data types will not be customizable. **The format may change over time without prior notice**, so you shouldn't rely on the exact representation (as is the case with any `toString` methods), but any changes in behavior will be communicated in the release notes.

If you need a different `toString`, we suggest that you implement one yourself. You may also want to look at [pprint](https://github.com/com-lihaoyi/PPrint).

## Contributing

If you want to add a new feature, check if it's already been discussed in the issues list.

Before you start working on an existing feature / bugfix, let us know you're taking it on in its comments :)

To add a new Scala version:

1. Add it to `./scala-versions`
2. Run `sbt generateAll`
3. Commit and open a pull request.

## Maintainers

The maintainers of this project (people who can merge PRs and make releases) are:

- Jakub Kozłowski ([@kubukoz](https://github.com/kubukoz))
- Michał Pawlik ([@majk-p](https://github.com/majk-p))
- Mikołaj Robakowski ([@mrobakowski](https://github.com/mrobakowski))

## Community

This project supports the [Scala code of conduct](https://www.scala-lang.org/conduct/) and wants communication on all its channels (GitHub etc.) to be inclusive environments.

If you have any concerns about someone's behavior on these channels, contact [Jakub Kozłowski](mailto:kubukoz@gmail.com).
