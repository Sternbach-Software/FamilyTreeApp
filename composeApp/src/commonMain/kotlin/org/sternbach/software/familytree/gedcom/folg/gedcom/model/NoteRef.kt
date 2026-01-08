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
 * User: dallan
 * Date: 1/3/12
 * don't make this a SourceCitationContainer, because that would allow notes within note refs
 */
class NoteRef : ExtensionContainer() {
    var ref: String? = null
    private var sourceCitations: MutableList<SourceCitation>? = null

    /**
     * Convenience function to dereference note
     * @param gedcom Gedcom
     * @return referenced note
     */
    fun getNote(gedcom: Gedcom): Note? {
        return gedcom.getNote(ref)
    }

    fun getSourceCitations(): List<SourceCitation> {
        return sourceCitations ?: emptyList()
    }

    fun setSourceCitations(sourceCitations: MutableList<SourceCitation>?) {
        this.sourceCitations = sourceCitations
    }

    fun addSourceCitation(sourceCitation: SourceCitation) {
        if (sourceCitations == null) {
            sourceCitations = mutableListOf()
        }
        sourceCitations!!.add(sourceCitation)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            for (sourceCitation in getSourceCitations()) {
                sourceCitation.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
