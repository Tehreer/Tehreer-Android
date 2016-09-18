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

extern "C" {
#include <SFPattern.h>
}

#include "PatternCache.h"

using namespace Tehreer;

PatternCache::PatternCache()
{
}

void PatternCache::put(const PatternKey &key, SFPatternRef pattern)
{
    m_mutex.lock();

    m_patterns[key] = PatternValue(SFPatternRetain(pattern), &SFPatternRelease);

    m_mutex.unlock();
}

SFPatternRef PatternCache::get(const PatternKey &key)
{
    SFPatternRef pattern = nullptr;

    m_mutex.lock();

    auto pair = m_patterns.find(key);
    if (pair != m_patterns.end()) {
        pattern = pair->second.get();
    }

    m_mutex.unlock();

    return pattern;
}
