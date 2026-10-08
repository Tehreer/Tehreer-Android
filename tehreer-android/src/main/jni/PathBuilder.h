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

#ifndef _TEHREER__PATH_BUILDER_H
#define _TEHREER__PATH_BUILDER_H

#include <jni.h>

#include <Tehreer/TRPath.h>

#include "JavaBridge.h"

namespace Tehreer {

/* The android path that the segments of a path of Core are added to. */
struct PathBuilder {
    JavaBridge bridge;
    jobject path;

    explicit PathBuilder(JNIEnv *env)
        : bridge(env)
        , path(bridge.Path_construct())
    {
    }

    /* The callbacks expect the builder as the user data. */
    static TRPathCallbacks callbacks()
    {
        TRPathCallbacks callbacks = {};
        callbacks.moveTo = [](void *user, TRFloat x, TRFloat y) {
            auto builder = static_cast<PathBuilder *>(user);
            builder->bridge.Path_moveTo(builder->path, x, y);
        };
        callbacks.lineTo = [](void *user, TRFloat x, TRFloat y) {
            auto builder = static_cast<PathBuilder *>(user);
            builder->bridge.Path_lineTo(builder->path, x, y);
        };
        callbacks.quadTo = [](void *user, TRFloat controlX, TRFloat controlY, TRFloat x, TRFloat y) {
            auto builder = static_cast<PathBuilder *>(user);
            builder->bridge.Path_quadTo(builder->path, controlX, controlY, x, y);
        };
        callbacks.cubicTo = [](void *user, TRFloat control1X, TRFloat control1Y,
                               TRFloat control2X, TRFloat control2Y, TRFloat x, TRFloat y) {
            auto builder = static_cast<PathBuilder *>(user);
            builder->bridge.Path_cubicTo(builder->path, control1X, control1Y,
                                         control2X, control2Y, x, y);
        };
        callbacks.close = [](void *user) {
            auto builder = static_cast<PathBuilder *>(user);
            builder->bridge.Path_close(builder->path);
        };

        return callbacks;
    }
};

}

#endif
