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

#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <jni.h>

#include "StreamUtils.h"

using namespace Tehreer;

void *StreamUtils::toRawBuffer(const JavaBridge &bridge, jobject stream, jint *length)
{
    JNIEnv *env = bridge.env();

    const jint chunkLength = 4096;
    jbyteArray chunkArray = env->NewByteArray(chunkLength);

    jint bufferCapacity = chunkLength;
    void *streamBuffer = malloc(bufferCapacity);

    jint bufferOffset = 0;
    *length = 0;

    jint bytesRead;
    while ((bytesRead = bridge.InputStream_read(stream, chunkArray, 0, chunkLength)) > 0) {
        jint newLength = *length + bytesRead;
        if (newLength > bufferCapacity) {
            bufferCapacity = bufferCapacity * 2;
            if (bufferCapacity < newLength) {
                bufferCapacity = newLength;
            }

            streamBuffer = realloc(streamBuffer, bufferCapacity);
        }

        void *chunkData = env->GetPrimitiveArrayCritical(chunkArray, nullptr);
        memcpy((uint8_t *)streamBuffer + bufferOffset, chunkData, bytesRead);
        env->ReleasePrimitiveArrayCritical(chunkArray, chunkData, 0);

        bufferOffset += bytesRead;
        *length = newLength;
    }

    return realloc(streamBuffer, *length);
}
