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

package com.mta.tehreer.layout.style

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * The `TypeSizeSpan` class represents a span for specifying absolute type size.
 *
 * @property size The absolute type size in pixels.
 */
class TypeSizeSpan(val size: Float) : MetricAffectingSpan() {
    override fun updateMeasureState(textPaint: TextPaint) {
    }

    override fun updateDrawState(textPaint: TextPaint) {
    }
}
