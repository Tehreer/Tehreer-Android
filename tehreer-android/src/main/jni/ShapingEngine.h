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

#ifndef _TEHREER__SHAPING_ENGINE_H
#define _TEHREER__SHAPING_ENGINE_H

#include <cstdint>
#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingEngine.h>

#include "ShapingResult.h"

namespace Tehreer {

/**
 * Shapes text with the shaping engine of Core. The engine of Core has no getters, so the settings
 * are kept here too.
 */
class ShapingEngine {
public:
    static TRWritingDirection getScriptDefaultDirection(uint32_t scriptTag);

    ShapingEngine();
    ~ShapingEngine();

    void setTypeface(TRTypefaceRef typeface);

    jfloat typeSize() const { return m_typeSize; }
    void setTypeSize(jfloat typeSize);

    uint32_t scriptTag() const { return m_scriptTag; }
    void setScriptTag(uint32_t scriptTag);

    uint32_t languageTag() const { return m_languageTag; }
    void setLanguageTag(uint32_t languageTag);

    void setOpenTypeFeatures(const std::vector<uint32_t> &featureTags, const std::vector<uint16_t> &featureValues);

    TRShapingOrder shapingOrder() const { return m_shapingOrder; }
    void setShapingOrder(TRShapingOrder shapingOrder);

    TRWritingDirection writingDirection() const { return m_writingDirection; }
    void setWritingDirection(TRWritingDirection writingDirection);

    void shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd);

private:
    TRShapingEngineRef m_core;
    jfloat m_typeSize;
    uint32_t m_scriptTag;
    uint32_t m_languageTag;
    TRShapingOrder m_shapingOrder;
    TRWritingDirection m_writingDirection;
};

}

jint register_com_mta_tehreer_sfnt_ShapingEngine(JNIEnv *env);

#endif
