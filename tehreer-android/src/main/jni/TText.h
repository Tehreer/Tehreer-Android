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

#ifndef _TEHREER__TTEXT_H
#define _TEHREER__TTEXT_H

#include <jni.h>

#include <Tehreer/TRText.h>

/* The native handle of a text is the text object of Core itself. */
static inline TRTextRef toText(jlong handle)
{
    return reinterpret_cast<TRTextRef>(handle);
}

static inline TRMutableTextRef toMutableText(jlong handle)
{
    return reinterpret_cast<TRMutableTextRef>(handle);
}

jint register_com_mta_tehreer_layout_TText(JNIEnv *env);

#endif
