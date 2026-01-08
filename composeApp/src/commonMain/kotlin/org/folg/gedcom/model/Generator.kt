/*
 * Copyright 2011 Foundation for On-Line Genealogy, Inc.
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
package org.folg.gedcom.model

/**
 * User: Dallan
 * Date: 12/25/11
 */
class Generator : ExtensionContainer() {
    var value: String? = null
    var name: String? = null
    var version: String? = null
    var generatorCorporation: GeneratorCorporation? = null
    var generatorData: GeneratorData? = null

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            if (generatorCorporation != null) {
                generatorCorporation!!.accept(visitor)
            }
            if (generatorData != null) {
                generatorData!!.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
