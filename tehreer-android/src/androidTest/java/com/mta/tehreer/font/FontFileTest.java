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

package com.mta.tehreer.font;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;

import com.mta.tehreer.graphics.TypeSlope;
import com.mta.tehreer.graphics.TypeWeight;
import com.mta.tehreer.graphics.TypeWidth;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.SfntTag;
import com.mta.tehreer.util.FontFileStore;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FontFileTest {
    @Test
    public void testWithSudoFont() {
        FontFile sudo = FontFileStore.getSudo();
        List<Typeface> typefaces = sudo.getTypefaces();

        List<VariationAxis> variationAxes = Arrays.asList(
            VariationAxis.of(SfntTag.make("ital"), "Italic", 0, 0.0f, 0.0f, 1.0f),
            VariationAxis.of(SfntTag.make("wght"), "Weight", 0, 400.0f, 200.0f, 700.0f)
        );
        List<NamedStyle> namedStyles = Arrays.asList(
            NamedStyle.of("Regular", new float[] { 0.0f, 400.0f }, null),
            NamedStyle.of("Thin", new float[] { 0.0f, 200.0f }, null),
            NamedStyle.of("Light", new float[] { 0.0f, 309.375f }, null),
            NamedStyle.of("Regular", new float[] { 0.0f, 387.5f }, null),
            NamedStyle.of("Medium", new float[] { 0.0f, 543.75f }, null),
            NamedStyle.of("Bold", new float[] { 0.0f, 700.0f }, null),
            NamedStyle.of("Thin Italic", new float[] { 1.0f, 200.0f }, null),
            NamedStyle.of("Light Italic", new float[] { 1.0f, 309.375f }, null),
            NamedStyle.of("Regular Italic", new float[] { 1.0f, 387.5f }, null),
            NamedStyle.of("Medium Italic", new float[] { 1.0f, 543.75f }, null),
            NamedStyle.of("Bold Italic", new float[] { 1.0f, 700.0f }, null)
        );

        assertNotNull(typefaces);
        assertEquals(typefaces.size(), 11);

        Typeface regular = typefaces.get(0);
        assertTrue(regular.isVariable());
        assertEquals(regular.getVariationAxes(), variationAxes);
        assertEquals(regular.getNamedStyles(), namedStyles);
        assertArrayEquals(regular.getVariationCoordinates(), new float[] { 0.0f, 400.0f }, 0.0f);
        assertNull(regular.getPaletteEntryNames());
        assertNull(regular.getPredefinedPalettes());
        assertNull(regular.getAssociatedColors());
        assertEquals(regular.getFamilyName(), "Sudo");
        assertEquals(regular.getStyleName(), "Regular");
        assertEquals(regular.getFullName(), "Sudo Regular");
        assertEquals(regular.getWeight(), TypeWeight.REGULAR);
        assertEquals(regular.getWidth(), TypeWidth.NORMAL);
        assertEquals(regular.getSlope(), TypeSlope.PLAIN);
        assertEquals(regular.getUnitsPerEm(), 1024);
        assertEquals(regular.getAscent(), 832);
        assertEquals(regular.getDescent(), 192);
        assertEquals(regular.getLeading(), 0);
        assertEquals(regular.getGlyphCount(), 1077);
        assertEquals(regular.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(regular.getUnderlinePosition(), -160);
        assertEquals(regular.getUnderlineThickness(), 64);
        assertEquals(regular.getStrikeoutPosition(), 268);
        assertEquals(regular.getStrikeoutThickness(), 64);

        Typeface thin = typefaces.get(1);
        assertTrue(thin.isVariable());
        assertEquals(thin.getVariationAxes(), variationAxes);
        assertEquals(thin.getNamedStyles(), namedStyles);
        assertArrayEquals(thin.getVariationCoordinates(), new float[] { 0.0f, 200.0f }, 0.0f);
        assertNull(thin.getPaletteEntryNames());
        assertNull(thin.getPredefinedPalettes());
        assertNull(thin.getAssociatedColors());
        assertEquals(thin.getFamilyName(), "Sudo");
        assertEquals(thin.getStyleName(), "Thin");
        assertEquals(thin.getFullName(), "Sudo Thin");
        assertEquals(thin.getWeight(), TypeWeight.EXTRA_LIGHT);
        assertEquals(thin.getWidth(), TypeWidth.NORMAL);
        assertEquals(thin.getSlope(), TypeSlope.PLAIN);
        assertEquals(thin.getUnitsPerEm(), 1024);
        assertEquals(thin.getAscent(), 832);
        assertEquals(thin.getDescent(), 192);
        assertEquals(thin.getLeading(), 0);
        assertEquals(thin.getGlyphCount(), 1077);
        assertEquals(thin.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(thin.getUnderlinePosition(), -160);
        assertEquals(thin.getUnderlineThickness(), 64);
        assertEquals(thin.getStrikeoutPosition(), 268);
        assertEquals(thin.getStrikeoutThickness(), 64);

        Typeface light = typefaces.get(2);
        assertTrue(light.isVariable());
        assertEquals(light.getVariationAxes(), variationAxes);
        assertEquals(light.getNamedStyles(), namedStyles);
        assertArrayEquals(light.getVariationCoordinates(), new float[] { 0.0f, 309.375f }, 0.0f);
        assertNull(light.getPaletteEntryNames());
        assertNull(light.getPredefinedPalettes());
        assertNull(light.getAssociatedColors());
        assertEquals(light.getFamilyName(), "Sudo");
        assertEquals(light.getStyleName(), "Light");
        assertEquals(light.getFullName(), "Sudo Light");
        assertEquals(light.getWeight(), TypeWeight.LIGHT);
        assertEquals(light.getWidth(), TypeWidth.NORMAL);
        assertEquals(light.getSlope(), TypeSlope.PLAIN);
        assertEquals(light.getUnitsPerEm(), 1024);
        assertEquals(light.getAscent(), 832);
        assertEquals(light.getDescent(), 192);
        assertEquals(light.getLeading(), 0);
        assertEquals(light.getGlyphCount(), 1077);
        assertEquals(light.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(light.getUnderlinePosition(), -160);
        assertEquals(light.getUnderlineThickness(), 64);
        assertEquals(light.getStrikeoutPosition(), 268);
        assertEquals(light.getStrikeoutThickness(), 64);

        Typeface regularVariant = typefaces.get(3);
        assertTrue(regularVariant.isVariable());
        assertEquals(regularVariant.getVariationAxes(), variationAxes);
        assertEquals(regularVariant.getNamedStyles(), namedStyles);
        assertArrayEquals(regularVariant.getVariationCoordinates(), new float[] { 0.0f, 387.5f }, 0.0f);
        assertNull(regularVariant.getPaletteEntryNames());
        assertNull(regularVariant.getPredefinedPalettes());
        assertNull(regularVariant.getAssociatedColors());
        assertEquals(regularVariant.getFamilyName(), "Sudo");
        assertEquals(regularVariant.getStyleName(), "Regular");
        assertEquals(regularVariant.getFullName(), "Sudo Regular");
        assertEquals(regularVariant.getWeight(), TypeWeight.REGULAR);
        assertEquals(regularVariant.getWidth(), TypeWidth.NORMAL);
        assertEquals(regularVariant.getSlope(), TypeSlope.PLAIN);
        assertEquals(regularVariant.getUnitsPerEm(), 1024);
        assertEquals(regularVariant.getAscent(), 832);
        assertEquals(regularVariant.getDescent(), 192);
        assertEquals(regularVariant.getLeading(), 0);
        assertEquals(regularVariant.getGlyphCount(), 1077);
        assertEquals(regularVariant.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(regularVariant.getUnderlinePosition(), -160);
        assertEquals(regularVariant.getUnderlineThickness(), 64);
        assertEquals(regularVariant.getStrikeoutPosition(), 268);
        assertEquals(regularVariant.getStrikeoutThickness(), 64);

        Typeface medium = typefaces.get(4);
        assertTrue(medium.isVariable());
        assertEquals(medium.getVariationAxes(), variationAxes);
        assertEquals(medium.getNamedStyles(), namedStyles);
        assertArrayEquals(medium.getVariationCoordinates(), new float[] { 0.0f, 543.75f }, 0.0f);
        assertNull(medium.getPaletteEntryNames());
        assertNull(medium.getPredefinedPalettes());
        assertNull(medium.getAssociatedColors());
        assertEquals(medium.getFamilyName(), "Sudo");
        assertEquals(medium.getStyleName(), "Medium");
        assertEquals(medium.getFullName(), "Sudo Medium");
        assertEquals(medium.getWeight(), TypeWeight.MEDIUM);
        assertEquals(medium.getWidth(), TypeWidth.NORMAL);
        assertEquals(medium.getSlope(), TypeSlope.PLAIN);
        assertEquals(medium.getUnitsPerEm(), 1024);
        assertEquals(medium.getAscent(), 832);
        assertEquals(medium.getDescent(), 192);
        assertEquals(medium.getLeading(), 0);
        assertEquals(medium.getGlyphCount(), 1077);
        assertEquals(medium.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(medium.getUnderlinePosition(), -160);
        assertEquals(medium.getUnderlineThickness(), 64);
        assertEquals(medium.getStrikeoutPosition(), 268);
        assertEquals(medium.getStrikeoutThickness(), 64);

        Typeface bold = typefaces.get(5);
        assertTrue(bold.isVariable());
        assertEquals(bold.getVariationAxes(), variationAxes);
        assertEquals(bold.getNamedStyles(), namedStyles);
        assertArrayEquals(bold.getVariationCoordinates(), new float[] { 0.0f, 700.0f }, 0.0f);
        assertNull(bold.getPaletteEntryNames());
        assertNull(bold.getPredefinedPalettes());
        assertNull(bold.getAssociatedColors());
        assertEquals(bold.getFamilyName(), "Sudo");
        assertEquals(bold.getStyleName(), "Bold");
        assertEquals(bold.getFullName(), "Sudo Bold");
        assertEquals(bold.getWeight(), TypeWeight.BOLD);
        assertEquals(bold.getWidth(), TypeWidth.NORMAL);
        assertEquals(bold.getSlope(), TypeSlope.PLAIN);
        assertEquals(bold.getUnitsPerEm(), 1024);
        assertEquals(bold.getAscent(), 832);
        assertEquals(bold.getDescent(), 192);
        assertEquals(bold.getLeading(), 0);
        assertEquals(bold.getGlyphCount(), 1077);
        assertEquals(bold.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(bold.getUnderlinePosition(), -160);
        assertEquals(bold.getUnderlineThickness(), 64);
        assertEquals(bold.getStrikeoutPosition(), 268);
        assertEquals(bold.getStrikeoutThickness(), 64);

        Typeface thinItalic = typefaces.get(6);
        assertTrue(thinItalic.isVariable());
        assertEquals(thinItalic.getVariationAxes(), variationAxes);
        assertEquals(thinItalic.getNamedStyles(), namedStyles);
        assertArrayEquals(thinItalic.getVariationCoordinates(), new float[] { 1.0f, 200.0f }, 0.0f);
        assertNull(thinItalic.getPaletteEntryNames());
        assertNull(thinItalic.getPredefinedPalettes());
        assertNull(thinItalic.getAssociatedColors());
        assertEquals(thinItalic.getFamilyName(), "Sudo");
        assertEquals(thinItalic.getStyleName(), "Thin Italic");
        assertEquals(thinItalic.getFullName(), "Sudo Thin Italic");
        assertEquals(thinItalic.getWeight(), TypeWeight.EXTRA_LIGHT);
        assertEquals(thinItalic.getWidth(), TypeWidth.NORMAL);
        assertEquals(thinItalic.getSlope(), TypeSlope.ITALIC);
        assertEquals(thinItalic.getUnitsPerEm(), 1024);
        assertEquals(thinItalic.getAscent(), 832);
        assertEquals(thinItalic.getDescent(), 192);
        assertEquals(thinItalic.getLeading(), 0);
        assertEquals(thinItalic.getGlyphCount(), 1077);
        assertEquals(thinItalic.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(thinItalic.getUnderlinePosition(), -160);
        assertEquals(thinItalic.getUnderlineThickness(), 64);
        assertEquals(thinItalic.getStrikeoutPosition(), 268);
        assertEquals(thinItalic.getStrikeoutThickness(), 64);

        Typeface lightItalic = typefaces.get(7);
        assertTrue(lightItalic.isVariable());
        assertEquals(lightItalic.getVariationAxes(), variationAxes);
        assertEquals(lightItalic.getNamedStyles(), namedStyles);
        assertArrayEquals(lightItalic.getVariationCoordinates(), new float[] { 1.0f, 309.375f }, 0.0f);
        assertNull(lightItalic.getPaletteEntryNames());
        assertNull(lightItalic.getPredefinedPalettes());
        assertNull(lightItalic.getAssociatedColors());
        assertEquals(lightItalic.getFamilyName(), "Sudo");
        assertEquals(lightItalic.getStyleName(), "Light Italic");
        assertEquals(lightItalic.getFullName(), "Sudo Light Italic");
        assertEquals(lightItalic.getWeight(), TypeWeight.LIGHT);
        assertEquals(lightItalic.getWidth(), TypeWidth.NORMAL);
        assertEquals(lightItalic.getSlope(), TypeSlope.ITALIC);
        assertEquals(lightItalic.getUnitsPerEm(), 1024);
        assertEquals(lightItalic.getAscent(), 832);
        assertEquals(lightItalic.getDescent(), 192);
        assertEquals(lightItalic.getLeading(), 0);
        assertEquals(lightItalic.getGlyphCount(), 1077);
        assertEquals(lightItalic.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(lightItalic.getUnderlinePosition(), -160);
        assertEquals(lightItalic.getUnderlineThickness(), 64);
        assertEquals(lightItalic.getStrikeoutPosition(), 268);
        assertEquals(lightItalic.getStrikeoutThickness(), 64);

        Typeface regularItalic = typefaces.get(8);
        assertTrue(regularItalic.isVariable());
        assertEquals(regularItalic.getVariationAxes(), variationAxes);
        assertEquals(regularItalic.getNamedStyles(), namedStyles);
        assertArrayEquals(regularItalic.getVariationCoordinates(), new float[] { 1.0f, 387.5f }, 0.0f);
        assertNull(regularItalic.getPaletteEntryNames());
        assertNull(regularItalic.getPredefinedPalettes());
        assertNull(regularItalic.getAssociatedColors());
        assertEquals(regularItalic.getFamilyName(), "Sudo");
        assertEquals(regularItalic.getStyleName(), "Regular Italic");
        assertEquals(regularItalic.getFullName(), "Sudo Regular Italic");
        assertEquals(regularItalic.getWeight(), TypeWeight.REGULAR);
        assertEquals(regularItalic.getWidth(), TypeWidth.NORMAL);
        assertEquals(regularItalic.getSlope(), TypeSlope.ITALIC);
        assertEquals(regularItalic.getUnitsPerEm(), 1024);
        assertEquals(regularItalic.getAscent(), 832);
        assertEquals(regularItalic.getDescent(), 192);
        assertEquals(regularItalic.getLeading(), 0);
        assertEquals(regularItalic.getGlyphCount(), 1077);
        assertEquals(regularItalic.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(regularItalic.getUnderlinePosition(), -160);
        assertEquals(regularItalic.getUnderlineThickness(), 64);
        assertEquals(regularItalic.getStrikeoutPosition(), 268);
        assertEquals(regularItalic.getStrikeoutThickness(), 64);

        Typeface mediumItalic = typefaces.get(9);
        assertTrue(mediumItalic.isVariable());
        assertEquals(mediumItalic.getVariationAxes(), variationAxes);
        assertEquals(mediumItalic.getNamedStyles(), namedStyles);
        assertArrayEquals(mediumItalic.getVariationCoordinates(), new float[] { 1.0f, 543.75f }, 0.0f);
        assertNull(mediumItalic.getPaletteEntryNames());
        assertNull(mediumItalic.getPredefinedPalettes());
        assertNull(mediumItalic.getAssociatedColors());
        assertEquals(mediumItalic.getFamilyName(), "Sudo");
        assertEquals(mediumItalic.getStyleName(), "Medium Italic");
        assertEquals(mediumItalic.getFullName(), "Sudo Medium Italic");
        assertEquals(mediumItalic.getWeight(), TypeWeight.MEDIUM);
        assertEquals(mediumItalic.getWidth(), TypeWidth.NORMAL);
        assertEquals(mediumItalic.getSlope(), TypeSlope.ITALIC);
        assertEquals(mediumItalic.getUnitsPerEm(), 1024);
        assertEquals(mediumItalic.getAscent(), 832);
        assertEquals(mediumItalic.getDescent(), 192);
        assertEquals(mediumItalic.getLeading(), 0);
        assertEquals(mediumItalic.getGlyphCount(), 1077);
        assertEquals(mediumItalic.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(mediumItalic.getUnderlinePosition(), -160);
        assertEquals(mediumItalic.getUnderlineThickness(), 64);
        assertEquals(mediumItalic.getStrikeoutPosition(), 268);
        assertEquals(mediumItalic.getStrikeoutThickness(), 64);

        Typeface bolcItalic = typefaces.get(10);
        assertTrue(bolcItalic.isVariable());
        assertEquals(bolcItalic.getVariationAxes(), variationAxes);
        assertEquals(bolcItalic.getNamedStyles(), namedStyles);
        assertArrayEquals(bolcItalic.getVariationCoordinates(), new float[] { 1.0f, 700.0f }, 0.0f);
        assertNull(bolcItalic.getPaletteEntryNames());
        assertNull(bolcItalic.getPredefinedPalettes());
        assertNull(bolcItalic.getAssociatedColors());
        assertEquals(bolcItalic.getFamilyName(), "Sudo");
        assertEquals(bolcItalic.getStyleName(), "Bold Italic");
        assertEquals(bolcItalic.getFullName(), "Sudo Bold Italic");
        assertEquals(bolcItalic.getWeight(), TypeWeight.BOLD);
        assertEquals(bolcItalic.getWidth(), TypeWidth.NORMAL);
        assertEquals(bolcItalic.getSlope(), TypeSlope.ITALIC);
        assertEquals(bolcItalic.getUnitsPerEm(), 1024);
        assertEquals(bolcItalic.getAscent(), 832);
        assertEquals(bolcItalic.getDescent(), 192);
        assertEquals(bolcItalic.getLeading(), 0);
        assertEquals(bolcItalic.getGlyphCount(), 1077);
        assertEquals(bolcItalic.getBoundingBox(), new Rect(-458, -209, 640, 960));
        assertEquals(bolcItalic.getUnderlinePosition(), -160);
        assertEquals(bolcItalic.getUnderlineThickness(), 64);
        assertEquals(bolcItalic.getStrikeoutPosition(), 268);
        assertEquals(bolcItalic.getStrikeoutThickness(), 64);
    }

    @Test
    public void testWithRocherColorFont() {
        FontFile rocherColor = FontFileStore.getRocherColor();
        List<Typeface> typefaces = rocherColor.getTypefaces();

        List<VariationAxis> variationAxes = Arrays.asList(
            VariationAxis.of(SfntTag.make("BVEL"), "Bevel", 0, 100.0f, 0.0f, 100.0f),
            VariationAxis.of(SfntTag.make("SHDW"), "Shadow", 0, 100.0f, 0.0f, 100.0f)
        );
        List<NamedStyle> namedStyles = Arrays.asList(
            NamedStyle.of("Regular", new float[] { 100.0f, 100.0f }, null),
            NamedStyle.of("Shadow", new float[] { 100.0f, 50.0f }, null),
            NamedStyle.of("Extrude", new float[] { 10.0f, 100.0f }, null),
            NamedStyle.of("Bevel", new float[] { 100.0f, 0.0f }, null),
            NamedStyle.of("Outline", new float[] { 0.0f, 0.0f }, null)
        );
        List<String> paletteEntryNames = Arrays.asList("", "", "", "");
        List<ColorPalette> predefinedPalettes = Arrays.asList(
            ColorPalette.of("", 0, new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 }),
            ColorPalette.of("", 0, new int[] { 0xFF513898, 0xFFF9869B, 0xFFE86D83, 0xFFFC9EAF }),
            ColorPalette.of("", 0, new int[] { 0xFF0E7851, 0xFF74E977, 0xFF4EDB6C, 0xFF91F787 }),
            ColorPalette.of("", 0, new int[] { 0xFFCD5646, 0xFFE2F9D9, 0xFFB5EADA, 0xFFE2F9D9 }),
            ColorPalette.of("", 0, new int[] { 0xFF386398, 0xFF87EFF9, 0xFF53C2F9, 0xFFB1F6FD }),
            ColorPalette.of("", 0, new int[] { 0xFF527078, 0xFFF2E667, 0xFFB5B47D, 0xFFFFF699 }),
            ColorPalette.of("", 0, new int[] { 0xFF6A4884, 0xFFA297F7, 0xFF8688F5, 0xFFC1AFF7 }),
            ColorPalette.of("", 0, new int[] { 0xFF5B559D, 0xFF66E6D2, 0xFF6BC4C7, 0xFF84F1E0 }),
            ColorPalette.of("", 0, new int[] { 0xFF996805, 0xFFF4D13E, 0xFFD7A619, 0xFFFBDB53 }),
            ColorPalette.of("", 0, new int[] { 0xFF6F8393, 0xFFCFD4DB, 0xFFAAB6C1, 0xFFDDE1E7 }),
            ColorPalette.of("", 0, new int[] { 0xFF6300BE, 0xFF28ACFF, 0xFF5EE8FF, 0xFF67FCC1 })
        );

        assertNotNull(typefaces);
        assertEquals(typefaces.size(), 5);

        Typeface regular = typefaces.get(0);
        assertTrue(regular.isVariable());
        assertEquals(regular.getVariationAxes(), variationAxes);
        assertEquals(regular.getNamedStyles(), namedStyles);
        assertArrayEquals(regular.getVariationCoordinates(), new float[] { 100.0f, 100.0f }, 0.0f);
        assertEquals(regular.getPaletteEntryNames(), paletteEntryNames);
        assertEquals(regular.getPredefinedPalettes(), predefinedPalettes);
        assertArrayEquals(regular.getAssociatedColors(), new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        assertEquals(regular.getFamilyName(), "Rocher Color");
        assertEquals(regular.getStyleName(), "Regular");
        assertEquals(regular.getFullName(), "Rocher Color Regular");
        assertEquals(regular.getWeight(), TypeWeight.REGULAR);
        assertEquals(regular.getWidth(), TypeWidth.NORMAL);
        assertEquals(regular.getSlope(), TypeSlope.PLAIN);
        assertEquals(regular.getUnitsPerEm(), 2000);
        assertEquals(regular.getAscent(), 1960);
        assertEquals(regular.getDescent(), 590);
        assertEquals(regular.getLeading(), 0);
        assertEquals(regular.getGlyphCount(), 2105);
        assertEquals(regular.getBoundingBox(), new Rect(-1664, -629, 2988, 2044));
        assertEquals(regular.getUnderlinePosition(), -100);
        assertEquals(regular.getUnderlineThickness(), 50);
        assertEquals(regular.getStrikeoutPosition(), 696);
        assertEquals(regular.getStrikeoutThickness(), 50);

        Typeface shadow = typefaces.get(1);
        assertTrue(shadow.isVariable());
        assertEquals(shadow.getVariationAxes(), variationAxes);
        assertEquals(shadow.getNamedStyles(), namedStyles);
        assertArrayEquals(shadow.getVariationCoordinates(), new float[] { 100.0f, 50.0f }, 0.0f);
        assertEquals(shadow.getPaletteEntryNames(), paletteEntryNames);
        assertEquals(shadow.getPredefinedPalettes(), predefinedPalettes);
        assertArrayEquals(shadow.getAssociatedColors(), new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        assertEquals(shadow.getFamilyName(), "Rocher Color");
        assertEquals(shadow.getStyleName(), "Shadow");
        assertEquals(shadow.getFullName(), "Rocher Color Shadow");
        assertEquals(shadow.getWeight(), TypeWeight.REGULAR);
        assertEquals(shadow.getWidth(), TypeWidth.NORMAL);
        assertEquals(shadow.getSlope(), TypeSlope.PLAIN);
        assertEquals(shadow.getUnitsPerEm(), 2000);
        assertEquals(shadow.getAscent(), 1960);
        assertEquals(shadow.getDescent(), 590);
        assertEquals(shadow.getLeading(), 0);
        assertEquals(shadow.getGlyphCount(), 2105);
        assertEquals(shadow.getBoundingBox(), new Rect(-1664, -629, 2988, 2044));
        assertEquals(shadow.getUnderlinePosition(), -100);
        assertEquals(shadow.getUnderlineThickness(), 50);
        assertEquals(shadow.getStrikeoutPosition(), 696);
        assertEquals(shadow.getStrikeoutThickness(), 50);

        Typeface extrude = typefaces.get(2);
        assertTrue(extrude.isVariable());
        assertEquals(extrude.getVariationAxes(), variationAxes);
        assertEquals(extrude.getNamedStyles(), namedStyles);
        assertArrayEquals(extrude.getVariationCoordinates(), new float[] { 10.0f, 100.0f }, 0.0f);
        assertEquals(extrude.getPaletteEntryNames(), paletteEntryNames);
        assertEquals(extrude.getPredefinedPalettes(), predefinedPalettes);
        assertArrayEquals(extrude.getAssociatedColors(), new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        assertEquals(extrude.getFamilyName(), "Rocher Color");
        assertEquals(extrude.getStyleName(), "Extrude");
        assertEquals(extrude.getFullName(), "Rocher Color Extrude");
        assertEquals(extrude.getWeight(), TypeWeight.REGULAR);
        assertEquals(extrude.getWidth(), TypeWidth.NORMAL);
        assertEquals(extrude.getSlope(), TypeSlope.PLAIN);
        assertEquals(extrude.getUnitsPerEm(), 2000);
        assertEquals(extrude.getAscent(), 1960);
        assertEquals(extrude.getDescent(), 590);
        assertEquals(extrude.getLeading(), 0);
        assertEquals(extrude.getGlyphCount(), 2105);
        assertEquals(extrude.getBoundingBox(), new Rect(-1664, -629, 2988, 2044));
        assertEquals(extrude.getUnderlinePosition(), -100);
        assertEquals(extrude.getUnderlineThickness(), 50);
        assertEquals(extrude.getStrikeoutPosition(), 696);
        assertEquals(extrude.getStrikeoutThickness(), 50);

        Typeface bevel = typefaces.get(3);
        assertTrue(bevel.isVariable());
        assertEquals(bevel.getVariationAxes(), variationAxes);
        assertEquals(bevel.getNamedStyles(), namedStyles);
        assertArrayEquals(bevel.getVariationCoordinates(), new float[] { 100.0f, 0.0f }, 0.0f);
        assertEquals(bevel.getPaletteEntryNames(), paletteEntryNames);
        assertEquals(bevel.getPredefinedPalettes(), predefinedPalettes);
        assertArrayEquals(bevel.getAssociatedColors(), new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        assertEquals(bevel.getFamilyName(), "Rocher Color");
        assertEquals(bevel.getStyleName(), "Bevel");
        assertEquals(bevel.getFullName(), "Rocher Color Bevel");
        assertEquals(bevel.getWeight(), TypeWeight.REGULAR);
        assertEquals(bevel.getWidth(), TypeWidth.NORMAL);
        assertEquals(bevel.getSlope(), TypeSlope.PLAIN);
        assertEquals(bevel.getUnitsPerEm(), 2000);
        assertEquals(bevel.getAscent(), 1960);
        assertEquals(bevel.getDescent(), 590);
        assertEquals(bevel.getLeading(), 0);
        assertEquals(bevel.getGlyphCount(), 2105);
        assertEquals(bevel.getBoundingBox(), new Rect(-1664, -629, 2988, 2044));
        assertEquals(bevel.getUnderlinePosition(), -100);
        assertEquals(bevel.getUnderlineThickness(), 50);
        assertEquals(bevel.getStrikeoutPosition(), 696);
        assertEquals(bevel.getStrikeoutThickness(), 50);

        Typeface outline = typefaces.get(4);
        assertTrue(outline.isVariable());
        assertEquals(outline.getVariationAxes(), variationAxes);
        assertEquals(outline.getNamedStyles(), namedStyles);
        assertArrayEquals(outline.getVariationCoordinates(), new float[] { 0.0f, 0.0f }, 0.0f);
        assertEquals(outline.getPaletteEntryNames(), paletteEntryNames);
        assertEquals(outline.getPredefinedPalettes(), predefinedPalettes);
        assertArrayEquals(outline.getAssociatedColors(), new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        assertEquals(outline.getFamilyName(), "Rocher Color");
        assertEquals(outline.getStyleName(), "Outline");
        assertEquals(outline.getFullName(), "Rocher Color Outline");
        assertEquals(outline.getWeight(), TypeWeight.REGULAR);
        assertEquals(outline.getWidth(), TypeWidth.NORMAL);
        assertEquals(outline.getSlope(), TypeSlope.PLAIN);
        assertEquals(outline.getUnitsPerEm(), 2000);
        assertEquals(outline.getAscent(), 1960);
        assertEquals(outline.getDescent(), 590);
        assertEquals(outline.getLeading(), 0);
        assertEquals(outline.getGlyphCount(), 2105);
        assertEquals(outline.getBoundingBox(), new Rect(-1664, -629, 2988, 2044));
        assertEquals(outline.getUnderlinePosition(), -100);
        assertEquals(outline.getUnderlineThickness(), 50);
        assertEquals(outline.getStrikeoutPosition(), 696);
        assertEquals(outline.getStrikeoutThickness(), 50);
    }
}
