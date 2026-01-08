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
 *
 * omit: Submitter, Submission, Place
 */
class Header : NoteContainer() {
    var generator: Generator? = null
    var destination: String? = null
    var dateTime: DateTime? = null
    var submitterRef: String? = null

    /**
     * Use Gedcom.getSubmission in place of this function
     * @return submission reference
     */
    var submissionRef: String? = null

    /**
     * Use Gedcom.getSubmission in place of this function
     * @return submission
     */
    var submission: Submission? = null
    var file: String? = null
    var copyright: String? = null
    var gedcomVersion: GedcomVersion? = null
    var characterSet: CharacterSet? = null
    var language: String? = null

    fun getSubmitter(gedcom: Gedcom): Submitter? {
        return gedcom.getSubmitter(submitterRef)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            if (generator != null) {
                generator!!.accept(visitor)
            }
            if (dateTime != null) {
                dateTime!!.accept(visitor)
            }
            if (submission != null) {
                submission!!.accept(visitor)
            }
            if (gedcomVersion != null) {
                gedcomVersion!!.accept(visitor)
            }
            if (characterSet != null) {
                characterSet!!.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
