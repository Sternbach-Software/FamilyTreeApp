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
 * add: type, aka, married name, media
 * (Media objects are very rare in gedcoms in the wild)
 * note: _aka and _marrnm should be their own Name, but this would break round-tripping, so do it as a separate step
 */
class Name : SourceCitationContainer() {
    var value: String? = null
    var given: String? = null
    var surname: String? = null
    var prefix: String? = null
    var suffix: String? = null

    /**
     * Rarely used
     * @return surname prefix
     */
    var surnamePrefix: String? = null
    var nickname: String? = null
    var fone: String? = null
    var romn: String? = null

    /**
     * Name has a type of ALIA when the GEDCOM had a ALIA sub-tag of INDI
     * @return The type.
     */
    var type: String? = null
    var typeTag: String? = null
    var aka: String? = null
    var akaTag: String? = null
    var foneTag: String? = null
    var romnTag: String? = null
    var marriedName: String? = null
    var marriedNameTag: String? = null

    private fun appendValue(buf: StringBuilder, value: String?) {
        if (value != null) {
            if (buf?.isNotEmpty() == true) {
                buf.append(' ')
            }
            buf.append(value)
        }
    }

    val displayValue: String
        get() {
            if (value != null) {
                return value as String
            } else {
                val buf = StringBuilder()
                appendValue(buf, prefix)
                appendValue(buf, given)
                appendValue(buf, surnamePrefix)
                appendValue(buf, surname)
                appendValue(buf, suffix)
                return buf.toString()
            }
        }


    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
