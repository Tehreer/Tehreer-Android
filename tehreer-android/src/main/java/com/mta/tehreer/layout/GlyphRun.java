/*
 * Copyright (C) 2016-2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout;

import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.Map;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;

/**
 * A glyph run represents a consecutive sequence of glyphs sharing the same attributes and
 * direction.
 */
public class GlyphRun {
    static {
        com.mta.tehreer.internal.JniBridge.loadLibrary();
    }

    /** The line that has this run, which keeps the run of Core alive. */
    private final @NonNull ComposedLine mOwner;
    private final long mNativeRun;

    private final int mCharStart;
    private final int mCharEnd;
    private final int mStartExtraLength;
    private final int mEndExtraLength;
    private final byte mBidiLevel;
    private final @NonNull WritingDirection mWritingDirection;
    private final boolean mIsBackward;
    private final int mGlyphCount;
    private final int mClusterCount;
    private final boolean mHasForegroundColor;
    private final int mForegroundColor;

    private final float mTypeSize;
    private final float mScaleX;
    private final float mAscent;
    private final float mDescent;
    private final float mLeading;
    private final float mWidth;
    private final float mHeight;
    private float originX;
    private float originY;

    private final @NonNull Typeface mTypeface;
    private final ReplacementHolder mReplacement;

    private volatile IntList mGlyphIds;
    private volatile PointList mGlyphOffsets;
    private volatile FloatList mGlyphAdvances;
    private volatile IntList mClusterMap;

    GlyphRun(@NonNull ComposedLine owner, long nativeRun, @NonNull Map<Long, Typeface> typefaces) {
        mOwner = owner;
        mNativeRun = nativeRun;

        int[] ints = new int[11];
        float[] floats = new float[9];

        nGetInts(nativeRun, ints);
        nGetFloats(nativeRun, floats);

        mCharStart = ints[0];
        mCharEnd = ints[1];
        mStartExtraLength = ints[2];
        mEndExtraLength = ints[3];
        mBidiLevel = (byte) ints[4];
        mWritingDirection = (ints[5] == 1 ? WritingDirection.RIGHT_TO_LEFT : WritingDirection.LEFT_TO_RIGHT);
        mIsBackward = (ints[6] != 0);
        mGlyphCount = ints[7];
        mClusterCount = ints[8];
        mHasForegroundColor = (ints[9] != 0);
        mForegroundColor = ints[10];

        mTypeSize = floats[0];
        mScaleX = floats[1];
        mAscent = floats[2];
        mDescent = floats[3];
        mLeading = floats[4];
        originX = floats[5];
        originY = floats[6];
        mWidth = floats[7];
        mHeight = floats[8];

        mTypeface = typefaces.get(nGetTypeface(nativeRun));
        mReplacement = (ReplacementHolder) nGetReplacement(nativeRun);
    }

    /** Returns the replacement span of this run, if it is made for one. */
    ReplacementHolder getReplacement() {
        return mReplacement;
    }

    private void checkCharIndex(int charIndex) {
        checkArgument(charIndex >= mCharStart && charIndex < mCharEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + mCharStart + ", " + mCharEnd + ')');
    }

    private void checkCaretIndex(int charIndex) {
        checkArgument(charIndex >= mCharStart && charIndex <= mCharEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + mCharStart + ", " + mCharEnd + ']');
    }

    private void checkGlyphRange(int glyphStart, int glyphEnd) {
        checkArgument(glyphStart >= 0, "Glyph Start: " + glyphStart);
        checkArgument(glyphEnd <= mGlyphCount, "Glyph End: " + glyphEnd + ", Glyph Count: " + mGlyphCount);
        checkArgument(glyphStart <= glyphEnd, "Bad Range: [" + glyphStart + ", " + glyphEnd + ')');
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return mCharStart;
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return mCharEnd;
    }

    /**
     * Returns the extra excluded length at the start of the cluster map.
     * <p>
     * If the first cluster of this run begins within the extra range, then its rendering will be
     * clipped from the start. The amount of clipping would be equal to the perceived trailing caret
     * position of last excluded character.
     * <p>
     * For example, consider three characters <code>f</code>, <code>i</code> and another
     * <code>i</code> form a cluster having a single ligature, <code>fii</code> and the run starts
     * from the second <code>i</code> with <code>f</code> and <code>i</code> being extra characters.
     * In this case, the ligature would be divided into three equal parts and the first two parts
     * would be clipped.
     *
     * @return The extra excluded length at the start of the cluster map.
     */
    public int getStartExtraLength() {
        return mStartExtraLength;
    }

