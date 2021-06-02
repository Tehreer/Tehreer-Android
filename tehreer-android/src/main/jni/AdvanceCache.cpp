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

#include <cstdint>

#include "AdvanceCache.h"

using namespace Tehreer;

AdvanceCache::AdvanceCache() = default;

void AdvanceCache::put(const uint16_t key, int32_t advance)
{
    m_advances[key] = advance;
}

bool AdvanceCache::get(const uint16_t key, int32_t *advance)
{
    bool found = false;

    auto pair = m_advances.find(key);
    if (pair != m_advances.end()) {
        *advance = pair->second;
        found = true;
    }

    return found;
}
