package com.kubukoz

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags.Module
import dotty.tools.dotc.core.Flags.Package
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.typer.FrontEnd

import scala.annotation.tailrec

import tpd.*

final class BetterToStringPlugin extends StandardPlugin:
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override def init(options: List[String]): List[PluginPhase] = List(new BetterToStringPluginPhase)

final class BetterToStringPluginPhase extends PluginPhase:

  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: Set[String] = Set(FrontEnd.name)

  override def transformTemplate(t: Template)(using ctx: Context): Tree =
    val clazz = ctx.owner.asClass

    val ownerOwner = ctx.owner.owner
    val isNested = ownerOwner.ownersIterator.exists(!_.is(Module))

    val enclosingObject =
      if ownerOwner.is(Module) then Some(ownerOwner)
      else None

    BetterToStringImpl
      .instance(Scala3CompilerApi.instance)
      .transformClass(Scala3CompilerApi.ClassContext(t, clazz), isNested, enclosingObject)
      .t
