# better-toString

[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

A Scala compiler plugin that replaces the default `toString` implementation of case classes with a more verbose one.

## ⚠️  Warning - experimental library. Hasn't been used in production yet

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
libraryDependencies += "com.kubukoz" % "better-tostring" % version cross CrossVersion.full
```

The plugin is currently published for Scala 2.12.10, 2.12.11, and 2.13.1.

## What does the plugin actually do?

1. Only case classes are changed.
2. Only the fields in the first parameter list are shown.
3. If the class already overrides `toString` *directly*, it's not replaced.

## Roadmap

- Ignore classes that inherit `toString` from a type that isn't `Object`
- Add a way of overriding default behavior (blacklisting/whitelisting certain classes) - probably via an annotation in an extra dependency
- Extend functionality to support ADTs - for example, `case object B extends A` inside `object A` could be shown as `A.B`

If you have ideas for improving the plugin, feel free to create an issue and I'll consider making it happen :)
