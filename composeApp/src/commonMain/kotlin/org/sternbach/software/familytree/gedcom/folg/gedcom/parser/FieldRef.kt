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

import java.lang.reflect.InvocationTargetException

/**
 * User: Dallan
 * Date: 12/26/11
 */
class FieldRef(val target: Any?, val fieldName: String) {
    val classFieldName: String
        get() = target!!.javaClass.name + "." + fieldName

    @get:Throws(NoSuchMethodException::class)
    @set:Throws(NoSuchMethodException::class)
    var value: String?
        get() {
            try {
                val method = target!!.javaClass.getMethod("get" + fieldName)
                return method.invoke(target) as String
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }
        set(value) {
            try {
                val method = target!!.javaClass.getMethod("set" + fieldName, String::class.java)
                method.invoke(target, value)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }

    @Throws(NoSuchMethodException::class)
    fun appendValue(value: String) {
        try {
            val currentValue = this.value
            this.value = (currentValue ?: "") + value
        } catch (e: NoSuchMethodException) {
            // try "add"
            try {
                val method = target!!.javaClass.getMethod("add" + fieldName, String::class.java)
                method.invoke(target, value)
            } catch (e1: InvocationTargetException) {
                e1.printStackTrace()
                throw RuntimeException(e)
            } catch (e1: IllegalAccessException) {
                e1.printStackTrace()
                throw RuntimeException(e)
            }
        }
    }
}
