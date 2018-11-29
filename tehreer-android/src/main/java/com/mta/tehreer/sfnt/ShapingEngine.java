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

import com.mta.tehreer.Disposable;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.Constants;
import com.mta.tehreer.internal.JniBridge;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * The <code>ShapingEngine</code> class represents text shaping engine.
 */
public class ShapingEngine implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

    private static final class Finalizable extends ShapingEngine {
        Finalizable(@NonNull ShapingEngine parent) {
            super(parent);
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException(Constants.EXCEPTION_FINALIZABLE_OBJECT);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                super.dispose();
            } finally {
                super.finalize();
            }
        }
    }

    /**
     * Wraps a shaping engine object into a finalizable instance which is guaranteed to be disposed
     * automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behavior is undefined if the passed-in object is already disposed
     * or wrapped into another finalizable instance.
     *
     * @param shapingEngine The shaping engine object to wrap into a finalizable instance.
     * @return The finalizable instance of the passed-in shaping engine object.
     */
    public static @NonNull ShapingEngine finalizable(@NonNull ShapingEngine shapingEngine) {
        if (shapingEngine.getClass() == ShapingEngine.class) {
            return new Finalizable(shapingEngine);
        }

        if (shapingEngine.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return shapingEngine;
    }

    /**
     * Checks whether a shaping engine object is finalizable or not.
     *
     * @param shapingEngine The shaping engine object to check.
     * @return <code>true</code> if the passed-in shaping engine object is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(@NonNull ShapingEngine shapingEngine) {
        return (shapingEngine.getClass() == Finalizable.class);
    }

    /**
     * Returns the default writing direction of a script.
     *
     * @param scriptTag The tag of the script whose default direction is returned.
     * @return The default writing direction of the script identified by <code>scriptTag</code>.
     */
    public static WritingDirection getScriptDirection(int scriptTag) {
        return WritingDirection.valueOf(nGetScriptDefaultDirection(scriptTag));
    }

    private static class Base {
        Typeface typeface = null;
        Set<OpenTypeFeature> features = Collections.emptySet();
    }

    private final Base base;
    long nativeEngine;

    /**
     * Constructs a shaping engine object.
     */
    public ShapingEngine() {
        base = new Base();
        nativeEngine = nCreate();
    }

    ShapingEngine(@NonNull ShapingEngine other) {
        this.base = other.base;
        this.nativeEngine = other.nativeEngine;
    }

    /**
     * Returns the typeface which this shaping engine will use for shaping text.
     *
     * @return This current typeface.
     */
	public Typeface getTypeface() {
		return base.typeface;
	}

    /**
     * Sets the typeface which this shaping engine will use for shaping text.
     *
     * @param typeface The new typeface.
     */
	public void setTypeface(Typeface typeface) {
        base.typeface = typeface;
		nSetTypeface(nativeEngine, typeface);
	}

    /**
     * Returns the type size which this shaping engine will use for shaping text.
     *
     * @return This current type size.
     */
    public float getTypeSize() {
        return nGetTypeSize(nativeEngine);
    }

    /**
     * Sets the type size which this shaping engine will use for shaping text.
     *
     * @param typeSize The new type size.
     */
    public void setTypeSize(float typeSize) {
        checkArgument(typeSize >= 0.0f, "The value of font size is negative");
        nSetTypeSize(nativeEngine, typeSize);
    }

    /**
     * Returns the script tag which this shaping engine will use for shaping text. The default value
     * is <code>'DFLT'</code>.
     *
     * @return The current script tag.
     */
    public int getScriptTag() {
		return nGetScriptTag(nativeEngine);
	}

    /**
     * Sets the script tag which this shaping engine will use for shaping text. The default value is
     * <code>'DFLT'</code>.
     * <p>
     * A tag can be created from string by using {@link SfntTag#make(String)} method.
     *
     * @param scriptTag The new script tag.
     */
	public void setScriptTag(int scriptTag) {
		nSetScriptTag(nativeEngine, scriptTag);
	}

    /**
     * Returns the language tag which this shaping engine will use for shaping text. The default
     * value is <code>'dflt'</code>.
     *
     * @return The current language tag.
     */
	public int getLanguageTag() {
		return nGetLanguageTag(nativeEngine);
	}

    /**
     * Sets the language tag which this shaping engine will use for shaping text. The default value
     * is <code>'dflt'</code>.
     * <p>
     * A tag can be created from string by using {@link SfntTag#make(String)} method.
     *
     * @param languageTag The new language tag.
     */
	public void setLanguageTag(int languageTag) {
		nSetLanguageTag(nativeEngine, languageTag);
	}

    /**
     * Returns the user-specified open type feature settings.
     *
     * @return A set of open type features.
     */
	public @NonNull Set<OpenTypeFeature> getOpenTypeFeatures() {
	    return Collections.unmodifiableSet(base.features);
    }

    /**
     * Sets the user-specified open type feature settings.
     *
     * If the value of a feature is set to zero, it would be disabled provided that it is not a
     * required feature of the chosen script. If the value of a feature is greater than zero, it
     * would be enabled. In case of an alternate feature, this value would be used to pick the
     * alternate glyph at this position.
     *
     * @param features A set of open type features.
     */
	public void setOpenTypeFeatures(@NonNull Set<OpenTypeFeature> features) {
	    checkNotNull(features);

	    base.features = new LinkedHashSet<>(features);

	    int size = features.size();
        int[] tags = new int[size];
        short[] values = new short[size];
        int index = 0;

        for (OpenTypeFeature feature : features) {
            tags[index] = feature.tag();
            values[index] = (short) feature.value();

            index += 1;
        }

        nSetOpenTypeFeatures(nativeEngine, tags, values);
    }

    /**
     * Returns the direction in which this shaping engine will place the resultant glyphs. The
     * default value is {@link WritingDirection#LEFT_TO_RIGHT}.
     *
     * @return The current writing direction.
     */
    public @NonNull WritingDirection getWritingDirection() {
        return WritingDirection.valueOf(nGetWritingDirection(nativeEngine));
    }

    /**
     * Sets the direction in which this shaping engine will place the resultant glyphs. The default
     * value is {@link WritingDirection#LEFT_TO_RIGHT}.
     * <p>
     * The value of <code>writingDirection</code> must reflect the rendering direction of source
     * script so that cursive and mark glyphs are placed at appropriate locations. It should not be
     * confused with the direction of a bidirectional run as that may not reflect the script
     * direction if overridden explicitly.
     *
     * @param writingDirection The new writing direction.
     */
    public void setWritingDirection(@NonNull WritingDirection writingDirection) {
        nSetWritingDirection(nativeEngine, writingDirection.value);
    }

    /**
     * Returns the order in which this shaping engine will process the text. The default value is
     * {@link ShapingOrder#FORWARD}.
     *
     * @return The current shaping order.
     */
    public @NonNull ShapingOrder getShapingOrder() {
        return ShapingOrder.valueOf(nGetShapingOrder(nativeEngine));
    }

    /**
     * Sets the order in which this shaping engine will process the text. The default value is
     * {@link ShapingOrder#FORWARD}.
     * <p>
     * This method provides a convenient way to shape a bidirectional run whose direction is
     * opposite to that of script. For example, if the direction of a run, 'car' is explicitly set
     * as right-to-left, backward order will automatically read it as 'rac' without reordering the
     * original text.
     *
     * @param shapingOrder The new shaping order.
     */
    public void setShapingOrder(@NonNull ShapingOrder shapingOrder) {
        nSetShapingOrder(nativeEngine, shapingOrder.value);
    }

    /**
     * Shapes the specified range of text into glyphs.
     * <p>
     * The output glyphs in the <code>ShapingResult</code> object flow visually in writing
     * direction. For left-to-right direction, the position of pen is incremented with glyph's
     * advance after rendering it. Similarly, for right-to-left direction, the position of pen is
     * decremented with glyph's advance after rendering it.
     *
     * @param text The text to shape into glyphs.
     * @param fromIndex The index of the first character (inclusive) to be shaped.
     * @param toIndex The index of the last character (exclusive) to be shaped.
     * @return A non-finalizable instance of a <code>ShapingResult</code> object.
     *
     * @throws IllegalStateException if current typeface is <code>null</code>.
     * @throws NullPointerException if <code>text</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>fromIndex</code> is negative, or
     *         <code>toIndex</code> is greater than <code>text.length()</code>, or
     *         <code>fromIndex</code> is greater than <code>toIndex</code>
     */
    public @NonNull ShapingResult shapeText(@NonNull String text, int fromIndex, int toIndex) {
        if (base.typeface == null) {
            throw new IllegalStateException("Typeface has not been set");
        }
        checkNotNull(text, "text");
        checkArgument(fromIndex >= 0, "From Index: " + fromIndex);
        checkArgument(toIndex <= text.length(), "To Index: " + toIndex + ", Text Length: " + text.length());
        checkArgument(toIndex >= fromIndex, "Bad Range: [" + fromIndex + ", " + toIndex + ')');

        ShapingResult result = new ShapingResult();
        nShapeText(nativeEngine, result.nativeResult, text, fromIndex, toIndex);

        return result;
    }

	@Override
	public void dispose() {
        nDispose(nativeEngine);
    }

    @Override
    public String toString() {
        return "ShapingEngine{typeface=" + getTypeface().toString()
                + ", typeSize=" + String.valueOf(getTypeSize())
                + ", scriptTag=" + SfntTag.toString(getScriptTag())
                + ", languageTag=" + SfntTag.toString(getLanguageTag())
                + ", writingDirection=" + getWritingDirection().toString()
                + ", shapingOrder=" + getShapingOrder().toString()
                + "}";
    }

    private static native int nGetScriptDefaultDirection(int scriptTag);

	private static native long nCreate();
	private static native void nDispose(long nativeEngine);

	private static native void nSetTypeface(long nativeEngine, Typeface typeface);

    private static native float nGetTypeSize(long nativeEngine);
    private static native void nSetTypeSize(long nativeEngine, float fontSize);

    private static native int nGetScriptTag(long nativeEngine);
	private static native void nSetScriptTag(long nativeEngine, int scriptTag);

    private static native int nGetLanguageTag(long nativeEngine);
    private static native void nSetLanguageTag(long nativeEngine, int languageTag);

    private static native void nSetOpenTypeFeatures(long nativeEngine, int[] tags, short[] values);

    private static native int nGetWritingDirection(long nativeEngine);
	private static native void nSetWritingDirection(long nativeEngine, int writingDirection);

    private static native int nGetShapingOrder(long nativeEngine);
    private static native void nSetShapingOrder(long nativeEngine, int shapingOrder);

	private static native void nShapeText(long nativeEngine, long nativeResult, String text, int fromIndex, int toIndex);
}
