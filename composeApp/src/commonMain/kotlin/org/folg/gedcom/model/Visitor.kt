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
 * If you override these functions, return false if you don't want to visit an object's children
 */
open class Visitor {
    open fun visit(address: Address): Boolean {
        return true
    }

    open fun visit(association: Association): Boolean {
        return true
    }

    open fun visit(change: Change?): Boolean {
        return true
    }

    open fun visit(characterSet: CharacterSet): Boolean {
        return true
    }

    open fun visit(childRef: ChildRef): Boolean {
        return true
    }

    open fun visit(dateTime: DateTime): Boolean {
        return true
    }

    open fun visit(eventFact: EventFact): Boolean {
        return true
    }

    open fun visit(extensionKey: String?, extension: Any?): Boolean {
        return true
    }

    open fun visit(family: Family): Boolean {
        return true
    }

    open fun visit(gedcom: Gedcom?): Boolean {
        return true
    }

    open fun visit(gedcomVersion: GedcomVersion): Boolean {
        return true
    }

    open fun visit(generator: Generator): Boolean {
        return true
    }

    open fun visit(generatorCorporation: GeneratorCorporation): Boolean {
        return true
    }

    open fun visit(generatorData: GeneratorData): Boolean {
        return true
    }

    open fun visit(header: Header): Boolean {
        return true
    }

    open fun visit(ldsOrdinance: LdsOrdinance): Boolean {
        return true
    }

    open fun visit(media: Media): Boolean {
        return true
    }

    open fun visit(mediaRef: MediaRef): Boolean {
        return true
    }

    open fun visit(name: Name): Boolean {
        return true
    }

    open fun visit(note: Note): Boolean {
        return true
    }

    open fun visit(noteRef: NoteRef): Boolean {
        return true
    }

    open fun visit(parentFamilyRef: ParentFamilyRef): Boolean {
        return true
    }

    open fun visit(parentRelationship: ParentRelationship, isFather: Boolean): Boolean {
        return true
    }

    open fun visit(person: Person): Boolean {
        return true
    }

    open fun visit(repository: Repository): Boolean {
        return true
    }

    open fun visit(repositoryRef: RepositoryRef): Boolean {
        return true
    }

    open fun visit(source: Source): Boolean {
        return true
    }

    open fun visit(sourceCitation: SourceCitation): Boolean {
        return true
    }

    open fun visit(spouseRef: SpouseRef, isHusband: Boolean): Boolean {
        return true
    }

    open fun visit(spouseFamilyRef: SpouseFamilyRef): Boolean {
        return true
    }

    open fun visit(submission: Submission): Boolean {
        return true
    }

    open fun visit(submitter: Submitter): Boolean {
        return true
    }

    open fun endVisit(obj: ExtensionContainer?) {}
}
