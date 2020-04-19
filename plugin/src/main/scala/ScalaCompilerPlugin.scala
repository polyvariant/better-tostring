package com.kubukoz

import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.transform.TypingTransformers
import scala.reflect.internal.Flags

class ScalaCompilerPlugin(override val global: Global) extends Plugin {
  override val name: String = "better-tostring"
  override val description: String = "scala compiler plugin for better default toString implementations"
  override val components: List[PluginComponent] = List(new ScalaCompilerPluginComponent(global))
}

class ScalaCompilerPluginComponent(val global: Global) extends PluginComponent with TypingTransformers {
  import global._
  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: List[String] = List("parser")

  override def newPhase(prev: Phase): Phase = new StdPhase(prev) {

    override def apply(unit: CompilationUnit): Unit = {
      val trans = new TypingTransformer(unit) {
        private def addToString(clazz: ClassDef): ClassDef = {
          val params = clazz.impl.body.collect {
            case v: ValDef if v.mods.hasFlag(Flags.CASEACCESSOR) => v
          }

          val toStringImpl: Tree = {
            val className = clazz.name.toString()

            val paramListParts: List[Tree] = params.zipWithIndex.flatMap {
              case (v, index) =>
                val commaPrefix = if (index > 0) ", " else ""

                List(
                  Literal(Constant(commaPrefix ++ v.name.toString ++ " = ")),
                  q"this.${v.name}"
                )
            }

            val parts =
              List(
                List(Literal(Constant(className ++ "("))),
                paramListParts,
                List(Literal(Constant(")")))
              ).flatten

            parts.reduceLeft((a, b) => q"$a + $b")
          }

          val methodBody = DefDef(
            Modifiers(Flags.OVERRIDE),
            TermName("toString"),
            Nil,
            List(List()),
            Ident(TypeName("String")),
            toStringImpl
          )

          val newClass = clazz.copy(impl = clazz.impl.copy(body = clazz.impl.body :+ methodBody))

          println(clazz.name.toString())
          newClass
        }
        private def transformClass(clazz: ClassDef): ClassDef = {
          val hasCustomToString: Boolean = clazz.impl.body.exists {

            case fun: DefDef =>
              //so meta
              fun.name.toString == "toString"
            case _ => false
          }

          val shouldModify = !hasCustomToString

          if (shouldModify) addToString(clazz)
          else clazz
        }

        private def modifyClasses(f: ClassDef => ClassDef)(tree: Tree): Tree = tree match {
          case p: PackageDef => p.copy(stats = p.stats.map(modifyClasses(f)))
          case m: ModuleDef  => m.copy(impl = m.impl.copy(body = m.impl.body.map(modifyClasses(f))))
          //Only case classes
          case clazz: ClassDef if clazz.mods.hasFlag(Flags.CASE) => f(clazz)
          case other                                             => other
        }

        override def transform(tree: Tree): Tree = modifyClasses(transformClass)(tree)
      }

      trans.transformUnit(unit)
    }
  }
}
