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

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class BidiLineMirrorIterableTest {
    private val DEFAULT_TEXT: String = "یہ ایک (car) ہے۔"

    private lateinit var bidiLine: BidiLine
    private lateinit var subject: BidiLine.MirrorIterable

    @Before
    fun setUp() {
        val text = DEFAULT_TEXT
        val bidiAlgorithm = BidiAlgorithm(text)
        val bidiParagraph = bidiAlgorithm.createParagraph(0, text.length, BaseDirection.DEFAULT_LEFT_TO_RIGHT)
        bidiLine = bidiParagraph.createLine(0, text.length)
        subject = BidiLine.MirrorIterable(bidiLine)
    }

    @Test
    fun testIterator() {
        // When
        val iterator: Iterator<BidiPair> = subject.iterator()

        // Then
        assertTrue(iterator is BidiLine.MirrorIterator)
        assertSame((iterator as BidiLine.MirrorIterator).owner, bidiLine)
    }
}
