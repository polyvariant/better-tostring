package org.polyvariant

import scala.tools.nsc.Global
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform.TypingTransformers

final class BetterToStringPlugin(override val global: Global) extends Plugin {
  override val name: String = "better-tostring"
  override val description: String =
    "scala compiler plugin for better default toString implementations"

  override val components: List[PluginComponent] = List(
    new BetterToStringPluginComponent(global)
  )

}

final class BetterToStringPluginComponent(val global: Global) extends PluginComponent with TypingTransformers {
  import global._
  override val phaseName: String = "better-tostring-phase"
  override val runsAfter: List[String] = List("parser")

  private val api: Scala2CompilerApi[global.type] = Scala2CompilerApi.instance(global)
  private val impl = BetterToStringImpl.instance(api)

  private def modifyClasses(tree: Tree, enclosingObject: Option[ModuleDef]): Tree =
    tree match {
      case p: PackageDef => p.copy(stats = p.stats.map(modifyClasses(_, None)))

      case m: ModuleDef if m.mods.isCase =>
        // isNested=false for the same reason as in the ClassDef case
        impl.transformClass(api.Classable.Obj(m), isNested = false, enclosingObject).merge

      case m: ModuleDef =>
        m.copy(impl = m.impl.copy(body = m.impl.body.map(modifyClasses(_, Some(m)))))

      case clazz: ClassDef =>
        impl
          .transformClass(
            api.Classable.Clazz(clazz),
            // If it was nested, we wouldn't be in this branch.
            // Scala 2.x compiler API limitation (classes can't tell what the owner is).
            // This should be more optimal as we don't traverse every template, but it hasn't been benchmarked.
            isNested = false,
            enclosingObject
          )
          .merge

      case other => other
    }

  override def newPhase(prev: Phase): Phase = new StdPhase(prev) {

    override def apply(unit: CompilationUnit): Unit =
      new Transformer {
        override def transform(tree: Tree): Tree = modifyClasses(tree, None)
      }.transformUnit(unit)

  }

}
