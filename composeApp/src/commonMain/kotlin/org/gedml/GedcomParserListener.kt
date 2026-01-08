package org.gedml

interface GedcomParserListener {
    fun startDocument()
    fun endDocument()
    fun startElement(tag: String, id: String?, xref: String?)
    fun endElement(tag: String)
    fun attribute(name: String, value: String)
    fun characters(text: String)
    fun warning(message: String, lineNumber: Int)
    fun error(message: String, lineNumber: Int)
    fun fatalError(message: String, lineNumber: Int)
}
