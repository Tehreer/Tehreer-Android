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

#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingEngine.h>

#include "JavaBridge.h"
#include "Typeface.h"
#include "ShapingEngine.h"
#include "ShapingResult.h"

using namespace std;
using namespace Tehreer;

static TRShapingEngineRef toEngine(jlong handle)
{
    return reinterpret_cast<TRShapingEngineRef>(handle);
}

static jint getScriptDefaultDirection(JNIEnv *env, jclass clazz, jint scriptTag)
{
    return static_cast<jint>(TRShapingEngineGetScriptDefaultDirection(static_cast<TRTag>(scriptTag)));
}

static jint makeLanguageTag(JNIEnv *env, jclass clazz, jstring name)
{
    const char *chars = name ? env->GetStringUTFChars(name, nullptr) : nullptr;
    TRTag tag = TRShapingEngineGetLanguageTagForName(chars);

    if (chars) {
        env->ReleaseStringUTFChars(name, chars);
    }

    return static_cast<jint>(tag);
}

static jlong create(JNIEnv *env, jclass clazz)
{
    return reinterpret_cast<jlong>(TRShapingEngineCreate());
}

static void dispose(JNIEnv *env, jclass clazz, jlong engineHandle)
{
    TRShapingEngineRelease(toEngine(engineHandle));
}

static void setTypeface(JNIEnv *env, jobject obj, jlong engineHandle, jlong typefaceHandle)
{
    TRShapingEngineSetTypeface(toEngine(engineHandle), toTypeface(typefaceHandle));
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong engineHandle)
{
    return TRShapingEngineGetTypeSize(toEngine(engineHandle));
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong engineHandle, jfloat typeSize)
{
    TRShapingEngineSetTypeSize(toEngine(engineHandle), typeSize);
}

static jint getScriptTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    return static_cast<jint>(TRShapingEngineGetScriptTag(toEngine(engineHandle)));
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong engineHandle, jint scriptTag)
{
    TRShapingEngineSetScriptTag(toEngine(engineHandle), static_cast<TRTag>(scriptTag));
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    return static_cast<jint>(TRShapingEngineGetLanguageTag(toEngine(engineHandle)));
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle, jint languageTag)
{
    TRShapingEngineSetLanguageTag(toEngine(engineHandle), static_cast<TRTag>(languageTag));
}

static void setOpenTypeFeatures(JNIEnv *env, jobject obj, jlong engineHandle, jintArray tags,
    jintArray values)
{
    vector<TROpenTypeFeature> features = readOpenTypeFeatures(env, tags, values);

    TRShapingEngineSetOpenTypeFeatures(toEngine(engineHandle), features.data(), features.size());
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle)
{
    return static_cast<jint>(TRShapingEngineGetWritingDirection(toEngine(engineHandle)));
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle, jint writingDirection)
{
    TRShapingEngineSetWritingDirection(toEngine(engineHandle),
        static_cast<TRWritingDirection>(writingDirection));
}

static jint getShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle)
{
    return static_cast<jint>(TRShapingEngineGetShapingOrder(toEngine(engineHandle)));
}

static void setShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingOrder)
{
    TRShapingEngineSetShapingOrder(toEngine(engineHandle), static_cast<TRShapingOrder>(shapingOrder));
}

static void shapeText(JNIEnv *env, jobject obj, jlong engineHandle, jlong resultHandle, jstring text, jint fromIndex, jint toIndex)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    const jchar *charArray = env->GetStringChars(text, nullptr);

    TRShapingResultRef core = TRShapingEngineCreateShapingResult(toEngine(engineHandle),
        charArray + fromIndex, static_cast<TRUInteger>(toIndex - fromIndex), TRStringEncodingUTF16);
    shapingResult->setup(core, fromIndex, toIndex);

    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nSetTypeface", "(JJ)V", (void *)setTypeface },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nSetOpenTypeFeatures", "(J[I[I)V", (void *)setOpenTypeFeatures },
    { "nMakeLanguageTag", "(Ljava/lang/String;)I", (void *)makeLanguageTag },
    { "nGetWritingDirection", "(J)I", (void *)getWritingDirection },
    { "nSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nGetShapingOrder", "(J)I", (void *)getShapingOrder },
    { "nSetShapingOrder", "(JI)V", (void *)setShapingOrder },
    { "nShapeText", "(JJLjava/lang/String;II)V", (void *)shapeText },
};

jint register_com_mta_tehreer_sfnt_ShapingEngine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingEngine", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
