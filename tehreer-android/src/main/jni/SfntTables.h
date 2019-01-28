/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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

#ifndef _TEHREER__SFNT_TABLES_H
#define _TEHREER__SFNT_TABLES_H

#include <cstdint>
#include <jni.h>
#include <string>
#include <vector>

namespace Tehreer {
namespace SFNT {

namespace head {

enum MacStyle : uint16_t {
    BOLD = 1 << 0,
    ITALIC = 1 << 1,
    CONDENSED = 1 << 5,
    EXTENDED = 1 << 6,
};

}

namespace name {

enum PlatformID : uint16_t {
    MACINTOSH = 1,
    WINDOWS = 3,
};

enum NameID : uint16_t {
    FONT_FAMILY = 1,
    FONT_SUBFAMILY = 2,
    FULL = 4,
    TYPOGRAPHIC_FAMILY = 16,
    TYPOGRAPHIC_SUBFAMILY = 17,
    WWS_FAMILY = 21,
    WWS_SUBFAMILY = 22,
};

class Locale {
public:
    Locale(uint16_t platformID, uint16_t languageID);

    const std::string *language() const { return (m_values->size() > 0 ? &m_values->at(0) : nullptr); }
    const std::string *region() const { return (m_values->size() > 1 ? &m_values->at(1) : nullptr); }
    const std::string *script() const { return (m_values->size() > 2 ? &m_values->at(2) : nullptr); }
    const std::string *variant() const { return (m_values->size() > 3 ? &m_values->at(3) : nullptr); }

private:
    const std::vector<std::string> *m_values;
};

class Encoding {
public:
    Encoding(uint16_t platformID, uint16_t encodingID);

    const char *name() const { return (m_name.length() > 0 ? m_name.c_str() : nullptr); }

private:
    std::string m_name;
};

}

namespace OS2 {

enum FSSelection : uint16_t {
    ITALIC = 1 << 0,
    WWS = 1 << 8,
    OBLIQUE = 1 << 9,
};

}

}
}

jint register_com_mta_tehreer_sfnt_tables_SfntTables(JNIEnv *env);

#endif
