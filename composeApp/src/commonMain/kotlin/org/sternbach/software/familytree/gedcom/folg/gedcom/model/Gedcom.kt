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
 * Date: 12/24/11
 */
class Gedcom : ExtensionContainer() {
    var header: Header? = null
    private var subms: MutableList<Submitter>? = null
    private var subn: Submission? = null
    private var people: MutableList<Person>? = null
    private var families: MutableList<Family>? = null
    private var media: MutableList<Media>? = null
    private var notes: MutableList<Note>? = null
    private var sources: MutableList<Source>? = null
    private var repositories: MutableList<Repository>? = null

    private var personIndex: MutableMap<String?, Person>? = null

    private var familyIndex: MutableMap<String?, Family>? = null

    private var mediaIndex: MutableMap<String?, Media>? = null

    private var noteIndex: MutableMap<String?, Note>? = null

    private var sourceIndex: MutableMap<String?, Source>? = null

    private var repositoryIndex: MutableMap<String?, Repository>? = null

    private var submitterIndex: MutableMap<String?, Submitter>? = null

    fun getPeople(): List<Person> {
        return people ?: emptyList()
    }

    fun getPerson(id: String?): Person? {
        return personIndex?.get(id)
    }

    fun setPeople(people: MutableList<Person>?) {
        this.people = people
    }

    fun addPerson(person: Person) {
        if (people == null) {
            people = mutableListOf()
        }
        people!!.add(person)
        if (personIndex != null) {
            personIndex!![person.id] = person
        }
    }

    fun getFamilies(): List<Family> {
        return families ?: emptyList()
    }

    fun getFamily(id: String?): Family? {
        return familyIndex?.get(id)
    }

    fun setFamilies(families: MutableList<Family>?) {
        this.families = families
    }

    fun addFamily(family: Family) {
        if (families == null) {
            families = mutableListOf()
        }
        families!!.add(family)
        if (familyIndex != null) {
            familyIndex!![family.id] = family
        }
    }

    fun getMedia(): List<Media> {
        return media ?: emptyList()
    }

    fun getMedia(id: String?): Media? {
        return mediaIndex?.get(id)
    }

    fun setMedia(media: MutableList<Media>?) {
        this.media = media
    }

    fun addMedia(m: Media) {
        if (media == null) {
            media = mutableListOf()
        }
        media!!.add(m)
        if (mediaIndex != null) {
            mediaIndex!![m.id] = m
        }
    }

    fun getNotes(): List<Note> {
        return notes ?: emptyList()
    }

    fun getNote(id: String?): Note? {
        return noteIndex?.get(id)
    }

    fun setNotes(notes: MutableList<Note>?) {
        this.notes = notes
    }

    fun addNote(note: Note) {
        if (notes == null) {
            notes = mutableListOf()
        }
        notes!!.add(note)
        if (noteIndex != null) {
            noteIndex!![note.id] = note
        }
    }

    fun getSources(): List<Source> {
        return sources ?: emptyList()
    }

    fun getSource(id: String?): Source? {
        return sourceIndex?.get(id)
    }

    fun setSources(sources: MutableList<Source>?) {
        this.sources = sources
    }

    fun addSource(source: Source) {
        if (sources == null) {
            sources = mutableListOf()
        }
        sources!!.add(source)
        if (sourceIndex != null) {
            sourceIndex!![source.id] = source
        }
    }

    fun getRepositories(): List<Repository> {
        return repositories ?: emptyList()
    }

    fun getRepository(id: String?): Repository? {
        return repositoryIndex?.get(id)
    }

    fun setRepositories(repositories: MutableList<Repository>?) {
        this.repositories = repositories
    }

    fun addRepository(repository: Repository) {
        if (repositories == null) {
            repositories = mutableListOf()
        }
        repositories!!.add(repository)
        if (repositoryIndex != null) {
            repositoryIndex!![repository.id] = repository
        }
    }

    fun getSubmitter(id: String?): Submitter? {
        return submitterIndex?.get(id)
    }

    val submitters: List<Submitter>
        get() = subms ?: emptyList()

    fun setSubmitters(submitters: MutableList<Submitter>?) {
        this.subms = submitters
    }

