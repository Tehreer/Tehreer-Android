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

#ifndef _TEHREER__SHAPING_RESULT_H
#define _TEHREER__SHAPING_RESULT_H

#include <cstdint>
#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingResult.h>

namespace Tehreer {

/**
 * Holds the result of Core, which is replaced each time that text is shaped into it. The glyphs of
 * Core are already in the order of the writing direction, and in the unit of the type size.
 */
class ShapingResult {
public:
    ShapingResult();
    ~ShapingResult();

    void setup(TRShapingResultRef core, jint charStart, jint charEnd);

    bool isBackward() const { return m_core && TRShapingResultIsBackward(m_core); }
    bool isRTL() const { return m_core && TRShapingResultIsRTL(m_core); }
    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }
    unsigned int glyphCount() const { return m_glyphCount; }

    jint glyphIdAt(jint index) const { return m_glyphIds[index]; }
    jfloat glyphXOffsetAt(jint index) const { return m_glyphOffsets[index].x; }
    jfloat glyphYOffsetAt(jint index) const { return m_glyphOffsets[index].y; }
    jfloat glyphAdvanceAt(jint index) const { return m_glyphAdvances[index]; }

    /* The cluster map of Core has an element for each code unit as large as a pointer, while the
     * Java side reads 32-bit values. */
    const jint *clusterMapPtr() const { return m_clusterMap.data(); }

    void getCaretEdges(const TRBoolean *caretStops, jfloat *caretEdges) const;
    void copyGlyphIds(jint offset, jint length, jint *destination) const;
    void copyGlyphOffsets(jint offset, jint length, jfloat *destination) const;
    void copyGlyphAdvances(jint offset, jint length, jfloat *destination) const;

private:
    TRShapingResultRef m_core;
    const TRGlyphID *m_glyphIds;
    const TRPoint *m_glyphOffsets;
    const TRFloat *m_glyphAdvances;
    unsigned int m_glyphCount;
    std::vector<jint> m_clusterMap;

    jint m_charStart;
    jint m_charEnd;
};

}

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env);

#endif
