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

package com.mta.tehreer.unicode

import org.junit.Assert.assertEquals

import org.junit.Test

class CodePointTest {
    @Test
    fun testGetBidiClass() {
        assertEquals(CodePoint.getBidiClass('a'.code), BidiClass.LEFT_TO_RIGHT)
        assertEquals(CodePoint.getBidiClass('ا'.code), BidiClass.ARABIC_LETTER)
        assertEquals(CodePoint.getBidiClass('1'.code), BidiClass.EUROPEAN_NUMBER)
        assertEquals(CodePoint.getBidiClass(' '.code), BidiClass.WHITE_SPACE)
    }

    @Test
    fun testGetGeneralCategory() {
        assertEquals(CodePoint.getGeneralCategory('A'.code), GeneralCategory.UPPERCASE_LETTER)
        assertEquals(CodePoint.getGeneralCategory('a'.code), GeneralCategory.LOWERCASE_LETTER)
        assertEquals(CodePoint.getGeneralCategory('1'.code), GeneralCategory.DECIMAL_NUMBER)
        assertEquals(CodePoint.getGeneralCategory(' '.code), GeneralCategory.SPACE_SEPARATOR)
    }

    @Test
    fun testGetScript() {
        assertEquals(CodePoint.getScript(' '.code), Script.COMMON)
        assertEquals(CodePoint.getScript('ا'.code), Script.ARABIC)
        assertEquals(CodePoint.getScript('a'.code), Script.LATIN)
    }

    @Test
    fun testGetMirror() {
        assertEquals(CodePoint.getMirror('('.code), ')'.code)
        assertEquals(CodePoint.getMirror(')'.code), '('.code)
    }
}


