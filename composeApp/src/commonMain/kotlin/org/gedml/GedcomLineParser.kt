package org.gedml

/**
 * User: npowell
 * Date: May 2, 2007
 */
class GedcomLineParser {
    private var matchResult: MatchResult? = null

    fun parse(line: String?): Boolean {
        if (line == null) {
            matchResult = null
            return false
        }
        matchResult = pGedcomLine.matchEntire(line)
        return matchResult != null
    }

    val level: String
        get() = matchResult!!.groupValues[LEVEL_GROUP]

    val xRef: String
        get() = matchResult!!.groupValues[XREF_GROUP]

    val iD: String
        get() = matchResult!!.groupValues[ID_GROUP]

    val tag: String
        get() = matchResult!!.groupValues[TAG_GROUP]

    val value: String
        get() = matchResult!!.groupValues[VALUE_GROUP]

    companion object {
        // Kotlin Regex uses slightly different syntax, but basic regex is same.
        // DOT_MATCHES_ALL equivalent is implied if we handle newlines or use dotMatchesAll option.
        // However, the original pattern had ^ and $, so matchEntire is appropriate.
        // The original used Pattern.DOTALL.
        private val pGedcomLine = Regex(
            "^\\s*(\\d)\\s+(@([^@ ]+)@\\s+)?([a-zA-Z_0-9.]+)(\\s+@([^@ ]+)@)?(\\s(.*))?$", 
            setOf(RegexOption.MULTILINE)
        )

        private const val LEVEL_GROUP = 1
        private const val ID_GROUP = 3
        private const val TAG_GROUP = 4
        private const val XREF_GROUP = 6
        private const val VALUE_GROUP = 8
    }
}
