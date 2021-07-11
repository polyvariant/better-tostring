import munit.FunSuite

class Scala3Tests extends FunSuite:
  test("an enum made of constants should have a normal toString") {
    assertEquals(
      ScalaVersion.Scala2.toString,
      "ScalaVersion.Scala2"
    )
  }

  test("an enum being an ADT should get a custom toString - with parameters") {
    assertEquals(
      User.LoggedIn("admin").toString,
      "User.LoggedIn(name = admin)"
    )
  }

  test("an enum being an ADT should get a custom toString - no parameters") {
    assertEquals(
      User.Unauthorized.toString,
      "User.Unauthorized"
    )
  }

enum ScalaVersion:
  case Scala2, Scala3

enum User:
  case LoggedIn(name: String)
  case Unauthorized
