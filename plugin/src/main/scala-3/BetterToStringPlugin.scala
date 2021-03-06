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

  override def transformTemplate(t: Template)(using ctx: Context): Tree =
    val clazz = ctx.owner.asClass
    if !clazz.flags.is(CaseClass) then return t

    for m <- t.body do
      m match
        case d: DefDef if d.name.toString == "toString" => return t
        case _ =>

    val sym = Symbols.defn.Any_toString

    // this was adapted from dotty.tools.dotc.transform.SyntheticMembers (line 115)
    val toStringSymbol = sym.copy(
      owner = clazz,
      flags = sym.flags | Override,
      info = clazz.thisType.memberInfo(sym),
      coord = clazz.coord
    ).entered.asTerm
    // I think it should actually be enteredAfter, but this is simpler and it seems to work (PluginPhase is not a DenotTransformer)

    val vals = t.body.collect { case v: ValDef if v.mods.is(CaseAccessor) => v }

    val paramListParts = vals.zipWithIndex.flatMap {
      case (v, index) =>
        val commaPrefix = if (index > 0) ", " else ""

        List(
          Literal(Constant(commaPrefix ++ v.name.toString ++ " = ")),
          This(clazz).select(v.name)
        )
    }

    val className = clazz.name.toString

    val parts =
      List(
        List(Literal(Constant(className ++ "("))),
        paramListParts,
        List(Literal(Constant(")")))
      ).flatten

    val body = parts.reduceLeft((a, b) => a.select("+".toTermName).appliedTo(b))

    val toStringDef = DefDef(toStringSymbol, body)

    cpy.Template(t)(body = toStringDef :: t.body)
