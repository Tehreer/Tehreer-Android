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


#include <jni.h>
#include <vector>

#include <Tehreer/TRBase.h>
#include <Tehreer/TRTypeface.h>
#include <Tehreer/TRTypefaceManager.h>

#include "JavaBridge.h"
#include "Typeface.h"
#include "TypefaceManager.h"

using namespace std;
using namespace Tehreer;

static jboolean registerTypeface(JNIEnv *env, jclass clazz, jlong typefaceHandle, jint tag,
    jint familyId)
{
    return TRTypefaceManagerRegisterTypeface(toTypeface(typefaceHandle),
                                             static_cast<TRUInteger>(tag),
                                             static_cast<TRUInteger>(familyId))
         ? JNI_TRUE : JNI_FALSE;
}

static jboolean unregisterTypeface(JNIEnv *env, jclass clazz, jlong typefaceHandle)
{
    return TRTypefaceManagerUnregisterTypeface(toTypeface(typefaceHandle)) ? JNI_TRUE : JNI_FALSE;
}

/* The typefaces that the manager gives are not retained, and the wrapper keeps those it registered. */
static jlong getTypeface(JNIEnv *env, jclass clazz, jint tag)
{
    return reinterpret_cast<jlong>(TRTypefaceManagerGetTypeface(static_cast<TRUInteger>(tag)));
}

static jint getTypefaceTag(JNIEnv *env, jclass clazz, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceManagerGetTypefaceTag(toTypeface(typefaceHandle)));
}

static jint getTypefaceFamilyId(JNIEnv *env, jclass clazz, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceManagerGetTypefaceFamilyID(toTypeface(typefaceHandle)));
}

static jlong getMatchingTypefaceByFamilyId(JNIEnv *env, jclass clazz, jint familyId, jint width,
    jint weight, jint slope)
{
    return reinterpret_cast<jlong>(TRTypefaceManagerGetMatchingTypefaceByFamilyID(
        static_cast<TRUInteger>(familyId), static_cast<TRWidth>(width),
        static_cast<TRWeight>(weight), static_cast<TRSlope>(slope)));
}

static jlong getMatchingTypefaceByFamilyName(JNIEnv *env, jclass clazz, jstring familyName,
    jint width, jint weight, jint slope)
{
    const jchar *chars = env->GetStringChars(familyName, nullptr);
    TRStringView view;
    view.buffer = chars;
    view.length = static_cast<TRUInteger>(env->GetStringLength(familyName));
    view.encoding = TRStringEncodingUTF16;

    TRTypefaceRef typeface = TRTypefaceManagerGetMatchingTypefaceByFamilyName(&view,
        static_cast<TRWidth>(width), static_cast<TRWeight>(weight), static_cast<TRSlope>(slope));

    env->ReleaseStringChars(familyName, chars);

    return reinterpret_cast<jlong>(typeface);
}

static void collectTypeface(TRTypefaceRef typeface, void *context, TRBoolean *stop)
{
    static_cast<vector<jlong> *>(context)->push_back(reinterpret_cast<jlong>(typeface));
}

/* Hands the handles of the registered typefaces, in the order of their family and style names. */
static jlongArray getTypefaces(JNIEnv *env, jclass clazz)
{
    vector<jlong> handles;
    TRTypefaceManagerEnumerateTypefaces(collectTypeface, &handles);

    jlongArray array = env->NewLongArray(static_cast<jsize>(handles.size()));
    env->SetLongArrayRegion(array, 0, static_cast<jsize>(handles.size()), handles.data());

    return array;
}

namespace {

struct FamilyList {
    vector<jint> ids;
    vector<vector<jchar>> names;
};

}

static void collectFamily(TRUInteger familyID, const TRStringView *familyName, void *context,
    TRBoolean *stop)
{
    auto families = static_cast<FamilyList *>(context);
    vector<jchar> name;

    if (familyName && familyName->buffer) {
        if (familyName->encoding == TRStringEncodingUTF16) {
            auto chars = static_cast<const jchar *>(familyName->buffer);
            name.assign(chars, chars + familyName->length);
        } else {
            /* A name in UTF-8 is widened by its bytes, which is right for ASCII only. */
            auto chars = static_cast<const unsigned char *>(familyName->buffer);
            name.assign(chars, chars + familyName->length);
        }
    }

    families->ids.push_back(static_cast<jint>(familyID));
    families->names.push_back(name);
}

/* Hands the ids and the names of the families, in two arrays that go together, ordered by name. */
static jobjectArray getFamilies(JNIEnv *env, jclass clazz)
{
    FamilyList families;
    TRTypefaceManagerEnumerateFamilies(collectFamily, &families);

    jsize count = static_cast<jsize>(families.ids.size());
    jintArray ids = env->NewIntArray(count);
    env->SetIntArrayRegion(ids, 0, count, families.ids.data());

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray names = env->NewObjectArray(count, stringClass, nullptr);
    for (jsize i = 0; i < count; i++) {
        const vector<jchar> &name = families.names[i];
        jstring string = env->NewString(name.data(), static_cast<jsize>(name.size()));

        env->SetObjectArrayElement(names, i, string);
        env->DeleteLocalRef(string);
    }

    jclass objectClass = env->FindClass("java/lang/Object");
    jobjectArray result = env->NewObjectArray(2, objectClass, nullptr);
    env->SetObjectArrayElement(result, 0, ids);
    env->SetObjectArrayElement(result, 1, names);

    return result;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nRegisterTypeface", "(JII)Z", (void *)registerTypeface },
    { "nUnregisterTypeface", "(J)Z", (void *)unregisterTypeface },
    { "nGetTypeface", "(I)J", (void *)getTypeface },
    { "nGetTypefaceTag", "(J)I", (void *)getTypefaceTag },
    { "nGetTypefaceFamilyId", "(J)I", (void *)getTypefaceFamilyId },
    { "nGetMatchingTypefaceByFamilyId", "(IIII)J", (void *)getMatchingTypefaceByFamilyId },
    { "nGetMatchingTypefaceByFamilyName", "(Ljava/lang/String;III)J", (void *)getMatchingTypefaceByFamilyName },
    { "nGetTypefaces", "()[J", (void *)getTypefaces },
    { "nGetFamilies", "()[Ljava/lang/Object;", (void *)getFamilies },
};

jint register_com_mta_tehreer_graphics_TypefaceManager(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/TypefaceManager", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
