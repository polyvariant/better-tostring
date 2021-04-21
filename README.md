# better-tostring

[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.com/kubukoz/better-tostring.svg?branch=master)](https://travis-ci.com/kubukoz/better-tostring)
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

- 2.12.10, 2.12.11, 2.12.12, 2.12.13
- 2.13.1, 2.13.2, 2.13.3, 2.13.4, 2.13.5
- 3.0.0-M3, 3.0.0-RC1, 3.0.0-RC2, 3.0.0-RC3

## What does the plugin actually do?

1. Only case classes located directly in `package`s or `object`s are changed. Nested classes and classes local to functions are currently ignored.
2. Only the fields in the first parameter list are shown.
3. If the class already overrides `toString` *directly*, it's not replaced.

## Roadmap

- Ignore classes that inherit `toString` from a type that isn't `Object`
- Add a way of overriding default behavior (blacklisting/whitelisting certain classes) - probably via an annotation in an extra dependency
- Extend functionality to support ADTs - for example, `case object B extends A` inside `object A` could be shown as `A.B`
- Potentially ignore value classes

If you have ideas for improving the plugin, feel free to create an issue and I'll consider making it happen :)

## Maintainers

The maintainers of this project (people who can merge PRs and make releases) are:

- Jakub Kozłowski ([@kubukoz](https://github.com/kubukoz))
- Michał Pawlik ([@majk-p](https://github.com/majk-p))
- Mikołaj Robakowski ([@mrobakowski](https://github.com/mrobakowski))
