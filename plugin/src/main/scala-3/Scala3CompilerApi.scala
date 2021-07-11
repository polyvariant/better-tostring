package com.kubukoz

import dotty.tools.dotc.ast.Trees
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.*
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.core.Flags.CaseAccessor
import dotty.tools.dotc.core.Flags.CaseClass
import dotty.tools.dotc.core.Flags.Module
import dotty.tools.dotc.core.Flags.Override
import dotty.tools.dotc.core.Flags.Package
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.ClassSymbol
import dotty.tools.dotc.core.Types

import tpd.*

trait Scala3CompilerApi extends CompilerApi:
  type Tree = Trees.Tree[Types.Type]
  type Clazz = Scala3CompilerApi.ClassContext
  type Param = ValDef
  type ParamName = Names.TermName
  type Method = DefDef
  type EnclosingObject = Symbols.Symbol

object Scala3CompilerApi:
  final case class ClassContext(t: Template, clazz: ClassSymbol):
    def mapTemplate(f: Template => Template): ClassContext = copy(t = f(t))

  def instance(using Context): Scala3CompilerApi = new Scala3CompilerApi:

    def params(clazz: Clazz): List[Param] =
      clazz.t.body.collect {
        case v: ValDef if v.mods.is(CaseAccessor) => v
      }

    def className(clazz: Clazz): String =
      clazz.clazz.originalName.toString

    def isPackageOrPackageObject(enclosingObject: EnclosingObject): Boolean =
      enclosingObject.is(Package) || enclosingObject.isPackageObject

    def enclosingObjectName(enclosingObject: EnclosingObject): String =
      enclosingObject.effectiveName.toString

    def literalConstant(value: String): Tree = Literal(Constant(value))
    def paramName(param: Param): ParamName = param.name
    def selectInThis(clazz: Clazz, name: ParamName): Tree = This(clazz.clazz).select(name)
    def concat(l: Tree, r: Tree): Tree = l.select("+".toTermName).appliedTo(r)

    def createToString(owner: Clazz, body: Tree): Method = {
      val clazz = owner.clazz
      // this was adapted from dotty.tools.dotc.transform.SyntheticMembers (line 115)
      val sym = Symbols.defn.Any_toString

      val toStringSymbol = sym
        .copy(
          owner = clazz,
          flags = sym.flags | Override,
          info = clazz.thisType.memberInfo(sym),
          coord = clazz.coord
        )
        .entered
        .asTerm

      DefDef(toStringSymbol, body)
    }

    def addMethod(clazz: Clazz, method: Method): Clazz =
      clazz.mapTemplate(t => cpy.Template(t)(body = t.body :+ method))

    // note: also returns vals because why not
    def methodNames(clazz: Clazz): List[String] =
      clazz.t.body.collect { case d: (DefDef | ValDef) =>
        d.name.toString
      }

    def isCaseClass(clazz: Clazz): Boolean =
      // for some reason, this is true for case objects too
      clazz.clazz.flags.is(CaseClass)

    def isEnum(clazz: Clazz): Boolean =
      clazz.clazz.isSubClass(Symbols.requiredClass("scala.runtime.EnumValue"))

    def isObject(clazz: Clazz): Boolean =
      clazz.clazz.flags.is(Module)

end Scala3CompilerApi
