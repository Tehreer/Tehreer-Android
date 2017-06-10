/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

extern "C" {
#include <SFAlbum.h>
}

#include <jni.h>
#include <memory>

namespace Tehreer {

class ShapingResult {
public:
    ShapingResult();
    ~ShapingResult();

    SFAlbumRef sfAlbum() const { return m_sfAlbum; }

    void setAdditionalInfo(jfloat sizeByEm, bool isBackward, jint charStart, jint charEnd);
    void sanitizeClusterMap();

    jfloat sizeByEm() const { return m_sizeByEm; }
    bool isBackward() const { return m_isBackward; }
    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }

private:
    SFAlbumRef m_sfAlbum;
    jfloat m_sizeByEm;
    bool m_isBackward;
    jint m_charStart;
    jint m_charEnd;
};

}

jint register_com_mta_tehreer_opentype_ShapingResult(JNIEnv *env);

#endif
