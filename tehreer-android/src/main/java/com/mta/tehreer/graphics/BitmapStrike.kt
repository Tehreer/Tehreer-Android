/*
 * Copyright (C) 2017-2026 Muhammad Tayyab Akram
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
 * A size at which a typeface has the images of its glyphs.
 *
 * @property pixelWidth The number of pixels in the em square, horizontally.
 * @property pixelHeight The number of pixels in the em square, vertically.
 */
data class BitmapStrike(val pixelWidth: Float, val pixelHeight: Float)
