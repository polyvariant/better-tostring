package com.kubukoz

import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.transform.TypingTransformers
import scala.reflect.internal.Flags

final class BetterToStringPlugin(override val global: Global) extends Plugin {
  override val name: String = "better-tostring"
  override val description: String =
    "scala compiler plugin for better default toString implementations"
  override val components: List[PluginComponent] = List(
    new BetterToStringPluginComponent(global)
  )
}

trait Scala2CompilerApi[G <: Global] extends CompilerApi {
  val theGlobal: G
  import theGlobal._
  type Tree = theGlobal.Tree
  type Clazz = ClassDef
  type Param = ValDef
  type ParamName = TermName
  type Method = DefDef
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

      def methodNames(clazz: Clazz): List[String] = clazz.impl.body.collect {
        case d: DefDef => d.name.toString
      }

      def isCaseClass(clazz: Clazz): Boolean = clazz.mods.hasFlag(Flags.CASE)
    }
}

final class BetterToStringPluginComponent(val global: Global)
    extends PluginComponent
    with TypingTransformers {
  import global._
  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: List[String] = List("parser")

  private val impl: BetterToStringImpl[Scala2CompilerApi[global.type]] =
    BetterToStringImpl.instance(Scala2CompilerApi.instance(global))

  private def modifyClasses(tree: Tree): Tree =
    tree match {
      case p: PackageDef => p.copy(stats = p.stats.map(modifyClasses))
      case m: ModuleDef =>
        m.copy(impl = m.impl.copy(body = m.impl.body.map(modifyClasses)))
      case clazz: ClassDef =>
        impl.transformClass(
          clazz,
          // If it was nested, we wouldn't be in this branch.
          // Scala 2.x compiler API limitation (classes can't tell what the owner is).
          // This should be more optimal as we don't traverse every template, but it hasn't been benchmarked.
          isNested = _ => false
        )
      case other => other
    }

  override def newPhase(prev: Phase): Phase = new StdPhase(prev) {
    override def apply(unit: CompilationUnit): Unit =
      new Transformer {
        override def transform(tree: Tree): Tree = modifyClasses(tree)
      }.transformUnit(unit)
  }
}
