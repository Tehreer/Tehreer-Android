/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
import com.mta.tehreer.internal.util.ArrayDescription;
import com.mta.tehreer.internal.util.Sustain;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a collection of entries in SFNT 'name' table.
 */
public class SfntNames {

    /**
     * Private use subtag representing unicode platform in a {@link java.util.Locale Locale} object.
     */
    public static final String UNICODE_PLATFORM = "ucd";
    /**
     * Private use subtag representing macintosh platform in a {@link java.util.Locale Locale}
     * object.
     */
    public static final String MACINTOSH_PLATFORM = "mac";
    /**
     * Private use subtag representing iso platform in a {@link java.util.Locale Locale} object.
     */
    public static final String ISO_PLATFORM = "iso";
    /**
     * Private use subtag representing windows platform in a {@link java.util.Locale Locale} object.
     */
    public static final String WINDOWS_PLATFORM = "win";
    /**
     * Private use subtag representing custom platform in a {@link java.util.Locale Locale} object.
     */
    public static final String CUSTOM_PLATFORM = "cst";
    /**
     * Private use subtag representing unrecoginzed platform in a {@link java.util.Locale Locale}
     * object.
     */
    public static final String UNRECOGNIZED_PLATFORM = "unr";

    private static final int TAG_ID_PLATFORM = 0;
    private static final int TAG_ID_LANGUAGE = 1;
    private static final int TAG_ID_REGION = 2;
    private static final int TAG_ID_SCRIPT = 3;
    private static final int TAG_ID_VARIANT = 4;

    private static final int COPYRIGHT = 0;
    private static final int FONT_FAMILY = 1;
    private static final int FONT_SUBFAMILY = 2;
    private static final int UNIQUE_ID = 3;
    private static final int FULL_NAME = 4;
    private static final int VERSION = 5;
    private static final int POST_SCRIPT_NAME = 6;
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
    private static final int POST_SCRIPT_CID_FIND_FONT_NAME = 20;
    private static final int WWS_FAMILY = 21;
    private static final int WWS_SUBFAMILY = 22;
    private static final int LIGHT_BACKGROUND_PALETTE = 23;
    private static final int DARK_BACKGROUND_PALETTE = 24;
    private static final int VARIATIONS_POST_SCRIPT_NAME_PREFIX = 25;

    private final SparseArray<Map<Locale, String>> collection = new SparseArray<>(28);
    private final Typeface typeface;

    @Sustain
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

    @Sustain
    private static String decodeBytes(String encoding, byte[] bytes) {
        String string = null;

        try {
            Charset charset = Charset.forName(encoding);
            string = new String(bytes, charset);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
        }

        return string;
    }

