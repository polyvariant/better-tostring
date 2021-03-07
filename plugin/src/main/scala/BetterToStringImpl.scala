package com.kubukoz

// Source-compatible core between 2.x and 3.x implementations

trait CompilerApi {
  // type Parent

  type Tree
  type Clazz
  type Param
  type ParamName
  type Method

  def className(clazz: Clazz): String
  def params(clazz: Clazz): List[Param]
  def literalConstant(value: String): Tree

  def paramName(param: Param): ParamName
  def selectInThis(clazz: Clazz, name: ParamName): Tree
  def concat(l: Tree, r: Tree): Tree

  def createToString(clazz: Clazz, body: Tree): Method
  def addMethod(clazz: Clazz, method: Method): Clazz
}

trait BetterToStringImpl[+C <: CompilerApi] {
  val compilerApi: C
  def overrideToString(clazz: compilerApi.Clazz): compilerApi.Clazz
  // def transformClasses()
}

object BetterToStringImpl {
  def instance(
      api: CompilerApi
  ): BetterToStringImpl[api.type] =
    new BetterToStringImpl[api.type] {
      val compilerApi: api.type = api

      import api._

      def overrideToString(clazz: Clazz): Clazz = {
        addMethod(clazz, createToString(clazz, toStringImpl(clazz)))
      }
      private def toStringImpl(clazz: Clazz): Tree = {
        val className = api.className(clazz)

        val paramListParts: List[Tree] = params(clazz).zipWithIndex.flatMap {
          case (v, index) =>
            val commaPrefix = if (index > 0) ", " else ""

            val name = paramName(v)

            List(
              literalConstant(commaPrefix ++ name.toString ++ " = "),
              selectInThis(clazz, name)
            )
        }

        val parts =
          List(
            List(literalConstant(className ++ "(")),
            paramListParts,
            List(literalConstant(")"))
          ).flatten

        parts.reduceLeft(concat(_, _))
      }
    }
}
