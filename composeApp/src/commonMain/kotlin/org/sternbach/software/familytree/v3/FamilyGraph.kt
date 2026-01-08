package sternbach.software.familytreecompose

import kotlinx.datetime.LocalDate


data class FamilyGraph(
    val persons: Map<String, Person> = emptyMap(),
    val unions: List<Union> = emptyList(),
    // Graph bounds calculated by LayoutEngine
    var width: Float = 0f,
    var height: Float = 0f
)

data class Person(
    val id: String,
    val name: String,
    val birthDate: LocalDate? = null,
    val spouseIds: MutableList<String> = mutableListOf(),
    val childrenIds: MutableList<String> = mutableListOf(),
    val adoptiveChildrenIds: MutableList<String> = mutableListOf(),
    var generation: Int = 0,
    // Layout coordinates (to be assigned by LayoutEngine)
    var x: Float = 0f,
    var y: Float = 0f
)

data class Union(
    val id: String,
    val spouseIds: List<String>,
    val childrenIds: List<String> = emptyList(),
    var isIncestuous: Boolean = false,
    // Layout coordinates
    var x: Float = 0f,
    var y: Float = 0f
)