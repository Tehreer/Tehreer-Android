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

#include "Range.h"

namespace Tehreer {

class OpenTypeAlbum {
public:
    OpenTypeAlbum();
    ~OpenTypeAlbum();

    SFAlbumRef SFAlbum() const { return m_sfAlbum; }

    void associateText(Range textRange, bool isBackward);

    bool isBackward() const { return m_isBackward; }
    Range textRange() const { return m_textRange; }

    jint glyphCount() const { return SFAlbumGetGlyphCount(m_sfAlbum); }
    jint glyphIDAt(jint glyphIndex) const { return SFAlbumGetGlyphIDsPtr(m_sfAlbum)[glyphIndex]; }
    jint glyphXOffsetAt(jint glyphIndex) const { return SFAlbumGetGlyphOffsetsPtr(m_sfAlbum)[glyphIndex].x; }
    jint glyphYOffsetAt(jint glyphIndex) const { return SFAlbumGetGlyphOffsetsPtr(m_sfAlbum)[glyphIndex].y; }
    jint glyphAdvanceAt(jint glyphIndex) const { return SFAlbumGetGlyphAdvancesPtr(m_sfAlbum)[glyphIndex]; }
    jint getCharGlyphIndex(jint charIndex) const;

    void copyGlyphInfos(Range glyphRange, jfloat sizeScale,
            jint *glyphIDBuffer, jfloat *xOffsetBuffer, jfloat *yOffsetBuffer, jfloat *advanceBuffer) const;
    void copyCharGlyphIndexes(Range stringRange, jint *glyphIndexBuffer) const;

private:
    SFAlbumRef m_sfAlbum;
    Range m_textRange;
    bool m_isBackward;
};

}

jint register_com_mta_tehreer_opentype_OpenTypeAlbum(JNIEnv *env);

#endif
