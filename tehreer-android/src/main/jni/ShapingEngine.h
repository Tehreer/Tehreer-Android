/*
 * Copyright (C) 2016-2026 Muhammad Tayyab Akram
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

#ifndef _TEHREER__SHAPING_ENGINE_H
#define _TEHREER__SHAPING_ENGINE_H

#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingEngine.h>

namespace Tehreer {

/** Reads the settings of the OpenType features from the arrays of their tags and their values. */
inline std::vector<TROpenTypeFeature> readOpenTypeFeatures(JNIEnv *env, jintArray tags, jintArray values)
{
    jsize count = env->GetArrayLength(tags);
    std::vector<jint> tagItems(count);
    std::vector<jint> valueItems(count);
    std::vector<TROpenTypeFeature> features(count);

    env->GetIntArrayRegion(tags, 0, count, tagItems.data());
    env->GetIntArrayRegion(values, 0, count, valueItems.data());

    for (jsize i = 0; i < count; i++) {
        features[i].tag = static_cast<TRTag>(tagItems[i]);
        features[i].value = static_cast<TRUInt32>(valueItems[i]);
    }

    return features;
}

}

jint register_com_mta_tehreer_sfnt_ShapingEngine(JNIEnv *env);

#endif
