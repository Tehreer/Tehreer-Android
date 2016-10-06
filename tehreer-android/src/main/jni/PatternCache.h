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

#ifndef _TEHREER__PATTERN_CACHE_H
#define _TEHREER__PATTERN_CACHE_H

extern "C" {
#include <SFPattern.h>
}

#include <functional>
#include <memory>
#include <mutex>
#include <unordered_map>

namespace Tehreer {

struct PatternKey {
    uint32_t scriptTag;
    uint32_t languageTag;

    PatternKey()
    {
        scriptTag = 0;
        languageTag = 0;
    }

    PatternKey(uint32_t scriptTag, uint32_t languageTag)
    {
        this->scriptTag = scriptTag;
        this->languageTag = languageTag;
    }

    bool operator ==(const PatternKey &other) const
    {
        return scriptTag == other.scriptTag
            && languageTag == other.languageTag;
    }
};

class PatternCache {
public:
    PatternCache();

    void put(const PatternKey &key, SFPatternRef pattern);
    SFPatternRef get(const PatternKey &key);

private:
    typedef std::unique_ptr<_SFPattern, std::function<void (SFPatternRef)>> PatternValue;

    struct PatternHash {
        size_t operator()(const PatternKey &key) const
        {
            const size_t prime = 31;
            size_t result = 1;
            result = prime * result + std::hash<uint32_t>()(key.scriptTag);
            result = prime * result + std::hash<uint32_t>()(key.languageTag);

            return result;
        }
    };

    std::mutex m_mutex;
    std::unordered_map<PatternKey, PatternValue, PatternHash> m_patterns;
};

}

#endif
