package com.kubukoz

import scala.reflect.internal.Flags
import scala.tools.nsc.Global

trait Scala2CompilerApi[G <: Global] extends CompilerApi {
  val theGlobal: G
  import theGlobal._
  type Tree = theGlobal.Tree
  type Clazz = ClassDef
  type Param = ValDef
  type ParamName = TermName
  type Method = DefDef
  type ClazzParent = ModuleDef
}

object Scala2CompilerApi {

  def instance(global: Global): Scala2CompilerApi[global.type] =
    new Scala2CompilerApi[global.type] {
      val theGlobal: global.type = global
      import global._

      def params(clazz: Clazz): List[Param] = clazz.impl.body.collect {
        case v: ValDef if v.mods.hasFlag(Flags.CASEACCESSOR) => v
      }

      def className(clazz: Clazz): String = clazz.name.toString
      def parentName(parent: ClazzParent) = parent.name.toString
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

      def addMethod(clazz: Clazz, method: Method): Clazz =
        clazz.copy(impl = clazz.impl.copy(body = clazz.impl.body :+ method))

      def methodNames(clazz: Clazz): List[String] = clazz.impl.body.collect { case d: DefDef =>
        d.name.toString
      }

      def isCaseClass(clazz: Clazz): Boolean = clazz.mods.hasFlag(Flags.CASE)
    }

}
