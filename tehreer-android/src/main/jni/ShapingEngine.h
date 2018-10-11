/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

extern "C" {
#include <SFArtist.h>
#include <SFBase.h>
#include <SFScheme.h>
}

#include <cstdint>
#include <jni.h>
#include <map>
#include <memory>

#include "Typeface.h"
#include "ShapingResult.h"

class OpenTypeScheme;

namespace Tehreer {

enum ShapingOrder : uint32_t {
    FORWARD = SFTextModeForward,
    BACKWARD = SFTextModeBackward,
};

enum WritingDirection : uint32_t {
    LEFT_TO_RIGHT = SFTextDirectionLeftToRight,
    RIGHT_TO_LEFT = SFTextDirectionRightToLeft,
};

class ShapingEngine {
public:
    static WritingDirection getScriptDefaultDirection(uint32_t scriptTag);

    ShapingEngine();
    ~ShapingEngine();

    const Typeface *typeface() const { return m_typeface; }
    void setTypeface(Typeface *typeface) { m_typeface = typeface; }

    jfloat typeSize() const { return m_typeSize; }
    void setTypeSize(jfloat typeSize) { m_typeSize = typeSize; }

    SFTag scriptTag() const { return m_scriptTag; }
    void setScriptTag(SFTag scriptTag) { m_scriptTag = scriptTag; }

    SFTag languageTag() const { return m_languageTag; }
    void setLanguageTag(SFTag languageTag) { m_languageTag = languageTag; }

    ShapingOrder shapingOrder() const { return m_shapingOrder; }
    void setShapingOrder(ShapingOrder shapingOrder);

    WritingDirection writingDirection() const { return m_writingDirection; }
    void setWritingDirection(WritingDirection writingDirection);

    void shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd);

private:
    SFArtistRef m_sfArtist;
    SFSchemeRef m_sfScheme;
    Typeface *m_typeface;
    jfloat m_typeSize;
    SFTag m_scriptTag;
    SFTag m_languageTag;
    ShapingOrder m_shapingOrder;
    WritingDirection m_writingDirection;
};

}

jint register_com_mta_tehreer_sfnt_ShapingEngine(JNIEnv *env);

#endif
