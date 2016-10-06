/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.text;

/**
 * Specifies the rendering direction of text.
 */
public enum TextDirection {
    /**
     * Text is rendered in default direction of script as determined by the shaping engine.
     */
	DEFAULT,
    /**
     * Text is rendered from left-to-right.
     */
	LEFT_TO_RIGHT,
    /**
     * Text is rendered from right-to-left.
     */
	RIGHT_TO_LEFT,
}
