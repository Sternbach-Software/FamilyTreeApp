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
package org.folg.gedcom.visitors

import org.folg.gedcom.model.*
import org.folg.gedcom.model.SourceCitation.DataTagContents
import org.folg.gedcom.parser.ModelParser
import org.gedml.GedcomParser

/**
 * User: Dallan
 * Date: 12/25/11
 *
 * Export a model as GEDCOM
 */
class GedcomWriter : Visitor() {
    private var out: Appendable? = null
    private var eol = "\n"
    private var stack: MutableList<Any?> = mutableListOf()
    
    // We remove IOException as we are appending to a string/builder usually.
    // If Appendable throws, it propagates.

    fun write(gedcom: Gedcom, out: Appendable) {
        this.out = out
        stack.clear()
        
        // Charset detection logic moved or just simplified. 
        // We assume UTF-8 or standard string encoding.
        // If specific line endings are needed based on charset (like MacRoman),
        // we can still detect it from header but for now default to \n
        
        val charset = getCharsetName(gedcom)
        eol = if (charset == "x-MacRoman") "\r" else "\n"
        
        gedcom.accept(this)
        this.out!!.append("0 TRLR$eol")
    }

    private fun getCharsetName(gedcom: Gedcom?): String {
        val header = gedcom?.header
        val generator = header?.generator?.value
        var charset = header?.characterSet?.value
        val version = header?.characterSet?.version
        
        // getCorrectedCharsetName was in GedcomParser companion, likely removed or need to check if available.
        // Since we removed GedcomParser (the SAX one), we might need to copy that logic or ignore it.
        // For writing, we usually trust what is in the header or default to UTF-8 for new files.
        // Let's assume UTF-8 if null.
        
        if (charset.isNullOrEmpty()) {
            return "UTF-8"
        }
        return charset!!
    }

    private fun write(
        tag: String?,
        id: String? = null,
        ref: String? = null,
        value: String? = null,
        forceValueOnSeparateLine: Boolean = false
    ) {
        val level = stack.size
        out!!.append("$level ")
        if (!id.isNullOrEmpty()) {
            out!!.append("@$id@ ")
        }
        out!!.append(tag)
        if (!ref.isNullOrEmpty()) {
            out!!.append(" @$ref@")
        }
        if (!value.isNullOrEmpty()) {
            if (forceValueOnSeparateLine && !value!!.startsWith("\n")) {
                out!!.append(eol + (level + 1) + " CONC ")
            } else {
                out!!.append(" ")
            }
            val buf = StringBuilder(value)
            var cnt = 0
            var lastLine = false
            while (!lastLine) {
                val nlPos = buf.indexOf("\n")
                var line: String
                if (nlPos >= 0) {
                    line = buf.substring(0, nlPos)
                    buf.delete(0, nlPos + 1)
                } else {
                    line = buf.toString()
                    lastLine = true
                }
                if (cnt > 0) {
                    out!!.append(eol + (level + 1) + " CONT ")
                }
                while (line.length > 200) {
                    out!!.append(line.substring(0, 200))
                    line = line.substring(200)
                    out!!.append(eol + (level + 1) + " CONC ")
                }
                out!!.append(line)
                cnt++
            }
        }
        out!!.append(eol)
    }

    private fun write(tag: String?, value: String?) {
        write(tag, null, null, value)
    }

    private fun writeFieldExtensions(fieldName: String?, ec: ExtensionContainer) {
        val moreTags = ec.getExtension(ModelParser.MORE_TAGS_EXTENSION_KEY) as List<GedcomTag>?
        if (moreTags != null) {
            for (tag in moreTags) {
                if (fieldName == tag.parentTagName) {
                    stack.add(Any()) // placeholder
                    writeGedcomTag(tag)
                    stack.removeAt(stack.lastIndex)
                }
            }
        }
    }

    private fun writeString(tag: String?, ec: ExtensionContainer, value: String?) {
        if (!value.isNullOrEmpty()) {
            write(tag, value)
            writeFieldExtensions(tag, ec)
        }
    }

    private fun writeRef(tag: String, ec: ExtensionContainer, ref: String?) {
        if (!ref.isNullOrEmpty()) {
            write(tag, null, ref, null)
            writeFieldExtensions(tag, ec)
        }
    }

    override fun visit(address: Address): Boolean {
        write("ADDR", address.value)
        stack.add(address)
        writeString("ADR1", address, address.addressLine1)
        writeString("ADR2", address, address.addressLine2)
        writeString("CITY", address, address.city)
        writeString("STAE", address, address.state)
        writeString("POST", address, address.postalCode)
        writeString("CTRY", address, address.country)
        writeString("_NAME", address, address.name)
        return true
    }

