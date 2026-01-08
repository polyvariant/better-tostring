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

import scala.reflect.internal.Flags
import scala.tools.nsc.Global

trait Scala2CompilerApi[G <: Global] extends CompilerApi {
  val theGlobal: G
  import theGlobal._

  sealed trait Classable extends Product with Serializable {

    def bimap(
      clazz: ClassDef => ClassDef,
      obj: ModuleDef => ModuleDef
    ): Classable = this match {
      case Classable.Clazz(c) => Classable.Clazz(clazz(c))
      case Classable.Obj(o)   => Classable.Obj(obj(o))
    }

    def fold[A](
      clazz: ClassDef => A,
      obj: ModuleDef => A
    ): A = this match {
      case Classable.Clazz(c) => clazz(c)
      case Classable.Obj(o)   => obj(o)
    }

    def merge: ImplDef = fold(identity, identity)

  }

  object Classable {
    case class Clazz(c: ClassDef) extends Classable
    case class Obj(o: ModuleDef) extends Classable
  }

  type Tree = theGlobal.Tree
  type Clazz = Classable
  type Param = ValDef
  type ParamName = TermName
  type Method = DefDef
  type EnclosingObject = ModuleDef
}

object Scala2CompilerApi {

  def instance(global: Global): Scala2CompilerApi[global.type] =
    new Scala2CompilerApi[global.type] {
      val theGlobal: global.type = global
      import global._

      def params(clazz: Clazz): List[Param] = clazz.fold(
        clazz = _.impl.body.collect {
          case v: ValDef if v.mods.isCaseAccessor => v
        },
        obj = _ => Nil
      )

      def className(clazz: Clazz): String = clazz.merge.name.toString

      def isPackageOrPackageObject(enclosingObject: EnclosingObject): Boolean =
        // couldn't find any nice api for this. `m.symbol.isPackageObject` does not work after the parser compiler phase (needs to run later).
        enclosingObject.symbol.isInstanceOf[NoSymbol] && enclosingObject.name.toString == "package"

      def enclosingObjectName(enclosingObject: EnclosingObject): String = enclosingObject.name.toString
      def literalConstant(value: String): Tree = Literal(Constant(value))
      def paramName(param: Param): ParamName = param.name
      def selectInThis(clazz: Clazz, name: ParamName): Tree = q"this.$name"
      def concat(l: Tree, r: Tree): Tree = q"$l + $r"

      def createToString(clazz: Clazz, body: Tree): Method = DefDef(
        Modifiers(Flags.OVERRIDE),
        TermName("toString"),
        Nil,
        List(List()),
        Ident(TypeName("String")),
        body
      )

      def addMethod(clazz: Clazz, method: Method): Clazz = {
        val newBody = clazz.merge.impl.copy(body = clazz.merge.impl.body :+ method)
        clazz.bimap(
          clazz = _.copy(impl = newBody),
          obj = _.copy(impl = newBody)
        )
      }

      def methodNames(clazz: Clazz): List[String] = clazz.merge.impl.body.collect {
        case d: DefDef => d.name.toString
        case d: ValDef => d.name.toString
      }

      def isCaseClass(clazz: Clazz): Boolean = clazz.merge.mods.isCase

      // No enums in scala 2
      def isEnum(clazz: Clazz): Boolean = false

      def isViableDefinition(clazz: Clazz): Boolean = {
        val isCaseClassOrObject = clazz.merge.mods.isCase

        isCaseClassOrObject
      }

      // Always return true for ModuleDef - apparently ModuleDef doesn't have the module flag...
      def isObject(clazz: Clazz): Boolean = clazz.fold(
        clazz = _.mods.hasModuleFlag,
        obj = _ => true
      )

      def productPrefixParam: Nothing = sys.error("invalid state: this shouldn't be called in Scala 2")

    }

}
