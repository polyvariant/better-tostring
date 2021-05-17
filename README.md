# better-tostring

[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Latest version](https://index.scala-lang.org/kubukoz/better-tostring/better-tostring/latest.svg)](https://index.scala-lang.org/kubukoz/better-tostring/better-tostring)
[![Maven Central](https://img.shields.io/maven-central/v/com.kubukoz/better-tostring_2.13.5.svg)](http://search.maven.org/#search%7Cga%7C1%7Cbetter-tostring)

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
libraryDependencies += compilerPlugin("com.kubukoz" % "better-tostring" % version cross CrossVersion.full)
```

The plugin is currently published for the following Scala versions:

- 2.12.11, 2.12.12, 2.12.13
- 2.13.4, 2.13.5, 2.13.6
- 3.0.0

For older Scala versions, see [previous versions of better-tostring](https://repo1.maven.org/maven2/com/kubukoz).

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

## Maintainers

The maintainers of this project (people who can merge PRs and make releases) are:

- Jakub Kozłowski ([@kubukoz](https://github.com/kubukoz))
- Michał Pawlik ([@majk-p](https://github.com/majk-p))
- Mikołaj Robakowski ([@mrobakowski](https://github.com/mrobakowski))
