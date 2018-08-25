/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

/**
 * Provides static utility methods related to SFNT tags.
 */
public class SfntTag {
	private static void verifyChar(char val, String message) {
		if (!(val >= ' ' && val <= '~')) {
			throw new IllegalArgumentException(message);
		}
	}

    private static int makeNoVerify(byte a, byte b, byte c, byte d) {
        return (a << 24) | (b << 16) | (c << 8) | (d);
    }

    /**
     * Makes a four-byte integer, representing the passed-in tag as a string.
     *
     * @param tagStr The tag string to represent as an integer.
     * @return Integer representation of the tag.
     *
     * @throws IllegalArgumentException if <code>tagStr</code> is not four characters long, or any
     *         character is not a printing character represented by ASCII values 32-126.
     */
	public static int make(@NonNull @Size(4) String tagStr) {
		if (tagStr.length() != 4) {
			throw new IllegalArgumentException("The length of tag string is not equal to four");
		}
		verifyChar(tagStr.charAt(0), "Index: 0");
		verifyChar(tagStr.charAt(1), "Index: 1");
		verifyChar(tagStr.charAt(2), "Index: 2");
		verifyChar(tagStr.charAt(3), "Index: 3");

		return makeNoVerify((byte) tagStr.charAt(0), (byte) tagStr.charAt(1),
					        (byte) tagStr.charAt(2), (byte) tagStr.charAt(3));
	}

    /**
     * Returns the string representation of a tag.
     *
     * @param tag The tag.
     * @return The string representation of specified tag.
     */
    public static String toString(int tag) {
        char[] chars = {
            (char) (tag >> 24),
            (char) ((tag >> 16) & 0xFF),
            (char) ((tag >> 8) & 0xFF),
            (char) (tag & 0xFF)
        };

        return new String(chars);
    }

    private SfntTag() {
    }
}
