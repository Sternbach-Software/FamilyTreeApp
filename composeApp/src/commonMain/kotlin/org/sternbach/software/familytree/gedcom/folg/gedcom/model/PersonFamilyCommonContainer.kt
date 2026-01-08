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
 * Date: 12/28/11
 */
abstract class PersonFamilyCommonContainer : SourceCitationContainer() {
    var _eventsFacts: MutableList<EventFact>? = null
    var _ldsOrdinances: MutableList<LdsOrdinance>? = null
    var _refns: MutableList<String>? = null
    var rin: String? = null
    var change: Change? = null
    var uid: String? = null
    var uidTag: String? = null

    fun getEventsFacts(): List<EventFact> = _eventsFacts ?: emptyList()

    fun addEventFact(eventFact: EventFact) {
        if (_eventsFacts == null) {
            _eventsFacts = mutableListOf()
        }
        _eventsFacts!!.add(eventFact)
    }

    fun getLdsOrdinances(): List<LdsOrdinance> = _ldsOrdinances ?: emptyList()

    fun addLdsOrdinance(ldsOrdinance: LdsOrdinance) {
        if (_ldsOrdinances == null) {
            _ldsOrdinances = mutableListOf()
        }
        _ldsOrdinances!!.add(ldsOrdinance)
    }

    val referenceNumbers: List<String>
        get() = _refns ?: emptyList()

    fun addReferenceNumber(refn: String) {
        if (_refns == null) {
            _refns = mutableListOf()
        }
        _refns!!.add(refn)
    }

    override fun visitContainedObjects(visitor: Visitor) {
        for (eventFact in getEventsFacts()) {
            eventFact.accept(visitor)
        }
        for (ldsOrdinance in getLdsOrdinances()) {
            ldsOrdinance.accept(visitor)
        }
        if (change != null) {
            change!!.accept(visitor)
        }
        super.visitContainedObjects(visitor)
    }
}
