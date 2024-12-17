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

class Scala3Tests extends FunSuite:
  test("an enum made of constants should have a normal toString") {
    assertEquals(
      ScalaVersion.Scala2.toString,
      "ScalaVersion.Scala2"
    )
    assertEquals(
      ScalaVersion.Scala3.toString,
      "ScalaVersion.Scala3"
    )
  }

  test("an enum being an ADT should get a custom toString") {
    assertEquals(
      User.LoggedIn("admin").toString,
      "User.LoggedIn(name = admin)"
    )
    assertEquals(
      User.Unauthorized.toString,
      "User.Unauthorized"
    )
  }

  test("an enum with a custom toString should use it") {
    assertEquals(
      EnumCustomTostring.SimpleCase.toString,
      "example"
    )

    // https://github.com/polyvariant/better-tostring/issues/34
    // we aren't there yet - need to be able to find inherited `toString`s first
    assertEquals(
      EnumCustomTostring.ParameterizedCase("foo").toString,
      // Should be "example" because the existing toString should take precedence.
      // Update the test when #34 is fixed.
      "EnumCustomTostring.ParameterizedCase(value = foo)"
    )
  }

enum ScalaVersion:
  case Scala2, Scala3

enum EnumCustomTostring:
  case SimpleCase
  case ParameterizedCase(value: String)
  override def toString: String = "example"

enum User:
  case LoggedIn(name: String)
  case Unauthorized
