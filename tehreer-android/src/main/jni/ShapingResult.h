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
#include <hb.h>
#include <jni.h>
#include <vector>

namespace Tehreer {

class ShapingResult {
public:
    ShapingResult();
    ~ShapingResult();

    hb_buffer_t *hbBuffer() const { return m_hbBuffer; }

    void setup(jfloat sizeByEm, bool isBackward, bool isRTL, jint charStart, jint charEnd);

    jfloat sizeByEm() const { return m_sizeByEm; }
    bool isBackward() const { return m_isBackward; }
    bool isRTL() const { return m_isRTL; }
    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }
    unsigned int glyphCount() const { return m_glyphCount; }

    hb_codepoint_t glyphIdAt(jint index) const { return m_glyphInfos[at(index)].codepoint; }
    uint32_t glyphClusterAt(jint index) const { return m_glyphInfos[at(index)].cluster; }

    jfloat glyphXOffsetAt(jint index) const { return m_glyphPositions[at(index)].x_offset * m_sizeByEm; }
    jfloat glyphYOffsetAt(jint index) const { return m_glyphPositions[at(index)].y_offset * m_sizeByEm; }
    jfloat glyphAdvanceAt(jint index) const { return m_glyphPositions[at(index)].x_advance * m_sizeByEm; }

    const jint *clusterMapPtr() const { return m_clusterMap.data(); }

private:
    hb_buffer_t *m_hbBuffer;
    hb_glyph_info_t *m_glyphInfos;
    hb_glyph_position_t *m_glyphPositions;
    unsigned int m_glyphCount;
    std::vector<jint> m_clusterMap;

    jfloat m_sizeByEm;
    bool m_isBackward;
    bool m_isRTL;
    jint m_charStart;
    jint m_charEnd;

    inline jint at(jint index) const {
        return m_isRTL ? m_glyphCount - index - 1 : index;
    }

    std::vector<jint> buildClusterMap() const;
};

}

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env);

#endif
