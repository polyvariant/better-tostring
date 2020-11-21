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
  
  val eprintln = System.err.println(_: Any)
  
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
    
    def str = (s: String) => Literal(Constant(s))
    // can't be an extension block, because overloading methods doesn't work if the block is inside a method O_o 
    implicit class TreeExtensions(self: Tree):
      def concat(other: Tree) = self.select("+".toTermName).appliedTo(other)
      def concat(s: String) = self.select("+".toTermName).appliedTo(str(s))
      def concat(sym: Symbol) = self.select("+".toTermName).appliedTo(This(clazz).select(sym))

    val vals = t.body.collect { case v: ValDef if v.mods.is(CaseAccessor) => v }

    var body: Tree = str(clazz.name.toString + "(")
    for (v, i) <- vals.zipWithIndex do
      if i > 0 then body = body.concat(", ")
      body = body.concat(v.symbol.name.toString + " = ")
      body = body.concat(v.symbol)
    body = body.concat(")")
    
    val toStringDef = DefDef(toStringSymbol, body)
    
    eprintln(toStringDef.show)
    
    cpy.Template(t)(body = toStringDef :: t.body)
