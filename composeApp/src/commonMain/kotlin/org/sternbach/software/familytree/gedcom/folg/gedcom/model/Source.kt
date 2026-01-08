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
 * Date: 12/29/11
 *
 * omit: data
 * add: media type, call number, type, uid, paren, italic, date
 */
class Source : MediaContainer() {
    var id: String? = null
    var author: String? = null
    var title: String? = null
    var abbreviation: String? = null
    var publicationFacts: String? = null
    var text: String? = null
    var repositoryRef: RepositoryRef? = null
    var referenceNumber: String? = null
    var rin: String? = null
    var change: Change? = null
    var mediaType: String? = null
    var callNumber: String? = null
    var type: String? = null
    var typeTag: String? = null
    var uid: String? = null
    var uidTag: String? = null
    var paren: String? = null
    var italic: String? = null
    var date: String? = null

    fun getRepository(gedcom: Gedcom): Repository? {
        return if (repositoryRef != null) repositoryRef!!.getRepository(gedcom) else null
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            if (repositoryRef != null) {
                repositoryRef!!.accept(visitor)
            }
            if (change != null) {
                change!!.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
