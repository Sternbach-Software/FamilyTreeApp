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
 * add: name
 * note: addressLine1+2 are rarely used; addresses are generally stored in Value
 */
class Address : ExtensionContainer() {
    var value: String? = null
    var addressLine1: String? = null
    var addressLine2: String? = null
    var addressLine3: String? = null
    var city: String? = null
    var state: String? = null
    var postalCode: String? = null
    var country: String? = null
    var name: String? = null

    private fun appendValue(buf: StringBuilder, value: String?) {
        if (value != null) {
            if (buf?.isNotEmpty() == true) {
                buf.append("\n")
            }
            buf.append(value)
        }
    }

    val displayValue: String
        get() {
            val buf = StringBuilder()
            appendValue(buf, value)
            appendValue(buf, addressLine1)
            appendValue(buf, addressLine2)
            appendValue(buf, addressLine3)
            appendValue(
                buf,
                (if (city != null) city else "") + (if (city != null && state != null) ", " else "") + (if (state != null) state else "") +
                        (if ((city != null || state != null) && this.postalCode != null) " " else "") + (if (postalCode != null) postalCode else "")
            )
            appendValue(buf, country)
            return buf.toString()
        }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
