/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import static org.junit.Assert.assertNotEquals;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.sut.UnsafeSUTBuilder;

import org.junit.Test;

public abstract class BidiParagraphTestSuite extends DisposableTestSuite<BidiParagraph, BidiParagraph.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";

    private static class BidiParagraphBuilder extends UnsafeSUTBuilder<BidiParagraph, BidiParagraph.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        BidiAlgorithm bidiAlgorithm;

        protected BidiParagraphBuilder() {
            super(BidiParagraph.class, BidiParagraph.Finalizable.class);
        }

        @Override
        public BidiParagraph buildSUT() {
            bidiAlgorithm = new BidiAlgorithm(text);

            return bidiAlgorithm.createParagraph(startIndex, endIndex, baseLevel);
        }
    }

    protected String text = DEFAULT_TEXT;
    protected int startIndex = 0;
    protected int endIndex = text.length();
    protected byte baseLevel = 0;

    protected BidiParagraphTestSuite(DefaultMode defaultMode) {
        super(new BidiParagraphBuilder(), defaultMode);

        setOnPreBuildSUT((builder) -> {
            BidiParagraphBuilder sutBuilder = (BidiParagraphBuilder) builder;
            sutBuilder.text = text;
            sutBuilder.startIndex = startIndex;
            sutBuilder.endIndex = endIndex;
            sutBuilder.baseLevel = baseLevel;
        });
    }

    public static class StaticTest extends StaticTestSuite<BidiParagraph, BidiParagraph.Finalizable> {
        public StaticTest() {
            super(new BidiParagraphBuilder());
        }
    }

    public static class DisposableTest extends GeneralTestSuite {
        public DisposableTest() {
            super(DefaultMode.DISPOSABLE);
        }
    }

    public static class FinalizableTest extends GeneralTestSuite {
        public FinalizableTest() {
            super(DefaultMode.SAFE);
        }
    }

    public static abstract class GeneralTestSuite extends BidiParagraphTestSuite {
        protected GeneralTestSuite(DefaultMode defaultMode) {
            super(defaultMode);
        }

        @Test
        public void testNativePointers() {
            buildSUT((sut) -> {
                assertNotEquals(sut.nativeBuffer, 0);
                assertNotEquals(sut.nativeParagraph, 0);
            });
        }
    }
}
