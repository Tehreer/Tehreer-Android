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

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.internal.util.Convert;
import com.mta.tehreer.text.TextDirection;
import com.mta.tehreer.text.TextMode;
import com.mta.tehreer.util.Disposable;

/**
 * The <code>OpenTypeArtist</code> class represents the text shaping engine.
 */
public class OpenTypeArtist implements Disposable {

    private static class Finalizable extends OpenTypeArtist {

        private Finalizable(OpenTypeArtist parent) {
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
     * Wraps an open type artist object into a finalizable instance which is guaranteed to be
     * disposed automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param openTypeArtist The open type artist object to wrap into a finalizable instance.
     * @return The finalizable instance of the passed-in open type artist object.
     */
    public static OpenTypeArtist finalizable(OpenTypeArtist openTypeArtist) {
        if (openTypeArtist.getClass() == OpenTypeArtist.class) {
            return new Finalizable(openTypeArtist);
        }

        if (openTypeArtist.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return openTypeArtist;
    }

    /**
     * Checks whether an open type artist object is finalizable or not.
     *
     * @param openTypeArtist The open type artist object to check.
     * @return <code>true</code> if the passed-in open type artist object is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(OpenTypeArtist openTypeArtist) {
        return (openTypeArtist.getClass() == Finalizable.class);
    }

    /**
     * Returns the default rendering direction of a script.
     *
     * @param scriptTag The tag of the script whose default direction is returned.
     * @return The default direction of the script identified by <code>scriptTag</code>.
     */
    public static TextDirection getScriptDefaultDirection(int scriptTag) {
        return Convert.toJavaTextDirection(nativeGetScriptDefaultDirection(scriptTag));
    }

    private static class Base {
        Typeface typeface;
        String text;
    }

    private final Base base;
    long nativeArtist;

    /**
     * Constructs an open type artist object.
     */
    public OpenTypeArtist() {
        base = new Base();
        nativeArtist = nativeCreate();
    }

    private OpenTypeArtist(OpenTypeArtist other) {
        this.base = other.base;
        this.nativeArtist = other.nativeArtist;
    }

	private void ensureTypeface() {
	    if (base.typeface == null) {
            throw new IllegalStateException("Typeface has not been set");
        }
	}

	private void ensureText() {
	    if (base.text == null) {
            throw new IllegalStateException("Text has not been set");
        }
	}

    /**
     * Returns this artist's current typeface.
     *
     * @return This artist's current typeface.
     */
	public Typeface getTypeface() {
		return base.typeface;
	}

    /**
     * Sets this artist's typeface for text shaping.
     *
     * @param typeface The typeface to use for text shaping.
     */
	public void setTypeface(Typeface typeface) {
        base.typeface = typeface;
		nativeSetTypeface(nativeArtist, typeface);
	}

    /**
     * Returns this artist's current script tag.
     *
     * @return This artist's current script tag.
     */
    public int getScriptTag() {
		return nativeGetScriptTag(nativeArtist);
	}

    /**
     * Sets this artist's script tag for text shaping. The default value is <code>'dflt'</code>.
     * <p>
     * A tag can be created from string by using {@link OpenTypeTag#make(String)} method.
     *
     * @param scriptTag The script tag to use for text shaping.
     */
	public void setScriptTag(int scriptTag) {
		nativeSetScriptTag(nativeArtist, scriptTag);
	}

    /**
     * Returns this artist's current language tag.
     *
     * @return This artist's current language tag.
     */
	public int getLanguageTag() {
		return nativeGetLanguageTag(nativeArtist);
	}

    /**
     * Sets this artist's language tag for text shaping. The default value is <code>'dflt'</code>.
     * <p>
     * A tag can be created from string by using {@link OpenTypeTag#make(String)} method.
     *
     * @param languageTag The language tag to use for text shaping.
     */
	public void setLanguageTag(int languageTag) {
		nativeSetLanguageTag(nativeArtist, languageTag);
	}

    /**
     * Returns this artist's current text.
     *
     * @return This artist's current text.
     */
    public String getText() {
        return base.text;
    }

    /**
     * Sets this artist's text to shape while resetting the range to occupy whole text.
     *
     * @param text The text to shape.
     */
    public void setText(String text) {
        base.text = text;
        nativeSetText(nativeArtist, text);
    }

    /**
     * Sets the input range of text to shape.
     * <p>
     * The use of this method is preferred over manually taking substrings of continuous text for
     * shaping.
     *
     * @param charStart The index to the first character in source text.
     * @param charEnd The index after the last character in source text.
     *
     * @throws IllegalStateException if current text of artist is null.
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than <code>getText().length()</code>, or
     *         <code>charStart</code> is greater than <code>charEnd</code>
     */
    public void setTextRange(int charStart, int charEnd) {
        ensureText();
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > base.text.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd
                                               + ", Text Length: " + base.text.length());
        }
        if (charStart > charEnd) {
            throw new IllegalArgumentException("Char Start: " + charStart
                                               + ", Char End: " + charEnd);
        }

        nativeSetTextRange(nativeArtist, charStart, charEnd);
    }