    /**
     * Returns an <code>SfntNames</code> object for the specified typeface.
     *
     * @param typeface The typeface for which to create the <code>SfntNames</code> object.
     * @return A new <code>SfntNames</code> object for the specified typeface.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     */
    public static SfntNames forTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        return new SfntNames(typeface);
    }

    /**
     * Returns a map of matching names by filtering the specified platforms.
     *
     * @param names A map that contains the names to filter.
     * @param platforms A collection of platforms used for matching.
     * @return A new {@link java.util.Map Map&lt;K, V&gt;} object that contains the filtered names.
     *
     * @throws NullPointerException if <code>names</code> or <code>platform</code> is null.
     */
    public static Map<Locale, String> filterPlatforms(Map<Locale, String> names, Collection<String> platforms) {
        if (names == null) {
            throw new NullPointerException("The names map is null");
        }
        if (platforms == null) {
            throw new NullPointerException("The platforms collection is null");
        }

        Map<Locale, String> filter = new LinkedHashMap<>();

        for (Map.Entry<Locale, String> entry : names.entrySet()) {
            if (Build.VERSION.SDK_INT >= 21) {
                Locale locale = entry.getKey();
                String extension = locale.getExtension(Locale.PRIVATE_USE_EXTENSION);

                if (platforms.contains(extension)) {
                    filter.put(entry.getKey(), entry.getValue());
                }
            } else {
                Locale locale = entry.getKey();
                String variant = locale.getVariant();

                for (String p : platforms) {
                    if (variant.endsWith(p)) {
                        filter.put(entry.getKey(), entry.getValue());
                        break;
                    }
                }
            }
        }

        return Collections.unmodifiableMap(filter);
    }

    private SfntNames(Typeface typeface) {
        this.typeface = typeface;
        nativeAddStandardNames(this, typeface);
    }

    @Sustain
    private void addName(int nameId, Locale locale, String string) {
        Map<Locale, String> nameMap = collection.get(nameId);
        if (nameMap == null) {
            nameMap = new LinkedHashMap<>();
            collection.put(nameId, nameMap);
        }

        nameMap.put(locale, string);
    }

    private Map<Locale, String> getNameById(int nameId) {
        Map<Locale, String> map = collection.get(nameId);
        if (map == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(map);
    }

    public Typeface getTypeface() {
        return typeface;
    }

    /**
     * Returns the number of name entries in the SFNT 'name' table.
     *
     * @return The number of name entries in SFNT 'name' table.
     */
    public int getNameCount() {
        return nativeGetNameCount(typeface);
    }

    /**
     * Retrieves an entry of the SFNT 'name' table for a given index.
     *
     * @param index The index of the 'name' entry.
     * @return An entry of the SFNT 'name' table for a given index.
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is negative, or
     *         <code>index</code> is greater than or equal to {@link #getNameCount()}
     */
    public Entry getNameAt(int index) {
        if (index < 0 || index >= getNameCount()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return nativeGetNameAt(typeface, index);
    }

    /**
     * Returns the copyright notice information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent copyright notice information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a copyright notice string.
     */
    public Map<Locale, String> getCopyright() {
        return getNameById(COPYRIGHT);
    }

    /**
     * Returns the font family information retrieved from SFNT 'name' table. Up to four fonts can
     * share the font family name, forming a font style linking group (regular, italic, bold, bold
     * italic).
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent font family information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the font family
     *         name.
     *
     *  @see #getFontSubfamily()
     */
    public Map<Locale, String> getFontFamily() {
        return getNameById(FONT_FAMILY);
    }

    /**
     * Returns the font subfamily information retrieved from SFNT 'name' table. The font subfamily
     * name distinguishes the font in a group with the same font family name.
     * <p>
     * This is assumed to address style (italic, oblique) and weight (light, bold, black, etc.).
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent font subfamily information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the font
     *         subfamily name.
     *
     * @see #getFontFamily()
     */
    public Map<Locale, String> getFontSubfamily() {
        return getNameById(FONT_SUBFAMILY);
    }

    /**
     * Returns the unique id information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent unique id information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string expressing the unique font
     *         identifier.
     */
    public Map<Locale, String> getUniqueId() {
        return getNameById(UNIQUE_ID);
    }

    /**
     * Returns the full name information retrieved from SFNT 'name' table. It is a combination of
     * font family and subfamily, or a similar human-readable variant. If subfamily is "Regular",
     * it is sometimes omitted.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent full name information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string expressing the full font name.
     *
     * @see #getFontFamily()
     * @see #getFontSubfamily()
     */
    public Map<Locale, String> getFullName() {
        return getNameById(FULL_NAME);
    }

    /**
     * Returns the font version information retrieved from SFNT 'name' table. It generally begins
     * with the syntax 'Version &lt;number&gt;.&lt;number&gt;' (upper case, lower case, or mixed,
     * with a space between "Version" and the number).
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent font version information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a version string.
     */
    public Map<Locale, String> getVersion() {
        return getNameById(VERSION);
    }

    /**
     * Returns the PostScript name information retrieved from SFNT 'name' table. It is generally no
     * longer than 63 characters and restricted to the printable ASCII subset, codes 33 - 126,
     * except for the 10 characters '[', ']', '(', ')', '{', '}', '<', '>', '/', '%'.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent PostScript name information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the PostScript
     *         name.
     */
    public Map<Locale, String> getPostScriptName() {
        return getNameById(POST_SCRIPT_NAME);
    }

    /**
     * Returns the trademark notice information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent trademark information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a trademark notice string.
     */
    public Map<Locale, String> getTrademark() {
        return getNameById(TRADEMARK);
    }

    /**
     * Returns the manufacturer information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent manufacturer information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the manufacturer
     *         name.
     */
    public Map<Locale, String> getManufacturer() {
        return getNameById(MANUFACTURER);
    }

    /**
     * Returns the designer information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent designer information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string expressing the designer name.
     */
    public Map<Locale, String> getDesigner() {
        return getNameById(DESIGNER);
    }

    /**
     * Returns the typeface description retrieved from SFNT 'name' table. It may contain revision
     * information, usage recommendations, history, features, etc.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent typeface description. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string expressing the typeface
     *         description.
     */
    public Map<Locale, String> getDescription() {
        return getNameById(DESCRIPTION);
    }

    /**
     * Returns the vendor URL information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent vendor URL information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string that references a vendor URL.
     */
    public Map<Locale, String> getVendorUrl() {
        return getNameById(VENDOR_URL);
    }

    /**
     * Returns the designer URL information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent designer URL information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string that references a designer
     *         URL.
     */
    public Map<Locale, String> getDesignerUrl() {
        return getNameById(DESIGNER_URL);
    }

    /**
     * Returns the license information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent license information. The key is a {@link java.util.Locale Locale} object
     *         that identifies the locale. The value is a string expressing the license information.
     */
    public Map<Locale, String> getLicense() {
        return getNameById(LICENSE);
    }

    /**
     * Returns the license URL information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent license URL information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string that references a license
     *         URL.
     */
    public Map<Locale, String> getLicenseUrl() {
        return getNameById(LICENSE_URL);
    }

    /**
     * Returns the typographic family information retrieved from SFNT 'name' table. The typographic
     * family grouping doesn't impose any constraints on the number of faces within it, in contrast
     * with the 4-style family grouping. If it is absent, then font family is considered to be the
     * typographic family name.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent typographic family information. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string expressing the typographic family name.
     *
     * @see #getFontFamily()
     */
    public Map<Locale, String> getTypographicFamily() {
        return getNameById(TYPOGRAPHIC_FAMILY);
    }

    /**
     * Returns the typographic subfamily information retrieved from SFNT 'name' table. This allows
     * font designers to specify a subfamily name within the typographic family grouping. If it is
     * absent, then font subfamily is considered to be the typographic subfamily name.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent typographic subfamily information. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string expressing the typographic subfamily name.
     *
     * @see #getFontSubfamily()
     */
    public Map<Locale, String> getTypographicSubfamily() {
        return getNameById(TYPOGRAPHIC_SUBFAMILY);
    }

    /**
     * Returns the mac full name information retrieved from SFNT 'name' table. On the Macintosh, the
     * menu name is constructed using the FOND resource. This usually matches the full name.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent mac full name information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the mac
     *         compatible full name.
     *
     * @see #getFullName()
     */
    public Map<Locale, String> getMacFullName() {
        return getNameById(MAC_FULL_NAME);
    }

    /**
     * Returns the sample text information retrieved from SFNT 'name' table. This can be the font
     * name, or any other text that the designer thinks is the best sample to display the font in.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent sample text information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a sample text string.
     */
    public Map<Locale, String> getSampleText() {
        return getNameById(SAMPLE_TEXT);
    }

    /**
     * Returns the PostScript CID findfont name information retrieved from SFNT 'name' table. Its
     * presence in a font means that the PostScript name is meant to be used with the "composefont"
     * invocation in order to invoke the font in a PostScript interpreter.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent PostScript CID findfont name information. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string expressing the PostScript CID findfont name.
     *
     * @see #getPostScriptName()
     */
    public Map<Locale, String> getPostScriptCIDFindFontName() {
        return getNameById(POST_SCRIPT_CID_FIND_FONT_NAME);
    }

    /**
     * Returns the WWS family information retrieved from SFNT 'name' table. It provides a
     * WWS-conformant family name in case the entries for typographic family and subfamily do not
     * conform to the WWS model. (That is, in case the entry for typographic subfamily includes
     * qualifiers for some attribute other than weight, width or slope.)
     * <p>
     * Examples of WWS Family: "Minion Pro Caption" and "Minion Pro Display". (Typographic
     * family would be "Minion Pro" for these examples.)
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent WWS family information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the WWS family
     *         name.
     *
     * @see #getTypographicFamily()
     * @see #getTypographicSubfamily()
     */
    public Map<Locale, String> getWwsFamily() {
        return getNameById(WWS_FAMILY);
    }

    /**
     * Returns the WWS subfamily information retrieved from SFNT 'name' table. Used in conjunction
     * with WWS family, it provides a WWS-conformant subfamily name (reflecting only weight, width
     * and slope attributes) in case the entries for typographic family and subfamily do not conform
     * to the WWS model.
     * <p>
     * Examples of WWS Subfamily: "Semibold Italic", "Bold Condensed". (Typographic subfamily could
     * be "Semibold Italic Caption", or "Bold Condensed Display", for example.)
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent WWS subfamily information. The key is a {@link java.util.Locale Locale}
     *         object that identifies the locale. The value is a string expressing the WWS
     *         subfamily name.
     *
     * @see #getTypographicFamily()
     * @see #getTypographicSubfamily()
     */
    public Map<Locale, String> getWwsSubfamily() {
        return getNameById(WWS_SUBFAMILY);
    }

    /**
     * Returns the light background palette information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent light background palette information. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string that specify the user interface string associated with the palette.
     */
    public Map<Locale, String> getLightBackgroundPalette() {
        return getNameById(LIGHT_BACKGROUND_PALETTE);
    }

    /**
     * Returns the dark background palette information retrieved from SFNT 'name' table.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent dark background palette information. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string that specify the user interface string associated with the palette.
     */
    public Map<Locale, String> getDarkBackgroundPalette() {
        return getNameById(DARK_BACKGROUND_PALETTE);
    }

    /**
     * Returns the variations PostScript name prefix information retrieved from SFNT 'name' table.
     * If present in a variable font, it may be used as the family prefix in the PostScript Name
     * Generation for Variation Fonts algorithm.
     *
     * @return A {@link java.util.Map Map&lt;K, V&gt;} object that contains key/value pairs that
     *         represent variations PostScript name prefix. The key is a
     *         {@link java.util.Locale Locale} object that identifies the locale. The value is a
     *         string expressing the variations PostScript name prefix.
     */
    public Map<Locale, String> getVariationsPostScriptNamePrefix() {
        return getNameById(VARIATIONS_POST_SCRIPT_NAME_PREFIX);
    }

    private static native String nativeGetLocaleTag(int tagId, int platformId, int languageId);
    private static native String nativeGetCharsetName(int platformId, int encodingId);

    private static native void nativeAddStandardNames(SfntNames sfntNames, Typeface typeface);
    private static native int nativeGetNameCount(Typeface typeface);
    private static native Entry nativeGetNameAt(Typeface typeface, int index);

    /**
     * Represents a single entry of SFNT 'name' table.
     */
    public static class Entry {

        /**
         * The name id of this entry.
         */
        public int nameId;
        /**
         * The platform id of this entry.
         */
        public int platformId;
        /**
         * The language id of this entry.
         */
        public int languageId;
        /**
         * The encoding id of this entry.
         */
        public int encodingId;
        /**
         * The encoded bytes of this entry.
         */
        public byte[] bytes;

        @Sustain
        public Entry(int nameId, int platformId, int languageId, int encodingId, byte[] bytes) {
            this.nameId = nameId;
            this.platformId = platformId;
            this.languageId = languageId;
            this.encodingId = encodingId;
            this.bytes = bytes;
        }

        public Entry() {
        }

        /**
         * Generates a relevant locale for this entry by interpreting {@link #platformId} and
         * {@link #languageId}.
         *
         * @return The relevant locale for this entry.
         */
        public Locale locale() {
            String platform = nativeGetLocaleTag(TAG_ID_PLATFORM, platformId, languageId);
            String language = nativeGetLocaleTag(TAG_ID_LANGUAGE, platformId, languageId);
            String region = nativeGetLocaleTag(TAG_ID_REGION, platformId, languageId);
            String script = nativeGetLocaleTag(TAG_ID_SCRIPT, platformId, languageId);
            String variant = nativeGetLocaleTag(TAG_ID_VARIANT, platformId, languageId);

            return createLocale(platform, language, region, script, variant);
        }

        /**
         * Determines a suitable charset for this entry reflecting {@link #platformId} and
         * {@link #encodingId}. If a charset cannot be determined or is unsupported in the current
         * Java virtual machine, then <code>null</code> is returned.
         *
         * @return The suitable charset for this entry, or <code>null</code>.
         */
        public Charset charset() {
            Charset charset = null;

            try {
                String charsetName = nativeGetCharsetName(platformId, encodingId);
                if (charsetName != null) {
                    charset = Charset.forName(charsetName);
                }
            } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
            }

            return charset;
        }

        /**
         * Decodes the {@link #bytes} array into a string using a suitable charset. If no suitable
         * charset is available, then <code>null</code> is returned.
         *
         * @return The decoded string for this entry, or <code>null</code>.
         */
        public String string() {
            Charset charset = charset();
            if (charset != null && bytes != null) {
                return new String(bytes, charset);
            }

            return null;
        }

        @Override
        public String toString() {
            Charset charset = charset();

            return "SfntNames.Entry{nameId=" + nameId
                    + ", platformId=" + platformId
                    + ", languageId=" + languageId
                    + ", encodingId=" + encodingId
                    + ", bytes=" + ArrayDescription.forByteArray(bytes)
                    + ", locale=" + locale().toString()
                    + ", charset=" + (charset != null ? charset.name() : null)
                    + ", string=" + string()
                    + "}";
        }
    }
}
