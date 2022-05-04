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

package com.mta.tehreer.graphics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.mta.tehreer.font.ColorPalette;
import com.mta.tehreer.font.NamedStyle;
import com.mta.tehreer.font.VariationAxis;

import java.util.List;

public class TypefaceInfo {
    private boolean isVariable;
    private List<VariationAxis> variationAxes;
    private List<NamedStyle> namedStyles;
    private float[] variationCoordinates;
    private List<String> paletteEntryNames;
    private List<ColorPalette> predefinedPalettes;
    private int[] associatedColors;
    private String familyName;
    private String styleName;
    private String fullName;
    private TypeWeight weight;
    private TypeWidth width;
    private TypeSlope slope;
    private int unitsPerEm;
    private int ascent;
    private int descent;
    private int leading;
    private int glyphCount;
    private Rect boundingBox;
    private int underlinePosition;
    private int underlineThickness;
    private int strikeoutPosition;
    private int strikeoutThickness;

    public TypefaceInfo() { }

    public TypefaceInfo(@NonNull TypefaceInfo other) {
        this.isVariable = other.isVariable;
        this.variationAxes = other.variationAxes;
        this.namedStyles = other.namedStyles;
        this.variationCoordinates = other.variationCoordinates;
        this.paletteEntryNames = other.paletteEntryNames;
        this.predefinedPalettes = other.predefinedPalettes;
        this.associatedColors = other.associatedColors;
        this.familyName = other.familyName;
        this.styleName = other.styleName;
        this.fullName = other.fullName;
        this.weight = other.weight;
        this.width = other.width;
        this.slope = other.slope;
        this.unitsPerEm = other.unitsPerEm;
        this.ascent = other.ascent;
        this.descent = other.descent;
        this.leading = other.leading;
        this.glyphCount = other.glyphCount;
        this.boundingBox = other.boundingBox;
        this.underlinePosition = other.underlinePosition;
        this.underlineThickness = other.underlineThickness;
        this.strikeoutPosition = other.strikeoutPosition;
        this.strikeoutThickness = other.strikeoutThickness;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public void setVariable(boolean variable) {
        isVariable = variable;
    }

    public List<VariationAxis> getVariationAxes() {
        return variationAxes;
    }

    public void setVariationAxes(List<VariationAxis> variationAxes) {
        this.variationAxes = variationAxes;
    }

    public List<NamedStyle> getNamedStyles() {
        return namedStyles;
    }

    public void setNamedStyles(List<NamedStyle> namedStyles) {
        this.namedStyles = namedStyles;
    }

    public float[] getVariationCoordinates() {
        return variationCoordinates;
    }

    public void setVariationCoordinates(float[] variationCoordinates) {
        this.variationCoordinates = variationCoordinates;
    }

    public List<String> getPaletteEntryNames() {
        return paletteEntryNames;
    }

    public void setPaletteEntryNames(List<String> paletteEntryNames) {
        this.paletteEntryNames = paletteEntryNames;
    }

    public List<ColorPalette> getPredefinedPalettes() {
        return predefinedPalettes;
    }

    public void setPredefinedPalettes(List<ColorPalette> predefinedPalettes) {
        this.predefinedPalettes = predefinedPalettes;
    }

    public int[] getAssociatedColors() {
        return associatedColors;
    }

    public void setAssociatedColors(int[] associatedColors) {
        this.associatedColors = associatedColors;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public TypeWeight getWeight() {
        return weight;
    }

    public void setWeight(TypeWeight weight) {
        this.weight = weight;
    }

    public TypeWidth getWidth() {
        return width;
    }

    public void setWidth(TypeWidth width) {
        this.width = width;
    }

    public TypeSlope getSlope() {
        return slope;
    }

    public void setSlope(TypeSlope slope) {
        this.slope = slope;
    }

    public int getUnitsPerEm() {
        return unitsPerEm;
    }

    public void setUnitsPerEm(int unitsPerEm) {
        this.unitsPerEm = unitsPerEm;
    }

    public int getAscent() {
        return ascent;
    }

    public void setAscent(int ascent) {
        this.ascent = ascent;
    }

    public int getDescent() {
        return descent;
    }

    public void setDescent(int descent) {
        this.descent = descent;
    }

    public int getLeading() {
        return leading;
    }

    public void setLeading(int leading) {
        this.leading = leading;
    }

    public int getGlyphCount() {
        return glyphCount;
    }

    public void setGlyphCount(int glyphCount) {
        this.glyphCount = glyphCount;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getUnderlinePosition() {
        return underlinePosition;
    }

    public void setUnderlinePosition(int underlinePosition) {
        this.underlinePosition = underlinePosition;
    }

    public int getUnderlineThickness() {
        return underlineThickness;
    }

    public void setUnderlineThickness(int underlineThickness) {
        this.underlineThickness = underlineThickness;
    }

    public int getStrikeoutPosition() {
        return strikeoutPosition;
    }

    public void setStrikeoutPosition(int strikeoutPosition) {
        this.strikeoutPosition = strikeoutPosition;
    }

    public int getStrikeoutThickness() {
        return strikeoutThickness;
    }

    public void setStrikeoutThickness(int strikeoutThickness) {
        this.strikeoutThickness = strikeoutThickness;
    }

    public static void assertTypefaceEquals(@NonNull Typeface typeface, @NonNull TypefaceInfo info) {
        assertEquals(typeface.isVariable(), info.isVariable());
        assertEquals(typeface.getVariationAxes(), info.getVariationAxes());
        assertEquals(typeface.getNamedStyles(), info.getNamedStyles());
        assertArrayEquals(typeface.getVariationCoordinates(), info.getVariationCoordinates(), 0.0f);
        assertEquals(typeface.getPaletteEntryNames(), info.getPaletteEntryNames());
        assertEquals(typeface.getPredefinedPalettes(), info.getPredefinedPalettes());
        assertArrayEquals(typeface.getAssociatedColors(), info.getAssociatedColors());
        assertEquals(typeface.getFamilyName(), info.getFamilyName());
        assertEquals(typeface.getStyleName(), info.getStyleName());
        assertEquals(typeface.getFullName(), info.getFullName());
        assertEquals(typeface.getWeight(), info.getWeight());
        assertEquals(typeface.getWidth(), info.getWidth());
        assertEquals(typeface.getSlope(), info.getSlope());
        assertEquals(typeface.getUnitsPerEm(), info.getUnitsPerEm());
        assertEquals(typeface.getAscent(), info.getAscent());
        assertEquals(typeface.getDescent(), info.getDescent());
        assertEquals(typeface.getLeading(), info.getLeading());
        assertEquals(typeface.getGlyphCount(), info.getGlyphCount());
        assertEquals(typeface.getBoundingBox(), info.getBoundingBox());
        assertEquals(typeface.getUnderlinePosition(), info.getUnderlinePosition());
        assertEquals(typeface.getUnderlineThickness(), info.getUnderlineThickness());
        assertEquals(typeface.getStrikeoutPosition(), info.getStrikeoutPosition());
        assertEquals(typeface.getStrikeoutThickness(), info.getStrikeoutThickness());
    }
}
