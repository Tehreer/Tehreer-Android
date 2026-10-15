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

#ifndef _TEHREER__GLYPH_IMAGE_H
#define _TEHREER__GLYPH_IMAGE_H

#include <jni.h>

#include <Tehreer/TRGlyphImage.h>

static inline TRGlyphImageRef toGlyphImage(jlong handle)
{
    return reinterpret_cast<TRGlyphImageRef>(handle);
}

/**
 * Returns a local reference to the bitmap of an image. The bitmap is made when it is asked for
 * first, and is kept with the image as its native data, so every use of the image gets the same
 * one. It is null if the image has no pixels.
 */
jobject getGlyphImageBitmap(JNIEnv *env, TRGlyphImageRef image);

jint register_com_mta_tehreer_graphics_GlyphImage(JNIEnv *env);

#endif