    override fun visit(association: Association): Boolean {
        write("ASSO", null, association.ref, null)
        stack.add(association)
        writeString("TYPE", association, association.type)
        writeString("RELA", association, association.relation)
        return true
    }

    override fun visit(change: Change?): Boolean {
        write("CHAN")
        stack.add(change)
        return true
    }

    override fun visit(characterSet: CharacterSet): Boolean {
        write("CHAR", characterSet.value)
        stack.add(characterSet)
        writeString("VERS", characterSet, characterSet.version)
        return true
    }

    override fun visit(childRef: ChildRef): Boolean {
        write("CHIL", null, childRef.ref, null)
        stack.add(childRef)
        writeSpouseRefStrings(childRef)
        return true
    }

    override fun visit(dateTime: DateTime): Boolean {
        write("DATE", dateTime.value)
        stack.add(dateTime)
        writeString("TIME", dateTime, dateTime.time)
        return true
    }

    private fun writeEventFactStrings(eventFact: EventFact) {
        writeString("TYPE", eventFact, eventFact.type)
        writeString("DATE", eventFact, eventFact.date)
        writeString("PLAC", eventFact, eventFact.place)
        writeString("CAUS", eventFact, eventFact.cause)
        writeString("RIN", eventFact, eventFact.rin)
        writeString(eventFact.uidTag, eventFact, eventFact.uid)
    }

    override fun visit(eventFact: EventFact): Boolean {
        write(eventFact.tag, eventFact.value)
        stack.add(eventFact)
        writeEventFactStrings(eventFact)
        return true
    }

    private fun writeGedcomTag(tag: GedcomTag?) {
        if (tag == null) return
        write(tag.tag, tag.id, tag.ref, tag.value)
        stack.add(tag)
        for (child in tag.getChildren()) {
            writeGedcomTag(child)
        }
        stack.removeAt(stack.lastIndex)
    }

    override fun visit(extensionKey: String?, extension: Any?): Boolean {
        if (ModelParser.MORE_TAGS_EXTENSION_KEY == extensionKey) {
            val moreTags = extension as List<GedcomTag>?
            for (tag in moreTags!!) {
                if (tag.parentTagName == null) { // if field name is not null, the extension should have been written already
                    writeGedcomTag(tag)
                }
            }
        }
        return true
    }

    private fun writePersonFamilyCommonContainerStrings(pf: PersonFamilyCommonContainer) {
        for (refn in pf.referenceNumbers) {
            writeString("REFN", pf, refn)
        }
        writeString("RIN", pf, pf.rin)
        writeString(pf.uidTag, pf, pf.uid)
    }

    override fun visit(family: Family): Boolean {
        write("FAM", family.id, null, null)
        stack.add(family)
        writePersonFamilyCommonContainerStrings(family)
        return true
    }

    override fun visit(gedcom: Gedcom?): Boolean {
        return true
    }

    override fun visit(gedcomVersion: GedcomVersion): Boolean {
        write("GEDC")
        stack.add(gedcomVersion)
        writeString("VERS", gedcomVersion, gedcomVersion.version)
        writeString("FORM", gedcomVersion, gedcomVersion.form)
        return true
    }

    override fun visit(generator: Generator): Boolean {
        write("SOUR", generator.value)
        stack.add(generator)
        writeString("NAME", generator, generator.name)
        writeString("VERS", generator, generator.version)
        return true
    }

    override fun visit(generatorCorporation: GeneratorCorporation): Boolean {
        write("CORP", generatorCorporation.value)
        stack.add(generatorCorporation)
        writeString("PHON", generatorCorporation, generatorCorporation.phone)
        writeString(generatorCorporation.wwwTag, generatorCorporation, generatorCorporation.www)
        return true
    }

    override fun visit(generatorData: GeneratorData): Boolean {
        write("DATA", generatorData.value)
        stack.add(generatorData)
        writeString("DATE", generatorData, generatorData.date)
        writeString("COPR", generatorData, generatorData.copyright)
        return true
    }

    override fun visit(header: Header): Boolean {
        write("HEAD")
        stack.add(header)
        writeString("DEST", header, header.destination)
        writeString("FILE", header, header.file)
        writeString("COPR", header, header.copyright)
        writeString("LANG", header, header.language)
        writeRef("SUBM", header, header.submitterRef)
        writeRef("SUBN", header, header.submissionRef)
        return true
    }

