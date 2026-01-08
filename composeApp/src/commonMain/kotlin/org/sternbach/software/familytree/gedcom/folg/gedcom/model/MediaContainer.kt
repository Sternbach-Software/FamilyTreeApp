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
 * Date: 12/30/11
 */
abstract class MediaContainer : NoteContainer() {
    private var mediaRefs: MutableList<MediaRef>? = null
    private var media: MutableList<Media>? = null

    /**
     * Use this function in place of getMedia and getMediaRefs
     * @param gedcom Gedcom
     * @return inline media as well as referenced media
     */
    fun getAllMedia(gedcom: Gedcom): List<Media> {
        val media: MutableList<Media> = mutableListOf()
        for (mediaRef in getMediaRefs()) {
            val m = mediaRef.getMedia(gedcom)
            if (m != null) {
                media.add(m)
            }
        }
        media.addAll(getMedia())
        return media
    }

    fun getMediaRefs(): List<MediaRef> {
        return mediaRefs ?: emptyList()
    }

    fun setMediaRefs(mediaRefs: MutableList<MediaRef>?) {
        this.mediaRefs = mediaRefs
    }

    fun addMediaRef(mediaRef: MediaRef) {
        if (mediaRefs == null) {
            mediaRefs = mutableListOf()
        }
        mediaRefs!!.add(mediaRef)
    }

    fun getMedia(): List<Media> {
        return media ?: emptyList()
    }

    fun setMedia(media: MutableList<Media>?) {
        this.media = media
    }

    fun addMedia(mediaObject: Media) {
        if (media == null) {
            media = mutableListOf()
        }
        media!!.add(mediaObject)
    }

    override fun visitContainedObjects(visitor: Visitor) {
        for (mediaRef in getMediaRefs()) {
            mediaRef.accept(visitor)
        }
        for (m in getMedia()) {
            m.accept(visitor)
        }
        super.visitContainedObjects(visitor)
    }
}
