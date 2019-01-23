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
}

jint register_com_mta_tehreer_sfnt_tables_SfntTables(JNIEnv *env);

#endif
