/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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

package com.mta.tehreer.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.widget.TLabel;

public class LinkableLabel extends TLabel {
    private Paint mPaint = new Paint();

    private URLSpan mActiveLinkSpan;
    private Path mActiveLinkPath;

    public LinkableLabel(Context context) {
        super(context);
        setup();
    }

    public LinkableLabel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public LinkableLabel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFFCCCCEE);
    }

    private URLSpan urlSpanAtIndex(int charIndex) {
        Spanned spanned = getSpanned();
        ComposedFrame composedFrame = getComposedFrame();

        if (spanned == null || composedFrame == null) {
            return null;
        }
        if (charIndex < composedFrame.getCharStart() || charIndex >= composedFrame.getCharEnd()) {
            return null;
        }

        URLSpan[] spans = spanned.getSpans(charIndex, charIndex, URLSpan.class);
        if (spans != null && spans.length > 0) {
            return spans[0];
        }

        return null;
    }

    private URLSpan urlSpanAtPoint(float x, float y) {
        return urlSpanAtIndex(hitTestPosition(x, y));
    }

    private void refreshActiveLink() {
        Spanned spanned = getSpanned();
        ComposedFrame composedFrame = getComposedFrame();

        if (spanned != null && composedFrame != null && mActiveLinkSpan != null) {
            int spanStart = spanned.getSpanStart(mActiveLinkSpan);
            int spanEnd = spanned.getSpanEnd(mActiveLinkSpan);

            mActiveLinkPath = composedFrame.generateSelectionPath(spanStart, spanEnd);
            mActiveLinkPath.offset(composedFrame.getOriginX(), composedFrame.getOriginY());
        } else {
            mActiveLinkPath = null;
        }

        invalidate();
    }

    private void clearActiveLink() {
        mActiveLinkSpan = null;
        refreshActiveLink();
    }

    private void openActiveLink() {
        Uri uri = Uri.parse(mActiveLinkSpan.getURL());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Context context = getContext();
        context.startActivity(intent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mActiveLinkPath != null) {
            canvas.drawPath(mActiveLinkPath, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mActiveLinkSpan = urlSpanAtPoint(event.getX(), event.getY());
            if (mActiveLinkSpan != null) {
                refreshActiveLink();
                return true;
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if (mActiveLinkSpan != null) {
                URLSpan pointedLinkSpan = urlSpanAtPoint(event.getX(), event.getY());
                if (pointedLinkSpan != mActiveLinkSpan) {
                    clearActiveLink();
                }

                return true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mActiveLinkSpan != null) {
                performClick();
                openActiveLink();
                clearActiveLink();
                return true;
            }
            break;

        case MotionEvent.ACTION_CANCEL:
            if (mActiveLinkSpan != null) {
                clearActiveLink();
                return true;
            }
            break;
        }

        return super.onTouchEvent(event);
    }
}