    override fun visit(ldsOrdinance: LdsOrdinance): Boolean {
        write(ldsOrdinance.tag, ldsOrdinance.value)
        stack.add(ldsOrdinance)
        writeEventFactStrings(ldsOrdinance)
        writeString("STAT", ldsOrdinance, ldsOrdinance.status)
        writeString("TEMP", ldsOrdinance, ldsOrdinance.temple)
        return true
    }

    override fun visit(media: Media): Boolean {
        write("OBJE", media.id, null, null)
        stack.add(media)
        writeString("FORM", media, media.format)
        writeString("TITL", media, media.title)
        writeString("BLOB", media, media.blob)
        writeString(media.fileTag, media, media.file)
        writeString("_PRIM", media, media.primary)
        writeString("_TYPE", media, media.type)
        writeString("_SCBK", media, media.scrapbook)
        writeString("_SSHOW", media, media.slideShow)
        return true
    }

    override fun visit(mediaRef: MediaRef): Boolean {
        write("OBJE", null, mediaRef.ref, null)
        stack.add(mediaRef)
        return true
    }

    override fun visit(name: Name): Boolean {
        // handle ALIA name by recording it with that tag
        val tag: String
        var type = name.type
        if ("ALIA" == type) {
            tag = type
            type = null
        } else {
            tag = "NAME"
        }
        write(tag, name.value)
        stack.add(name)
        writeString("GIVN", name, name.given)
        writeString("SURN", name, name.surname)
        writeString("NPFX", name, name.prefix)
        writeString("NSFX", name, name.suffix)
        writeString("SPFX", name, name.surnamePrefix)
        writeString("NICK", name, name.nickname)
        writeString(name.typeTag, name, type)
        writeString(name.akaTag, name, name.aka)
        writeString(name.marriedNameTag, name, name.marriedName)
        return true
    }

    override fun visit(note: Note): Boolean {
        var visitChildren = false
        if (note.isSourceCitationsUnderValue && note._sourceCitations?.isNotEmpty() == true && !note.value.isNullOrEmpty()) {
            write("NOTE", note.id, null, note.value, true)
            stack.add(note)
            stack.add(Any()) // increment level to 2
            for (sc in note._sourceCitations!!) {
                sc.accept(this)
            }
            stack.removeAt(stack.lastIndex)
            visitChildren = true
        } else {
            write("NOTE", note.id, null, note.value)
            stack.add(note)
        }

        // write note strings
        writeString("RIN", note, note.rin)

        if (visitChildren) {
            // if we return false below we need to visit the rest of the children and pop the stack here
            note.visitContainedObjects(this, false)
            stack.removeAt(stack.lastIndex)
        }
        return !visitChildren
    }

    override fun visit(noteRef: NoteRef): Boolean {
        write("NOTE", null, noteRef.ref, null)
        stack.add(noteRef)
        return true
    }

    override fun visit(parentFamilyRef: ParentFamilyRef): Boolean {
        write("FAMC", null, parentFamilyRef.ref, null)
        stack.add(parentFamilyRef)
        writeSpouseFamilyRefStrings(parentFamilyRef)
        writeString("PEDI", parentFamilyRef, parentFamilyRef.relationshipType)
        writeString("_PRIMARY", parentFamilyRef, parentFamilyRef.primary)
        return true
    }

    override fun visit(parentRelationship: ParentRelationship, isFather: Boolean): Boolean {
        write(if (isFather) "_FREL" else "_MREL", parentRelationship.value)
        stack.add(parentRelationship)
        return true
    }

    override fun visit(person: Person): Boolean {
        write("INDI", person.id, null, null)
        stack.add(person)
        writeRef("ANCI", person, person.ancestorInterestSubmitterRef)
        writeRef("DESI", person, person.descendantInterestSubmitterRef)
        writeString("RFN", person, person.recordFileNumber)
        writeString("PHON", person, person.phone)
        writeString(person.emailTag, person, person.email)
        writePersonFamilyCommonContainerStrings(person)
        return true
    }

    override fun visit(repository: Repository): Boolean {
        write("REPO", repository.id, null, repository.value)
        stack.add(repository)
        writeString("NAME", repository, repository.name)
        writeString("PHON", repository, repository.phone)
        writeString("RIN", repository, repository.rin)
        writeString(repository.emailTag, repository, repository.email)
        writeString(repository.wwwTag, repository, repository.www)
        return true
    }

    override fun visit(repositoryRef: RepositoryRef): Boolean {
        write("REPO", null, repositoryRef.ref, repositoryRef.value)
        stack.add(repositoryRef)
        if (repositoryRef.isMediUnderCalnTag == true ||
            (!repositoryRef.callNumber.isNullOrEmpty())
        ) {
            write("CALN", repositoryRef.callNumber)
        }
        if (repositoryRef.isMediUnderCalnTag == true) {
            stack.add(Any()) // placeholder
        }
        writeString("MEDI", repositoryRef, repositoryRef.mediaType)
        if (repositoryRef.isMediUnderCalnTag == true) {
            stack.removeAt(stack.lastIndex)
        }

        return true
    }

