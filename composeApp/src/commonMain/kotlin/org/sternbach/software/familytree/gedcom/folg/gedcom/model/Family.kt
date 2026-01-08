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
 * omit: Submitter, Reference type
 * add: Uid
 */
class Family : PersonFamilyCommonContainer() {
    var id: String? = null
    var _husbandRefs: MutableList<SpouseRef?>? =
        null // may be >1 if not sure which one (presumably one is preferred)
    var _wifeRefs: MutableList<SpouseRef?>? = null // ditto
    var _childRefs: MutableList<ChildRef?>? = null

    private fun getFamilyMembers(gedcom: Gedcom, memberRefs: List<SpouseRef?>, preferredFirst: Boolean): List<Person> {
        val members: MutableList<Person> = mutableListOf()
        for (memberRef in memberRefs) {
            val member = memberRef!!.getPerson(gedcom)
            if (member != null) {
                if (preferredFirst && "Y" == memberRef.preferred) {
                    members.add(0, member)
                } else {
                    members.add(member)
                }
            }
        }
        return members
    }

    /**
     * Convenience function to dereference husband refs
     * Return preferred in first position
     * @param gedcom Gedcom
     * @return list of husbands, generally just one unless there are several alternatives with one preferred
     */
    fun getHusbands(gedcom: Gedcom): List<Person> {
        return getFamilyMembers(gedcom, getHusbandRefs(), true)
    }

    private fun getHusbandRefs(): List<SpouseRef?> = _husbandRefs ?: emptyList<SpouseRef>()

    fun addHusband(husband: SpouseRef?) {
        if (_husbandRefs == null) {
            _husbandRefs = mutableListOf()
        }
        _husbandRefs!!.add(husband)
    }

    /**
     * Convenience function to dereference wife refs
     * Return preferred in first position
     * @param gedcom Gedcom
     * @return list of wives, generally just one unless there are several alternatives with one preferred
     */
    fun getWives(gedcom: Gedcom): List<Person> {
        return getFamilyMembers(gedcom, getWifeRefs(), true)
    }

    private fun getWifeRefs(): List<SpouseRef?> = _wifeRefs ?: emptyList<SpouseRef>()

    fun addWife(wife: SpouseRef?) {
        if (_wifeRefs == null) {
            _wifeRefs = mutableListOf()
        }
        _wifeRefs!!.add(wife)
    }

    /**
     * Convenience function to dereference child refs
     * @param gedcom Gedcom
     * @return list of children
     */
    fun getChildren(gedcom: Gedcom): List<Person> {
        return getFamilyMembers(gedcom, getChildRefs(), false)
    }

    fun getChildRefs(): List<ChildRef?> = _childRefs ?: emptyList<ChildRef>()

    fun addChild(childRef: ChildRef?) {
        if (_childRefs == null) {
            _childRefs = mutableListOf()
        }
        _childRefs!!.add(childRef)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            for (husband in getHusbandRefs()) {
                husband!!.accept(visitor, true)
            }
            for (wife in getWifeRefs()) {
                wife!!.accept(visitor, false)
            }
            for (childRef in getChildRefs()) {
                childRef!!.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
