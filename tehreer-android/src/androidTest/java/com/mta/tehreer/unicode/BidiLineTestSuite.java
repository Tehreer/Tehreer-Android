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

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.sut.UnsafeSUTBuilder;

public abstract class BidiLineTestSuite extends DisposableTestSuite<BidiLine, BidiLine.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";

    private static class BidiLineBuilder extends UnsafeSUTBuilder<BidiLine, BidiLine.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        protected BidiLineBuilder() {
            super(BidiLine.class, BidiLine.Finalizable.class);
        }

        @Override
        public BidiLine buildSUT() {
            BidiAlgorithm bidiAlgorithm = BidiAlgorithm.finalizable(new BidiAlgorithm(text));
            BidiParagraph bidiParagraph = BidiParagraph.finalizable(
                    bidiAlgorithm.createParagraph(0, text.length(), baseLevel));

            return bidiParagraph.createLine(startIndex, endIndex);
        }
    }

    protected String text = DEFAULT_TEXT;
    protected int startIndex = 0;
    protected int endIndex = text.length();
    protected byte baseLevel = 0;

    protected BidiLineTestSuite(DisposableTestSuite.DefaultMode defaultMode) {
        super(new BidiLineBuilder(), defaultMode);

        setOnPreBuildSUT((builder) -> {
            BidiLineBuilder sutBuilder = (BidiLineBuilder) builder;
            sutBuilder.text = text;
            sutBuilder.startIndex = startIndex;
            sutBuilder.endIndex = endIndex;
            sutBuilder.baseLevel = baseLevel;
        });
    }

    public static class StaticTest extends DisposableTestSuite.StaticTestSuite<BidiLine, BidiLine.Finalizable> {
        public StaticTest() {
            super(new BidiLineBuilder());
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

    public static abstract class GeneralTestSuite extends BidiLineTestSuite {
        protected GeneralTestSuite(DefaultMode defaultMode) {
            super(defaultMode);
        }
    }
}
