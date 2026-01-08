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
package org.folg.gedcom.parser

import org.folg.gedcom.model.*
import org.folg.gedcom.model.SourceCitation.DataTagContents
import org.gedml.GedcomParser
import org.gedml.GedcomParserListener

/**
 * User: Dallan
 * Date: 12/25/11
 */
class ModelParser : GedcomParserListener {
    private var tagStack: MutableList<String> = mutableListOf()
    private var objectStack: MutableList<Any> = mutableListOf()
    var gedcom: Gedcom? = null
        private set
    
    var errors: MutableList<String> = mutableListOf()
    var warnings: MutableList<String> = mutableListOf()

    private fun joinTagStack(): String {
        return tagStack.drop(1).joinToString(" ")
    }

    override fun startDocument() {
        gedcom = null
        tagStack.clear()
        objectStack.clear()
        errors.clear()
        warnings.clear()
    }

    override fun endDocument() {
        // ignore
    }
    
    override fun attribute(name: String, value: String) {
        // Not used in current GEDCOM parser logic which passes ID/REF in startElement
    }

    override fun warning(message: String, lineNumber: Int) {
        warnings.add("Warning: $message @ $lineNumber")
    }

    override fun error(message: String, lineNumber: Int) {
        errors.add("Error: $message @ $lineNumber")
    }
    
    override fun fatalError(message: String, lineNumber: Int) {
        errors.add("Fatal Error: $message @ $lineNumber")
        throw RuntimeException("Fatal Error: $message @ $lineNumber")
    }

    enum class Tag {
        ABBR, ADDR, ADR1, ADR2, ADR3, _AKA, ALIA, ANCI, ASSO, AUTH,
        BLOB,
        CALN, CAUS, CHAN, CHAR, CHIL, CITY, CONC, CONT, COPR, CORP, CTRY,
        DATA, DATE, DESC, DESI, DEST,
        EMAIL, _EMAIL, _EML,
        FAM, FAMC, FAMS, FAX, _FILE, FILE, FONE, FORM, _FREL,
        GED, GEDC, GIVN,
        HEAD, HUSB,
        INDI, _ITALIC,
        LANG,
        _MARRNM, _MAR, _MARNM, MEDI, _MREL,
        NAME, _NAME, NICK, NOTE, NPFX, NSFX,
        OBJE, ORDI,
        PAGE, _PAREN, PEDI, PHON, POST, PLAC, _PREF, _PRIM, _PRIMARY, PUBL,
        QUAY,
        REFN, RELA, REPO, RFN, RIN, ROMN,
        _SCBK, SOUR, SPFX, _SSHOW, STAE, STAT, SUBM, SUBN, SURN,
        TEMP, TEXT, TIME, TITL, TRLR, TYPE, _TYPE,
        UID, _UID, _URL,
        VERS,
        WIFE, _WEB, WWW, _WWW,

        // personal LDS ordinances
        BAPL, CONL, WAC, ENDL, SLGC,

        // family LDS ordinances
        SLGS
    }

