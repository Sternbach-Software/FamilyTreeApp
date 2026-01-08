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
 * omit: Submitter, Reference type, Restriction, &gt;1 anci, &gt;1 desi
 * add: Uid, Address, phone, email, title (is a Name)
 * change: alia from xref to Name
 */
class Person : PersonFamilyCommonContainer() {
    var id: String? = null
    private var names: MutableList<Name>? = null
    private var famc: MutableList<ParentFamilyRef?>? = null
    private var fams: MutableList<SpouseFamilyRef?>? = null
    private var assos: MutableList<Association>? = null
    var ancestorInterestSubmitterRef: String? = null
    var descendantInterestSubmitterRef: String? = null
    var recordFileNumber: String? = null
    var address: Address? = null
    var phone: String? = null
    var fax: String? = null
    var email: String? = null
    var emailTag: String? = null
    var www: String? = null
    var wwwTag: String? = null

    fun getNames(): List<Name> {
        return names ?: emptyList()
    }

    fun setNames(names: MutableList<Name>?) {
        this.names = names
    }

    fun addName(name: Name) {
        if (names == null) {
            names = mutableListOf()
        }
        names!!.add(name)
    }

    private fun getFamilies(gedcom: Gedcom, familyRefs: List<SpouseFamilyRef?>): List<Family> {
        val families: MutableList<Family> = mutableListOf()
        for (familyRef in familyRefs) {
            val family = familyRef!!.getFamily(gedcom)
            if (family != null) {
                families.add(family)
            }
        }
        return families
    }

    /**
     * Convenience function to dereference parent family refs
     * @param gedcom Gedcom
     * @return list of parent families
     */
    fun getParentFamilies(gedcom: Gedcom): List<Family> {
        return getFamilies(gedcom, parentFamilyRefs)
    }

    val parentFamilyRefs: List<ParentFamilyRef?>
        get() = if (famc != null) famc!! else emptyList<ParentFamilyRef>()

    fun setParentFamilyRefs(famc: MutableList<ParentFamilyRef?>?) {
        this.famc = famc
    }

    fun addParentFamilyRef(parentFamilyRef: ParentFamilyRef?) {
        if (famc == null) {
            famc = mutableListOf()
        }
        famc!!.add(parentFamilyRef)
    }

    /**
     * Convenience function to dereference spouse family refs
     * @param gedcom Gedcom
     * @return list of spouse families
     */
    fun getSpouseFamilies(gedcom: Gedcom): List<Family> {
        return getFamilies(gedcom, spouseFamilyRefs)
    }

    val spouseFamilyRefs: List<SpouseFamilyRef?>
        get() = if (fams != null) fams!! else emptyList<SpouseFamilyRef>()

    fun setSpouseFamilyRefs(fams: MutableList<SpouseFamilyRef?>?) {
        this.fams = fams
    }

    fun addSpouseFamilyRef(spouseFamilyRef: SpouseFamilyRef?) {
        if (fams == null) {
            fams = mutableListOf()
        }
        fams!!.add(spouseFamilyRef)
    }

    val associations: List<Association>
        get() = assos ?: emptyList()

    fun setAssociations(assos: MutableList<Association>?) {
        this.assos = assos
    }

    fun addAssociation(asso: Association) {
        if (assos == null) {
            assos = mutableListOf()
        }
        assos!!.add(asso)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            for (name in getNames()) {
                name.accept(visitor)
            }
            for (parentFamilyRef in parentFamilyRefs) {
                parentFamilyRef!!.accept(visitor)
            }
            for (spouseFamilyRef in spouseFamilyRefs) {
                spouseFamilyRef!!.accept(visitor)
            }
            for (association in associations) {
                association.accept(visitor)
            }
            if (address != null) {
                address!!.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