    /**
     * Returns the extra excluded length at the end of the cluster map.
     * <p>
     * If the last cluster of this run finishes within the excluded range, then its rendering will
     * be clipped from the end. The amount of clipping would be equal to the perceived leading caret
     * position of first excluded character.
     * <p>
     * For example, consider three characters <code>f</code>, <code>i</code> and another
     * <code>i</code> form a cluster having a single ligature, <code>fii</code> and the run consists
     * of just <code>f</code> with both <code>i</code> being extra characters. In this case, the
     * ligature would be divided into three equal parts and the last two parts would be clipped.
     *
     * @return The extra excluded length at the end of the cluster map.
     */
    public int getEndExtraLength() {
        return mEndExtraLength;
    }

    /**
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return mBidiLevel;
    }

    /**
     * Returns the typeface of this run.
     *
     * @return The typeface of this run.
     */
    public @NonNull Typeface getTypeface() {
        return mTypeface;
    }

    /**
     * Returns the type size of this run.
     *
     * @return The type size of this run.
     */
    public float getTypeSize() {
        return mTypeSize;
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public @NonNull WritingDirection getWritingDirection() {
        return mWritingDirection;
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return mGlyphCount;
    }

    /**
     * Returns the glyph IDs of this run.
     *
     * @return The glyph IDs of this run.
     */
    public @NonNull IntList getGlyphIds() {
        IntList glyphIds = mGlyphIds;
        if (glyphIds == null) {
            int[] array = new int[mGlyphCount];
            nGetGlyphIds(mNativeRun, array);

            glyphIds = IntList.of(array);
            mGlyphIds = glyphIds;
        }

        return glyphIds;
    }

    /**
     * Returns the glyph offsets of this run.
     *
     * @return The glyph offsets of this run.
     */
    public @NonNull PointList getGlyphOffsets() {
        PointList glyphOffsets = mGlyphOffsets;
        if (glyphOffsets == null) {
            float[] array = new float[mGlyphCount * 2];
            nGetGlyphOffsets(mNativeRun, array);

            glyphOffsets = PointList.of(array);
            mGlyphOffsets = glyphOffsets;
        }

        return glyphOffsets;
    }

    /**
     * Returns the glyph advances of this run.
     *
     * @return The glyph advances of this run.
     */
    public @NonNull FloatList getGlyphAdvances() {
        FloatList glyphAdvances = mGlyphAdvances;
        if (glyphAdvances == null) {
            float[] array = new float[mGlyphCount];
            nGetGlyphAdvances(mNativeRun, array);

            glyphAdvances = FloatList.of(array);
            mGlyphAdvances = glyphAdvances;
        }

        return glyphAdvances;
    }

    /**
     * Returns the indexes, mapping each character of this run to corresponding glyph.
     *
     * @return The indexes, mapping each character of this run to corresponding glyph.
     */
    public @NonNull IntList getClusterMap() {
        IntList clusterMap = mClusterMap;
        if (clusterMap == null) {
            int[] array = new int[mClusterCount];
            nGetClusterMap(mNativeRun, array);

            clusterMap = IntList.of(array);
            mClusterMap = clusterMap;
        }

        return clusterMap;
    }

    /**
     * Returns the x- origin of this run in parent line.
     *
     * @return The x- origin of this run in parent line.
     */
    public float getOriginX() {
        return originX;
    }

    void setOriginX(float originX) {
        this.originX = originX;
    }

    /**
     * Returns the y- origin of this run in parent line.
     *
     * @return The y- origin of this run in parent line.
     */
    public float getOriginY() {
        return originY;
    }

    void setOriginY(float originY) {
        this.originY = originY;
    }

    /**
     * Returns the ascent of this run. The ascent is the distance from the top of the
     * <code>GlyphRun</code> to the baseline. It is always either positive or zero.
     *
     * @return The ascent of this run.
     */
    public float getAscent() {
        return mAscent;
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return mDescent;
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return mLeading;
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return mHeight;
    }

    /**
     * Returns the index to the first character of specified cluster in source string. In most
     * cases, it would be the same index as the specified one. But if the character occurs within a
     * cluster, then a previous index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index to the first character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getActualClusterStart(int charIndex) {
        checkCharIndex(charIndex);

        return nGetClusterStart(mNativeRun, charIndex);
    }

    /**
     * Returns the index after the last character of specified cluster in source string. In most
     * cases, it would be an index after the specified one. But if the character occurs within a
     * cluster, then a farther index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index after the last character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getActualClusterEnd(int charIndex) {
        checkCharIndex(charIndex);

        return nGetClusterEnd(mNativeRun, charIndex);
    }

    /**
     * Returns the index of leading glyph related to the specified cluster. It will come after the
     * trailing glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of leading glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getLeadingGlyphIndex(int charIndex) {
        checkCharIndex(charIndex);

        return nGetLeadingGlyphIndex(mNativeRun, charIndex);
    }

    /**
     * Returns the index of trailing glyph related to the specified cluster. It will come before the
     * leading glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of trailing glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getTrailingGlyphIndex(int charIndex) {
        checkCharIndex(charIndex);

        return nGetTrailingGlyphIndex(mNativeRun, charIndex);
    }

    /**
     * Returns the distance of specified character from the start of the run assumed at zero.
     *
     * @param charIndex The index of a character in source string.
     * @return The distance of specified character from the start of the run assumed at zero.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than run end.
     */
    public float computeCharDistance(int charIndex) {
        checkCaretIndex(charIndex);

        return nGetDistance(mNativeRun, charIndex);
    }

    float computeRangeDistance(int fromIndex, int toIndex) {
        return Math.abs(nGetDistance(mNativeRun, toIndex) - nGetDistance(mNativeRun, fromIndex));
    }

    /**
     * Determines the index of character nearest to the specified distance.
     * <p>
     * The process involves iterating over the clusters of this glyph run. If a cluster consists of
     * multiple characters, its total advance is evenly distributed among the number of characters
     * it contains. The advance of each character is added to track the covered distance. This way
     * leading and trailing characters are determined close to the specified distance. Afterwards,
     * the index of nearer character is returned.
     * <p>
     * If <code>distance</code> is negative, then run's starting index is returned. If it is beyond
     * run's extent, then ending index is returned. The indices will be reversed in case of
     * right-to-left run.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     *
     * @see #getCharStart()
     * @see #getCharEnd()
     */
    public int computeNearestCharIndex(float distance) {
        return nGetIndexOfCodeUnit(mNativeRun, distance);
    }

    @NonNull RectF computeBoundingBox(@NonNull Renderer renderer) {
        return computeBoundingBox(renderer, 0, getGlyphCount());
    }

    /**
     * Calculates the bounding box for the given glyph range in this run. The bounding box is a
     * rectangle that encloses the paths of this run's glyphs in the given range, as tightly as
     * possible.
     *
     * @param renderer The renderer to use for calculating the bounding box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the bounding box.
     * @param glyphStart The index to the first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return A rectangle that tightly encloses the paths of this run's glyphs in the given range.
     *
     * @throws IllegalArgumentException if <code>glyphStart</code> is negative, or
     *         <code>glyphEnd</code> is greater than total number of glyphs in the run, or
     *         <code>glyphStart</code> is greater than <code>glyphEnd</code>.
     */
    public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        checkGlyphRange(glyphStart, glyphEnd);

        float[] box = new float[4];
        nGetBoundingBox(mNativeRun, glyphStart, glyphEnd, renderer.getNativeHandle(), box);
        renderer.syncNative();

        return new RectF(box[0], box[1], box[2], box[3]);
    }

