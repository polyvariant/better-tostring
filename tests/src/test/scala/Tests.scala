import munit.FunSuite
import scala.runtime.ScalaRunTime
import munit.TestOptions
import b2s.buildinfo.BuildInfo

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

  // https://github.com/polyvariant/better-tostring/issues/59
  // On scala 2, this is expected to fail.
  test {
    val name = "Case object with toString val should not get extra toString"

    val isScala3 = BuildInfo.scalaVersion.startsWith("3")
    if (isScala3) name: TestOptions else name.fail
  } {
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
