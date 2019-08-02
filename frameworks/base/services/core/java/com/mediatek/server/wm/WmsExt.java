/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2017. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.server.wm;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.DisplayInfo;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.android.server.policy.WindowManagerPolicy.WindowState;
import com.android.server.wm.AppWindowToken;

public class WmsExt {

    public static final String TAG = "WindowManager";

    /// M: add for fullscreen switch feature @{
    public static final Rect mEmptyFrame = new Rect();

    public boolean isFullscreenSwitchSupport() {
        return false;
    }

    public boolean isFocusWindowReady(WindowState focus) {
        return false;
    }

    /**
     * Used by PhoneWindowManager
     * @param win current window
     * @param focus focus window
     * @param mOverscanScreenWidth screen width
     * @param mOverscanScreenHeight screen height
     * @return black retion rect
     */
    public Rect getSwitchFrame(WindowState win, WindowState focus,
            int mOverscanScreenWidth, int mOverscanScreenHeight) {
        return mEmptyFrame;
    };

    public void resetSwitchFrame() {
    }

    public boolean initFullscreenSwitchState(IBinder token) {
        return true;
    }

    public boolean isMultiWindow(AppWindowToken token) {
        return false;
    }

    public boolean isFullScreenCropState(AppWindowToken focusedApp) {
        return false;
    }

    /**
     * Used by WindowManagerService
     * @param logicWidth current screen width
     * @param logicHeight current screen height
     * @return black retion rect
     */
    public Rect getSwitchFrame(int logicWidth, int logicHeight) {
        return mEmptyFrame;
    }

    public boolean getFullscreenMode(String packageName) {
        return true;
    }
    /// @}


    /// M: add for App Resolution Tuner feature @{
    public boolean isAppResolutionTunerSupport() {
        return false;
    }

    public void loadResolutionTunerAppList() {
    }

    public void setWindowScaleByWL(com.android.server.wm.WindowState win,DisplayInfo displayInfo,
              WindowManager.LayoutParams attrs, int requestedWidth,int requestedHeight) {
    }
   /// @}
}