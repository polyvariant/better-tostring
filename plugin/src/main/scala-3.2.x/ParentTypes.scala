package org.polyvariant

import dotty.tools.dotc.core.Symbols.ClassSymbol
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.core.Types.ClassInfo

extension (clazz: ClassSymbol) {

  // 3.1.x/3.2.x doesn't have this method, this is the inlined definition from 3.3.x
  def parentTypes(using Context): List[Type] = clazz.info match
    case classInfo: ClassInfo => classInfo.declaredParents
    case _                    => Nil

}