    /**
     * Returns this artist's current text direction.
     *
     * @return This artist's current text direction.
     */
	public TextDirection getTextDirection() {
		return Convert.toJavaTextDirection(nativeGetTextDirection(nativeArtist));
	}

    /**
     * Sets this artist's text direction for shaping.
     * <p>
     * The value of <code>textDirection</code> must reflect the rendering direction of source script
     * so that cursive and mark glyphs are placed at appropriate locations. It should not be
     * confused with the direction of a bidirectional run as that may not reflect the script
     * direction if overridden explicitly.
     *
     * @param textDirection The text direction to use for shaping.
     */
	public void setTextDirection(TextDirection textDirection) {
		nativeSetTextDirection(nativeArtist, Convert.toNativeTextDirection(textDirection));
	}

    /**
     * Returns this artist's current text mode.
     *
     * @return This artist's current text mode.
     */
	public TextMode getTextMode() {
		return Convert.toJavaTextMode(nativeGetTextMode(nativeArtist));
	}

    /**
     * Sets this artist's text mode for shaping.
     * <p>
     * This method provides a convenient way to shape a bidirectional run whose direction is
     * opposite to that of script. For example, if the direction of a run, 'car' is explicitly set
     * as right-to-left, backward mode will automatically read it as 'rac' without reordering the
     * original text.
     *
     * @param textMode The text mode to use for shaping.
     */
	public void setTextMode(TextMode textMode) {
		nativeSetTextMode(nativeArtist, Convert.toNativeTextMode(textMode));
	}

    /**
     * Shapes the specified range of source text with appropriate shaping engine, filling the album
     * with shaping results. The <code>album</code> is cleared first, if not empty.
     * <p>
     * The output glyphs in the album flow in logical text direction. For left-to-right direction,
     * the position of pen is incremented with glyph's advance after rendering it. Similarly, for
     * right-to-left direction, the position of pen is decremented with glyph's advance after
     * rendering it.
     *
     * @param album The album that should be filled with shaping results.
     *
     * @throws IllegalStateException if either artist's current typeface is <code>null</code>, or
     *         artist's current text is <code>null</code>.
     * @throws NullPointerException if <code>album</code> is <code>null</code>.
     */
	public void fillAlbum(OpenTypeAlbum album) {
		ensureTypeface();
		ensureText();
        if (album == null) {
            throw new NullPointerException("Album is null");
        }

		nativeFillAlbum(nativeArtist, album.nativeAlbum);
	}

	@Override
	public void dispose() {
        nativeDispose(nativeArtist);
    }

    @Override
    public String toString() {
        return "OpenTypeArtist{typeface=" + getTypeface().toString()
                + ", scriptTag=" + Convert.toStringTag(getScriptTag())
                + ", languageTag=" + Convert.toStringTag(getLanguageTag())
                + ", text=" + getText()
                + ", textRange=Range(" + nativeGetTextStart(nativeArtist)
                                + ", " + nativeGetTextEnd(nativeArtist) + ")"
                + ", textDirection=" + getTextDirection().toString()
                + ", textMode=" + getTextMode().toString()
                + "}";
    }

    private static native int nativeGetScriptDefaultDirection(int scriptTag);

	private static native long nativeCreate();
	private static native void nativeDispose(long nativeArtist);

	private static native void nativeSetTypeface(long nativeArtist, Typeface typeface);

    private static native int nativeGetScriptTag(long nativeArtist);
	private static native void nativeSetScriptTag(long nativeArtist, int scriptTag);

    private static native int nativeGetLanguageTag(long nativeArtist);
    private static native void nativeSetLanguageTag(long nativeArtist, int languageTag);

    private static native void nativeSetText(long nativeArtist, String text);

    private static native int nativeGetTextStart(long nativeArtist);
    private static native int nativeGetTextEnd(long nativeArtist);
    private static native void nativeSetTextRange(long nativeArtist, int charStart, int charEnd);

    private static native int nativeGetTextDirection(long nativeArtist);
	private static native void nativeSetTextDirection(long nativeArtist, int textDirection);

    private static native int nativeGetTextMode(long nativeArtist);
	private static native void nativeSetTextMode(long nativeArtist, int textMode);

	private static native void nativeFillAlbum(long nativeArtist, long nativeAlbum);
}
