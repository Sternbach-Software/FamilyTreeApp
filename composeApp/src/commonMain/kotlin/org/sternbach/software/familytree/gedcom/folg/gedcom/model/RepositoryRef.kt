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
 * Date: 12/30/11
 *
 * omit: multiple call numbers
 * add: value (a few sources inline the repo name as a value)
 * move: media type from under call number to directly under repository ref
 */
class RepositoryRef : NoteContainer() {
    var ref: String? = null
    var value: String? = null
    var callNumber: String? = null
    var mediaType: String? = null
    private var mediUnderCalnTag: String? = null // use string instead of boolean so it isn't saved to json when false
    var isMediUnderCalnTag: Boolean?
        get() = mediUnderCalnTag != null
        set(value) {
            mediUnderCalnTag = (if (value == true) "true" else null)
        }

    fun getRepository(gedcom: Gedcom): Repository? {
        return gedcom.getRepository(ref)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