    // region Drawing

    private static class ClusterRange {
        int actualStart;
        int actualEnd;
        int glyphStart;
        int glyphEnd;
    }

    private boolean isRTL() {
        return (mBidiLevel & 1) == 1;
    }

    private float getLeadingEdge(int fromIndex, int toIndex) {
        return nGetDistance(mNativeRun, !mIsBackward ? fromIndex : toIndex);
    }

    private ClusterRange getClusterRange(int charIndex, ClusterRange exclusion) {
        ClusterRange cluster = new ClusterRange();
        cluster.actualStart = nGetClusterStart(mNativeRun, charIndex);
        cluster.actualEnd = nGetClusterEnd(mNativeRun, charIndex);

        int leadingIndex = nGetLeadingGlyphIndex(mNativeRun, charIndex);
        int trailingIndex = nGetTrailingGlyphIndex(mNativeRun, charIndex);

        cluster.glyphStart = Math.min(leadingIndex, trailingIndex);
        cluster.glyphEnd = Math.max(leadingIndex, trailingIndex) + 1;

        if (exclusion != null) {
            int minStart = Math.min(exclusion.glyphStart, cluster.glyphEnd);
            int maxEnd = Math.max(cluster.glyphStart, exclusion.glyphEnd);

            cluster.glyphStart = (!mIsBackward ? maxEnd : cluster.glyphStart);
            cluster.glyphEnd = (mIsBackward ? minStart : cluster.glyphEnd);
        }

        return (cluster.glyphStart < cluster.glyphEnd ? cluster : null);
    }

    private void drawGlyphs(@NonNull Renderer renderer, @NonNull Canvas canvas, int glyphStart, int glyphEnd) {
        renderer.drawGlyphs(canvas,
                            getGlyphIds().subList(glyphStart, glyphEnd),
                            getGlyphOffsets().subList(glyphStart, glyphEnd),
                            getGlyphAdvances().subList(glyphStart, glyphEnd));
    }

