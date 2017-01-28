/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

#ifndef _TEHREER__BIDI_BUFFER_H
#define _TEHREER__BIDI_BUFFER_H

#include <atomic>
#include <jni.h>

namespace Tehreer {

class BidiBuffer {
public:
    static BidiBuffer *create(const jchar *charArray, jsize charCount);

    jchar *data() const { return m_data; }
    jsize length() const { return m_length; }

    void retain();
    void release();

private:
    jchar *m_data;
    jsize m_length;
    std::atomic_int m_retainCount;
};

}

jint register_com_mta_tehreer_bidi_BidiBuffer(JNIEnv *env);

#endif
