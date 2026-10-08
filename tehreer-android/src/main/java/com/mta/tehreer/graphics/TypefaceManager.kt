/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mta.tehreer.graphics

/**
 * The `TypefaceManager` class provides management activities related to typefaces.
 */
object TypefaceManager {
    private val tags = HashMap<Any, Typeface>()
    private val typefaces = ArrayList<Typeface>()
    private var sorted = false

    private val comparator = Comparator<Typeface> { first, second ->
        val result = first.familyName.compareTo(second.familyName, ignoreCase = true)

        if (result == 0) first.styleName.compareTo(second.styleName, ignoreCase = true) else result
    }

    /**
     * Registers a typeface in `TypefaceManager`.
     *
     * @param typeface The typeface that will be registered.
     * @param tag An optional tag to identify the typeface.
     *
     * @throws IllegalArgumentException if `typeface` is already registered, or `tag` is already
     *         taken.
     */
    fun registerTypeface(typeface: Typeface, tag: Any?) {
        synchronized(this) {
            require(!typefaces.contains(typeface)) { "This typeface is already registered" }

            if (tag != null) {
                require(!tags.containsKey(tag)) { "This tag is already taken" }

                tags[tag] = typeface
                typeface.tag = tag
            }

            sorted = false
            typefaces.add(typeface)
        }
    }

    /**
     * Unregisters a typeface in `TypefaceManager`.
     *
     * @param typeface The typeface to unregister.
     *
     * @throws IllegalArgumentException if `typeface` is not registered.
     */
    fun unregisterTypeface(typeface: Typeface) {
        synchronized(this) {
            require(typefaces.remove(typeface)) { "This typeface is not registered" }

            typeface.tag?.let { tags.remove(it) }
            typeface.tag = null
        }
    }

    /**
     * Returns the typeface registered against the specified tag.
     *
     * @param tag The tag object that identifies the typeface.
     * @return The registered typeface, or `null` if no typeface is registered against the specified
     *         tag.
     */
    fun getTypeface(tag: Any): Typeface? {
        synchronized(this) {
            return tags[tag]
        }
    }

    /**
     * Returns the tag of a registered typeface.
     *
     * @param typeface The typeface whose tag is returned.
     * @return The tag of the typeface, or `null` if no tag was specified while registration.
     *
     * @throws IllegalArgumentException if `typeface` is not registered.
     */
    fun getTypefaceTag(typeface: Typeface): Any? {
        synchronized(this) {
            require(typefaces.contains(typeface)) { "This typeface is not registered" }

            return typeface.tag
        }
    }

    /**
     * Looks for a type family having specified family name.
     *
     * @param familyName The name of the family.
     * @return A type family having specified family name.
     */
    fun getTypeFamily(familyName: String): TypeFamily? {
        val entries = synchronized(this) {
            sortTypefaces()

            typefaces.filter { it.familyName.equals(familyName, ignoreCase = true) }
        }

        return if (entries.isNotEmpty()) TypeFamily(familyName, entries) else null
    }

    /**
     * Looks for a registered typeface having specified full name.
     *
     * @param fullName The full name of the typeface.
     * @return The typeface having specified full name, or `null` if no such typeface is registered.
     */
    fun getTypefaceByName(fullName: String): Typeface? {
        synchronized(this) {
            return typefaces.firstOrNull { it.fullName.equals(fullName, ignoreCase = true) }
        }
    }

    /**
     * Returns a list of available type families sorted by their names in ascending order.
     *
     * @return A list of available type families.
     */
    fun getAvailableFamilies(): List<TypeFamily> {
        val familyMap = java.util.TreeMap<String, MutableList<Typeface>>(String.CASE_INSENSITIVE_ORDER)

        synchronized(this) {
            sortTypefaces()

            for (typeface in typefaces) {
                familyMap.getOrPut(typeface.familyName) { ArrayList() }.add(typeface)
            }
        }

        return familyMap.map { TypeFamily(it.key, it.value) }
    }

    /**
     * Returns a list of available typefaces sorted by their family and style names in ascending
     * order.
     *
     * @return A list of available typefaces.
     */
    fun getAvailableTypefaces(): List<Typeface> {
        synchronized(this) {
            sortTypefaces()

            return ArrayList(typefaces)
        }
    }

    private fun sortTypefaces() {
        if (!sorted) {
            typefaces.sortWith(comparator)
            sorted = true
        }
    }
}
