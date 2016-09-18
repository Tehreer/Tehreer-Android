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

#ifndef _TEHREER__OPEN_TYPE_ARTIST_H
#define _TEHREER__OPEN_TYPE_ARTIST_H

extern "C" {
#include <SFArtist.h>
#include <SFPattern.h>
#include <SFScheme.h>
}

#include <cstdint>
#include <jni.h>
#include <map>
#include <memory>

#include "TextDirection.h"
#include "Typeface.h"
#include "OpenTypeAlbum.h"

class OpenTypeScheme;

namespace Tehreer {

class OpenTypeArtist {
public:
    static TextDirection getScriptDefaultDirection(uint32_t scriptTag);

    OpenTypeArtist();
    ~OpenTypeArtist();

    const Typeface *typeface() const { return m_typeface; }
    void setTypeface(Typeface *typeface) { m_typeface = typeface; }

    uint32_t scriptTag() const { return m_scriptTag; }
    void setScriptTag(uint32_t scriptTag) { m_scriptTag = scriptTag; }

    uint32_t languageTag() const { return m_languageTag; }
    void setLanguageTag(uint32_t languageTag) { m_languageTag = languageTag; }

    void setText(const jchar *charArray, jint charCount);

    Range textRange() const { return m_textRange; };
    void setTextRange(Range textRange);

    TextDirection textDirection() const { return m_textDirection; }
    void setTextDirection(TextDirection textDirection) { m_textDirection = textDirection; }

    SFTextMode textMode() const { return m_textMode; }
    void setTextMode(SFTextMode textMode);
    
    void fillAlbum(OpenTypeAlbum &album);

private:
    SFArtistRef m_sfArtist;
    SFSchemeRef m_sfScheme;
    Typeface *m_typeface;
    uint32_t m_scriptTag;
    uint32_t m_languageTag;
    jchar *m_charArray;
    Range m_textRange;
    TextDirection m_textDirection;
    SFTextMode m_textMode;
};

}

jint register_com_mta_tehreer_opentype_OpenTypeArtist(JNIEnv *env);

#endif
