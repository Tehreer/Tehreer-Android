/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

#ifndef _TEHREER__LAYOUT_HANDLES_H
#define _TEHREER__LAYOUT_HANDLES_H

#include <jni.h>

#include <Tehreer/TRBase.h>
#include <Tehreer/TRComposedFrame.h>
#include <Tehreer/TRComposedLine.h>
#include <Tehreer/TRGlyphRun.h>
#include <Tehreer/TRRenderer.h>
#include <Tehreer/TRTypesetter.h>

/* The native handle of each layout object is the object of Core itself. */

static inline TRTypesetterRef toTypesetter(jlong handle)
{
    return reinterpret_cast<TRTypesetterRef>(handle);
}

static inline TRComposedFrameRef toFrame(jlong handle)
{
    return reinterpret_cast<TRComposedFrameRef>(handle);
}

static inline TRComposedLineRef toLine(jlong handle)
{
    return reinterpret_cast<TRComposedLineRef>(handle);
}

static inline TRGlyphRunRef toRun(jlong handle)
{
    return reinterpret_cast<TRGlyphRunRef>(handle);
}

static inline TRRendererRef toRenderer(jlong handle)
{
    return reinterpret_cast<TRRendererRef>(handle);
}

static inline TRRange makeRange(jint start, jint end)
{
    TRRange range;
    range.index = static_cast<TRUInteger>(start);
    range.length = static_cast<TRUInteger>(end - start);

    return range;
}

#endif
