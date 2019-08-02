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
package com.mediatek.server.wifi;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.wifi.hostapd.V1_0.HostapdStatus;
import android.hardware.wifi.hostapd.V1_0.IHostapd;
import android.net.wifi.WifiConfiguration;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.R;
import com.android.server.wifi.HostapdHal;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.util.NativeUtil;
import com.mediatek.provider.MtkSettingsExt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import mediatek.net.wifi.HotspotClient;

import vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapdCallback;

/**
 * To maintain thread-safety, the locking protocol is that every non-static method (regardless of
 * access level) acquires mLock.
 */
@ThreadSafe
public class MtkHostapdHal {
    private static final String TAG = "MtkHostapdHal";
    private static String sIfaceName;

    /**
     * Add and start a new access point.
     *
     * @param ifaceName Name of the interface.
     * @param config Configuration to use for the AP.
     * @return true on success, false otherwise.
     */
    public static boolean addAccessPoint(
            @NonNull String ifaceName, @NonNull WifiConfiguration config) {
        sIfaceName = ifaceName;
        Context context = getContext();
        boolean enableAcs =
                context.getResources().getBoolean(R.bool.config_wifi_softap_acs_supported);
        boolean enableIeee80211AC =
                context.getResources().getBoolean(R.bool.config_wifi_softap_ieee80211ac_supported);
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "addAccessPoint";
            IHostapd.IfaceParams ifaceParams = new IHostapd.IfaceParams();
            ifaceParams.ifaceName = ifaceName;
            ifaceParams.hwModeParams.enable80211N = true;
            ifaceParams.hwModeParams.enable80211AC = enableIeee80211AC;
            try {
                ifaceParams.channelParams.band = getBand(hostapdHal, config);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unrecognized apBand " + config.apBand);
                return false;
            }
            if (enableAcs) {
                ifaceParams.channelParams.enableAcs = true;
                ifaceParams.channelParams.acsShouldExcludeDfs = true;
            } else {
                // Downgrade IHostapd.Band.BAND_ANY to IHostapd.Band.BAND_2_4_GHZ if ACS
                // is not supported.
                // We should remove this workaround once channel selection is moved from
                // ApConfigUtil to here.
                if (ifaceParams.channelParams.band == IHostapd.Band.BAND_ANY) {
                    Log.d(TAG, "ACS is not supported on this device, using 2.4 GHz band.");
                    ifaceParams.channelParams.band = IHostapd.Band.BAND_2_4_GHZ;
                }
                ifaceParams.channelParams.enableAcs = false;
                ifaceParams.channelParams.channel = config.apChannel;
            }

            vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.NetworkParams nwParams =
                    new vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.NetworkParams();
            // TODO(b/67745880) Note that config.SSID is intended to be either a
            // hex string or "double quoted".
            // However, it seems that whatever is handing us these configurations does not obey
            // this convention.
            nwParams.ssid.addAll(NativeUtil.stringToByteArrayList(config.SSID));
            nwParams.isHidden = config.hiddenSSID;
            nwParams.encryptionType = getEncryptionType(hostapdHal, config);
            nwParams.pskPassphrase = (config.preSharedKey != null) ? config.preSharedKey : "";
            nwParams.maxNumSta = Settings.System.getInt(
                    context.getContentResolver(),
                    MtkSettingsExt.System.WIFI_HOTSPOT_MAX_CLIENT_NUM,
                    10);
            nwParams.macAddrAcl = (Settings.System.getInt(
                    context.getContentResolver(),
                    MtkSettingsExt.System.WIFI_HOTSPOT_IS_ALL_DEVICES_ALLOWED, 1) == 1)
                    ? "0" : "1";
            List<HotspotClient> acceptMacList = MtkSoftApManager.getAllowedDevices();
            String content = "";
            for (HotspotClient device : acceptMacList) {
                String prefix = device.isBlocked ? "-" : "";
                content += prefix + device.deviceAddress + "\n";
            }
            nwParams.acceptMacFileContent = content;
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.addAccessPoint(ifaceParams, nwParams);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "addAccessPoint: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    public static boolean registerCallback(IHostapdCallback callback) {
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "registerCallback";
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.registerCallback(callback);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "registerCallback: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    /**
     * Block client.
     *
     * @param deviceAddress MAC address of client to be blocked.
     * @return true if request is sent successfully, false otherwise.
     */
    public static boolean blockClient(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) return false;
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "blockClient";
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.blockClient(deviceAddress);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "blockClient: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    /**
     * Unblock client.
     *
     * @param deviceAddress MAC address of client to be unblocked.
     * @return true if request is sent successfully, false otherwise.
     */
    public static boolean unblockClient(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) return false;
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "unblockClient";
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.unblockClient(deviceAddress);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "unblockClient: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    /**
     * Update allowed list.
     *
     * @param acceptMacFileContent File content of allowed list which wanna update.
     * @return true if request is sent successfully, false otherwise.
     */
    public static boolean updateAllowedList(String acceptMacFileContent) {
        if (TextUtils.isEmpty(acceptMacFileContent)) return false;
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "updateAllowedList";
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.updateAllowedList(acceptMacFileContent);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "updateAllowedList: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    /**
     * Set all devices allowed.
     *
     * @param enable true to enable, false to disable.
     * @return true if request is sent successfully, false otherwise.
     */
    public static boolean setAllDevicesAllowed(boolean enable) {
        HostapdHal hostapdHal = getHostapdHal();
        synchronized (getLock(hostapdHal)) {
            final String methodStr = "setAllDevicesAllowed";
            if (!checkHostapdAndLogFailure(hostapdHal, methodStr)) return false;
            try {
                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd iHostapd =
                        vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.castFrom(
                                vendor.mediatek.hardware.wifi.hostapd.V2_0.IHostapd.getService());
                if (iHostapd != null) {
                    HostapdStatus status = iHostapd.setAllDevicesAllowed(enable);
                    return checkStatusAndLogFailure(hostapdHal, status, methodStr);
                } else {
                    Log.e(TAG, "setAllDevicesAllowed: Failed to get IHostapd");
                    return false;
                }
            } catch (RemoteException e) {
                handleRemoteException(hostapdHal, e, methodStr);
                return false;
            }
        }
    }

    public static String getIfaceName() {
        return sIfaceName;
    }

    private static Context getContext() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        try {
            Field contextField = wifiInjector.getClass().getDeclaredField("mContext");
            contextField.setAccessible(true);
            return (Context) contextField.get(wifiInjector);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HostapdHal getHostapdHal() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        HostapdHal hostapdHal = null;
        try {
            Field hostapdHalField = wifiInjector.getClass().getDeclaredField("mHostapdHal");
            hostapdHalField.setAccessible(true);
            hostapdHal = (HostapdHal) hostapdHalField.get(wifiInjector);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return hostapdHal;
    }

    private static Object getLock(HostapdHal hostapdHal) {
        Object lock;
        try {
            Field lockField = hostapdHal.getClass().getDeclaredField("mLock");
            lockField.setAccessible(true);
            lock = lockField.get(hostapdHal);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            lock = new Object();
        }
        return lock;
    }

    private static int getEncryptionType(HostapdHal hostapdHal, WifiConfiguration localConfig) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod(
                    "getEncryptionType", WifiConfiguration.class);
            method.setAccessible(true);
            return (int) method.invoke(hostapdHal, localConfig);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return IHostapd.EncryptionType.NONE;
        }
    }

    private static int getBand(HostapdHal hostapdHal, WifiConfiguration localConfig) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod(
                    "getBand", WifiConfiguration.class);
            method.setAccessible(true);
            return (int) method.invoke(hostapdHal, localConfig);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return IHostapd.Band.BAND_2_4_GHZ;
        }
    }

    private static boolean checkHostapdAndLogFailure(HostapdHal hostapdHal, String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod(
                    "checkHostapdAndLogFailure", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(hostapdHal, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean checkStatusAndLogFailure(HostapdHal hostapdHal, HostapdStatus status,
            String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod(
                    "checkStatusAndLogFailure", HostapdStatus.class, String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(hostapdHal, status, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleRemoteException(HostapdHal hostapdHal, RemoteException re,
            String methodStr) {
        try {
            Method method = hostapdHal.getClass().getDeclaredMethod(
                    "handleRemoteException", RemoteException.class, String.class);
            method.setAccessible(true);
            method.invoke(hostapdHal, re, methodStr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
