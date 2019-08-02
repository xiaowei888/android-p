/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2017 The Android Open Source Project
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

//#define LOG_NDEBUG 0
#define LOG_TAG "MtkMPEG2ExtractorBundle"
#include <utils/Log.h>

#include <media/MediaExtractor.h>
#include "MtkMPEG2TSExtractor.h"

namespace android {

extern "C" {
// This is the only symbol that needs to be exported
__attribute__ ((visibility ("default")))
MediaExtractor::ExtractorDef GETEXTRACTORDEF() {
    return {
        MediaExtractor::EXTRACTORDEF_VERSION,
        UUID("7bac35bb-a5fc-4094-a182-2247412b8022"),
        1,
        "MPEG2-TS Extractor",
        [](
                DataSourceBase *source,
                float *confidence,
                void **,
                MediaExtractor::FreeMetaFunc *) -> MediaExtractor::CreatorFunc {
            if (MtkSniffMPEG2TS(source, confidence)) {
                return [](
                        DataSourceBase *source,
                        void *) -> MediaExtractor* {
                    return new MtkMPEG2TSExtractor(source);};
            }
            return NULL;
        }
    };
}

} // extern "C"

} // namespace android
