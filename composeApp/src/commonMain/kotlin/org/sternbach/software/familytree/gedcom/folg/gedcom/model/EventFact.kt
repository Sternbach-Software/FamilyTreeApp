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
 * omit: Phone, Age, Agency, Cause (except for death events)
 * add: Rin, Uid
 */
open class EventFact : SourceCitationContainer() {
    var value: String? = null
    var tag: String? = null
    var type: String? = null
    var date: String? = null
    var place: String? = null
    var address: Address? = null
    var phone: String? = null
    var fax: String? = null
    var rin: String? = null
    var cause: String? = null
    var uid: String? = null
    var uidTag: String? = null
    var email: String? = null
    var emailTag: String? = null
    var www: String? = null
    var wwwTag: String? = null

    val displayType: String
        /**
         * Return human-friendly event type
         * @return human-friendly event type
         */
        get() {
            if (tag != null) {
                val key = DISPLAY_TYPE[tag!!.uppercase()]
                if (key != null) {
                    return EventFactResources.getString(key) ?: EventFactResources.getString("other") ?: "Other"
                }
            }
            return EventFactResources.getString("other") ?: "Other"
        }

    override fun visitContainedObjects(visitor: Visitor) {
        if (address != null) {
            address!!.accept(visitor)
        }
        super.visitContainedObjects(visitor)
    }

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this)) {
            this.visitContainedObjects(visitor)
            visitor.endVisit(this)
        }
    }

    companion object {
        val PERSONAL_EVENT_FACT_TAGS: Set<String> = setOf(
                "ADOP",
                "ADOPTION",
                "ADULT_CHRISTNG",
                "AFN",
                "ARRI",
                "ARVL",
                "ARRIVAL",
                "_ATTR",
                "BAP",
                "BAPM",
                "BAPT",
                "BAPTISM",
                "BARM",
                "BAR_MITZVAH",
                "BASM",
                "BAS_MITZVAH",
                "BATM",
                "BAT_MITZVAH",
                "BIRT",
                "BIRTH",
                "BLES",
                "BLESS",
                "BLESSING",
                "BLSL",
                "BURI",
                "BURIAL",
                "CAST",
                "CASTE",
                "CAUS",
                "CENS",
                "CENSUS",
                "CHILDREN_COUNT",
                "CHR",
                "CHRA",
                "CHRISTENING",
                "CIRC",
                "CITN",
                "_COLOR",
                "CONF",
                "CONFIRMATION",
                "CREM",
                "CREMATION",
                "_DCAUSE",
                "DEAT",
                "DEATH",
                "_DEATH_OF_SPOUSE",
                "DEED",
                "_DEG",
                "_DEGREE",
                "DEPA",
                "DPRT",
                "DSCR",
                "DWEL",
                "EDUC",
                "EDUCATION",
                "_ELEC",
                "EMAIL",
                "EMIG",
                "EMIGRATION",
                "EMPL",
                "_EMPLOY",
                "ENGA",
                "ENLIST",
                "EVEN",
                "EVENT",
                "_EXCM",
                "EXCO",
                "EYES",
                "FACT",
                "FCOM",
                "FIRST_COMMUNION",
                "_FNRL",
                "_FUN",
                "_FA1",
                "_FA2",
                "_FA3",
                "_FA4",
                "_FA5",
                "_FA6",
                "_FA7",
                "_FA8",
                "_FA9",
                "_FA10",
                "_FA11",
                "_FA12",
                "_FA13",
                "GRAD",
                "GRADUATION",
                "HAIR",
                "HEIG",
                "_HEIG",
                "_HEIGHT",
                "IDNO",
                "IDENT_NUMBER",
                "_INTE",
                "ILL",
                "ILLN",
                "IMMI",
                "IMMIGRATION",
                "LVG",
                "LVNG",
                "MARR",
                "MARRIAGE_COUNT",
                "_MDCL",
                "_MEDICAL",
                "MIL",
                "_MIL",
                "MILA",
                "MILD",
                "MILI",
                "_MILI",
                "MILT",
                "_MILT",
                "_MILTID",
                "MISE",
                "_MISE",
                "_MILITARY_SERVICE",
                "MISN ",
                "_MISN",
                "MOVE",
                "_NAMS",
                "NATI",
                "NATIONALITY",
                "NATU",
                "NATURALIZATION",
                "NCHI",
                "NMR",
                "OCCU",
                "OCCUPATION",
                "ORDI",
                "ORDL",
                "ORDN",
                "ORDINATION",
                "PHON",
                "PHY_DESCRIPTION",
                "PROB",
                "PROBATE",
                "PROP",
                "PROPERTY",
                "RACE",
                "RELI",
                "RELIGION",
                "RESI",
                "RESIR",
                "RESIDENCE",
                "RETI",
                "RETIREMENT",
                "SEX",
                "SOC_SEC_NUMBER",
                "SSN",
                "STIL",
                "STLB",
                "TITL",
                "TITLE",
                "WEIG",
                "_WEIG",
                "_WEIGHT",
                "WILL"
        )
        val FAMILY_EVENT_FACT_TAGS: Set<String> = setOf(
                "ANUL", "ANNULMENT",
                "CENS", "CLAW",
                "_DEATH_OF_SPOUSE", "DIV", "DIVF", "DIVORCE", "_DIV",
                "EMIG", "ENGA", "EVEN", "EVENT",
                "IMMI",
                "MARB", "MARC", "MARL", "MARR", "MARRIAGE", "MARS", "_MBON",
                "NCHI",
                "RESI",
                "SEPA", "_SEPR", "_SEPARATED"
        )

        val DISPLAY_TYPE: Map<String, String> = mapOf(
            "ADOP" to "adop",
            "ADOPTION" to "adop",
            "AFN" to "afn",
            "ANUL" to "anul",
            "ANNULMENT" to "anul",
            "ARRIVAL" to "arvl",
            "ARRI" to "arvl",
            "ARVL" to "arvl",
            "_ATTR" to "attr",
            "BAP" to "bapm",
            "BAPM" to "bapm",
            "BAPT" to "bapm",
            "BAPTISM" to "bapm",
            "BARM" to "barm",
            "BAR_MITZVAH" to "barm",
            "BATM" to "batm",
            "BIRT" to "birt",
            "BIRTH" to "birt",
            "BLES" to "bles",
            "BURI" to "buri",
            "BURIAL" to "buri",
            "CAST" to "cast",
            "CAUS" to "caus",
            "CAUSE" to "caus",
            "CENS" to "cens",
            "CHR" to "chr",
            "CHRISTENING" to "chr",
            "CLAW" to "claw",
            "_COLOR" to "color",
            "CONF" to "conf",
            "CREM" to "crem",
            "_DCAUSE" to "caus",
            "DEAT" to "deat",
            "DEATH" to "deat",
            "_DEATH_OF_SPOUSE" to "death_of_spouse",
            "DEED" to "deed",
            "_DEG" to "deg",
            "_DEGREE" to "deg",
            "DEPA" to "dprt",
            "DPRT" to "dprt",
            "DIV" to "div",
            "DIVF" to "divf",
            "DIVORCE" to "div",
            "_DIV" to "div",
            "DSCR" to "dscr",
            "EDUC" to "educ",
            "EDUCATION" to "educ",
            "_ELEC" to "elec",
            "EMAIL" to "email",
            "EMIG" to "emig",
            "EMIGRATION" to "emig",
            "EMPL" to "empl",
            "_EMPLOY" to "empl",
            "ENGA" to "enga",
            "ENLIST" to "milt",
            "EVEN" to "even",
            "EVENT" to "even",
            "_EXCM" to "excm",
            "EYES" to "eyes",
            "FCOM" to "fcom",
            "_FNRL" to "fnrl",
            "_FUN" to "fnrl",
            "GRAD" to "grad",
            "GRADUATION" to "grad",
            "HAIR" to "hair",
            "HEIG" to "heig",
            "_HEIG" to "heig",
            "_HEIGHT" to "heig",
            "ILL" to "ill",
            "IMMI" to "immi",
            "IMMIGRATION" to "immi",
            "MARB" to "marb",
            "MARC" to "marc",
            "MARL" to "marl",
            "MARR" to "marr",
            "MARRIAGE" to "marr",
            "MARS" to "mars",
            "_MBON" to "marb",
            "_MDCL" to "mdcl",
            "_MEDICAL" to "mdcl",
            "MIL" to "milt",
            "_MIL" to "milt",
            "MILI" to "milt",
            "_MILI" to "milt",
            "_MILT" to "milt",
            "_MILTID" to "milt",
            "_MILITARY_SERVICE" to "milt",
            "MISE" to "milt",
            "_MISN" to "misn",
            "_NAMS" to "nams",
            "NATI" to "nati",
            "NATU" to "natu",
            "NATURALIZATION" to "natu",
            "NCHI" to "nchi",
            "OCCU" to "occu",
            "OCCUPATION" to "occu",
            "ORDI" to "ordn",
            "ORDN" to "ordn",
            "PHON" to "phon",
            "PROB" to "prob",
            "PROP" to "prop",
            "RELI" to "reli",
            "RELIGION" to "reli",
            "RESI" to "resi",
            "RESIDENCE" to "resi",
            "RETI" to "reti",
            "SEPA" to "sepa",
            "_SEPARATED" to "sepa",
            "_SEPR" to "sepa",
            "SEX" to "sex",
            "SSN" to "ssn",
            "SOC_SEC_NUMBER" to "ssn",
            "TITL" to "titl",
            "TITLE" to "titl",
            "_WEIG" to "weig",
            "_WEIGHT" to "weig",
            "WILL" to "will"
        )

        @Deprecated("")
        val OTHER_TYPE: String = "Other"
    }
}
