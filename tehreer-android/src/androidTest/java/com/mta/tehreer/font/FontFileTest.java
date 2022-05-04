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

import static com.mta.tehreer.graphics.TypefaceInfo.assertTypefaceEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Rect;

import com.mta.tehreer.graphics.TypeSlope;
import com.mta.tehreer.graphics.TypeWeight;
import com.mta.tehreer.graphics.TypeWidth;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceInfo;
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

        TypefaceInfo info = new TypefaceInfo();
        info.setVariable(true);
        info.setVariationAxes(variationAxes);
        info.setNamedStyles(namedStyles);
        info.setFamilyName("Sudo");
        info.setWeight(TypeWeight.REGULAR);
        info.setWidth(TypeWidth.NORMAL);
        info.setSlope(TypeSlope.PLAIN);
        info.setUnitsPerEm(1024);
        info.setAscent(832);
        info.setDescent(192);
        info.setLeading(0);
        info.setGlyphCount(1077);
        info.setBoundingBox(new Rect(-458, -209, 640, 960));
        info.setUnderlinePosition(-160);
        info.setUnderlineThickness(64);
        info.setStrikeoutPosition(268);
        info.setStrikeoutThickness(64);

        TypefaceInfo regular = new TypefaceInfo(info);
        regular.setVariationCoordinates(new float[] { 0.0f, 400.0f });
        regular.setStyleName("Regular");
        regular.setFullName("Sudo Regular");

        TypefaceInfo thin = new TypefaceInfo(info);
        thin.setVariationCoordinates(new float[] { 0.0f, 200.0f });
        thin.setStyleName("Thin");
        thin.setFullName("Sudo Thin");
        thin.setWeight(TypeWeight.EXTRA_LIGHT);

        TypefaceInfo light = new TypefaceInfo(info);
        light.setVariationCoordinates(new float[] { 0.0f, 309.375f });
        light.setStyleName("Light");
        light.setFullName("Sudo Light");
        light.setWeight(TypeWeight.LIGHT);

        TypefaceInfo regularVariant = new TypefaceInfo(info);
        regularVariant.setVariationCoordinates(new float[] { 0.0f, 387.5f });
        regularVariant.setStyleName("Regular");
        regularVariant.setFullName("Sudo Regular");
        regularVariant.setWeight(TypeWeight.REGULAR);

        TypefaceInfo medium = new TypefaceInfo(info);
        medium.setVariationCoordinates(new float[] { 0.0f, 543.75f });
        medium.setStyleName("Medium");
        medium.setFullName("Sudo Medium");
        medium.setWeight(TypeWeight.MEDIUM);

        TypefaceInfo bold = new TypefaceInfo(info);
        bold.setVariationCoordinates(new float[] { 0.0f, 700.0f });
        bold.setStyleName("Bold");
        bold.setFullName("Sudo Bold");
        bold.setWeight(TypeWeight.BOLD);

        TypefaceInfo thinItalic = new TypefaceInfo(info);
        thinItalic.setVariationCoordinates(new float[] { 1.0f, 200.0f });
        thinItalic.setStyleName("Thin Italic");
        thinItalic.setFullName("Sudo Thin Italic");
        thinItalic.setWeight(TypeWeight.EXTRA_LIGHT);
        thinItalic.setSlope(TypeSlope.ITALIC);

        TypefaceInfo lightItalic = new TypefaceInfo(info);
        lightItalic.setVariationCoordinates(new float[] { 1.0f, 309.375f });
        lightItalic.setStyleName("Light Italic");
        lightItalic.setFullName("Sudo Light Italic");
        lightItalic.setWeight(TypeWeight.LIGHT);
        lightItalic.setSlope(TypeSlope.ITALIC);

        TypefaceInfo regularItalic = new TypefaceInfo(info);
        regularItalic.setVariationCoordinates(new float[] { 1.0f, 387.5f });
        regularItalic.setStyleName("Regular Italic");
        regularItalic.setFullName("Sudo Regular Italic");
        regularItalic.setWeight(TypeWeight.REGULAR);
        regularItalic.setSlope(TypeSlope.ITALIC);

        TypefaceInfo mediumItalic = new TypefaceInfo(info);
        mediumItalic.setVariationCoordinates(new float[] { 1.0f, 543.75f });
        mediumItalic.setStyleName("Medium Italic");
        mediumItalic.setFullName("Sudo Medium Italic");
        mediumItalic.setWeight(TypeWeight.MEDIUM);
        mediumItalic.setSlope(TypeSlope.ITALIC);

        TypefaceInfo boldItalic = new TypefaceInfo(info);
        boldItalic.setVariationCoordinates(new float[] { 1.0f, 700.0f });
        boldItalic.setStyleName("Bold Italic");
        boldItalic.setFullName("Sudo Bold Italic");
        boldItalic.setWeight(TypeWeight.BOLD);
        boldItalic.setSlope(TypeSlope.ITALIC);

        assertTypefaceEquals(typefaces.get(0), regular);
        assertTypefaceEquals(typefaces.get(1), thin);
        assertTypefaceEquals(typefaces.get(2), light);
        assertTypefaceEquals(typefaces.get(3), regularVariant);
        assertTypefaceEquals(typefaces.get(4), medium);
        assertTypefaceEquals(typefaces.get(5), bold);
        assertTypefaceEquals(typefaces.get(6), thinItalic);
        assertTypefaceEquals(typefaces.get(7), lightItalic);
        assertTypefaceEquals(typefaces.get(8), regularItalic);
        assertTypefaceEquals(typefaces.get(9), mediumItalic);
        assertTypefaceEquals(typefaces.get(10), boldItalic);
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

        TypefaceInfo info = new TypefaceInfo();
        info.setVariable(true);
        info.setVariationAxes(variationAxes);
        info.setNamedStyles(namedStyles);
        info.setPaletteEntryNames(paletteEntryNames);
        info.setPredefinedPalettes(predefinedPalettes);
        info.setFamilyName("Rocher Color");
        info.setWeight(TypeWeight.REGULAR);
        info.setWidth(TypeWidth.NORMAL);
        info.setSlope(TypeSlope.PLAIN);
        info.setUnitsPerEm(2000);
        info.setAscent(1960);
        info.setDescent(590);
        info.setLeading(0);
        info.setGlyphCount(2105);
        info.setBoundingBox(new Rect(-1664, -629, 2988, 2044));
        info.setUnderlinePosition(-100);
        info.setUnderlineThickness(50);
        info.setStrikeoutPosition(696);
        info.setStrikeoutThickness(50);

        TypefaceInfo regular = new TypefaceInfo(info);
        regular.setVariationCoordinates(new float[] { 100.0f, 100.0f });
        regular.setAssociatedColors(new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        regular.setStyleName("Regular");
        regular.setFullName("Rocher Color Regular");

        TypefaceInfo shadow = new TypefaceInfo(info);
        shadow.setVariationCoordinates(new float[] { 100.0f, 50.0f });
        shadow.setAssociatedColors(new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        shadow.setStyleName("Shadow");
        shadow.setFullName("Rocher Color Shadow");

        TypefaceInfo extrude = new TypefaceInfo(info);
        extrude.setVariationCoordinates(new float[] { 10.0f, 100.0f });
        extrude.setAssociatedColors(new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        extrude.setStyleName("Extrude");
        extrude.setFullName("Rocher Color Extrude");

        TypefaceInfo bevel = new TypefaceInfo(info);
        bevel.setVariationCoordinates(new float[] { 100.0f, 0.0f });
        bevel.setAssociatedColors(new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        bevel.setStyleName("Bevel");
        bevel.setFullName("Rocher Color Bevel");

        TypefaceInfo outline = new TypefaceInfo(info);
        outline.setVariationCoordinates(new float[] { 0.0f, 0.0f });
        outline.setAssociatedColors(new int[] { 0xFF513D32, 0xFFF5B944, 0xFFE08E37, 0xFFF5CA56 });
        outline.setStyleName("Outline");
        outline.setFullName("Rocher Color Outline");

        assertTypefaceEquals(typefaces.get(0), regular);
        assertTypefaceEquals(typefaces.get(1), shadow);
        assertTypefaceEquals(typefaces.get(2), extrude);
        assertTypefaceEquals(typefaces.get(3), bevel);
        assertTypefaceEquals(typefaces.get(4), outline);
    }
}
