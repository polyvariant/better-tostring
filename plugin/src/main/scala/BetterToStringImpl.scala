package com.kubukoz

// Source-compatible core between 2.x and 3.x implementations

trait CompilerApi {
  type Tree
  type Clazz
  type Param
  type ParamName
  type Method
  type ClazzParent

  def className(clazz: Clazz): String
  def parentName(parent: ClazzParent): String
  def params(clazz: Clazz): List[Param]
  def literalConstant(value: String): Tree

  def paramName(param: Param): ParamName
  def selectInThis(clazz: Clazz, name: ParamName): Tree
  def concat(l: Tree, r: Tree): Tree

  def createToString(clazz: Clazz, body: Tree): Method
  def addMethod(clazz: Clazz, method: Method): Clazz
  def methodNames(clazz: Clazz): List[String]
  def isCaseClass(clazz: Clazz): Boolean
}

trait BetterToStringImpl[+C <: CompilerApi] {
  val compilerApi: C

  def transformClass(
    clazz: compilerApi.Clazz,
    isNested: Boolean,
    parent: Option[compilerApi.ClazzParent]
  ): compilerApi.Clazz

}

object BetterToStringImpl {

  def instance(
    api: CompilerApi
  ): BetterToStringImpl[api.type] =
    new BetterToStringImpl[api.type] {
      val compilerApi: api.type = api

      import api._

      def transformClass(
        clazz: Clazz,
        isNested: Boolean,
        parent: Option[ClazzParent]
      ): Clazz = {
        val hasToString: Boolean = methodNames(clazz).contains("toString")

        val shouldModify =
          isCaseClass(clazz) && !isNested && !hasToString

        if (shouldModify) overrideToString(clazz, parent)
        else clazz
      }

      private def overrideToString(clazz: Clazz, parent: Option[ClazzParent]): Clazz =
        addMethod(clazz, createToString(clazz, toStringImpl(clazz, parent)))

      private def toStringImpl(clazz: Clazz, parent: Option[ClazzParent]): Tree = {
        val className = api.className(clazz)
        val parentPrefix = parent.map(p => api.parentName(p) ++ ".").getOrElse("")

        val paramListParts: List[Tree] = params(clazz).zipWithIndex.flatMap { case (v, index) =>
          val commaPrefix = if (index > 0) ", " else ""

          val name = paramName(v)

          List(
            literalConstant(commaPrefix ++ name.toString ++ " = "),
            selectInThis(clazz, name)
          )
        }

        val parts =
          List(
            List(literalConstant(parentPrefix ++ className ++ "(")),
            paramListParts,
            List(literalConstant(")"))
          ).flatten

        parts.reduceLeft(concat(_, _))
      }

    }

}
