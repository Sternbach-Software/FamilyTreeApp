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
 * Date: 12/29/11
 */
abstract class NoteContainer : ExtensionContainer() {
    private var noteRefs: MutableList<NoteRef>? = null
    private var notes: MutableList<Note>? = null

    /**
     * Use this function in place of getNotes and getNoteRefs
     * @param gedcom Gedcom
     * @return inline notes as well as referenced notes
     */
    fun getAllNotes(gedcom: Gedcom): List<Note> {
        val notes: MutableList<Note> = mutableListOf()
        for (noteRef in getNoteRefs()) {
            val note = noteRef.getNote(gedcom)
            if (note != null) {
                notes.add(note)
            }
        }
        notes.addAll(getNotes())
        return notes
    }

    fun getNoteRefs(): List<NoteRef> {
        return noteRefs ?: emptyList()
    }

    fun setNoteRefs(noteRefs: MutableList<NoteRef>?) {
        this.noteRefs = noteRefs
    }

    fun addNoteRef(noteRef: NoteRef) {
        if (noteRefs == null) {
            noteRefs = mutableListOf()
        }
        noteRefs!!.add(noteRef)
    }

    fun getNotes(): List<Note> {
        return notes ?: emptyList()
    }

    fun setNotes(notes: MutableList<Note>?) {
        this.notes = notes
    }

    fun addNote(note: Note) {
        if (notes == null) {
            notes = mutableListOf()
        }
        notes!!.add(note)
    }

    override fun visitContainedObjects(visitor: Visitor) {
        for (noteRef in getNoteRefs()) {
            noteRef.accept(visitor)
        }
        for (note in getNotes()) {
            note.accept(visitor)
        }
        super.visitContainedObjects(visitor)
    }
}
