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

#ifndef _TEHREER__SHAPING_RESULT_H
#define _TEHREER__SHAPING_RESULT_H

#include <jni.h>

#include <Tehreer/TRShapingResult.h>

namespace Tehreer {

/**
 * Holds the result of Core, which is replaced each time that text is shaped into it, with the range
 * of the text that was shaped. The glyphs of Core are already in the order of the writing
 * direction, and in the unit of the type size.
 */
class ShapingResult {
public:
    ShapingResult();
    ~ShapingResult();

    void setup(TRShapingResultRef core, jint charStart, jint charEnd);

    TRShapingResultRef core() const { return m_core; }
    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }

private:
    TRShapingResultRef m_core;
    jint m_charStart;
    jint m_charEnd;
};

}

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env);

#endif