    fun addSubmitter(submitter: Submitter) {
        if (subms == null) {
            subms = mutableListOf()
        }
        subms!!.add(submitter)

        if (submitterIndex != null) {
            submitterIndex!![submitter.id] = submitter
        }
    }

    var submission: Submission?
        /**
         * Use this function in place of Header.getSubmission
         *
         * @return Submission top-level record or from header
         */
        get() {
            if (subn != null) {
                return subn
            } else if (header != null) {
                return header?.submission
            }
            return null
        }
        set(subn) {
            this.subn = subn
        }

    fun updateReferences() {
        for (person in getPeople()) {
            for (spouseRef in person.spouseFamilyRefs) {
                val family = getFamily(spouseRef?.ref) ?: continue
                
                val spouseRefInHusbands = family._husbandRefs?.any { ref -> ref?.ref == person.id } == true
                val spouseRefInWives = family._wifeRefs?.any { ref -> ref?.ref == person.id } == true

                if (!spouseRefInHusbands && !spouseRefInWives) {
                    val fact = person._eventsFacts?.find { it.tag == "SEX" }
                    val ref = SpouseRef()
                    ref.ref = person.id

                    if (fact?.value == "F") {
                        family.addWife(ref)
                    } else {
                        family.addHusband(ref)
                    }
                }
            }

            for (parentRef in person.parentFamilyRefs) {
                val family = getFamily(parentRef?.ref) ?: continue
                val containsReference = family._childRefs?.any { ref -> ref?.ref == person.id } == true
                
                if (!containsReference) {
                    val ref = ChildRef()
                    ref.ref = person.id
                    family.addChild(ref)
                }
            }
        }

        for (family in getFamilies()) {
            for (ref in family._husbandRefs ?: emptyList()) {
                val person = getPerson(ref?.ref) ?: continue
                val containsRef = person.spouseFamilyRefs.any { it?.ref == family.id }
                
                if (!containsRef) {
                    val spouseFamilyRef = SpouseFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person.addSpouseFamilyRef(spouseFamilyRef)
                }
            }
            for (ref in family._wifeRefs ?: emptyList()) {
                val person = getPerson(ref?.ref) ?: continue
                val containsRef = person.spouseFamilyRefs.any { it?.ref == family.id }
                
                if (!containsRef) {
                    val spouseFamilyRef = SpouseFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person.addSpouseFamilyRef(spouseFamilyRef)
                }
            }

            for (ref in family._childRefs ?: emptyList()) {
                val person = getPerson(ref?.ref) ?: continue
                val containsRef = person.parentFamilyRefs.any { it?.ref == family.id }
                
                if (!containsRef) {
                    val spouseFamilyRef = ParentFamilyRef()
                    spouseFamilyRef.ref = family.id
                    person.addParentFamilyRef(spouseFamilyRef)
                }
            }
        }
    }

    fun createIndexes() {
        personIndex = mutableMapOf()
        for (person in getPeople()) {
            personIndex!![person.id] = person
        }
        familyIndex = mutableMapOf()
        for (family in getFamilies()) {
            familyIndex!![family.id] = family
        }
        mediaIndex = mutableMapOf()
        for (m in getMedia()) {
            mediaIndex!![m.id] = m
        }
        noteIndex = mutableMapOf()
        for (note in getNotes()) {
            noteIndex!![note.id] = note
        }
        sourceIndex = mutableMapOf()
        for (source in getSources()) {
            sourceIndex!![source.id] = source
        }
        repositoryIndex = mutableMapOf()
        for (repository in getRepositories()) {
            repositoryIndex!![repository.id] = repository
        }

        submitterIndex = mutableMapOf()
        for (submitter in submitters) {
            submitterIndex!![submitter.id] = submitter
        }
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            if (header != null) {
                header!!.accept(visitor)
            }
            for (submitter in submitters) {
                submitter.accept(visitor)
            }
            if (subn != null) {
                subn!!.accept(visitor)
            }
            for (person in getPeople()) {
                person.accept(visitor)
            }
            for (family in getFamilies()) {
                family.accept(visitor)
            }
            for (media in getMedia()) {
                media.accept(visitor)
            }
            for (note in getNotes()) {
                note.accept(visitor)
            }
            for (source in getSources()) {
                source.accept(visitor)
            }
            for (repository in getRepositories()) {
                repository.accept(visitor)
            }
            super.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }
}
