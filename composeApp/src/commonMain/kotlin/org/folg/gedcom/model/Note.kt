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
 * Date: 12/26/11
 *
 * omit: reference number
 * don't make this a SourceCitationContainer, because that would allow nested notes
 */
class Note : ExtensionContainer() {
    var id: String? = null
    var value: String? = null
    var rin: String? = null
    var change: Change? = null
    var _sourceCitations: MutableList<SourceCitation>? = null
    var isSourceCitationsUnderValue: Boolean =
        false // yuck: Reunion does this: 0 NOTE 1 CONT ... 2 SOUR; remember for round-trip

    fun getSourceCitations(): List<SourceCitation> {
        return _sourceCitations ?: emptyList()
    }

    fun setSourceCitations(sourceCitations: MutableList<SourceCitation>?) {
        this._sourceCitations = sourceCitations
    }

    fun addSourceCitation(sourceCitation: SourceCitation) {
        if (_sourceCitations == null) {
            _sourceCitations = mutableListOf()
        }
        _sourceCitations!!.add(sourceCitation)
    }

    fun visitContainedObjects(visitor: Visitor, includeSourceCitations: Boolean) {
        if (change != null) {
            change!!.accept(visitor)
        }
        if (includeSourceCitations) {
            for (sourceCitation in getSourceCitations()) {
                sourceCitation.accept(visitor)
            }
        }
        super.visitContainedObjects(visitor)
    }

    override fun visitContainedObjects(visitor: Visitor) {
        visitContainedObjects(visitor, true)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            this.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
