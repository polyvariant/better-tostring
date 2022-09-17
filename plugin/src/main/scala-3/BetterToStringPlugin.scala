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

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags.Module
import dotty.tools.dotc.core.Flags.Package
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin

import scala.annotation.tailrec

import tpd.*

final class BetterToStringPlugin extends StandardPlugin:
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override def init(options: List[String]): List[PluginPhase] = List(new BetterToStringPluginPhase)

final class BetterToStringPluginPhase extends PluginPhase:

  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: Set[String] = Set(org.polyvariant.AfterPhase.name)

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
