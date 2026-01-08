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

import org.folg.gedcom.model.GedcomTag
import org.gedml.GedcomParser
import org.gedml.GedcomParserListener

/**
 * Call parseGedcom to parse a gedcom file into a list of GedcomTag's
 * User: Dallan
 * Date: 12/23/11
 */
class TreeParser : GedcomParserListener {
    private var tree: GedcomTag? = null
    private var nodeStack: MutableList<GedcomTag> = mutableListOf()
    var errors: MutableList<String> = mutableListOf()
    var warnings: MutableList<String> = mutableListOf()

    override fun startDocument() {
        tree = null
        nodeStack.clear()
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

    override fun startElement(tag: String, id: String?, xref: String?) {
        val node = GedcomTag(id, tag, xref)
        if (tree == null) {
            tree = node
        } else {
            if (nodeStack.isNotEmpty()) {
                nodeStack.last().addChild(node)
            }
        }
        nodeStack.add(node)
    }

    override fun endElement(tag: String) {
        if (nodeStack.isNotEmpty()) {
            nodeStack.removeAt(nodeStack.lastIndex)
        }
    }

    override fun characters(text: String) {
        if (nodeStack.isNotEmpty()) {
            val tos = nodeStack.last()
            tos.appendValue(text)
        }
    }

    fun parseGedcom(lines: Sequence<String>): List<GedcomTag?> {
        val parser = GedcomParser()
        parser.parse(lines, this)
        // tree is the root "GED" tag. The children of GED are the content.
        return tree?.getChildren() ?: emptyList()
    }
}