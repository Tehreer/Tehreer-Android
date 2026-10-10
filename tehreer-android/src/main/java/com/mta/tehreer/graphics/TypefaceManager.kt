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

import com.mta.tehreer.internal.JniBridge

/**
 * The `TypefaceManager` class provides management activities related to typefaces. The registry
 * itself lives in Tehreer Core, which identifies typefaces by integer tags, groups them into
 * families and matches them to a style as described by CSS Fonts Level 4.
 */
object TypefaceManager {
    // Core hands out native typefaces, and this maps them back to the objects that were registered.
    private val registered = HashMap<Long, Typeface>()

    init {
        JniBridge.loadLibrary()
    }

    /**
     * Registers a typeface in `TypefaceManager`.
     *
     * @param typeface The typeface that will be registered.
     * @param tag An optional positive tag to identify the typeface, or zero for none.
     * @param familyId An optional positive ID of the family that the typeface belongs to, or zero
     *        to group it with the typefaces that have no family ID and the same family name.
     *
     * @throws IllegalArgumentException if `typeface` is already registered, or `tag` is already
     *         taken.
     */
    @JvmOverloads
    fun registerTypeface(typeface: Typeface, tag: Int = 0, familyId: Int = 0) {
        require(tag >= 0) { "The tag is negative" }
        require(familyId >= 0) { "The family ID is negative" }

        synchronized(this) {
            require(nRegisterTypeface(typeface.nativeTypeface, tag, familyId)) {
                "This typeface is already registered, or the tag is already taken"
            }

            registered[typeface.nativeTypeface] = typeface
        }
    }

    /**
     * Unregisters a typeface from `TypefaceManager`.
     *
     * @param typeface The typeface that will be unregistered.
     *
     * @throws IllegalArgumentException if `typeface` is not registered.
     */
    fun unregisterTypeface(typeface: Typeface) {
        synchronized(this) {
            require(nUnregisterTypeface(typeface.nativeTypeface)) { "This typeface is not registered" }

            registered.remove(typeface.nativeTypeface)
        }
    }

    /**
     * Returns the typeface that is registered with the given tag.
     *
     * @param tag The tag of the typeface.
     * @return The typeface registered with the given tag, or `null` if there is none.
     */
    fun getTypeface(tag: Int): Typeface? {
        synchronized(this) {
            return registered[nGetTypeface(tag)]
        }
    }

    /**
     * Returns the tag of a registered typeface.
     *
     * @param typeface The typeface whose tag is desired.
     * @return The tag of the typeface, or zero if it has none.
     *
     * @throws IllegalArgumentException if `typeface` is not registered.
     */
    fun getTypefaceTag(typeface: Typeface): Int {
        synchronized(this) {
            require(registered.containsKey(typeface.nativeTypeface)) { "This typeface is not registered" }

            return nGetTypefaceTag(typeface.nativeTypeface)
        }
    }

    /**
     * Returns the family whose name matches the given one, ignoring the case.
     *
     * @param familyName The name of the family.
     * @return The family with the given name, or `null` if no typeface has it.
     */
    fun getTypeFamily(familyName: String): TypeFamily? {
        return getAvailableFamilies().firstOrNull { it.familyName.equals(familyName, ignoreCase = true) }
    }

    /**
     * Returns the typeface of a family that is closest to the given style.
     *
     * @param familyName The name of the family, which is matched ignoring the case.
     * @param typeWidth The typographic width of desired typeface.
     * @param typeWeight The typographic weight of desired typeface.
     * @param typeSlope The typographic slope of desired typeface.
     * @return The best matching typeface, or `null` if no typeface has the family name.
     */
    fun getTypefaceByStyle(
        familyName: String, typeWidth: TypeWidth, typeWeight: TypeWeight, typeSlope: TypeSlope
    ): Typeface? {
        synchronized(this) {
            val byName = nGetMatchingTypefaceByFamilyName(
                familyName, typeWidth.value, typeWeight.value, typeSlope.ordinal
            )

            if (registered.containsKey(byName)) {
                return registered[byName]
            }

            // The typefaces that have a family ID are not matched by the name of their family.
            val family = getAvailableFamilies().firstOrNull {
                it.familyId != 0 && it.familyName.equals(familyName, ignoreCase = true)
            }

            return family?.let { getTypefaceByFamilyId(it.familyId, typeWidth, typeWeight, typeSlope) }
        }
    }

    /**
     * Returns the registered typeface that has the given full name, ignoring the case.
     *
     * @param fullName The full name of the typeface.
     * @return The typeface with the given full name, or `null` if there is none.
     */
    fun getTypefaceByName(fullName: String): Typeface? {
        return getAvailableTypefaces().firstOrNull { it.fullName.equals(fullName, ignoreCase = true) }
    }

    /**
     * Returns the families of the registered typefaces, ordered by name.
     *
     * @return The list of the available families.
     */
    fun getAvailableFamilies(): List<TypeFamily> {
        synchronized(this) {
            val families = nGetFamilies()
            val ids = families[0] as IntArray
            @Suppress("UNCHECKED_CAST")
            val names = families[1] as Array<String>
            val typefaces = getAvailableTypefaces()

            return ids.indices.map { index ->
                val id = ids[index]
                val name = names[index]
                val members = typefaces.filter {
                    if (id != 0) {
                        nGetTypefaceFamilyId(it.nativeTypeface) == id
                    } else {
                        nGetTypefaceFamilyId(it.nativeTypeface) == 0 &&
                            it.familyName.equals(name, ignoreCase = true)
                    }
                }

                TypeFamily(name, id, members)
            }.filter { it.typefaces.isNotEmpty() }
        }
    }

    /**
     * Returns the registered typefaces, ordered by family name and then by style name.
     *
     * @return The list of the available typefaces.
     */
    fun getAvailableTypefaces(): List<Typeface> {
        synchronized(this) {
            return nGetTypefaces().toList().mapNotNull { registered[it] }
        }
    }

    internal fun getTypefaceByFamilyId(
        familyId: Int, typeWidth: TypeWidth, typeWeight: TypeWeight, typeSlope: TypeSlope
    ): Typeface? {
        synchronized(this) {
            return registered[nGetMatchingTypefaceByFamilyId(
                familyId, typeWidth.value, typeWeight.value, typeSlope.ordinal
            )]
        }
    }

    private external fun nRegisterTypeface(nativeTypeface: Long, tag: Int, familyId: Int): Boolean
    private external fun nUnregisterTypeface(nativeTypeface: Long): Boolean
    private external fun nGetTypeface(tag: Int): Long
    private external fun nGetTypefaceTag(nativeTypeface: Long): Int
    private external fun nGetTypefaceFamilyId(nativeTypeface: Long): Int
    private external fun nGetMatchingTypefaceByFamilyId(
        familyId: Int, width: Int, weight: Int, slope: Int
    ): Long
    private external fun nGetMatchingTypefaceByFamilyName(
        familyName: String, width: Int, weight: Int, slope: Int
    ): Long
    private external fun nGetTypefaces(): LongArray
    private external fun nGetFamilies(): Array<Any>
}
