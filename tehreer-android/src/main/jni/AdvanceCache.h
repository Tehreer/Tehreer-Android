/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

#ifndef _TEHREER__ADVANCE_CACHE_H
#define _TEHREER__ADVANCE_CACHE_H

#include <cstdint>
#include <unordered_map>

namespace Tehreer {

class AdvanceCache {
public:
    AdvanceCache();

    void put(const uint16_t key, int32_t advance);
    bool get(const uint16_t key, int32_t *advance);

private:
    std::unordered_map<uint16_t, int32_t> m_advances;
};

}

#endif
