/*
 * Copyright (C) 2009 The Android Open Source Project
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

#ifndef ACODEC_MTK_MP3_H_
#define ACODEC_MTK_MP3_H_
#include <media/IOMX.h>

namespace android {

class AcodecMtkMp3{
    public:
        AcodecMtkMp3();
        static status_t setOmxReadMultiFrame(const sp<IOMXNode> &omxNode,
                const sp<AMessage> &msg);

    protected:
        virtual ~AcodecMtkMp3();

    private:
        AcodecMtkMp3(const AcodecMtkMp3 &);
        AcodecMtkMp3 &operator=(const AcodecMtkMp3 &);
};

}
#endif
