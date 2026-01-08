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
 * Simple data structure for a gedcom node - json-friendly
 * User: Dallan
 * Date: 12/23/11
 */
class GedcomTag(var id: String? = null, var tag: String? = null, var ref: String? = null) : Comparable<GedcomTag> {
    var value: String? = null
    var parentTagName: String? = null // used by ModelParser to store tags under string fields
    var _children: MutableList<GedcomTag>? = null

    fun appendValue(value: String?) {
        if (value != null) {
            if (this.value == null) {
                this.value = value
            } else {
                this.value += value
            }
        }
    }

    fun getChildren(): List<GedcomTag> = _children ?: emptyList()

    fun addChild(child: GedcomTag?) {
        if (child != null) {
            if (_children == null) {
                _children = mutableListOf()
            }
            _children!!.add(child)
        }
    }

    private fun getSortedChildren(children: List<GedcomTag>): List<GedcomTag> = children.sorted()

    val isEmpty: Boolean
        get() = (id.isNullOrEmpty()) &&
                (ref.isNullOrEmpty()) &&
                (parentTagName.isNullOrEmpty()) &&
                (value.isNullOrEmpty()) &&
                (_children.isNullOrEmpty())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        val gt = other as GedcomTag

        if ((tag ?: "") != (gt.tag ?: "")) return false
        if ((id ?: "") != (gt.id ?: "")) return false
        if ((ref ?: "") != (gt.ref ?: "")) return false
        if ((parentTagName ?: "") != (gt.parentTagName ?: "")) return false
        if ((value ?: "") != (gt.value ?: "")) return false
        
        val thisChildren = getChildren()
        val otherChildren = gt.getChildren()
        
        if (thisChildren.size != otherChildren.size) return false

        val sortedChildren = getSortedChildren(thisChildren)
        val compareChildren = getSortedChildren(otherChildren)

        for (indx in sortedChildren.indices) {
            if (sortedChildren[indx] != compareChildren[indx]) {
                // Logging removed
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = if (!id.isNullOrEmpty()) id.hashCode() else 0
        result = 31 * result + (if (!tag.isNullOrEmpty()) tag.hashCode() else 0)
        result = 31 * result + (if (!ref.isNullOrEmpty()) ref.hashCode() else 0)
        result = 31 * result + (if (!parentTagName.isNullOrEmpty()) parentTagName.hashCode() else 0)
        result = 31 * result + (if (!value.isNullOrEmpty()) value.hashCode() else 0)

        // Calculate the children hash code based on the actual value of each child hashcode
        if (_children != null) {
            for (child in getSortedChildren(_children!!)) {
                result = 31 * result + child.hashCode()
            }
        }

        return result
    }

    override fun toString(): String {
        val buf = StringBuilder()
        if (tag != null) {
            buf.append(" tag:$tag")
        }
        if (id != null) {
            buf.append(" id:$id")
        }
        if (ref != null) {
            buf.append(" ref:$ref")
        }
        if (parentTagName != null) {
            buf.append(" parentTag:$parentTagName")
        }
        if (value != null) {
            buf.append(" value:$value")
        }
        if (_children != null) {
            buf.append(" [")
            for (child in getSortedChildren(_children!!)) {
                buf.append(child.toString())
            }
            buf.append(" ]")
        }
        return buf.toString()
    }

    override fun compareTo(other: GedcomTag): Int {
        var c = (tag ?: "").compareTo(other.tag ?: "")
        if (c != 0) return c
        c = (id ?: "").compareTo(other.id ?: "")
        if (c != 0) return c
        c = (ref ?: "").compareTo(other.ref ?: "")
        if (c != 0) return c
        c = (parentTagName ?: "").compareTo(other.parentTagName ?: "")
        if (c != 0) return c
        c = (value ?: "").compareTo(other.value ?: "")
        if (c != 0) return c
        return this.hashCode().compareTo(other.hashCode())
    }
}
