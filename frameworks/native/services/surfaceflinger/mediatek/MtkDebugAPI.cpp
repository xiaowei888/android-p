/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2018. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#include <log/log.h>
#include <layerproto/LayerProtoParser.h>

#include "mediatek/SFProperty.h"
#include "mediatek/PropertiesState.h"
#include "mediatek/SFDebugAPILoader.h"
#include "MtkDebugAPI.h"
#include "Layer.h"

#define LOG_MAX_SIZE 256

#ifdef MTK_SF_DEBUG_SUPPORT

void slowMotion()
{
    // to slow down FPS
    if (CC_UNLIKELY(0 != SFProperty::getInstance().getPropState()->mDelayTime)) {
        ALOGI("SurfaceFlinger slow motion timer: %d ms", SFProperty::getInstance().getPropState()->mDelayTime);
        usleep(1000 * SFProperty::getInstance().getPropState()->mDelayTime);
    }
}

void startLogRepaint(const sp<const DisplayDevice>& displayDevice)
{
    // doDisplayComposition debug msg
    if (CC_UNLIKELY(SFProperty::getInstance().getPropState()->mLogRepaint)) {
        ALOGD("[doDisplayComposition] (type:%d hwcid:%d name:%s) +",
            displayDevice->getDisplayType(), displayDevice->getHwcDisplayId(),
            displayDevice->getDisplayName().string());
    }
}

void logRepaint(const Region& region, const char* msg, ...)
{
    // debug log
    if (CC_UNLIKELY(SFProperty::getInstance().getPropState()->mLogRepaint)) {
        char buf[LOG_MAX_SIZE] = {'\0'};

        if (msg != nullptr)
        {
            va_list ap;
            va_start(ap, msg);
            vsnprintf(buf, LOG_MAX_SIZE, msg, ap);
            va_end(ap);
        }

        String8 str;
        region.dump(str, "");

        ALOGD("%s%s", buf, str.string());
    }
}

void logTransaction(Layer* layer)
{
    // dump state result after transaction committed
    if (CC_UNLIKELY(SFProperty::getInstance().getPropState()->mLogTransaction)) {
        String8 result;
        LayersProto layersProto;
        LayerProto* layerProto = layersProto.add_layers();
        layer->writeToProto(layerProto, LayerVector::StateSet::Drawing);
        auto layerTree = LayerProtoParser::generateLayerTree(layersProto);
        result.append(LayerProtoParser::layersToString(std::move(layerTree)).c_str());
        ALOGD("%s", result.string());
    }
}

void dumpScreenShot(sp<GraphicBuffer>* outBuffer)
{
    if (CC_UNLIKELY(SFProperty::getInstance().getPropState()->mDumpScreenShot > 0)) {
        String8 s = String8::format("captureScreen_%08d", SFProperty::getInstance().getPropState()->mDumpScreenShot);
        if (*outBuffer != nullptr)
        {
            if ((*outBuffer)->handle != nullptr)
            {
                SFDebugAPI::getInstance().dumpBuffer((*outBuffer)->handle, s.string());
            }
        }
        SFProperty::getInstance().getPropState()->mDumpScreenShot++;
    }
}

#endif
