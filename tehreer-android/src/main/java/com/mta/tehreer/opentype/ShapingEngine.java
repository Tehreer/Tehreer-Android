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

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.internal.util.Convert;
import com.mta.tehreer.util.Disposable;

/**
 * The <code>ShapingEngine</code> class represents the text shaping engine.
 */
public class ShapingEngine implements Disposable {

    private static class Finalizable extends ShapingEngine {

        private Finalizable(ShapingEngine parent) {
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
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param shapingEngine The shaping engine object to wrap into a finalizable instance.
     * @return The finalizable instance of the passed-in shaping engine object.
     */
    public static ShapingEngine finalizable(ShapingEngine shapingEngine) {
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
    public static boolean isFinalizable(ShapingEngine shapingEngine) {
        return (shapingEngine.getClass() == Finalizable.class);
    }

    /**
     * Returns the default rendering direction of a script.
     *
     * @param scriptTag The tag of the script whose default direction is returned.
     * @return The default direction of the script identified by <code>scriptTag</code>.
     */
    public static WritingDirection getScriptDefaultDirection(int scriptTag) {
        return Convert.toJavaTextDirection(nativeGetScriptDefaultDirection(scriptTag));
    }

    private static class Base {
        Typeface typeface;
    }

    private final Base base;
    long nativeEngine;

    /**
     * Constructs a shaping engine object.
     */
    public ShapingEngine() {
        base = new Base();
        nativeEngine = nativeCreate();
    }

    private ShapingEngine(ShapingEngine other) {
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
		nativeSetTypeface(nativeEngine, typeface);
	}

    /**
     * Returns the type size which this shaping engine will use for shaping text.
     *
     * @return This current type size.
     */
    public float getTypeSize() {
        return nativeGetTypeSize(nativeEngine);
    }

    /**
     * Sets the type size which this shaping engine will use for shaping text.
     *
     * @param typeSize The new type size.
     */
    public void setTypeSize(float typeSize) {
        if (typeSize < 0.0) {
            throw new IllegalArgumentException("The value of font size is negative");
        }

        nativeSetTypeSize(nativeEngine, typeSize);
    }

    /**
     * Returns the script tag which this shaping engine will use for shaping text.
     *
     * @return The current script tag.
     */
    public int getScriptTag() {
		return nativeGetScriptTag(nativeEngine);
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
		nativeSetScriptTag(nativeEngine, scriptTag);
	}

    /**
     * Returns the language tag which this shaping engine will use for shaping text.
     *
     * @return The current language tag.
     */
	public int getLanguageTag() {
		return nativeGetLanguageTag(nativeEngine);
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
		nativeSetLanguageTag(nativeEngine, languageTag);
	}

    /**
     * Returns the direction in which this shaping engine will place the resultant glyphs.
     *
     * @return The current writing direction.
     */
    public WritingDirection getWritingDirection() {
        return Convert.toJavaTextDirection(nativeGetWritingDirection(nativeEngine));
    }

    /**
     * Sets the direction in which this shaping engine will place the resultant glyphs.
     * <p>
     * The value of <code>writingDirection</code> must reflect the rendering direction of source
     * script so that cursive and mark glyphs are placed at appropriate locations. It should not be
     * confused with the direction of a bidirectional run as that may not reflect the script
     * direction if overridden explicitly.
     *
     * @param writingDirection The new writing direction.
     */
    public void setWritingDirection(WritingDirection writingDirection) {
        nativeSetWritingDirection(nativeEngine, writingDirection.value);
    }

    /**
     * Returns the order in which this shaping engine will process the text. The default value is
     * {@link ShapingOrder#FORWARD}.
     *
     * @return The current shaping order.
     */
    public ShapingOrder getShapingOrder() {
        return Convert.toJavaTextMode(nativeGetShapingOrder(nativeEngine));
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
    public void setShapingOrder(ShapingOrder shapingOrder) {
        nativeSetShapingOrder(nativeEngine, shapingOrder.value);
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
     * @param fromIndex
     * @param toIndex
     *
     * @throws IllegalStateException if current typeface is <code>null</code>.
     * @throws NullPointerException if <code>text</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>fromIndex</code> is negative, or
     *         <code>toIndex</code> is greater than <code>text.length()</code>, or
     *         <code>fromIndex</code> is greater than <code>toIndex</code>

     */
    public ShapingResult shapeText(String text, int fromIndex, int toIndex) {
        if (base.typeface == null) {
            throw new IllegalStateException("Typeface has not been set");
        }
        if (text == null) {
            throw new NullPointerException("Text is null");
        }
        if (fromIndex < 0) {
            throw new IllegalArgumentException("Char Start: " + fromIndex);
        }
        if (toIndex > text.length()) {
            throw new IllegalArgumentException("Char End: " + toIndex
                    + ", Text Length: " + text.length());
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("Char Start: " + fromIndex
                    + ", Char End: " + toIndex);
        }

        ShapingResult result = new ShapingResult();
        nativeShapeText(nativeEngine, result.nativeResult, text, fromIndex, toIndex);

        return result;
    }

	@Override
	public void dispose() {
        nativeDispose(nativeEngine);
    }

    @Override
    public String toString() {
        return "ShapingEngine{typeface=" + getTypeface().toString()
                + ", typeSize=" + String.valueOf(getTypeSize())
                + ", scriptTag=" + Convert.toStringTag(getScriptTag())
                + ", languageTag=" + Convert.toStringTag(getLanguageTag())
                + ", writingDirection=" + getWritingDirection().toString()
                + ", shapingOrder=" + getShapingOrder().toString()
                + "}";
    }

    private static native int nativeGetScriptDefaultDirection(int scriptTag);

	private static native long nativeCreate();
	private static native void nativeDispose(long nativeEngine);

	private static native void nativeSetTypeface(long nativeEngine, Typeface typeface);

    private static native float nativeGetTypeSize(long nativeEngine);
    private static native void nativeSetTypeSize(long nativeEngine, float fontSize);

    private static native int nativeGetScriptTag(long nativeEngine);
	private static native void nativeSetScriptTag(long nativeEngine, int scriptTag);

    private static native int nativeGetLanguageTag(long nativeEngine);
    private static native void nativeSetLanguageTag(long nativeEngine, int languageTag);

    private static native int nativeGetWritingDirection(long nativeEngine);
	private static native void nativeSetWritingDirection(long nativeEngine, int shapingDirection);

    private static native int nativeGetShapingOrder(long nativeEngine);
    private static native void nativeSetShapingOrder(long nativeEngine, int shapingOrder);

	private static native void nativeShapeText(long nativeEngine, long nativeResult, String text, int fromIndex, int toIndex);
}
