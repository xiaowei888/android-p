/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2015. All rights reserved.
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
package com.mediatek.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;

import com.mediatek.server.wifi.MtkWifiServiceAdapter.IMtkWifiService;

import java.util.List;

public class WifiOperatorFactoryBase {

    public IMtkWifiServiceExt createWifiFwkExt(Context context, IMtkWifiService service) {
        return new DefaultMtkWifiServiceExt(context, service);
    }

    public interface IMtkWifiServiceExt {

        String AUTOCONNECT_SETTINGS_CHANGE =
                "com.mediatek.common.wifi.AUTOCONNECT_SETTINGS_CHANGE";
        String AUTOCONNECT_ENABLE_ALL_NETWORKS =
                "com.mediatek.common.wifi.AUTOCONNECT_ENABLE_ALL_NETWORK";

        String RESELECT_DIALOG_CLASSNAME = "com.mediatek.op01.plugin.WifiReselectApDialog";
        String ACTION_RESELECTION_AP = "android.net.wifi.WIFI_RESELECTION_AP";
        String ACTION_SUSPEND_NOTIFICATION = "com.mediatek.wifi.ACTION_SUSPEND_NOTIFICATION";
        String EXTRA_SUSPEND_TYPE = "type";
        String WIFISETTINGS_CLASSNAME = "com.android.settings.Settings$WifiSettingsActivity";
        String ACTION_WIFI_FAILOVER_GPRS_DIALOG = "com.mediatek.intent.WIFI_FAILOVER_GPRS_DIALOG";
        String WIFI_NOTIFICATION_ACTION = "android.net.wifi.WIFI_NOTIFICATION";
        String EXTRA_NOTIFICATION_SSID = "ssid";
        String EXTRA_NOTIFICATION_NETWORKID = "network_id";
        String EXTRA_SHOW_RESELECT_DIALOG_FLAG = "SHOW_RESELECT_DIALOG";
        long SUSPEND_NOTIFICATION_DURATION = 60 * 60 * 1000;
        int DEFAULT_FRAMEWORK_SCAN_INTERVAL_MS = 15000;
        int MIN_INTERVAL_CHECK_WEAK_SIGNAL_MS = 60000; /* 60 seconds */
        // If we scan too frequently then supplicant cannot disconnect from weak-signal network.
        int MIN_INTERVAL_SCAN_SUPRESSION_MS = 10000;
        int BEST_SIGNAL_THRESHOLD = -79;
        int WEAK_SIGNAL_THRESHOLD = -85;
        int MIN_NETWORKS_NUM = 2;
        int BSS_EXPIRE_AGE = 10;
        int BSS_EXPIRE_COUNT = 1;

        int NOTIFY_TYPE_SWITCH = 0;
        int NOTIFY_TYPE_RESELECT = 1;

        int WIFI_CONNECT_REMINDER_ALWAYS = 0;

        int OP_NONE = 0;
        int OP_01 = 1;
        int OP_03 = 3;

        void init();
        boolean hasCustomizedAutoConnect();
        boolean shouldAutoConnect();
        boolean isWifiConnecting(int connectingNetworkId, List<Integer> disconnectNetworks);
        boolean hasConnectableAp();
        boolean handleNetworkReselection();
        void suspendNotification(int type);
        int defaultFrameworkScanIntervalMs();
        int getSecurity(WifiConfiguration config);
        int getSecurity(ScanResult result);
        String getApDefaultSsid();
        boolean needRandomSsid();
        void setCustomizedWifiSleepPolicy(Context context);
        int hasNetworkSelection();
    }

    public static class DefaultMtkWifiServiceExt implements IMtkWifiServiceExt {

        private static final String TAG = "DefaultMtkWifiServiceExt";
        protected Context mContext;
        protected IMtkWifiService mService;

        static final int SECURITY_NONE = 0;
        static final int SECURITY_WEP = 1;
        static final int SECURITY_PSK = 2;
        static final int SECURITY_EAP = 3;
        static final int SECURITY_WAPI_PSK = 4;
        static final int SECURITY_WAPI_CERT = 5;
        static final int SECURITY_WPA2_PSK = 6;

        public DefaultMtkWifiServiceExt(Context context, IMtkWifiService service) {
            mContext = context;
            mService = service;
        }

        public void init() {
        }

        public boolean hasCustomizedAutoConnect() {
            return false;
        }

        public boolean shouldAutoConnect() {
            return true;
        }

        public boolean isWifiConnecting(int connectingNetworkId, List<Integer> disconnectNetworks) {
            // this method should only be invoked when hasCustomizedAutoConnect() returns true
            return false;
        }

        public boolean hasConnectableAp() {
            return false;
        }

        public void suspendNotification(int type) {
        }

        public int defaultFrameworkScanIntervalMs() {
            return mContext.getResources().getInteger(com.android.internal.R.integer.
                config_wifi_framework_scan_interval);
        }

        public int getSecurity(WifiConfiguration config) {
            if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
                return SECURITY_PSK;
            }
            if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
                return SECURITY_EAP;
            }
            if (config.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
                return SECURITY_WAPI_PSK;
            }
            if (config.allowedKeyManagement.get(KeyMgmt.WAPI_CERT)) {
                return SECURITY_WAPI_CERT;
            }
            if (config.wepTxKeyIndex >= 0 && config.wepTxKeyIndex < config.wepKeys.length
                && config.wepKeys[config.wepTxKeyIndex] != null) {
                return SECURITY_WEP;
            }
            if (config.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
                return SECURITY_WPA2_PSK;
            }
            return SECURITY_NONE;
        }

        public int getSecurity(ScanResult result) {
            if (result.capabilities.contains("WAPI-PSK")) {
                return SECURITY_WAPI_PSK;
            } else if (result.capabilities.contains("WAPI-CERT")) {
                return SECURITY_WAPI_CERT;
            } else if (result.capabilities.contains("WEP")) {
                return SECURITY_WEP;
            } else if (result.capabilities.contains("PSK")) {
                return SECURITY_PSK;
            } else if (result.capabilities.contains("EAP")) {
                return SECURITY_EAP;
            } else if (result.capabilities.contains("WPA2-PSK")) {
                return SECURITY_WPA2_PSK;
            }
            return SECURITY_NONE;
        }

        public String getApDefaultSsid() {
            return mContext.getString(com.android.internal.R.string.
                wifi_tether_configure_ssid_default);
        }

        public boolean needRandomSsid() {
            return false;
        }

        public void setCustomizedWifiSleepPolicy(Context context) {
        }

        public boolean handleNetworkReselection() {
            return false;
        }

        public int hasNetworkSelection() {
           return IMtkWifiServiceExt.OP_NONE;
        }

        public boolean isPppoeSupported() {
            return false;
        }

        public void setNotificationVisible(boolean visible) {
        }
    }
}