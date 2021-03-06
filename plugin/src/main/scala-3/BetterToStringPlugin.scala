package com.kubukoz

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Flags._
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.typer.FrontEnd
import dotty.tools.dotc.transform.PostTyper
import dotty.tools.dotc.core.Decorators._

class BetterToStringPlugin extends StandardPlugin:
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override def init(options: List[String]): List[PluginPhase] = List(new BetterToStringPluginPhase)

class BetterToStringPluginPhase extends PluginPhase:
  import tpd._

  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: Set[String] = Set(FrontEnd.name)

  private def addToString(t: Template, clazz: Symbols.ClassSymbol)(using Context): Template = {
    val params = t.body.collect {
      case v: ValDef if v.mods.is(CaseAccessor) => v
    }

    val toStringImpl = {
      val className = clazz.name.toString

      val paramListParts = params.zipWithIndex.flatMap {
        case (v, index) =>
          val commaPrefix = if (index > 0) ", " else ""

          List(
            Literal(Constant(commaPrefix ++ v.name.toString ++ " = ")),
            This(clazz).select(v.name)
          )
      }

      val parts =
        List(
          List(Literal(Constant(className ++ "("))),
          paramListParts,
          List(Literal(Constant(")")))
        ).flatten


      parts.reduceLeft((a, b) => a.select("+".toTermName).appliedTo(b))
    }

    // this was adapted from dotty.tools.dotc.transform.SyntheticMembers (line 115)

    val sym = Symbols.defn.Any_toString

    val toStringSymbol = sym.copy(
      owner = clazz,
      flags = sym.flags | Override,
      info = clazz.thisType.memberInfo(sym),
      coord = clazz.coord
    ).entered.asTerm
    // I think it should actually be enteredAfter, but this is simpler and it seems to work (PluginPhase is not a DenotTransformer)

    val toStringDef = DefDef(toStringSymbol, toStringImpl)

    cpy.Template(t)(body = toStringDef :: t.body)
  }

  override def transformTemplate(t: Template)(using ctx: Context): Tree =
    val clazz = ctx.owner.asClass

    val isCaseClass = clazz.flags.is(CaseClass)

    val isNested = !(ctx.owner.owner.isPackageObject || ctx.owner.owner.is(Module))

    val hasToString = t.body.exists {
      case d: DefDef if d.name.toString == "toString" => true
      case _ => false
    }

    if(isCaseClass && !isNested && !hasToString)
      addToString(t, ctx.owner.asClass)
    else t
