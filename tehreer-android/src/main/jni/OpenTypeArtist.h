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
#include <SFBase.h>
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

    SFTag scriptTag() const { return m_scriptTag; }
    void setScriptTag(SFTag scriptTag) { m_scriptTag = scriptTag; }

    SFTag languageTag() const { return m_languageTag; }
    void setLanguageTag(SFTag languageTag) { m_languageTag = languageTag; }

    void setText(const jchar *charArray, jint charCount);

    jint charStart() const { return m_charStart; }
    jint charEnd() const { return m_charEnd; }
    void setTextRange(jint charStart, jint charEnd);

    TextDirection textDirection() const { return m_textDirection; }
    void setTextDirection(TextDirection textDirection) { m_textDirection = textDirection; }

    SFTextMode textMode() const { return m_textMode; }
    void setTextMode(SFTextMode textMode);

    void fillAlbum(OpenTypeAlbum &album);

private:
    SFArtistRef m_sfArtist;
    SFSchemeRef m_sfScheme;
    Typeface *m_typeface;
    SFTag m_scriptTag;
    SFTag m_languageTag;
    jchar *m_charArray;
    jint m_charStart;
    jint m_charEnd;
    TextDirection m_textDirection;
    SFTextMode m_textMode;
};

}

jint register_com_mta_tehreer_opentype_OpenTypeArtist(JNIEnv *env);

#endif