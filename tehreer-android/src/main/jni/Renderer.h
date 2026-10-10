/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

#ifndef _TEHREER__RENDERER_H
#define _TEHREER__RENDERER_H

#include <jni.h>

#include <Tehreer/TRDrawCallbacks.h>
#include <Tehreer/TRRenderer.h>

#include "JavaBridge.h"

namespace Tehreer {

/**
 * Lets the draw functions of Core paint onto a canvas while it lives. It sets the draw callbacks of
 * the renderer, and clears them at the end. The drawer is an optional object of Java that draws the
 * replacements, which Core only positions.
 */
class Drawing {
public:
    Drawing(JNIEnv *env, TRRendererRef renderer, jobject canvas, jobject paint, jobject drawer);
    ~Drawing();

    Drawing(const Drawing &) = delete;
    Drawing &operator=(const Drawing &) = delete;

    JNIEnv *env;
    jobject canvas;
    jobject paint;
    jobject drawer;
    jmethodID drawReplacement;

    /* Sets the color of the paint, unless it already has it. */
    void setColor(const JavaBridge &bridge, TRColor color);

private:
    TRRendererRef m_renderer;
    bool m_hasColor;
    TRColor m_color;
};

}

jint register_com_mta_tehreer_graphics_Renderer(JNIEnv *env);

#endif
