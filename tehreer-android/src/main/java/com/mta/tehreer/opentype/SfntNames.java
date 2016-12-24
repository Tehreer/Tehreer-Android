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

import android.os.Build;
import android.util.SparseArray;

import com.mta.tehreer.graphics.Typeface;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SfntNames {

    private static final int COPYRIGHT = 0;
    private static final int FONT_FAMILY = 1;
    private static final int FONT_SUBFAMILY = 2;
    private static final int UNIQUE_ID = 3;
    private static final int FULL_NAME = 4;
    private static final int VERSION = 5;
    private static final int POSTSCRIPT_NAME = 6;
    private static final int TRADEMARK = 7;
    private static final int MANUFACTURER = 8;
    private static final int DESIGNER = 9;
    private static final int DESCRIPTION = 10;
    private static final int VENDOR_URL = 11;
    private static final int DESIGNER_URL = 12;
    private static final int LICENSE = 13;
    private static final int LICENSE_URL = 14;
    private static final int RESERVED = 15;
    private static final int TYPOGRAPHIC_FAMILY = 16;
    private static final int TYPOGRAPHIC_SUBFAMILY = 17;
    private static final int MAC_FULL_NAME = 18;
    private static final int SAMPLE_TEXT = 19;
    private static final int POSTSCRIPT_CID_FIND_FONT_NAME = 20;
    private static final int WWS_FAMILY = 21;
    private static final int WWS_SUBFAMILY = 22;
    private static final int LIGHT_BACKGROUND_PALETTE = 23;
    private static final int DARK_BACKGROUND_PALETTE = 24;
    private static final int VARIATIONS_POSTSCRIPT_NAME_PREFIX = 25;

    private final SparseArray<Map<Locale, String>> standardNames = new SparseArray<>(25);
    private final Typeface typeface;

    private static Locale createLocale(String platform, String language, String region, String script, String variant) {
        if (Build.VERSION.SDK_INT >= 21) {
            Locale.Builder builder = new Locale.Builder();
            builder.setLanguage(language);
            builder.setRegion(region);
            builder.setScript(script);
            builder.setVariant(variant);
            builder.setExtension(Locale.PRIVATE_USE_EXTENSION, platform);

            return builder.build();
        }

        String secret = "";
        if (script != null) {
            secret += script;
        }
        if (variant != null) {
            if (secret.length() > 0) {
                secret += '-';
            }
            secret += variant;
        }
        if (secret.length() > 0) {
            secret += '-';
        }
        secret += "x-" + platform;

        return new Locale(language, region, secret);
    }

    private static String decodeBytes(String encoding, byte[] bytes) {
        String string = null;

        try {
            Charset charset = Charset.forName(encoding);
            string = new String(bytes, charset);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
        }

        return string;
    }

    public static SfntNames forTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        return new SfntNames(typeface);
    }

    private SfntNames(Typeface typeface) {
        this.typeface = typeface;
        nativeAddStandardNames(this, typeface);
    }

    private void addName(int nameId, Locale relevantLocale, String decodedString) {
        Map<Locale, String> nameMap = standardNames.get(nameId);
        if (nameMap == null) {
            nameMap = new HashMap<>();
            standardNames.put(nameId, nameMap);
        }

        nameMap.put(relevantLocale, decodedString);
    }

    private Map<Locale, String> getNameById(int nameId) {
        Map<Locale, String> map = standardNames.get(nameId);
        if (map == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(map);
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public int getNameCount() {
        return nativeGetNameCount(typeface);
    }

    public NameEntry getNameAt(int index) {
        if (index < 0 || index >= getNameCount()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return nativeGetNameAt(typeface, index);
    }

    public Map<Locale, String> getCopyright() {
        return getNameById(COPYRIGHT);
    }

    public Map<Locale, String> getFontFamily() {
        return getNameById(FONT_FAMILY);
    }

    public Map<Locale, String> getFontSubfamily() {
        return getNameById(FONT_SUBFAMILY);
    }

    public Map<Locale, String> getUniqueId() {
        return getNameById(UNIQUE_ID);
    }

    public Map<Locale, String> getFullName() {
        return getNameById(FULL_NAME);
    }

    public Map<Locale, String> getVersion() {
        return getNameById(VERSION);
    }

    public Map<Locale, String> getPostscriptName() {
        return getNameById(POSTSCRIPT_NAME);
    }

    public Map<Locale, String> getTrademark() {
        return getNameById(TRADEMARK);
    }

    public Map<Locale, String> getManufacturer() {
        return getNameById(MANUFACTURER);
    }

    public Map<Locale, String> getDesigner() {
        return getNameById(DESIGNER);
    }

    public Map<Locale, String> getDescription() {
        return getNameById(DESCRIPTION);
    }

    public Map<Locale, String> getVendorUrl() {
        return getNameById(VENDOR_URL);
    }

    public Map<Locale, String> getDesignerUrl() {
        return getNameById(DESIGNER_URL);
    }

    public Map<Locale, String> getLicense() {
        return getNameById(LICENSE);
    }

    public Map<Locale, String> getLicenseUrl() {
        return getNameById(LICENSE_URL);
    }

    public Map<Locale, String> getTypographicFamily() {
        return getNameById(TYPOGRAPHIC_FAMILY);
    }

    public Map<Locale, String> getTypographicSubfamily() {
        return getNameById(TYPOGRAPHIC_SUBFAMILY);
    }

    public Map<Locale, String> getMacFullName() {
        return getNameById(MAC_FULL_NAME);
    }

    public Map<Locale, String> getSampleText() {
        return getNameById(SAMPLE_TEXT);
    }

    public Map<Locale, String> getPostscriptCIDFindFontName() {
        return getNameById(POSTSCRIPT_CID_FIND_FONT_NAME);
    }

    public Map<Locale, String> getWwsFamily() {
        return getNameById(WWS_FAMILY);
    }

    public Map<Locale, String> getWwsSubfamily() {
        return getNameById(WWS_SUBFAMILY);
    }

    public Map<Locale, String> getLightBackgroundPalette() {
        return getNameById(LIGHT_BACKGROUND_PALETTE);
    }

    public Map<Locale, String> getDarkBackgroundPalette() {
        return getNameById(DARK_BACKGROUND_PALETTE);
    }

    public Map<Locale, String> getVariationsPostscriptNamePrefix() {
        return getNameById(VARIATIONS_POSTSCRIPT_NAME_PREFIX);
    }

    private static native void nativeAddStandardNames(SfntNames sfntNames, Typeface typeface);

    private static native int nativeGetNameCount(Typeface typeface);
    private static native NameEntry nativeGetNameAt(Typeface typeface, int index);
}
