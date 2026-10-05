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

package com.mta.tehreer.layout

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.mta.tehreer.internal.layout.RunCollection
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safety of `RunCollection`'s block list (see `RunCollection.finalizeBlocks`, called once
 * by `ShapeResolver.createParagraphsAndRuns()` right before it hands the collection back). Reuses
 * [NeverShownBlockSpan] (declared in [BreakResolverViewSpanTest]'s file, same package), same as
 * [RunCollectionBlockTest].
 *
 * The bug this replaces (`by lazy(LazyThreadSafetyMode.NONE)`) was a race on the *first read* of
 * the lazily-computed block list, which is undefined behaviour if two threads can reach it before
 * either has finished computing it - exactly the situation a `Typesetter`'s docs promise is safe
 * ("other frames of the same typesetter may be in use"). Computing eagerly, before the
 * `RunCollection` is ever handed to anything but the thread that built it, removes the race: by
 * construction there is no unsynchronized write left for a later read to race with.
 */
class RunCollectionThreadSafetyTest {
    private fun typesetterOf(build: SpannableStringBuilder.() -> Unit): Typesetter {
        val spanned = SpannableStringBuilder().apply(build)
        val typeface = TypefaceStore.getNafeesWeb()
        val defaultSpans = listOf<Any>(TypefaceSpan(typeface), TypeSizeSpan(32.0f))

        return Typesetter(spanned, defaultSpans)
    }

    private fun SpannableStringBuilder.appendBlock(): Int {
        val start = length
        append("￼")
        setSpan(NeverShownBlockSpan(), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    /**
     * Two `RunCollection`s, each built entirely on its own background thread (mirroring
     * `ShapeResolver.createParagraphsAndRuns()`'s "built once, synchronously" contract - just not
     * necessarily on the main/test thread), published back to the test thread only once
     * construction (and so `finalizeBlocks()`) has completed. Both must see a fully-formed,
     * correct block list - nothing about a `Typesetter`'s validity should depend on which thread
     * happened to build it.
     */
    @Test
    fun twoRunCollectionsBuiltOnDifferentThreadsBothSeeAFullyFormedBlockList() {
        val firstResult = AtomicReference<Typesetter>()
        var firstBlockStart = 0

        val secondResult = AtomicReference<Typesetter>()
        var secondBlockStart = 0
        var secondBlockStart2 = 0

        val firstThread = Thread {
            var blockStart = 0
            val typesetter = typesetterOf {
                append("AAAA")
                blockStart = appendBlock()
                append("BBBB")
            }
            firstBlockStart = blockStart
            firstResult.set(typesetter)
        }

        val secondThread = Thread {
            var blockStart1 = 0
            var blockStart2 = 0
            val typesetter = typesetterOf {
                append("CCCC")
                blockStart1 = appendBlock()
                append("DDDD")
                blockStart2 = appendBlock()
                append("EEEE")
            }
            secondBlockStart = blockStart1
            secondBlockStart2 = blockStart2
            secondResult.set(typesetter)
        }

        firstThread.start()
        secondThread.start()
        firstThread.join(20_000)
        secondThread.join(20_000)

        val firstTypesetter = firstResult.get()
        val secondTypesetter = secondResult.get()

        assertNotNull("the first thread's typesetter never finished", firstTypesetter)
        assertNotNull("the second thread's typesetter never finished", secondTypesetter)
        firstTypesetter!!
        secondTypesetter!!

        val firstRuns = firstTypesetter.getRuns()
        val firstEnd = firstTypesetter.spanned.length
        assertEquals(firstBlockStart, firstRuns.findBlockForward(0, firstEnd)?.startIndex)
        assertEquals(firstBlockStart, firstRuns.findBlockBackward(0, firstEnd)?.startIndex)

        val secondRuns = secondTypesetter.getRuns()
        val secondEnd = secondTypesetter.spanned.length
        assertEquals(secondBlockStart, secondRuns.findBlockForward(0, secondEnd)?.startIndex)
        assertEquals(secondBlockStart2, secondRuns.findBlockBackward(0, secondEnd)?.startIndex)
    }

    /**
     * A single `RunCollection`, built (as it always is) on one thread, then published to several
     * reader threads that hammer `findBlockForward`/`findBlockBackward` concurrently. This is the
     * shape the "other frames of the same typesetter may be in use" comment on
     * `ReplacementRun.forFrame` describes: many frames of the same `Typesetter`, on different
     * threads, reading the same `RunCollection` at once. Every reader must see the same, correct
     * answer every time, and nothing should throw - which is only guaranteed because the block
     * list is a plain field computed before publication, with no lazy first-write left to race on.
     */
    @Test
    fun concurrentReadsAfterPublicationAreConsistent() {
        var firstStart = 0
        var secondStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            firstStart = appendBlock()
            append("BBBB")
            secondStart = appendBlock()
            append("CCCC")
        }
        val runs = typesetter.getRuns()
        val end = typesetter.spanned.length

        val readerCount = 8
        val iterationsPerReader = 2_000
        val readyLatch = CountDownLatch(readerCount)
        val startLatch = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val threads = mutableListOf<Thread>()

        repeat(readerCount) {
            val thread = Thread {
                readyLatch.countDown()
                startLatch.await()

                try {
                    repeat(iterationsPerReader) {
                        val forward = runs.findBlockForward(0, end)
                        val backward = runs.findBlockBackward(0, end)

                        if (forward?.startIndex != firstStart || backward?.startIndex != secondStart) {
                            throw AssertionError(
                                "inconsistent read: forward=${forward?.startIndex} " +
                                    "backward=${backward?.startIndex}"
                            )
                        }
                    }
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                }
            }
            threads.add(thread)
            thread.start()
        }

        // Every reader thread is alive and past its own setup before any of them starts hammering
        // the collection, to make the concurrent window as wide as possible.
        readyLatch.await()
        startLatch.countDown()

        for (thread in threads) {
            thread.join(20_000)
        }

        failure.get()?.let { throw it }
        assertNull(firstResult(runs, end))
    }

    /** `findBlockForward`/`findBlockBackward` outside the whole document's range find nothing. */
    private fun firstResult(runs: RunCollection, end: Int) = runs.findBlockForward(end, end)
}
