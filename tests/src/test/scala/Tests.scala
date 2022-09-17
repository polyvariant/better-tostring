/*
 * Copyright 2020 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import munit.FunSuite

class Tests extends FunSuite {

  test("Simple case class stringifies nicely") {
    assertEquals(
      SimpleCaseClass(
        "Joe",
        23
      ).toString,
      "SimpleCaseClass(name = Joe, age = 23)"
    )
  }

  test("Case class with multiple parameter lists only has the first list included") {
    assertEquals(
      MultiParameterList("foo", 20)(
        "s"
      ).toString,
      "MultiParameterList(name = foo, age = 20)"
    )
  }

  test("Case class with custom toString should not be overridden") {
    assertEquals(
      CustomTostring("Joe").toString,
      "***"
    )
  }
  test("Case class with custom toString val should not be overridden") {
    assertEquals(
      CustomTostringVal("Joe").toString,
      "***"
    )
  }

  test("Method with alternate constructors should stringify based on primary constructor") {
    assertEquals(
      new HasOtherConstructors(
        10
      ).toString,
      "HasOtherConstructors(s = 10 beers)"
    )
  }

  test("Case class nested in an object should include enclosing object's name") {
    assertEquals(
      ObjectNestedParent.ObjectNestedClass("Joe").toString,
      "ObjectNestedParent.ObjectNestedClass(name = Joe)"
    )
  }

  test("Case object nested in an object should include enclosing object's name") {
    assertEquals(
      ObjectNestedParent.ObjectNestedObject.toString,
      "ObjectNestedParent.ObjectNestedObject"
    )
  }

  test("Class nested in a package object should not include package's name") {
    assertEquals(
      pack.InPackageObject("Joe").toString,
      "InPackageObject(name = Joe)"
    )
  }

  test("Class nested in another class should stringify normally") {
    assertEquals(
      new NestedParent().NestedChild("a").toString,
      "NestedChild(a)"
    )
  }

  test("Class nested in an object itself nested in a class should stringify normally") {
    assertEquals(
      new DeeplyNestedInClassGrandparent()
        .DeeplyNestedInClassParent
        .DeeplyNestedInClassClass("a")
        .toString,
      "DeeplyNestedInClassClass(a)"
    )
  }

  test("Method-local class should stringify normally") {
    assertEquals(
      MethodLocalWrapper.methodLocalClassStringify,
      "LocalClass(a)"
    )
  }

  test("Lone case object should use the default toString") {
    assertEquals(CaseObject.toString, "CaseObject")
  }

  test("Case object with toString should not get extra toString") {
    assertEquals(
      CaseObjectWithToString.toString,
      "example"
    )
  }

  test("Case object with toString val should not get extra toString") {
    assertEquals(
      CaseObjectWithToStringVal.toString,
      "example"
    )
  }

}

case object CaseObject

case object CaseObjectWithToString {
  override def toString: String = "example"
}

case object CaseObjectWithToStringVal {
  override val toString: String = "example"
}

final case class SimpleCaseClass(name: String, age: Int)
final case class MultiParameterList(name: String, age: Int)(val s: String)

final case class CustomTostring(name: String) {
  override def toString: String = "***"
}

final case class CustomTostringVal(name: String) {
  override val toString: String = "***"
}

final case class HasOtherConstructors(s: String) {
  def this(a: Int) = this(a.toString + " beers")
}

final class NestedParent() {
  case class NestedChild(name: String)
}

object ObjectNestedParent {
  case class ObjectNestedClass(name: String)
  case object ObjectNestedObject
}

final class DeeplyNestedInClassGrandparent {

  object DeeplyNestedInClassParent {
    case class DeeplyNestedInClassClass(name: String)
  }

}

object MethodLocalWrapper {

  def methodLocalClassStringify: String = {
    final case class LocalClass(name: String)

    LocalClass("a").toString()
  }

}
