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

#ifndef _TEHREER__CONVERT_H
#define _TEHREER__CONVERT_H

extern "C" {
#include <ft2build.h>
#include FT_TYPES_H
}

static inline FT_F26Dot6 toF26Dot6(float value)
{
    return static_cast<FT_F26Dot6>((value * 64) + 0.5);
}

static inline FT_Fixed toF16Dot16(float value)
{
    return static_cast<FT_Fixed>((value * 0x10000) + 0.5);
}

static inline float f16Dot16toFloat(FT_Fixed value)
{
    return value / static_cast<float>(0x10000);
}

static inline float f26Dot6PosToFloat(FT_Pos value)
{
    return static_cast<float>(value / 64.0);
}

#endif
