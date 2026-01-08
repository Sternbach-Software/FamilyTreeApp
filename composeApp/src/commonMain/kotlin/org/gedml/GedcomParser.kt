package org.gedml

class GedcomParser {
    fun parse(lines: Sequence<String>, listener: GedcomParserListener) {
        var line: String
        var thisLevel: Int
        var prevLevel = -1
        var iden: String
        var tag: String
        var xref: String
        var valu: String
        var lineNr = 0
        val stack = mutableListOf<String>()
        stack.add("GED")
        val buf = StringBuilder()

        listener.startDocument()
        // Artificial start element for GEDCOM root (as per original logic)
        listener.startElement("GED", "GED", null)

        var goodLine = false
        val lineParser = GedcomLineParser()

        for (rawLine in lines) {
            lineNr++
            
            // remove control chars
            buf.setLength(0)
            for (j in 0 until rawLine.length) {
                val c = rawLine[j]
                if (c.code >= 32 || c.code == 9) {
                    buf.append(c)
                }
            }
            line = buf.toString()

            if (line.isNotEmpty()) {
                if (!lineParser.parse(line)) {
                    if (goodLine) {
                        listener.error("Line does not appear to be standard", lineNr)
                        listener.characters(line)
                    }
                    if (lineNr > 20 && !goodLine) {
                        break
                    }
                } else {
                    thisLevel = lineParser.level.toInt()
                    tag = lineParser.tag

                    if (thisLevel > prevLevel + 1) {
                        listener.error("Level > prevLevel+1", lineNr)
                    } else if (thisLevel < 0) {
                        listener.error("Level < 0", lineNr)
                    } else if (tag.isEmpty()) {
                        listener.error("Tag not found", lineNr)
                    } else {
                        iden = lineParser.iD
                        xref = lineParser.xRef
                        valu = lineParser.value

                        // insert any necessary closing tags
                        while (thisLevel <= prevLevel) {
                            if (stack.isNotEmpty()) {
                                val endtag = stack.removeAt(stack.lastIndex)
                                listener.endElement(endtag)
                            }
                            prevLevel--
                        }

                        // Attributes logic in SAX was: ID, REF
                        // Here we pass them to startElement directly
                        
                        listener.startElement(tag, if (iden.isNotEmpty()) iden else null, if (xref.isNotEmpty()) xref else null)
                        goodLine = true
                        stack.add(tag)
                        prevLevel = thisLevel
                        
                        if (valu.isNotEmpty()) {
                            listener.characters(valu)
                        }
                    }
                }
            }
        }

        if (!goodLine) {
             listener.fatalError("no good lines found in the first 20 lines", lineNr)
        }
        
        // close remaining tags
        while (prevLevel >= 0 && stack.isNotEmpty()) { // Close up to GED tag (level -1 logic implied?)
             // The original logic closed until stack was empty or similar.
             // Original: while (thisLevel <= prevLevel) ...
             // At end of loop:
             // contentHandler!!.endElement("", "GED", "GED")
        }
        
        // Force close all remaining except "GED" which we manually close after loop?
        // Actually original logic pushed "GED" and manually closed it.
        // My logic above pops from stack. "GED" is at bottom.
        // The loop `while (thisLevel <= prevLevel)` handles closing when level drops.
        // But at end of file, we need to close everything back to root.
        
        while (stack.size > 1) { // Leave "GED"
            val endtag = stack.removeAt(stack.lastIndex)
            listener.endElement(endtag)
        }
        
        listener.endElement("GED")
        listener.endDocument()
    }
}