    override fun startElement(tag: String, id: String?, xref: String?) {
        val tagName = tag
        val tagNameUpper = tagName.uppercase()
        val ref = xref
        val tos = if (objectStack.isNotEmpty()) objectStack.last() else null
        var obj: Any? = null

        try {
            val tagEnum = Tag.valueOf(tagNameUpper)
            obj = when (tagEnum) {
                Tag.ABBR -> handleAbbr(tos)
                Tag.ADDR -> handleAddr(tos)
                Tag.ADR1 -> handleAdr1(tos)
                Tag.ADR2 -> handleAdr2(tos)
                Tag.ADR3 -> handleAdr3(tos)
                Tag._AKA -> handleAka(tos, tagName)
                Tag.ALIA -> handleAlia(tos, ref)
                Tag.ANCI -> handleAnci(tos, ref)
                Tag.ASSO -> handleAsso(tos, ref)
                Tag.AUTH -> handleAuth(tos)
                Tag.BLOB -> handleBlob(tos)
                Tag.CALN -> handleCaln(tos)
                Tag.CAUS -> handleCaus(tos)
                Tag.CHAN -> handleChan(tos)
                Tag.CHAR -> handleChar(tos)
                Tag.CHIL -> handleChil(tos, ref)
                Tag.CITY -> handleCity(tos)
                Tag.CONC -> handleCont(tos, false)
                Tag.CONT -> handleCont(tos, true)
                Tag.COPR -> handleCopr(tos)
                Tag.CORP -> handleCorp(tos)
                Tag.CTRY -> handleCtry(tos)
                Tag.DATA -> handleData(tos)
                Tag.DATE -> handleDate(tos)
                Tag.DESC -> handleDesc(tos)
                Tag.DESI -> handleDesi(tos, ref)
                Tag.DEST -> handleDest(tos)
                Tag.EMAIL, Tag._EMAIL, Tag._EML -> handleEmail(tos, tagName)
                Tag.FAM -> handleFam(tos, id)
                Tag.FAMC -> handleFamc(tos, ref)
                Tag.FAMS -> handleFams(tos, ref)
                Tag.FAX -> handleFax(tos)
                Tag._FILE, Tag.FILE -> handleFile(tos, tagName)
                Tag.FONE -> handleFone(tos, tagName)
                Tag.FORM -> handleForm(tos)
                Tag._FREL -> handleFRel(tos)
                Tag.GED -> handleGed()
                Tag.GEDC -> handleGedc(tos)
                Tag.GIVN -> handleGivn(tos)
                Tag.HEAD -> handleHead(tos)
                Tag.HUSB -> handleHusb(tos, ref)
                Tag.INDI -> handleIndi(tos, id)
                Tag._ITALIC -> handleItalic(tos)
                Tag.LANG -> handleLang(tos)
                Tag._MARRNM, Tag._MARNM, Tag._MAR -> handleMarrnm(tos, tagName)
                Tag.MEDI -> handleMedi(tos)
                Tag._MREL -> handleMRel(tos)
                Tag.NAME, Tag._NAME -> handleName(tos)
                Tag.NICK -> handleNick(tos)
                Tag.NPFX -> handleNpfx(tos)
                Tag.NSFX -> handleNsfx(tos)
                Tag.NOTE -> handleNote(tos, id, ref)
                Tag.OBJE -> handleObje(tos, id, ref)
                Tag.ORDI -> handleOrdi(tos)
                Tag.PAGE -> handlePage(tos)
                Tag._PAREN -> handleParen(tos)
                Tag.PEDI -> handlePedi(tos)
                Tag.PHON -> handlePhon(tos)
                Tag.PLAC -> handlePlac(tos)
                Tag.POST -> handlePost(tos)
                Tag._PREF -> handlePref(tos)
                Tag._PRIM, Tag._PRIMARY -> handlePrim(tos)
                Tag.PUBL -> handlePubl(tos)
                Tag.QUAY -> handleQuay(tos)
                Tag.REFN -> handleRefn(tos)
                Tag.RELA -> handleRela(tos)
                Tag.REPO -> handleRepo(tos, id, ref)
                Tag.RFN -> handleRfn(tos)
                Tag.RIN -> handleRin(tos)
                Tag.ROMN -> handleRomn(tos, tagName)
                Tag._SCBK -> handleScbk(tos)
                Tag.SOUR -> handleSour(tos, id, ref)
                Tag.SPFX -> handleSpfx(tos)
                Tag._SSHOW -> handleSshow(tos)
                Tag.STAE -> handleStae(tos)
                Tag.STAT -> handleStat(tos)
                Tag.SUBM -> handleSubm(tos, id, ref)
                Tag.SUBN -> handleSubn(tos, id, ref)
                Tag.SURN -> handleSurn(tos)
                Tag.TEMP -> handleTemp(tos)
                Tag.TEXT -> handleText(tos)
                Tag.TIME -> handleTime(tos)
                Tag.TITL -> handleTitl(tos)
                Tag.TRLR -> handleTrlr(tos)
                Tag.TYPE, Tag._TYPE -> handleType(tos, tagName)
                Tag.VERS -> handleVers(tos)
                Tag._UID, Tag.UID -> handleUid(tos, tagName)
                Tag.WIFE -> handleWife(tos, ref)
                Tag.WWW, Tag._WWW, Tag._WEB, Tag._URL -> handleWww(tos, tagName)
                Tag.BAPL, Tag.CONL, Tag.WAC, Tag.ENDL, Tag.SLGC -> handleLdsOrdinance(tos, true, tagNameUpper)
                Tag.SLGS -> handleLdsOrdinance(tos, false, tagNameUpper)
            }
        } catch (e: IllegalArgumentException) {
            // handle events/facts below
        }
        if (obj == null) {
            obj = handleEventFact(tos, tagName, tagNameUpper)
        }

        if (obj == null) {
            // unexpected tag
            obj = GedcomTag(id, tagName, ref)
            if (tos is ExtensionContainer) {
                addGedcomTag(tos as ExtensionContainer?, obj)
            } else if (tos is GedcomTag) {
                tos.addChild(obj as GedcomTag?)
            } else if (tos is FieldRef && tos.target is ExtensionContainer) {
                (obj as GedcomTag).parentTagName = tagStack.last()
                val ec = tos.target as ExtensionContainer
                addGedcomTag(ec, obj)
            } else {
                error("Dropped tag: " + joinTagStack() + " " + tagName, -1)
            }
        }

        tagStack.add(tagName)
        objectStack.add(obj!!)
    }

