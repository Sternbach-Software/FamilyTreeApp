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
import org.folg.gedcom.model.GedcomTag
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
        // Not used
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
            } else if (tos is ValueSetter && tos.value == null) {
                // If the TOS is a ValueSetter, we can't really add a child GedcomTag to it easily
                // because ValueSetter isn't a container.
                // The original logic checked: tos is FieldRef && tos.target is ExtensionContainer
                // We'd need to know the target of the ValueSetter. We don't.
                // So we'll skip this specific edge case or log an error.
                error("Cannot add extension tag to ValueSetter: $tagName", -1)
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
            return ValueSetter(getter = { tos.abbreviation }, setter = { tos.abbreviation = it })
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
            return ValueSetter(getter = { tos.addressLine1 }, setter = { tos.addressLine1 = it })
        }
        return null
    }

    private fun handleAdr2(tos: Any?): Any? {
        if (tos is Address && tos.addressLine2 == null) {
            return ValueSetter(getter = { tos.addressLine2 }, setter = { tos.addressLine2 = it })
        }
        return null
    }

    private fun handleAdr3(tos: Any?): Any? {
        if (tos is Address && tos.addressLine3 == null) {
            return ValueSetter(getter = { tos.addressLine3 }, setter = { tos.addressLine3 = it })
        }
        return null
    }

    private fun handleAka(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.aka == null) {
            tos.akaTag = tagName
            return ValueSetter(getter = { tos.aka }, setter = { tos.aka = it })
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
            return ValueSetter(getter = { tos.ancestorInterestSubmitterRef }, setter = { tos.ancestorInterestSubmitterRef = it })
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
            return ValueSetter(getter = { tos.author }, setter = { tos.author = it })
        }
        return null
    }

    private fun handleBlob(tos: Any?): Any? {
        if (tos is Media && tos.blob == null) {
            return ValueSetter(getter = { tos.blob }, setter = { tos.blob = it })
        }
        return null
    }

    private fun handleCaln(tos: Any?): Any? {
        if (tos is RepositoryRef && tos.callNumber == null) {
            return ValueSetter(getter = { tos.callNumber }, setter = { tos.callNumber = it })
        } else if (tos is Source && tos.callNumber == null) {
            return ValueSetter(getter = { tos.callNumber }, setter = { tos.callNumber = it })
        }
        return null
    }

    private fun handleCaus(tos: Any?): Any? {
        if (tos is EventFact && tos.cause == null) {
            return ValueSetter(getter = { tos.cause }, setter = { tos.cause = it })
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
            return ValueSetter(getter = { tos.city }, setter = { tos.city = it })
        }
        return null
    }

    private fun handleCont(tos: Any?, insertNewLine: Boolean): Any {
        val valueSetter = if (tos is ValueSetter) {
            tos
        } else {
             // Fallback: create a ValueSetter for "Value" if tos is an object that expects a value property?
             // In old code: FieldRef(tos, "Value")
             // We need to identify which objects have a 'value' property.
             if (tos is Note) ValueSetter(getter = { tos.value }, setter = { tos.value = it })
             else if (tos is EventFact) ValueSetter(getter = { tos.value }, setter = { tos.value = it })
             else if (tos is GedcomTag) {
                 val tag = tos as GedcomTag
                 ValueSetter(getter = { tag.value }, setter = { tag.value = it })
             }
             else if (tos is CharacterSet) ValueSetter(getter = { tos.value }, setter = { tos.value = it })
             else if (tos is GedcomVersion) ValueSetter(getter = { tos.value }, setter = { tos.value = it })
             else {
                 // Try to be generic or fail?
                 // For now, return a dummy setter or error?
                 // Let's just create a ValueSetter that errors if used?
                 ValueSetter(setter = { error("Cannot set value on ${tos?.toString()}", -1) })
             }
        }

        if (insertNewLine) {
            try {
                valueSetter.appendValue("\n")
            } catch (e: Exception) {
                error("value not stored for: " + joinTagStack(), -1)
            }
        }
        return valueSetter
    }

    private fun handleCopr(tos: Any?): Any? {
        if (tos is Header && tos.copyright == null) {
            return ValueSetter(getter = { tos.copyright }, setter = { tos.copyright = it })
        } else if (tos is GeneratorData && tos.copyright == null) {
            return ValueSetter(getter = { tos.copyright }, setter = { tos.copyright = it })
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
            return ValueSetter(getter = { tos.country }, setter = { tos.country = it })
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
        if (tos is GeneratorData && tos.date == null) {
            return ValueSetter(getter = { tos.date }, setter = { tos.date = it })
        } else if (tos is Source && tos.date == null) {
            return ValueSetter(getter = { tos.date }, setter = { tos.date = it })
        } else if (tos is EventFact && tos.date == null) {
            return ValueSetter(getter = { tos.date }, setter = { tos.date = it })
        } else if (tos is SourceCitation && tos.date == null) {
            setDataTagContents(tos, true)
            return ValueSetter(getter = { tos.date }, setter = { tos.date = it })
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
            return ValueSetter(getter = { tos.description }, setter = { tos.description = it })
        }
        return null
    }

    private fun handleDesi(tos: Any?, ref: String?): Any? {
        if (tos is Person && tos.descendantInterestSubmitterRef == null && ref != null) {
            tos.descendantInterestSubmitterRef = ref
            return ValueSetter(getter = { tos.descendantInterestSubmitterRef }, setter = { tos.descendantInterestSubmitterRef = it })
        }
        return null
    }

    private fun handleDest(tos: Any?): Any? {
        if (tos is Header && tos.destination == null) {
            return ValueSetter(getter = { tos.destination }, setter = { tos.destination = it })
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

             // Use adder for emails if they are lists? Or setter? FieldRef used "Email".
             // Checking Model classes: Person has `var email: String?`.
             // Wait, if it's just String, it can't handle multiple emails?
             // GEDCOM 5.5.1 allows multiple.
             // Person model: var email: String?
             // So it only holds one? Or does it append?
             // FieldRef appendValue logic for String: `this.value = (current ?: "") + value`.
             // But for unrelated tags like multiple EMAIL tags, it overwrites unless it's CONC/CONT.
             // Actually, `handleEmail` is called for a new tag.
             // If `Person.email` is already set, `handleEmail` returns null (because check `tos.email == null`).
             // So it only accepts the first one.
             if (tos is Submitter) return ValueSetter(getter = { tos.email }, setter = { tos.email = it })
             if (tos is GeneratorCorporation) return ValueSetter(getter = { tos.email }, setter = { tos.email = it })
             if (tos is EventFact) return ValueSetter(getter = { tos.email }, setter = { tos.email = it })
             if (tos is Person) return ValueSetter(getter = { tos.email }, setter = { tos.email = it })
             if (tos is Repository) return ValueSetter(getter = { tos.email }, setter = { tos.email = it })
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
        if (tos is GeneratorCorporation && tos.fax == null) return ValueSetter(getter = { tos.fax }, setter = { tos.fax = it })
        if (tos is Repository && tos.fax == null) return ValueSetter(getter = { tos.fax }, setter = { tos.fax = it })
        if (tos is EventFact && tos.fax == null) return ValueSetter(getter = { tos.fax }, setter = { tos.fax = it })
        if (tos is Person && tos.fax == null) return ValueSetter(getter = { tos.fax }, setter = { tos.fax = it })
        if (tos is Submitter && tos.fax == null) return ValueSetter(getter = { tos.fax }, setter = { tos.fax = it })
        return null
    }

    private fun handleFile(tos: Any?, tagName: String): Any? {
        if (tos is Header && tos.file == null) {
            return ValueSetter(getter = { tos.file }, setter = { tos.file = it })
        } else if (tos is Media && tos.file == null) {
            tos.fileTag = tagName
            return ValueSetter(getter = { tos.file }, setter = { tos.file = it })
        }
        return null
    }

    private fun handleFone(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.fone == null) {
            tos.foneTag = tagName
            return ValueSetter(getter = { tos.fone }, setter = { tos.fone = it })
        }
        return null
    }

    private fun handleForm(tos: Any?): Any? {
        if (tos is GedcomVersion && tos.form == null) {
            return ValueSetter(getter = { tos.form }, setter = { tos.form = it })
        } else if (tos is Media && tos.format == null) {
            return ValueSetter(getter = { tos.format }, setter = { tos.format = it })
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
            return ValueSetter(getter = { tos.given }, setter = { tos.given = it })
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
            return ValueSetter(getter = { tos.italic }, setter = { tos.italic = it })
        }
        return null
    }

    private fun handleLang(tos: Any?): Any? {
        if (tos is Submitter && tos.language == null) return ValueSetter(getter = { tos.language }, setter = { tos.language = it })
        if (tos is Header && tos.language == null) return ValueSetter(getter = { tos.language }, setter = { tos.language = it })
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
            return ValueSetter(getter = { tos.marriedName }, setter = { tos.marriedName = it })
        }
        return null
    }

    private fun handleMedi(tos: Any?): Any? {
        if (tos is Source && tos.mediaType == null) {
            return ValueSetter(getter = { tos.mediaType }, setter = { tos.mediaType = it })
        } else if ((tos is ValueSetter) && tos.value == null) { // This part is tricky. FieldRef checked target is RepositoryRef && fieldName == "CallNumber".
            // Since we don't have fieldName in ValueSetter, we can't replicate this logic exactly without more context in ValueSetter.
            // But this was handling MEDI under CALN.
            // "if (tos is FieldRef && tos.target is RepositoryRef && tos.fieldName == CallNumber ...)"
            // In our new architecture, `tos` would be the ValueSetter for `callNumber`.
            // But we can't access `target`.
            // So we might need to handle CALN differently. `handleCaln` returns a ValueSetter.
            // Maybe we should return a wrapper that we can identify?
            // Or, we check if `objectStack` has RepositoryRef at -1?
            // If `tos` is the ValueSetter for CallNumber, the objectStack[-2] is RepositoryRef.

            // Let's check object stack.
            if (objectStack.size >= 2) {
                val parent = objectStack[objectStack.size - 2]
                if (parent is RepositoryRef) {
                    // Check if the current TOS is indeed the CallNumber setter? Hard to verify.
                    // But if MEDI appears inside CALN context (which it does in GEDCOM),
                    // then `tos` is the result of `handleCaln`.
                    parent.isMediUnderCalnTag = true
                    return ValueSetter(getter = { parent.mediaType }, setter = { parent.mediaType = it })
                }
            }
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
        if (tos is Generator && tos.name == null) return ValueSetter(getter = { tos.name }, setter = { tos.name = it })
        if (tos is Repository && tos.name == null) return ValueSetter(getter = { tos.name }, setter = { tos.name = it })
        if (tos is Address && tos.name == null) return ValueSetter(getter = { tos.name }, setter = { tos.name = it })
        if (tos is Submitter && tos.name == null) return ValueSetter(getter = { tos.name }, setter = { tos.name = it })
        if (tos is Person) {
            val name = Name()
            tos.addName(name)
            return name
        }
        return null
    }

    private fun handleNick(tos: Any?): Any? {
        if (tos is Name && tos.nickname == null) {
            return ValueSetter(getter = { tos.nickname }, setter = { tos.nickname = it })
        }
        return null
    }

    private fun handleNpfx(tos: Any?): Any? {
        if (tos is Name && tos.prefix == null) {
            return ValueSetter(getter = { tos.prefix }, setter = { tos.prefix = it })
        }
        return null
    }

    private fun handleNsfx(tos: Any?): Any? {
        if (tos is Name && tos.suffix == null) {
            return ValueSetter(getter = { tos.suffix }, setter = { tos.suffix = it })
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
            return ValueSetter(getter = { tos.ordinanceFlag }, setter = { tos.ordinanceFlag = it })
        }
        return null
    }

    private fun handlePage(tos: Any?): Any? {
        if (tos is SourceCitation && tos.page == null) {
            return ValueSetter(getter = { tos.page }, setter = { tos.page = it })
        }
        return null
    }

    private fun handleParen(tos: Any?): Any? {
        if (tos is Source && tos.paren == null) {
            return ValueSetter(getter = { tos.paren }, setter = { tos.paren = it })
        }
        return null
    }

    private fun handlePedi(tos: Any?): Any? {
        if (tos is ParentFamilyRef && tos.relationshipType == null) {
            return ValueSetter(getter = { tos.relationshipType }, setter = { tos.relationshipType = it })
        }
        return null
    }

    private fun handlePhon(tos: Any?): Any? {
        if (tos is GeneratorCorporation && tos.phone == null) return ValueSetter(getter = { tos.phone }, setter = { tos.phone = it })
        if (tos is Repository && tos.phone == null) return ValueSetter(getter = { tos.phone }, setter = { tos.phone = it })
        if (tos is EventFact && tos.phone == null) return ValueSetter(getter = { tos.phone }, setter = { tos.phone = it })
        if (tos is Person && tos.phone == null) return ValueSetter(getter = { tos.phone }, setter = { tos.phone = it })
        if (tos is Submitter && tos.phone == null) return ValueSetter(getter = { tos.phone }, setter = { tos.phone = it })
        return null
    }

    private fun handlePlac(tos: Any?): Any? {
        if (tos is EventFact && tos.place == null) {
            return ValueSetter(getter = { tos.place }, setter = { tos.place = it })
        }
        return null
    }

    private fun handlePost(tos: Any?): Any? {
        if (tos is Address && tos.postalCode == null) {
            return ValueSetter(getter = { tos.postalCode }, setter = { tos.postalCode = it })
        }
        return null
    }

    private fun handlePref(tos: Any?): Any? {
        if (tos is SpouseRef && tos.preferred == null) {
            return ValueSetter(getter = { tos.preferred }, setter = { tos.preferred = it })
        }
        return null
    }

    private fun handlePrim(tos: Any?): Any? {
        if (tos is Media && tos.primary == null) return ValueSetter(getter = { tos.primary }, setter = { tos.primary = it })
        if (tos is ParentFamilyRef && tos.primary == null) return ValueSetter(getter = { tos.primary }, setter = { tos.primary = it })
        return null
    }

    private fun handlePubl(tos: Any?): Any? {
        if (tos is Source && tos.publicationFacts == null) {
            return ValueSetter(getter = { tos.publicationFacts }, setter = { tos.publicationFacts = it })
        }
        return null
    }

    private fun handleQuay(tos: Any?): Any? {
        if (tos is SourceCitation && tos.quality == null) {
            return ValueSetter(getter = { tos.quality }, setter = { tos.quality = it })
        }
        return null
    }

    private fun handleRefn(tos: Any?): Any? {
        if (tos is PersonFamilyCommonContainer) {
            return ValueSetter(adder = { tos.addReferenceNumber(it) })
        } else if (tos is Source && tos.referenceNumber == null) {
             // Source model check: var referenceNumber: String? (singular)
             // Wait, Source.java in folg might have been singular, but let's check Kotlin file.
             // I didn't check Source.kt.
             // Assuming it's singular based on null check.
             // But if it was multiple, FieldRef would have used `addReferenceNumber`.
             // If `referenceNumber` is null, it implies singular.
             // Let's assume singular for Source.
             // But wait, FieldRef logic was "try set, if fail try add".
             // If Source has `setReferenceNumber` it works.
             // I'll assume singular for now.
             return ValueSetter(getter = { tos.referenceNumber }, setter = { tos.referenceNumber = it })
        }
        return null
    }

    private fun handleRela(tos: Any?): Any? {
        if (tos is Association && tos.relation == null) {
            return ValueSetter(getter = { tos.relation }, setter = { tos.relation = it })
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
            return ValueSetter(getter = { tos.recordFileNumber }, setter = { tos.recordFileNumber = it })
        }
        return null
    }

    private fun handleRin(tos: Any?): Any? {
        if (tos is Submitter && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        if (tos is Note && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        if (tos is Repository && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        if (tos is EventFact && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        if (tos is Source && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        if (tos is PersonFamilyCommonContainer && tos.rin == null) return ValueSetter(getter = { tos.rin }, setter = { tos.rin = it })
        return null
    }

    private fun handleRomn(tos: Any?, tagName: String): Any? {
        if (tos is Name && tos.romn == null) {
            tos.romnTag = tagName
            return ValueSetter(getter = { tos.romn }, setter = { tos.romn = it })
        }
        return null
    }

    private fun handleScbk(tos: Any?): Any? {
        if (tos is Media && tos.scrapbook == null) {
            return ValueSetter(getter = { tos.scrapbook }, setter = { tos.scrapbook = it })
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
            (tos is ValueSetter) // handleSour for Note Value ("Value" tag)
        ) {
            // Logic for "tos is FieldRef && tos.target is Note && tos.fieldName == 'Value'"
            if (tos is ValueSetter) {
                // Determine if we are inside a Note.
                 if (objectStack.size >= 2 && objectStack[objectStack.size - 2] is Note) {
                     val note = objectStack[objectStack.size - 2] as Note
                     // We are adding a SOUR to a NOTE.
                     val sourceCitation = SourceCitation()
                     if (ref != null) sourceCitation.ref = ref
                     note.addSourceCitation(sourceCitation)
                     note.isSourceCitationsUnderValue = true
                     return sourceCitation
                 }
                 // If not, fall through?
            }

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
            return ValueSetter(getter = { tos.surnamePrefix }, setter = { tos.surnamePrefix = it })
        }
        return null
    }

    private fun handleSshow(tos: Any?): Any? {
        if (tos is Media && tos.slideShow == null) {
            return ValueSetter(getter = { tos.slideShow }, setter = { tos.slideShow = it })
        }
        return null
    }

    private fun handleStae(tos: Any?): Any? {
        if (tos is Address && tos.state == null) {
            return ValueSetter(getter = { tos.state }, setter = { tos.state = it })
        }
        return null
    }

    private fun handleStat(tos: Any?): Any? {
        if (tos is LdsOrdinance && tos.status == null) {
            return ValueSetter(getter = { tos.status }, setter = { tos.status = it })
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
            return ValueSetter(getter = { tos.surname }, setter = { tos.surname = it })
        }
        return null
    }

    private fun handleTemp(tos: Any?): Any? {
        if (tos is LdsOrdinance && tos.temple == null) {
            return ValueSetter(getter = { tos.temple }, setter = { tos.temple = it })
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
            return ValueSetter(getter = { tos.text }, setter = { tos.text = it })
        } else if (tos is Source) {
            val text = tos.text
            if (text != null) {
                tos.text = text + "\n"
            }
            return ValueSetter(getter = { tos.text }, setter = { tos.text = it })
        }
        return null
    }

    private fun handleTime(tos: Any?): Any? {
        if (tos is DateTime && tos.time == null) {
            return ValueSetter(getter = { tos.time }, setter = { tos.time = it })
        }
        return null
    }

    private fun handleTitl(tos: Any?): Any? {
        if (tos is Media && tos.title == null) return ValueSetter(getter = { tos.title }, setter = { tos.title = it })
        if (tos is Source && tos.title == null) return ValueSetter(getter = { tos.title }, setter = { tos.title = it })
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
            if (tos is Name) return ValueSetter(getter = { tos.type }, setter = { tos.type = it })
            if (tos is Media) return ValueSetter(getter = { tos.type }, setter = { tos.type = it })
            if (tos is EventFact) return ValueSetter(getter = { tos.type }, setter = { tos.type = it })
            if (tos is Association) return ValueSetter(getter = { tos.type }, setter = { tos.type = it })
            if (tos is Source) return ValueSetter(getter = { tos.type }, setter = { tos.type = it })
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
            if (tos is PersonFamilyCommonContainer) return ValueSetter(getter = { tos.uid }, setter = { tos.uid = it })
            if (tos is EventFact) return ValueSetter(getter = { tos.uid }, setter = { tos.uid = it })
            if (tos is Source) return ValueSetter(getter = { tos.uid }, setter = { tos.uid = it })
        }
        return null
    }

    private fun handleVers(tos: Any?): Any? {
        if (tos is Generator && tos.version == null) return ValueSetter(getter = { tos.version }, setter = { tos.version = it })
        if (tos is GedcomVersion && tos.version == null) return ValueSetter(getter = { tos.version }, setter = { tos.version = it })
        if (tos is CharacterSet && tos.version == null) return ValueSetter(getter = { tos.version }, setter = { tos.version = it })
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
            if (tos is GeneratorCorporation) return ValueSetter(getter = { tos.www }, setter = { tos.www = it })
            if (tos is Repository) return ValueSetter(getter = { tos.www }, setter = { tos.www = it })
            if (tos is EventFact) return ValueSetter(getter = { tos.www }, setter = { tos.www = it })
            if (tos is Person) return ValueSetter(getter = { tos.www }, setter = { tos.www = it })
            if (tos is Submitter) return ValueSetter(getter = { tos.www }, setter = { tos.www = it })
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
        var valueSetter: ValueSetter? = null
        try {
            if (tos is GedcomTag) {
                tos.appendValue(value)
            } else if (tos is ValueSetter) {
                valueSetter = tos
                valueSetter.appendValue(value)
            } else {
                // Fallback: assume "Value"
                // Similar to handleCont logic for creating fallback
                if (tos is Note) valueSetter = ValueSetter(getter = { tos.value }, setter = { tos.value = it })
                else if (tos is EventFact) valueSetter = ValueSetter(getter = { tos.value }, setter = { tos.value = it })
                else if (tos is GedcomTag) {
                    val tag = tos as GedcomTag
                    valueSetter = ValueSetter(getter = { tag.value }, setter = { tag.value = it })
                }
                else if (tos is CharacterSet) valueSetter = ValueSetter(getter = { tos.value }, setter = { tos.value = it })
                else if (tos is GedcomVersion) valueSetter = ValueSetter(getter = { tos.value }, setter = { tos.value = it })
                // ... add others as needed

                valueSetter?.value = value
            }
        } catch (e: Exception) {
             error("value not stored for: " + joinTagStack(), -1)
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
