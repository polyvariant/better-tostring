package com.kubukoz

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags.Module
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import dotty.tools.dotc.typer.FrontEnd
import tpd._

final class BetterToStringPlugin extends StandardPlugin:
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override def init(options: List[String]): List[PluginPhase] = List(new BetterToStringPluginPhase)

final class BetterToStringPluginPhase extends PluginPhase:

  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: Set[String] = Set(FrontEnd.name)

  override def transformApply(apply: Apply)(using ctx: Context): Tree = {
    var selector: Tree = null
    val matches = apply.filterSubTrees {
      case appl@Apply(function, List(param)) =>

        function.filterSubTrees {
          case Select(selected, name) if name.show == "map"  =>
            param match {
              case Block(
                List(
                  DefDef(
                    name,
                    List(
                      List(
                        ValDef(
                          term,
                          tpe1,
                          EmptyTree
                        )
                      )
                    ),
                    tpe2,
                    Ident(term2)
                  )
                ),
                Closure(
                  Nil,
                  Ident(name2),
                  EmptyTree
                )
              )
                if name == name2 && term == term2 && tpe1.sameTree(tpe2)
              =>
                selector = selected

                true
            }
          case _ => false
        }.nonEmpty

      case _ => false
    }.nonEmpty

    // if(matches)
    //   println(selector.show)

    if(matches)
      selector
    else
      super.transformApply(apply)
  }

  override def transformTemplate(t: Template)(using ctx: Context): Tree =
    val clazz = ctx.owner.asClass

    val isNested = !(ctx.owner.owner.isPackageObject || ctx.owner.owner.is(Module))

    BetterToStringImpl
      .instance(Scala3CompilerApi.instance)
      .transformClass(Scala3CompilerApi.ClassContext(t, clazz), isNested)
      .t
