/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

#include <Tehreer/TRComposedFrame.h>
#include <Tehreer/TRComposedLine.h>
#include <Tehreer/TRFrameResolver.h>
#include <Tehreer/TRTypesetter.h>

#include "FloatCollector.h"
#include "FrameResolver.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Renderer.h"

using namespace Tehreer;

/*
 * Resolves a frame of a range of the text with the given properties. The place of truncation is
 * negative if the lines are not truncated.
 */
static jlong createFrame(JNIEnv *env, jclass clazz, jlong typesetterHandle, jint start, jint end,
    jfloat width, jfloat height, jboolean fitsHorizontally, jboolean fitsVertically,
    jint textAlignment, jint verticalAlignment, jint truncationMode, jint truncationPlace,
    jboolean isJustificationEnabled, jfloat justificationLevel, jint maxLines,
    jfloat extraLineSpacing, jfloat lineHeightMultiplier)
{
    TRFrameResolverRef resolver = TRFrameResolverCreate();
    if (!resolver) {
        return 0;
    }

    TRFrameResolverSetTypesetter(resolver, toTypesetter(typesetterHandle));
    TRFrameResolverSetFrameSize(resolver, width, height);
    TRFrameResolverSetFitsHorizontally(resolver, fitsHorizontally ? TRTrue : TRFalse);
    TRFrameResolverSetFitsVertically(resolver, fitsVertically ? TRTrue : TRFalse);
    TRFrameResolverSetTextAlignment(resolver, static_cast<TRTextAlignment>(textAlignment));
    TRFrameResolverSetVerticalAlignment(resolver, static_cast<TRVerticalAlignment>(verticalAlignment));
    TRFrameResolverSetTruncationMode(resolver, static_cast<TRBreakMode>(truncationMode));
    if (truncationPlace >= 0) {
        TRFrameResolverSetTruncationPlace(resolver, static_cast<TRTruncationPlace>(truncationPlace));
    }
    TRFrameResolverSetTruncationEnabled(resolver, truncationPlace >= 0 ? TRTrue : TRFalse);
    TRFrameResolverSetJustificationEnabled(resolver, isJustificationEnabled ? TRTrue : TRFalse);
    TRFrameResolverSetJustificationLevel(resolver, justificationLevel);
    TRFrameResolverSetMaxLines(resolver, static_cast<TRUInteger>(maxLines));
    TRFrameResolverSetExtraLineSpacing(resolver, extraLineSpacing);
    TRFrameResolverSetLineHeightMultiplier(resolver, lineHeightMultiplier);

    TRComposedFrameRef frame = TRFrameResolverCreateFrame(resolver, toIndex(start),
                                                          toLength(start, end));
    TRFrameResolverRelease(resolver);

    return reinterpret_cast<jlong>(frame);
}

static JNINativeMethod RESOLVER_METHODS[] = {
    { "nCreateFrame", "(JIIFFZZIIIIZFIFF)J", (void *)createFrame },
};

jint register_com_mta_tehreer_layout_FrameResolver(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/FrameResolver", RESOLVER_METHODS,
                                     sizeof(RESOLVER_METHODS) / sizeof(RESOLVER_METHODS[0]));
}
