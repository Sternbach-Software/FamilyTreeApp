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
 *
 * omit: event, data
 * add: text and date from data
 */
class SourceCitation : MediaContainer() {
    enum class DataTagContents {
        DATE, TEXT, COMBINED, SEPARATE
    }

    var ref: String? = null
    var value: String? = null
    var page: String? = null
    var date: String? = null
    var text: String? = null
    var quality: String? = null

    // yuck - some gedcom's don't use the data tag, some include write both text and date under the same tag, others use two data tags
    var dataTagContents: DataTagContents? = null // set to null in default case (no data tag) so it isn't saved to json

    fun getSource(gedcom: Gedcom): Source? {
        return gedcom.getSource(ref)
    }

    val textOrValue: String
        /**
         * Use this function to get text from value or text field
         * @return text, or value if text is empty
         */
        get() = if (text != null) text!! else value!!

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
