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
#include <SBMirrorLocator.h>
#include <SBLine.h>
}

#include <jni.h>

#include "BidiBuffer.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "BidiMirrorLocator.h"

using namespace Tehreer;

static jlong create(JNIEnv *env, jobject obj)
{
    SBMirrorLocatorRef mirrorLocator = SBMirrorLocatorCreate();
    return reinterpret_cast<jlong>(mirrorLocator);
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(handle);
    SBMirrorLocatorRelease(mirrorLocator);
}

static void loadLine(JNIEnv *env, jobject obj, jlong locatorHandle, jlong lineHandle, jlong bufferHandle)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(locatorHandle);
    SBLineRef line = reinterpret_cast<SBLineRef>(lineHandle);
    BidiBuffer *buffer = reinterpret_cast<BidiBuffer *>(bufferHandle);

    SBMirrorLocatorLoadLine(mirrorLocator, line, buffer->data());
}

static jobject getNextPair(JNIEnv *env, jobject obj, jlong handle, jobject agentBuffer)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(handle);
    if (SBMirrorLocatorMoveNext(mirrorLocator)) {
        SBMirrorAgentRef mirrorAgent = SBMirrorLocatorGetAgent(mirrorLocator);
        JavaBridge bridge(env);

        return bridge.BidiPair_construct(mirrorAgent->index, mirrorAgent->mirror);
    }

    return nullptr;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeLoadLine", "(JJJ)V", (void *)loadLine },
    { "nativeGetNextPair", "(J)Lcom/mta/tehreer/bidi/BidiPair;", (void *)getNextPair },
};

jint register_com_mta_tehreer_bidi_BidiMirrorLocator(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/bidi/BidiMirrorLocator", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
