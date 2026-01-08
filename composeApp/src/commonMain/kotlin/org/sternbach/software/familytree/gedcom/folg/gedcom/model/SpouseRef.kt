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
 * Date: 12/27/11
 */
open class SpouseRef : ExtensionContainer() {
    var ref: String? = null
    var preferred: String? = null

    /**
     * Convenience function to dereference person
     * @param gedcom Gedcom
     * @return referenced person
     */
    fun getPerson(gedcom: Gedcom): Person? {
        return gedcom.getPerson(ref)
    }

    override fun accept(visitor: Visitor) {
        throw RuntimeException("Not implemented - pass isHusband")
    }

    /**
     * Handle the visitor
     * @param visitor Visitor
     * @param isHusband false for wife; ChildRef overrides this method
     */
    fun accept(visitor: Visitor, isHusband: Boolean) {
        if (visitor.visit(this, isHusband)) {
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
