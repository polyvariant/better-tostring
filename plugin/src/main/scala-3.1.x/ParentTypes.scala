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
