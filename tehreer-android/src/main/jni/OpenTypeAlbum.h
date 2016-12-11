/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

#ifndef _TEHREER__OPEN_TYPE_ALBUM_H
#define _TEHREER__OPEN_TYPE_ALBUM_H

extern "C" {
#include <SFAlbum.h>
}

#include <jni.h>
#include <memory>

namespace Tehreer {

class OpenTypeAlbum {
public:
    OpenTypeAlbum();
    ~OpenTypeAlbum();

    SFAlbumRef sfAlbum() const { return m_sfAlbum; }

    void associateText(jint charStart, jint charEnd, bool isBackward);

    bool isBackward() const { return m_isBackward; }
    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }

    jint getCharGlyphIndex(jint charIndex) const;

    void copyGlyphInfos(jint fromIndex, jint toIndex, jfloat scaleFactor,
        jint *glyphIDBuffer, jfloat *xOffsetBuffer, jfloat *yOffsetBuffer, jfloat *advanceBuffer) const;
    void copyCharGlyphIndexes(jint fromIndex, jint toIndex, jint *glyphIndexBuffer) const;

private:
    SFAlbumRef m_sfAlbum;
    bool m_isBackward;
    jint m_charStart;
    jint m_charEnd;
};

}

jint register_com_mta_tehreer_opentype_OpenTypeAlbum(JNIEnv *env);

#endif
