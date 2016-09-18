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

package com.mta.tehreer.opentype;

public class OpenTypeTag {

	private static boolean isValidByte(byte val) {
		return (val >= 'a' && val <= 'z')
				|| (val >= 'A' && val <= 'Z')
				|| (val == ' ');
	}

	private static boolean isValidChar(char val) {
		return (val <= Byte.MAX_VALUE) && isValidByte((byte) val);
	}

	private static void verifyByte(byte val, String message) {
		if (!isValidByte(val)) {
			throw new IllegalArgumentException(message);
		}
	}

	private static void verifyChar(char val, String message) {
		if (!isValidChar(val)) {
			throw new IllegalArgumentException(message);
		}
	}

    private static int makeNoVerify(byte a, byte b, byte c, byte d) {
        return (a << 24) | (b << 16) | (c << 8) | (d);
    }

	public static int make(String tag) {
		if (tag.length() != 4) {
			throw new IllegalArgumentException("The length of tag is not equal to four characters");
		}
		verifyChar(tag.charAt(0), "Index: 0");
		verifyChar(tag.charAt(1), "Index: 1");
		verifyChar(tag.charAt(2), "Index: 2");
		verifyChar(tag.charAt(3), "Index: 3");

		return makeNoVerify((byte) tag.charAt(0), (byte) tag.charAt(1),
					        (byte) tag.charAt(2), (byte) tag.charAt(3));
	}

	public static int make(byte a, byte b, byte c, byte d) {
		verifyByte(a, "a");
		verifyByte(b, "b");
		verifyByte(c, "c");
		verifyByte(d, "d");

		return makeNoVerify(a, b, c, d);
	}
}