    override fun visit(source: Source): Boolean {
        write("SOUR", source.id, null, null)
        stack.add(source)
        writeString("AUTH", source, source.author)
        writeString("TITL", source, source.title)
        writeString("ABBR", source, source.abbreviation)
        writeString("PUBL", source, source.publicationFacts)
        writeString("TEXT", source, source.text)
        writeString("REFN", source, source.referenceNumber)
        writeString("RIN", source, source.rin)
        writeString("MEDI", source, source.mediaType)
        writeString("CALN", source, source.callNumber)
        writeString(source.typeTag, source, source.type)
        writeString(source.uidTag, source, source.uid)
        writeString("_PAREN", source, source.paren)
        writeString("_ITALIC", source, source.italic)
        writeString("DATE", source, source.date)
        return true
    }

    private fun writeUnderData(tag: String, sourceCitation: SourceCitation, value: String?) {
        if (!value.isNullOrEmpty()) {
            write("DATA")
            stack.add(Any()) // placeholder
            writeString(tag, sourceCitation, value)
            stack.removeAt(stack.lastIndex)
        }
    }

    override fun visit(sourceCitation: SourceCitation): Boolean {
        write("SOUR", null, sourceCitation.ref, sourceCitation.value)
        stack.add(sourceCitation)
        writeString("PAGE", sourceCitation, sourceCitation.page)
        writeString("QUAY", sourceCitation, sourceCitation.quality)
        if (sourceCitation.dataTagContents == DataTagContents.COMBINED &&
            (!sourceCitation.date.isNullOrEmpty() ||
                    !sourceCitation.text.isNullOrEmpty())
        ) {
            write("DATA")
            stack.add(Any()) // placeholder
            writeString("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
            stack.removeAt(stack.lastIndex)
        } else if (sourceCitation.dataTagContents == DataTagContents.DATE) {
            writeUnderData("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
        } else if (sourceCitation.dataTagContents == DataTagContents.TEXT) {
            writeUnderData("TEXT", sourceCitation, sourceCitation.text)
            writeString("DATE", sourceCitation, sourceCitation.date)
        } else if (sourceCitation.dataTagContents == DataTagContents.SEPARATE) {
            writeUnderData("DATE", sourceCitation, sourceCitation.date)
            writeUnderData("TEXT", sourceCitation, sourceCitation.text)
        } else if (sourceCitation.dataTagContents == null) {
            writeString("DATE", sourceCitation, sourceCitation.date)
            writeString("TEXT", sourceCitation, sourceCitation.text)
        }
        return true
    }

    private fun writeSpouseRefStrings(spouseRef: SpouseRef) {
        writeString("_PREF", spouseRef, spouseRef.preferred)
    }

    override fun visit(spouseRef: SpouseRef, isHusband: Boolean): Boolean {
        write(if (isHusband) "HUSB" else "WIFE", null, spouseRef.ref, null)
        stack.add(spouseRef)
        writeSpouseRefStrings(spouseRef)
        return true
    }

    private fun writeSpouseFamilyRefStrings(spouseFamilyRef: SpouseFamilyRef) {
        // nothing to write
    }

    override fun visit(spouseFamilyRef: SpouseFamilyRef): Boolean {
        write("FAMS", null, spouseFamilyRef.ref, null)
        stack.add(spouseFamilyRef)
        writeSpouseFamilyRefStrings(spouseFamilyRef)
        return true
    }

    override fun visit(submission: Submission): Boolean {
        write("SUBN", submission.id, null, null)
        stack.add(submission)
        writeString("DESC", submission, submission.description)
        writeString("ORDI", submission, submission.ordinanceFlag)
        return true
    }

    override fun visit(submitter: Submitter): Boolean {
        write("SUBM", submitter.id, null, submitter.value)
        stack.add(submitter)
        writeString("PHON", submitter, submitter.phone)
        writeString("NAME", submitter, submitter.name)
        writeString("RIN", submitter, submitter.rin)
        writeString("LANG", submitter, submitter.language)
        writeString(submitter.wwwTag, submitter, submitter.www)
        writeString(submitter.emailTag, submitter, submitter.email)
        return true
    }

    override fun endVisit(obj: ExtensionContainer?) {
        if (obj !is Gedcom) {
            stack.removeAt(stack.lastIndex)
        }
    }
}