    private void drawEdgeCluster(@NonNull Renderer renderer, @NonNull Canvas canvas, @NonNull ClusterRange cluster) {
        boolean startClipped = (cluster.actualStart < mCharStart);
        boolean endClipped = (cluster.actualEnd > mCharEnd);

        float clipLeft;
        float clipRight;

        if (!isRTL()) {
            clipLeft = (startClipped ? nGetDistance(mNativeRun, mCharStart) : -Float.MAX_VALUE);
            clipRight = (endClipped ? nGetDistance(mNativeRun, mCharEnd) : Float.MAX_VALUE);
        } else {
            clipRight = (startClipped ? nGetDistance(mNativeRun, mCharStart) : Float.MAX_VALUE);
            clipLeft = (endClipped ? nGetDistance(mNativeRun, mCharEnd) : -Float.MAX_VALUE);
        }

        canvas.save();
        canvas.clipRect(clipLeft, -Float.MAX_VALUE, clipRight, Float.MAX_VALUE);
        canvas.translate(getLeadingEdge(cluster.actualStart, cluster.actualEnd), 0.0f);

        drawGlyphs(renderer, canvas, cluster.glyphStart, cluster.glyphEnd);

        canvas.restore();
    }

    /**
     * Draws this run completely onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        if (mReplacement != null) {
            mReplacement.draw(canvas, mAscent, mDescent);
            return;
        }

        renderer.setTypeface(mTypeface);
        renderer.setTypeSize(mTypeSize);
        renderer.setScaleX(mScaleX);
        renderer.setWritingDirection(mWritingDirection);

        int defaultFillColor = renderer.getFillColor();
        if (mHasForegroundColor) {
            renderer.setFillColor(mForegroundColor);
        }

        int firstIndex = mCharStart;
        int lastIndex = mCharEnd - 1;

        ClusterRange firstCluster = null;
        ClusterRange lastCluster = null;

        if (mStartExtraLength > 0) {
            firstCluster = getClusterRange(firstIndex, null);
        }
        if (mEndExtraLength > 0) {
            lastCluster = getClusterRange(lastIndex, firstCluster);
        }

        int glyphStart = 0;
        int glyphEnd = mGlyphCount;

        int chunkStart = firstIndex;
        int chunkEnd = lastIndex + 1;

        if (firstCluster != null) {
            drawEdgeCluster(renderer, canvas, firstCluster);

            // Exclude first cluster characters.
            chunkStart = firstCluster.actualEnd;
            // Exclude first cluster glyphs.
            glyphStart = (!mIsBackward ? firstCluster.glyphEnd : glyphStart);
            glyphEnd = (mIsBackward ? firstCluster.glyphStart : glyphEnd);
        }
        if (lastCluster != null) {
            // Exclude last cluster characters.
            chunkEnd = lastCluster.actualStart;
            // Exclude last cluster glyphs.
            glyphEnd = (!mIsBackward ? lastCluster.glyphStart : glyphEnd);
            glyphStart = (mIsBackward ? lastCluster.glyphEnd : glyphStart);
        }

        canvas.save();
        canvas.translate(getLeadingEdge(chunkStart, chunkEnd), 0.0f);

        drawGlyphs(renderer, canvas, glyphStart, glyphEnd);

        canvas.restore();

        if (lastCluster != null) {
            drawEdgeCluster(renderer, canvas, lastCluster);
        }

        renderer.setFillColor(defaultFillColor);
    }

    // endregion

    @Override
    public String toString() {
        return "GlyphRun{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds()
                + ", glyphOffsets=" + getGlyphOffsets()
                + ", glyphAdvances=" + getGlyphAdvances()
                + ", clusterMap=" + getClusterMap()
                + ", originX=" + originX
                + ", originY=" + originY
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + "}";
    }

    private static native void nGetInts(long nativeRun, int[] values);
    private static native void nGetFloats(long nativeRun, float[] values);
    private static native long nGetTypeface(long nativeRun);
    private static native Object nGetReplacement(long nativeRun);
    private static native void nGetGlyphIds(long nativeRun, int[] destination);
    private static native void nGetGlyphOffsets(long nativeRun, float[] destination);
    private static native void nGetGlyphAdvances(long nativeRun, float[] destination);
    private static native void nGetClusterMap(long nativeRun, int[] destination);
    private static native int nGetClusterStart(long nativeRun, int charIndex);
    private static native int nGetClusterEnd(long nativeRun, int charIndex);
    private static native int nGetLeadingGlyphIndex(long nativeRun, int charIndex);
    private static native int nGetTrailingGlyphIndex(long nativeRun, int charIndex);
    private static native float nGetDistance(long nativeRun, int charIndex);
    private static native int nGetIndexOfCodeUnit(long nativeRun, float distance);
    private static native void nGetBoundingBox(long nativeRun, int glyphStart, int glyphEnd, long nativeRenderer, float[] box);
}
