package org.sternbach.software.familytree

import org.folg.gedcom.model.Gedcom
import sternbach.software.familytreecompose.FamilyGraph
import sternbach.software.familytreecompose.Person
import sternbach.software.familytreecompose.Union
import kotlinx.datetime.LocalDate

object GedcomMapper {
    fun mapGedcomToFamilyGraph(gedcom: Gedcom): FamilyGraph {
        val persons = mutableMapOf<String, Person>()
        val unions = mutableListOf<Union>()

        // 1. Create Persons
        gedcom.getPeople().forEach { gedcomPerson ->
            val id = gedcomPerson.id ?: return@forEach
            val name = gedcomPerson.getNames().firstOrNull()?.let {
                "${it.given ?: ""} ${it.surname ?: ""}".trim()
            } ?: "Unknown"

            // Attempt to parse birth date
            // EventFact has property `date` which is String?
            val birthEvent = gedcomPerson.getEventsFacts().find { it.tag == "BIRT" }
            val birthDateStr = birthEvent?.date
            var birthDate: LocalDate? = null

            if (birthDateStr != null) {
                birthDate = parseApproximateDate(birthDateStr)
            }

            persons[id] = Person(
                id = id,
                name = name,
                birthDate = birthDate
            )
        }

        // 2. Create Unions and Link
        gedcom.getFamilies().forEach { fam ->
            val id = fam.id ?: return@forEach
            val spouseIds = mutableListOf<String>()

            // _husbandRefs and _wifeRefs are var MutableList<SpouseRef?>?
            // fam._husbandRefs is accessible because it is public in Kotlin

            fam._husbandRefs?.forEach { ref ->
                ref?.ref?.let { spouseIds.add(it) }
            }
            fam._wifeRefs?.forEach { ref ->
                ref?.ref?.let { spouseIds.add(it) }
            }

            // _childRefs is var MutableList<ChildRef?>?
            val childrenIds = fam._childRefs?.mapNotNull { it?.ref } ?: emptyList()

            val union = Union(
                id = id,
                spouseIds = spouseIds,
                childrenIds = childrenIds
            )
            unions.add(union)

            // Update Person links
            spouseIds.forEach { spouseId ->
                persons[spouseId]?.let { person ->
                    // Add other spouses to this person's spouse list
                    spouseIds.forEach { otherId ->
                        if (otherId != spouseId) {
                            if (!person.spouseIds.contains(otherId)) {
                                person.spouseIds.add(otherId)
                            }
                        }
                    }

                    // Add children to this person
                    childrenIds.forEach { childId ->
                         if (!person.childrenIds.contains(childId)) {
                             person.childrenIds.add(childId)
                         }
                    }
                }
            }
        }

        return FamilyGraph(persons = persons, unions = unions)
    }

    private fun parseApproximateDate(dateStr: String): LocalDate? {
        try {
            val yearRegex = Regex("\\d{4}")
            val match = yearRegex.find(dateStr)
            if (match != null) {
                val year = match.value.toInt()
                return LocalDate(year, 1, 1)
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }
}
