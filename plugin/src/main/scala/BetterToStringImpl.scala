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

package org.polyvariant

// Source-compatible core between 2.x and 3.x implementations

trait CompilerApi {
  type Tree
  type Clazz
  type Param
  type ParamName
  type Method
  type EnclosingObject

  def className(clazz: Clazz): String
  def isPackageOrPackageObject(enclosingObject: EnclosingObject): Boolean
  def enclosingObjectName(enclosingObject: EnclosingObject): String
  def params(clazz: Clazz): List[Param]
  def literalConstant(value: String): Tree

  def paramName(param: Param): ParamName
  def selectInThis(clazz: Clazz, name: ParamName): Tree
  def concat(l: Tree, r: Tree): Tree

  def createToString(clazz: Clazz, body: Tree): Method
  def addMethod(clazz: Clazz, method: Method): Clazz
  def methodNames(clazz: Clazz): List[String]
  // better name: "is case class or object"
  def isCaseClass(clazz: Clazz): Boolean
  def isViableDefinition(clazz: Clazz): Boolean
  def isEnum(clazz: Clazz): Boolean
  def isObject(clazz: Clazz): Boolean
  def productPrefixParam: ParamName
}

trait BetterToStringImpl[+C <: CompilerApi] {
  val compilerApi: C

  def transformClass(
    clazz: compilerApi.Clazz,
    isNested: Boolean,
    enclosingObject: Option[compilerApi.EnclosingObject]
  ): compilerApi.Clazz

}

object BetterToStringImpl {

  def instance(
    api: CompilerApi
  ): BetterToStringImpl[api.type] =
    new BetterToStringImpl[api.type] {
      val compilerApi: api.type = api

      import api._

      def transformClass(
        clazz: Clazz,
        isNested: Boolean,
        enclosingObject: Option[EnclosingObject]
      ): Clazz = {
        // technically, the method found by this can be even something like "def toString(s: String): Unit", but we're ignoring that
        val hasToString: Boolean = methodNames(clazz).contains("toString")

        val shouldModify = isViableDefinition(clazz) && !isNested && !hasToString

        if (shouldModify) overrideToString(clazz, enclosingObject)
        else clazz
      }

      private def overrideToString(clazz: Clazz, enclosingObject: Option[EnclosingObject]): Clazz =
        addMethod(clazz, createToString(clazz, toStringImpl(clazz, enclosingObject)))

      private def toStringImpl(clazz: Clazz, enclosingObject: Option[EnclosingObject]): Tree = {
        val className = api.className(clazz)
        val parentPrefix = enclosingObject.filterNot(api.isPackageOrPackageObject).fold("")(api.enclosingObjectName(_) ++ ".")

        val namePart = literalConstant(parentPrefix ++ className)

        val paramListParts: List[Tree] = params(clazz).zipWithIndex.flatMap { case (v, index) =>
          val commaPrefix = if (index > 0) ", " else ""

          val name = paramName(v)

          List(
            literalConstant(commaPrefix ++ name.toString ++ " = "),
            selectInThis(clazz, name)
          )
        }

        val paramParts =
          if (api.isObject(clazz)) Nil
          else if (api.isEnum(clazz)) List(literalConstant("."), selectInThis(clazz, productPrefixParam))
          else
            List(
              List(literalConstant("(")),
              paramListParts,
              List(literalConstant(")"))
            ).flatten

        val parts =
          namePart :: paramParts

        parts.reduceLeft(concat(_, _))
      }

    }

}
