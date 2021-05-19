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

extern "C" {
#include <ft2build.h>
#include FT_SIZES_H
#include FT_TYPES_H
}

#include <cmath>
#include <cstdint>
#include <hb.h>
#include <hb-ot.h>
#include <jni.h>
#include <vector>

#include "JavaBridge.h"
#include "ShapingEngine.h"

using namespace std;
using namespace Tehreer;

WritingDirection ShapingEngine::getScriptDefaultDirection(uint32_t scriptTag)
{
    hb_script_t script = hb_ot_tag_to_script(scriptTag);
    hb_direction_t direction = hb_script_get_horizontal_direction(script);

    if (direction == HB_DIRECTION_RTL) {
        return WritingDirection::RIGHT_TO_LEFT;
    }

    return WritingDirection::LEFT_TO_RIGHT;
}

ShapingEngine::ShapingEngine()
    : m_typeface(nullptr)
    , m_typeSize(16.0)
    , m_scriptTag(FT_MAKE_TAG('D', 'F', 'L', 'T'))
    , m_languageTag(FT_MAKE_TAG('d', 'f', 'l', 't'))
    , m_shapingOrder(ShapingOrder::FORWARD)
    , m_writingDirection(WritingDirection::LEFT_TO_RIGHT)
{
}

ShapingEngine::~ShapingEngine()
{
}

void ShapingEngine::setOpenTypeFeatures(const vector<uint32_t> &featureTags, const vector<uint16_t> &featureValues)
{
    m_featureTags = featureTags;
    m_featureValues = featureValues;
}

void ShapingEngine::setShapingOrder(ShapingOrder shapingOrder)
{
    m_shapingOrder = shapingOrder;
}

void ShapingEngine::setWritingDirection(WritingDirection writingDirection)
{
    m_writingDirection = writingDirection;
}

bool ShapingEngine::isRTL()
{
    if (m_shapingOrder == ShapingOrder::BACKWARD) {
        return m_writingDirection != WritingDirection::RIGHT_TO_LEFT;
    }

    return m_writingDirection == WritingDirection::RIGHT_TO_LEFT;
}

void ShapingEngine::shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd)
{
    hb_script_t script = hb_ot_tag_to_script(m_scriptTag);
    hb_language_t language = hb_ot_tag_to_language(m_languageTag);
    hb_direction_t direction;

    if (m_writingDirection == WritingDirection::RIGHT_TO_LEFT) {
        direction = HB_DIRECTION_RTL;
    } else {
        direction = HB_DIRECTION_LTR;
    }

    hb_buffer_t *buffer = shapingResult.hbBuffer();
    hb_buffer_clear_contents(buffer);
    hb_buffer_set_script(buffer, script);
    hb_buffer_set_language(buffer, language);
    hb_buffer_set_direction(buffer, direction);

    const jchar *codeUnits = charArray + charStart;
    jint length = charEnd - charStart;

    hb_buffer_add_utf16(buffer, codeUnits, length, 0, length);

    size_t numFeatures = m_featureTags.size();
    hb_feature_t features[numFeatures];

    for (size_t i = 0; i < m_featureTags.size(); i++) {
        features[i].tag = m_featureTags[i];
        features[i].value = m_featureValues[i];
        features[i].start = 0;
        features[i].end = length;
    }

    hb_font_t *hbFont = hb_font_create_sub_font(m_typeface->hbFont());
    auto ppem = lround(m_typeSize);
    hb_font_set_ppem(hbFont, ppem, ppem);

    hb_shape(hbFont, shapingResult.hbBuffer(), features, numFeatures);

    hb_font_destroy(hbFont);

    jfloat sizeByEm = m_typeSize / m_typeface->unitsPerEM();
    bool isBackward = m_shapingOrder == ShapingOrder::BACKWARD;

    shapingResult.setup(sizeByEm, isBackward, isRTL(), charStart, charEnd);
}

static jint getScriptDefaultDirection(JNIEnv *env, jobject obj, jint scriptTag)
{
    auto inputTag = static_cast<uint32_t>(scriptTag);
    WritingDirection defaultDirection = ShapingEngine::getScriptDefaultDirection(inputTag);

    return static_cast<jint>(defaultDirection);
}

static jlong create(JNIEnv *env, jobject obj)
{
    auto shapingEngine = new ShapingEngine();
    return reinterpret_cast<jlong>(shapingEngine);
}

static void dispose(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    delete shapingEngine;
}

static void setTypeface(JNIEnv *env, jobject obj, jlong engineHandle, jobject jtypeface)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    Typeface *typeface = nullptr;

    if (jtypeface) {
        jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
        typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    }

    shapingEngine->setTypeface(typeface);
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    return shapingEngine->typeSize();
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong engineHandle, jfloat typeSize)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    shapingEngine->setTypeSize(typeSize);
}

static jint getScriptTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t scriptTag = shapingEngine->scriptTag();

    return static_cast<jint>(scriptTag);
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong engineHandle, jint scriptTag)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto inputTag = static_cast<uint32_t>(scriptTag);

    shapingEngine->setScriptTag(inputTag);
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t languageTag = shapingEngine->languageTag();

    return static_cast<jint>(languageTag);
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle, jint languageTag)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto inputTag = static_cast<uint32_t>(languageTag);

    shapingEngine->setLanguageTag(inputTag);
}

static void setOpenTypeFeatures(JNIEnv *env, jobject obj, jlong engineHandle, jintArray tagsArray, jshortArray valuesArray)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);

    void *rawTags = env->GetPrimitiveArrayCritical(tagsArray, nullptr);
    void *rawValues = env->GetPrimitiveArrayCritical(valuesArray, nullptr);

    auto actualTags = static_cast<uint32_t *>(rawTags);
    auto actualValues = static_cast<uint16_t *>(rawValues);
    jint featureCount = env->GetArrayLength(tagsArray);

    const vector<uint32_t> featureTags(actualTags, actualTags + featureCount);
    const vector<uint16_t> featureValues(actualValues, actualValues + featureCount);

    shapingEngine->setOpenTypeFeatures(featureTags, featureValues);

    env->ReleasePrimitiveArrayCritical(tagsArray, rawTags, 0);
    env->ReleasePrimitiveArrayCritical(valuesArray, rawValues, 0);
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    WritingDirection writingDirection = shapingEngine->writingDirection();

    return static_cast<jint>(writingDirection);
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle, jint writingDirection)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto layoutDirection = static_cast<WritingDirection>(writingDirection);

    shapingEngine->setWritingDirection(layoutDirection);
}

static jint getShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    ShapingOrder shapingOrder = shapingEngine->shapingOrder();

    return static_cast<jint>(shapingOrder);
}

static void setShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingOrder)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto memoryOrder = static_cast<ShapingOrder>(shapingOrder);

    shapingEngine->setShapingOrder(memoryOrder);
}

static void shapeText(JNIEnv *env, jobject obj, jlong engineHandle, jlong resultHandle, jstring text, jint fromIndex, jint toIndex)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);

    const jchar *charArray = env->GetStringChars(text, nullptr);

    shapingEngine->shapeText(*shapingResult, charArray, fromIndex, toIndex);

    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nSetTypeface", "(JLcom/mta/tehreer/graphics/Typeface;)V", (void *)setTypeface },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nSetOpenTypeFeatures", "(J[I[S)V", (void *)setOpenTypeFeatures },
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