    private fun addGedcomTag(ec: ExtensionContainer?, tag: GedcomTag) {
        var moreTags = ec!!.getExtension(MORE_TAGS_EXTENSION_KEY) as MutableList<GedcomTag?>?
        if (moreTags == null) {
            moreTags = mutableListOf()
            ec.putExtension(MORE_TAGS_EXTENSION_KEY, moreTags)
        }
        moreTags.add(tag)
        warning("Tag added as extension: " + joinTagStack() + " " + tag.tag, -1)
    }

    private fun handleAbbr(tos: Any?): Any? {
        if (tos is Source && tos.abbreviation == null) {
            return FieldRef(tos, "Abbreviation")
        }
        return null
    }

    private fun handleAddr(tos: Any?): Any? {
        if ((tos is GeneratorCorporation && tos.address == null) ||
            (tos is EventFact && tos.address == null) ||
            (tos is Person && tos.address == null) ||
            (tos is Repository && tos.address == null) ||
            (tos is Submitter && tos.address == null)
        ) {
            val address = Address()
            if (tos is GeneratorCorporation) {
                tos.address = address
            } else if (tos is EventFact) {
                tos.address = address
            } else if (tos is Person) {
                tos.address = address
            } else if (tos is Repository) {
                tos.address = address
            } else {
                (tos as Submitter).address = address
            }
            return address
        }
        return null
    }

    private fun handleAdr1(tos: Any?): Any? {
        if (tos is Address && tos.addressLine1 == null) {
            return FieldRef(tos, "AddressLine1")
        }
        return null
    }

    private fun handleAdr2(tos: Any?): Any? {
        if (tos is Address && tos.addressLine2 == null) {
            return FieldRef(tos, "AddressLine2")
        }
        return null
    }

    private fun handleAdr3(tos: Any?): Any? {
        if (tos is Address && tos.addressLine3 == null) {
            return FieldRef(tos, "AddressLine3")
        }
        return null
    }

