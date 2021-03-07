package com.kubukoz

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Flags._
import dotty.tools.dotc.core.Types
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.core.Symbols.ClassSymbol
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.typer.FrontEnd
import dotty.tools.dotc.transform.PostTyper
import dotty.tools.dotc.core.Decorators._

final class BetterToStringPlugin extends StandardPlugin:
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override def init(options: List[String]): List[PluginPhase] = List(new BetterToStringPluginPhase)

import dotty.tools.dotc.ast.Trees
import tpd._

trait Scala3CompilerApi extends CompilerApi:
  type Tree = Trees.Tree[Types.Type]
  type Clazz = Scala3CompilerApi.ClassContext
  type Param = ValDef
  type ParamName = Names.TermName
  type Method = DefDef

object Scala3CompilerApi:
  final case class ClassContext(t: Template, clazz: ClassSymbol):
    def mapTemplate(f: Template => Template): ClassContext = copy(t = f(t))

  def instance(using Context): Scala3CompilerApi = new Scala3CompilerApi:
    def params(clazz: Clazz): List[Param] =
      clazz.t.body.collect {
        case v: ValDef if v.mods.is(CaseAccessor) => v
      }

    def className(clazz: Clazz): String = clazz.clazz.name.toString
    def literalConstant(value: String): Tree = Literal(Constant(value))
    def paramName(param: Param): ParamName = param.name
    def selectInThis(clazz: Clazz, name: ParamName): Tree = This(clazz.clazz).select(name)
    def concat(l: Tree, r: Tree): Tree = l.select("+".toTermName).appliedTo(r)

    def createToString(owner: Clazz, body: Tree): Method = {
      val clazz = owner.clazz
      // this was adapted from dotty.tools.dotc.transform.SyntheticMembers (line 115)
      val sym = Symbols.defn.Any_toString

      val toStringSymbol = sym.copy(
        owner = clazz,
        flags = sym.flags | Override,
        info = clazz.thisType.memberInfo(sym),
        coord = clazz.coord
      ).entered.asTerm

      DefDef(toStringSymbol, body)
    }

    def addMethod(clazz: Clazz, method: Method): Clazz =
      clazz.mapTemplate(
        t => cpy.Template(t)(body = t.body :+ method)
      )

    def methodNames(clazz: Clazz): List[String] = clazz.t.body.collect {
      case d: DefDef => d.name.toString
    }

    def isCaseClass(clazz: Clazz): Boolean = clazz.clazz.flags.is(CaseClass)

final class BetterToStringPluginPhase extends PluginPhase:

  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: Set[String] = Set(FrontEnd.name)

  override def transformTemplate(t: Template)(using ctx: Context): Tree =
    val clazz = ctx.owner.asClass

    val isNested = !(ctx.owner.owner.isPackageObject || ctx.owner.owner.is(Module))

    BetterToStringImpl
      .instance(Scala3CompilerApi.instance)
      .transformClass(Scala3CompilerApi.ClassContext(t, clazz), _ => isNested)
      .t