    private fun handleAka(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.aka == null) {
            tos.akaTag = tagName
            return FieldRef(tos, "Aka")
        }
        return null
    }

    private fun handleAlia(tos: Any?, ref: String?): Any? {
        if (tos is Person && ref == null) {
            val name = Name()
            name.type = "ALIA"
            tos.addName(name)
            return name
        }
        return null
    }

    private fun handleAnci(tos: Any?, ref: String?): Any? {
        if (tos is Person && tos.ancestorInterestSubmitterRef == null && ref != null) {
            tos.ancestorInterestSubmitterRef = ref
            return FieldRef(tos, "AncestorInterestSubmitterRef")
        }
        return null
    }

    private fun handleAsso(tos: Any?, ref: String?): Any? {
        if (tos is Person) {
            val association = Association()
            if (ref != null) {
                association.ref = ref
            }
            tos.addAssociation(association)
            return association
        }
        return null
    }

    private fun handleAuth(tos: Any?): Any? {
        if (tos is Source && tos.author == null) {
            return FieldRef(tos, "Author")
        }
        return null
    }

    private fun handleBlob(tos: Any?): Any? {
        if (tos is Media && tos.blob == null) {
            return FieldRef(tos, "Blob")
        }
        return null
    }

    private fun handleCaln(tos: Any?): Any? {
        if ((tos is RepositoryRef && tos.callNumber == null) ||
            (tos is Source && tos.callNumber == null)
        ) {
            return FieldRef(tos, "CallNumber")
        }
        return null
    }

    private fun handleCaus(tos: Any?): Any? {
        if (tos is EventFact && tos.cause == null) {
            return FieldRef(tos, "Cause")
        }
        return null
    }

    private fun handleChan(tos: Any?): Any? {
        if ((tos is PersonFamilyCommonContainer && tos.change == null) ||
            (tos is Media && tos.change == null) ||
            (tos is Note && tos.change == null) ||
            (tos is Source && tos.change == null) ||
            (tos is Repository && tos.change == null) ||
            (tos is Submitter && tos.change == null)
        ) {
            val change = Change()
            if (tos is PersonFamilyCommonContainer) {
                tos.change = change
            } else if (tos is Media) {
                tos.change = change
            } else if (tos is Note) {
                tos.change = change
            } else if (tos is Source) {
                tos.change = change
            } else if (tos is Repository) {
                tos.change = change
            } else {
                (tos as Submitter).change = change
            }
            return change
        }
        return null
    }

    private fun handleChar(tos: Any?): Any? {
        if (tos is Header && tos.characterSet == null) {
            val charset = CharacterSet()
            tos.characterSet = charset
            return charset
        }
        return null
    }

    private fun handleChil(tos: Any?, ref: String?): Any? {
        if (tos is Family && ref != null) {
            val childRef = ChildRef()
            childRef.ref = ref
            tos.addChild(childRef)
            return childRef
        }
        return null
    }

    private fun handleCity(tos: Any?): Any? {
        if (tos is Address && tos.city == null) {
            return FieldRef(tos, "City")
        }
        return null
    }

    private fun handleCont(tos: Any?, insertNewLine: Boolean): Any {
        val fieldRef = if (tos is FieldRef) {
            tos
        } else {
            FieldRef(tos, "Value")
        }

        if (insertNewLine) {
            try {
                fieldRef.appendValue("\n")
            } catch (e: Exception) {
                error("value not stored for: " + joinTagStack(), -1)
            }
        }
        return fieldRef
    }

    private fun handleCopr(tos: Any?): Any? {
        if ((tos is Header && tos.copyright == null) ||
            (tos is GeneratorData && tos.copyright == null)
        ) {
            return FieldRef(tos, "Copyright")
        }
        return null
    }

    private fun handleCorp(tos: Any?): Any? {
        if (tos is Generator && tos.generatorCorporation == null) {
            val generatorCorporation = GeneratorCorporation()
            tos.generatorCorporation = generatorCorporation
            return generatorCorporation
        }
        return null
    }

    private fun handleCtry(tos: Any?): Any? {
        if (tos is Address && tos.country == null) {
            return FieldRef(tos, "Country")
        }
        return null
    }

    private fun handleData(tos: Any?): Any? {
        if (tos is Generator && tos.generatorData == null) {
            val generatorData = GeneratorData()
            tos.generatorData = generatorData
            return generatorData
        } else if (tos is SourceCitation) {
            if (tos.dataTagContents == DataTagContents.DATE ||
                tos.dataTagContents == DataTagContents.TEXT
            ) {
                tos.dataTagContents = DataTagContents.SEPARATE
            }
            return tos
        }
        return null
    }

    private fun setDataTagContents(tos: Any, addDate: Boolean) {
        if (objectStack.size > 1 && objectStack[objectStack.size - 2] is SourceCitation) {
            var dataTagContents = (tos as SourceCitation).dataTagContents
            if (dataTagContents == null && addDate) {
                dataTagContents = DataTagContents.DATE
            } else if (dataTagContents == null && !addDate) {
                dataTagContents = DataTagContents.TEXT
            } else if (dataTagContents == DataTagContents.TEXT && addDate ||
                dataTagContents == DataTagContents.DATE && !addDate
            ) {
                dataTagContents = DataTagContents.COMBINED
            }
            tos.dataTagContents = dataTagContents
        }
    }

    private fun handleDate(tos: Any?): Any? {
        if ((tos is GeneratorData && tos.date == null) ||
            (tos is Source && tos.date == null) ||
            (tos is EventFact && tos.date == null)
        ) {
            return FieldRef(tos, "Date")
        } else if (tos is SourceCitation && tos.date == null) {
            setDataTagContents(tos, true)
            return FieldRef(tos, "Date")
        } else if ((tos is Header && tos.dateTime == null) ||
            (tos is Change && tos.dateTime == null)
        ) {
            val dateTime = DateTime()
            if (tos is Header) {
                tos.dateTime = dateTime
            } else {
                (tos as Change).dateTime = dateTime
            }
            return dateTime
        }
        return null
    }

    private fun handleDesc(tos: Any?): Any? {
        if (tos is Submission && tos.description == null) {
            return FieldRef(tos, "Description")
        }
        return null
    }

    private fun handleDesi(tos: Any?, ref: String?): Any? {
        if (tos is Person && tos.descendantInterestSubmitterRef == null && ref != null) {
            tos.descendantInterestSubmitterRef = ref
            return FieldRef(tos, "DescendantInterestSubmitterRef")
        }
        return null
    }

    private fun handleDest(tos: Any?): Any? {
        if (tos is Header && tos.destination == null) {
            return FieldRef(tos, "Destination")
        }
        return null
    }

    private fun handleEmail(tos: Any?, tagName: String): Any? {
        if ((tos is Submitter && tos.email == null) ||
            (tos is GeneratorCorporation && tos.email == null) ||
            (tos is EventFact && tos.email == null) ||
            (tos is Person && tos.email == null) ||
            (tos is Repository && tos.email == null)
        ) {
            if (tos is Submitter) {
                tos.emailTag = tagName
            } else if (tos is GeneratorCorporation) {
                tos.emailTag = tagName
            } else if (tos is EventFact) {
                tos.emailTag = tagName
            } else if (tos is Person) {
                tos.emailTag = tagName
            } else {
                (tos as Repository).emailTag = tagName
            }
            return FieldRef(tos, "Email")
        }
        return null
    }

    private fun handleEventFact(tos: Any?, tagName: String, tagNameUpper: String): Any? {
        if ((tos is Person && EventFact.PERSONAL_EVENT_FACT_TAGS.contains(tagNameUpper)) ||
            (tos is Family && EventFact.FAMILY_EVENT_FACT_TAGS.contains(tagNameUpper))
        ) {
            val eventFact = EventFact()
            eventFact.tag = tagName
            (tos as PersonFamilyCommonContainer).addEventFact(eventFact)
            return eventFact
        }
        return null
    }

    private fun handleFam(tos: Any?, id: String?): Any? {
        if (tos is Gedcom && id != null) {
            val family = Family()
            family.id = id
            tos.addFamily(family)
            return family
        }
        return null
    }

    private fun handleFamc(tos: Any?, ref: String?): Any? {
        if (tos is Person && ref != null) {
            val parentFamilyRef = ParentFamilyRef()
            parentFamilyRef.ref = ref
            tos.addParentFamilyRef(parentFamilyRef)
            return parentFamilyRef
        }
        return null
    }

    private fun handleFams(tos: Any?, ref: String?): Any? {
        if (tos is Person && ref != null) {
            val spouseFamilyRef = SpouseFamilyRef()
            spouseFamilyRef.ref = ref
            tos.addSpouseFamilyRef(spouseFamilyRef)
            return spouseFamilyRef
        }
        return null
    }

    private fun handleFax(tos: Any?): Any? {
        if ((tos is GeneratorCorporation && tos.fax == null) ||
            (tos is Repository && tos.fax == null) ||
            (tos is EventFact && tos.fax == null) ||
            (tos is Person && tos.fax == null) ||
            (tos is Submitter && tos.fax == null)
        ) {
            return FieldRef(tos, "Fax")
        }
        return null
    }

    private fun handleFile(tos: Any?, tagName: String): Any? {
        if (tos is Header && tos.file == null) {
            return FieldRef(tos, "File")
        } else if (tos is Media && tos.file == null) {
            tos.fileTag = tagName
            return FieldRef(tos, "File")
        }
        return null
    }

    private fun handleFone(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.fone == null) {
            tos.foneTag = tagName
            return FieldRef(tos, "Fone")
        }
        return null
    }

    private fun handleForm(tos: Any?): Any? {
        if (tos is GedcomVersion && tos.form == null) {
            return FieldRef(tos, "Form")
        } else if (tos is Media && tos.format == null) {
            return FieldRef(tos, "Format")
        }
        return null
    }

    private fun handleFRel(tos: Any?): Any? {
        if (tos is ChildRef && tos.fatherRelationship == null) {
            val parentRelationship = ParentRelationship()
            tos.fatherRelationship = parentRelationship
            return parentRelationship
        }
        return null
    }

    private fun handleGed(): Any {
        gedcom = Gedcom()
        return gedcom!!
    }

    private fun handleGedc(tos: Any?): Any? {
        if (tos is Header && tos.gedcomVersion == null) {
            val gedcomVersion = GedcomVersion()
            tos.gedcomVersion = gedcomVersion
            return gedcomVersion
        }
        return null
    }

    private fun handleGivn(tos: Any?): Any? {
        if (tos is Name && tos.given == null) {
            return FieldRef(tos, "Given")
        }
        return null
    }

    private fun handleHead(tos: Any?): Any? {
        if (tos is Gedcom && tos.header == null) {
            val header = Header()
            tos.header = header
            return header
        }
        return null
    }

    private fun handleHusb(tos: Any?, ref: String?): Any? {
        if (tos is Family && ref != null) {
            val spouseRef = SpouseRef()
            spouseRef.ref = ref
            tos.addHusband(spouseRef)
            return spouseRef
        }
        return null
    }

    private fun handleIndi(tos: Any?, id: String?): Any? {
        if (tos is Gedcom && id != null) {
            val person = Person()
            person.id = id
            tos.addPerson(person)
            return person
        }
        return null
    }

    private fun handleItalic(tos: Any?): Any? {
        if (tos is Source && tos.italic == null) {
            return FieldRef(tos, "Italic")
        }
        return null
    }

    private fun handleLang(tos: Any?): Any? {
        if ((tos is Submitter && tos.language == null) ||
            (tos is Header && tos.language == null)
        ) {
            return FieldRef(tos, "Language")
        }
        return null
    }

    private fun handleLdsOrdinance(tos: Any?, isPersonalOrdinance: Boolean, tagName: String): Any? {
        if ((tos is Person && isPersonalOrdinance) ||
            (tos is Family && !isPersonalOrdinance)
        ) {
            val ldsOrdinance = LdsOrdinance()
            (tos as PersonFamilyCommonContainer).addLdsOrdinance(ldsOrdinance)
            ldsOrdinance.tag = tagName
            return ldsOrdinance
        }
        return null
    }

    private fun handleMarrnm(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.marriedName == null) {
            tos.marriedNameTag = tagName
            return FieldRef(tos, "MarriedName")
        }
        return null
    }

    private fun handleMedi(tos: Any?): Any? {
        if (tos is Source && tos.mediaType == null) {
            return FieldRef(tos, "MediaType")
        } else if ((tos is FieldRef && tos.target is RepositoryRef) && tos.fieldName == "CallNumber" && (tos.target as RepositoryRef).mediaType == null) {
            tos.target.isMediUnderCalnTag = true
            return FieldRef(tos.target, "MediaType")
        }
        return null
    }

    private fun handleMRel(tos: Any?): Any? {
        if (tos is ChildRef && tos.motherRelationship == null) {
            val parentRelationship = ParentRelationship()
            tos.motherRelationship = parentRelationship
            return parentRelationship
        }
        return null
    }

    private fun handleName(tos: Any?): Any? {
        if ((tos is Generator && tos.name == null) ||
            (tos is Repository && tos.name == null) ||
            (tos is Address && tos.name == null) ||
            (tos is Submitter && tos.name == null)
        ) {
            return FieldRef(tos, "Name")
        } else if (tos is Person) {
            val name = Name()
            tos.addName(name)
            return name
        }
        return null
    }

    private fun handleNick(tos: Any?): Any? {
        if (tos is Name && tos.nickname == null) {
            return FieldRef(tos, "Nickname")
        }
        return null
    }

    private fun handleNpfx(tos: Any?): Any? {
        if (tos is Name && tos.prefix == null) {
            return FieldRef(tos, "Prefix")
        }
        return null
    }

    private fun handleNsfx(tos: Any?): Any? {
        if (tos is Name && tos.suffix == null) {
            return FieldRef(tos, "Suffix")
        }
        return null
    }

    private fun handleNote(tos: Any?, id: String?, ref: String?): Any? {
        if (tos is NoteContainer) {
            if (ref == null) {
                val note = Note()
                tos.addNote(note)
                return note
            } else {
                val noteRef = NoteRef()
                noteRef.ref = ref
                tos.addNoteRef(noteRef)
                return noteRef
            }
        } else if (tos is Gedcom) {
            val note = Note()
            if (id != null) {
                note.id = id
            }
            if (ref != null) {
                note.value = "@$ref@"
            }
            tos.addNote(note)
            return note
        }
        return null
    }

    private fun handleObje(tos: Any?, id: String?, ref: String?): Any? {
        if (tos is MediaContainer) {
            if (ref == null) {
                val media = Media()
                tos.addMedia(media)
                return media
            } else {
                val mediaRef = MediaRef()
                mediaRef.ref = ref
                tos.addMediaRef(mediaRef)
                return mediaRef
            }
        } else if (tos is Gedcom) {
            val media = Media()
            if (id != null) {
                media.id = id
            }
            tos.addMedia(media)
            return media
        }
        return null
    }

    private fun handleOrdi(tos: Any?): Any? {
        if (tos is Submission && tos.ordinanceFlag == null) {
            return FieldRef(tos, "OrdinanceFlag")
        }
        return null
    }

    private fun handlePage(tos: Any?): Any? {
        if (tos is SourceCitation && tos.page == null) {
            return FieldRef(tos, "Page")
        }
        return null
    }

    private fun handleParen(tos: Any?): Any? {
        if (tos is Source && tos.paren == null) {
            return FieldRef(tos, "Paren")
        }
        return null
    }

    private fun handlePedi(tos: Any?): Any? {
        if (tos is ParentFamilyRef && tos.relationshipType == null) {
            return FieldRef(tos, "RelationshipType")
        }
        return null
    }

    private fun handlePhon(tos: Any?): Any? {
        if ((tos is GeneratorCorporation && tos.phone == null) ||
            (tos is Repository && tos.phone == null) ||
            (tos is EventFact && tos.phone == null) ||
            (tos is Person && tos.phone == null) ||
            (tos is Submitter && tos.phone == null)
        ) {
            return FieldRef(tos, "Phone")
        }
        return null
    }

    private fun handlePlac(tos: Any?): Any? {
        if (tos is EventFact && tos.place == null) {
            return FieldRef(tos, "Place")
        }
        return null
    }

    private fun handlePost(tos: Any?): Any? {
        if (tos is Address && tos.postalCode == null) {
            return FieldRef(tos, "PostalCode")
        }
        return null
    }

    private fun handlePref(tos: Any?): Any? {
        if (tos is SpouseRef && tos.preferred == null) {
            return FieldRef(tos, "Preferred")
        }
        return null
    }

    private fun handlePrim(tos: Any?): Any? {
        if ((tos is Media && tos.primary == null) ||
            (tos is ParentFamilyRef && tos.primary == null)
        ) {
            return FieldRef(tos, "Primary")
        }
        return null
    }

    private fun handlePubl(tos: Any?): Any? {
        if (tos is Source && tos.publicationFacts == null) {
            return FieldRef(tos, "PublicationFacts")
        }
        return null
    }

    private fun handleQuay(tos: Any?): Any? {
        if (tos is SourceCitation && tos.quality == null) {
            return FieldRef(tos, "Quality")
        }
        return null
    }

    private fun handleRefn(tos: Any?): Any? {
        if (tos is PersonFamilyCommonContainer) {
            return FieldRef(tos, "ReferenceNumber")
        } else if (tos is Source && tos.referenceNumber == null) {
            return FieldRef(tos, "ReferenceNumber")
        }
        return null
    }

    private fun handleRela(tos: Any?): Any? {
        if (tos is Association && tos.relation == null) {
            return FieldRef(tos, "Relation")
        }
        return null
    }

    private fun handleRepo(tos: Any?, id: String?, ref: String?): Any? {
        if (tos is Source && tos.repositoryRef == null) {
            val repositoryRef = RepositoryRef()
            if (ref != null) {
                repositoryRef.ref = ref
            }
            tos.repositoryRef = repositoryRef
            return repositoryRef
        } else if (tos is Gedcom) {
            val repository = Repository()
            if (id != null) {
                repository.id = id
            }
            tos.addRepository(repository)
            return repository
        }
        return null
    }

    private fun handleRfn(tos: Any?): Any? {
        if (tos is Person && tos.recordFileNumber == null) {
            return FieldRef(tos, "RecordFileNumber")
        }
        return null
    }

    private fun handleRin(tos: Any?): Any? {
        if ((tos is Submitter && tos.rin == null) ||
            (tos is Note && tos.rin == null) ||
            (tos is Repository && tos.rin == null) ||
            (tos is EventFact && tos.rin == null) ||
            (tos is Source && tos.rin == null) ||
            (tos is PersonFamilyCommonContainer && tos.rin == null)
        ) {
            return FieldRef(tos, "Rin")
        }
        return null
    }

    private fun handleRomn(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.romn == null) {
            tos.romnTag = tagName
            return FieldRef(tos, "Romn")
        }
        return null
    }

    private fun handleScbk(tos: Any?): Any? {
        if (tos is Media && tos.scrapbook == null) {
            return FieldRef(tos, "Scrapbook")
        }
        return null
    }

    private fun handleSour(tos: Any?, id: String?, ref: String?): Any? {
        if (tos is Header && tos.generator == null) {
            val generator = Generator()
            tos.generator = generator
            return generator
        } else if (tos is SourceCitationContainer ||
            tos is Note ||
            tos is NoteRef ||
            (tos is FieldRef &&
                    tos.target is Note && tos.fieldName == "Value")
        ) {
            val sourceCitation = SourceCitation()
            if (ref != null) {
                sourceCitation.ref = ref
            }
            if (tos is SourceCitationContainer) {
                tos.addSourceCitation(sourceCitation)
            } else if (tos is Note) {
                tos.addSourceCitation(sourceCitation)
            } else if (tos is NoteRef) {
                tos.addSourceCitation(sourceCitation)
            } else {
                val note = (tos as FieldRef).target as Note
                note.addSourceCitation(sourceCitation)
                note.isSourceCitationsUnderValue = true
            }
            return sourceCitation
        } else if (tos is Gedcom) {
            val source = Source()
            if (id != null) {
                source.id = id
            }
            tos.addSource(source)
            return source
        }
        return null
    }

    private fun handleSpfx(tos: Any?): Any? {
        if (tos is Name && tos.surnamePrefix == null) {
            return FieldRef(tos, "SurnamePrefix")
        }
        return null
    }

    private fun handleSshow(tos: Any?): Any? {
        if (tos is Media && tos.slideShow == null) {
            return FieldRef(tos, "SlideShow")
        }
        return null
    }

    private fun handleStae(tos: Any?): Any? {
        if (tos is Address && tos.state == null) {
            return FieldRef(tos, "State")
        }
        return null
    }

    private fun handleStat(tos: Any?): Any? {
        if (tos is LdsOrdinance && tos.status == null) {
            return FieldRef(tos, "Status")
        }
        return null
    }

    private fun handleSubm(tos: Any?, id: String?, ref: String?): Any? {
        if ((tos is Header && ref != null) && tos.submitterRef == null) {
            tos.submitterRef = ref
            return Any() // placeholder
        } else if (tos is Gedcom) {
            val submitter = Submitter()
            if (id != null) {
                submitter.id = id
            }
            tos.addSubmitter(submitter)
            return submitter
        }
        return null
    }

    private fun handleSubn(tos: Any?, id: String?, ref: String?): Any? {
        if ((tos is Header && ref != null) && tos.submissionRef == null) {
            tos.submissionRef = ref
            return Any() // placeholder
        } else if (tos is Header && ref == null && tos.submission == null) {
            val submission = Submission()
            tos.submission = submission
            return submission
        } else if (tos is Gedcom && tos.submission == null) {
            val submission = Submission()
            if (id != null) {
                submission.id = id
            }
            tos.submission = submission
            return submission
        }
        return null
    }

    private fun handleSurn(tos: Any?): Any? {
        if (tos is Name && tos.surname == null) {
            return FieldRef(tos, "Surname")
        }
        return null
    }

    private fun handleTemp(tos: Any?): Any? {
        if (tos is LdsOrdinance && tos.temple == null) {
            return FieldRef(tos, "Temple")
        }
        return null
    }

    private fun handleText(tos: Any?): Any? {
        if (tos is SourceCitation) {
            setDataTagContents(tos, false)
            val text = tos.text
            if (text != null) {
                tos.text = text + "\n"
            }
            return FieldRef(tos, "Text")
        } else if (tos is Source) {
            val text = tos.text
            if (text != null) {
                tos.text = text + "\n"
            }
            return FieldRef(tos, "Text")
        }
        return null
    }

    private fun handleTime(tos: Any?): Any? {
        if (tos is DateTime && tos.time == null) {
            return FieldRef(tos, "Time")
        }
        return null
    }

    private fun handleTitl(tos: Any?): Any? {
        if ((tos is Media && tos.title == null) ||
            (tos is Source && tos.title == null)
        ) {
            return FieldRef(tos, "Title")
        }
        return null
    }

    private fun handleTrlr(tos: Any?): Any? {
        if (tos is Gedcom) {
            return Trailer()
        }
        return null
    }

    private fun handleType(tos: Any?, tagName: String): Any? {
        if ((tos is Name && tos.type == null) ||
            (tos is Media && tos.type == null) ||
            (tos is EventFact && tos.type == null) ||
            (tos is Association && tos.type == null) ||
            (tos is Source && tos.type == null)
        ) {
            if (tos is Source) {
                tos.typeTag = tagName
            } else if (tos is Name) {
                tos.typeTag = tagName
            }
            return FieldRef(tos, "Type")
        }
        return null
    }

    private fun handleUid(tos: Any?, tagName: String): Any? {
        if ((tos is PersonFamilyCommonContainer && tos.uid == null) ||
            (tos is EventFact && tos.uid == null) ||
            (tos is Source && tos.uid == null)
        ) {
            if (tos is PersonFamilyCommonContainer) {
                tos.uidTag = tagName
            } else if (tos is EventFact) {
                tos.uidTag = tagName
            } else {
                (tos as Source).uidTag = tagName
            }
            return FieldRef(tos, "Uid")
        }
        return null
    }

    private fun handleVers(tos: Any?): Any? {
        if ((tos is Generator && tos.version == null) ||
            (tos is GedcomVersion && tos.version == null) ||
            (tos is CharacterSet && tos.version == null)
        ) {
            return FieldRef(tos, "Version")
        }
        return null
    }

    private fun handleWife(tos: Any?, ref: String?): Any? {
        if (tos is Family && ref != null) {
            val spouseRef = SpouseRef()
            spouseRef.ref = ref
            tos.addWife(spouseRef)
            return spouseRef
        }
        return null
    }

    private fun handleWww(tos: Any?, tagName: String): Any? {
        if ((tos is GeneratorCorporation && tos.www == null) ||
            (tos is Repository && tos.www == null) ||
            (tos is EventFact && tos.www == null) ||
            (tos is Person && tos.www == null) ||
            (tos is Submitter && tos.www == null)
        ) {
            if (tos is GeneratorCorporation) {
                tos.wwwTag = tagName
            } else if (tos is Repository) {
                tos.wwwTag = tagName
            } else if (tos is EventFact) {
                tos.wwwTag = tagName
            } else if (tos is Person) {
                tos.wwwTag = tagName
            } else {
                (tos as Submitter).wwwTag = tagName
            }
            return FieldRef(tos, "Www")
        }
        return null
    }

    override fun endElement(tag: String) {
        if (objectStack.isNotEmpty()) objectStack.removeAt(objectStack.lastIndex)
        if (tagStack.isNotEmpty()) tagStack.removeAt(tagStack.lastIndex)
    }

    override fun characters(text: String) {
        val value = text
        val tos = if (objectStack.isNotEmpty()) objectStack.last() else null
        var fieldRef: FieldRef? = null
        try {
            if (tos is GedcomTag) {
                tos.appendValue(value)
            } else if (tos is FieldRef) {
                fieldRef = tos
                fieldRef.appendValue(value)
            } else {
                fieldRef = FieldRef(tos, "Value")
                fieldRef.value = value
            }
        } catch (e: Exception) {
            if (fieldRef?.fieldName == "Value") {
                error("value not stored for: " + joinTagStack(), -1)
            } else {
                fatalError("get method not found for: " + fieldRef?.classFieldName, -1)
            }
        }
    }

    fun parseGedcom(lines: Sequence<String>): Gedcom? {
        val parser = GedcomParser()
        parser.parse(lines, this)
        return gedcom
    }

    companion object {
        const val MORE_TAGS_EXTENSION_KEY: String = "folg.more_tags"
    }
}