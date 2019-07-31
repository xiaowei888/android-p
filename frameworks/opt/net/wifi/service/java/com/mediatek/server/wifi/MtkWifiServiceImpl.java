/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.Context;
import android.net.wifi.IWifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;

import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.ActiveModeManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachinePrime;
import com.mediatek.provider.MtkSettingsExt;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mediatek.net.wifi.HotspotClient;

/**
 * WifiService handles remote WiFi operation requests by implementing
 * the IWifiManager interface for WiFi hotspot manager.
 *
 * @hide
 */
public abstract class MtkWifiServiceImpl extends IWifiManager.Stub {
    private static final String TAG = "MtkWifiService";

    private final Context mContext;
    private final WifiInjector mWifiInjector;

    public MtkWifiServiceImpl(
            Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        mContext = context;
        mWifiInjector = wifiInjector;
    }

    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE,
                "WifiService");
    }

    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.CHANGE_WIFI_STATE,
                "WifiService");
    }

    /**
     * Return the hotspot clients.
     * @return a list of hotspot client in the form of a list
     * of {@link HotspotClient} objects.
     * @hide
     */
    public List<HotspotClient> getHotspotClients() {
        enforceAccessPermission();

        Slog.d(TAG, "getHotspotClients");

        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            return mtkSoftApManager.getHotspotClientsList();
        } else {
            List<HotspotClient> clients = new ArrayList<HotspotClient>();
            return clients;
        }
    }

    private ArrayList<String> readClientList(String filename) {
        FileInputStream fstream = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String s;
            // throw away the title line
            while (((s = br.readLine()) != null) && (s.length() != 0)) {
                list.add(s);
            }
        } catch (IOException ex) {
            // return current list, possibly empty
            Slog.e(TAG, "IOException:" + ex);
        } finally {
          if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Slog.e(TAG, "IOException:" + ex);
                }
            }
        }
        return list;
    }

    /**
     * Return the IP address of the client.
     * @param deviceAddress The mac address of the hotspot client
     * @return the IP address of the client
     * @hide
     */
    public String getClientIp(String deviceAddress) {
        enforceAccessPermission();

        Slog.d(TAG, "getClientIp deviceAddress = " + deviceAddress);

        if (TextUtils.isEmpty(deviceAddress)) {
            return null;
        }
        final String LEASES_FILE = "/data/misc/dhcp/dnsmasq.leases";

        for (String s : readClientList(LEASES_FILE)) {
            if (s.indexOf(deviceAddress) != -1) {
                String[] fields = s.split(" ");
                if (fields.length > 3) {
                    return fields[2];
                }
            }
        }
        return null;
    }

    /**
     * Return the device name of the client.
     * @param deviceAddress The mac address of the hotspot client
     * @return the device name of the client
     * @hide
     */
    public String getClientDeviceName(String deviceAddress) {
        enforceAccessPermission();

        Slog.d(TAG, "getClientDeviceName deviceAddress = " + deviceAddress);

        if (TextUtils.isEmpty(deviceAddress)) {
            return null;
        }
        final String LEASES_FILE = "/data/misc/dhcp/dnsmasq.leases";

        for (String s : readClientList(LEASES_FILE)) {
            if (s.indexOf(deviceAddress) != -1) {
                String[] fields = s.split(" ");
                if (fields.length > 4) {
                    return fields[3];
                }
            }
        }
        return null;
    }

    /**
     * Block the client.
     * @param client The hotspot client to be blocked
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
    public boolean blockClient(HotspotClient client) {
        enforceChangePermission();

        Slog.d(TAG, "blockClient client = " + client);

        if (client == null || client.deviceAddress == null) {
            Slog.e(TAG, "Client is null!");
            return false;
        }

        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            return mtkSoftApManager.syncBlockClient(client);
        } else {
            return false;
        }
    }

    /**
     * Unblock the client.
     * @param client The hotspot client to be unblocked
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
    public boolean unblockClient(HotspotClient client) {
        enforceChangePermission();

        Slog.d(TAG, "unblockClient client = " + client);

        if (client == null || client.deviceAddress == null) {
            Slog.e(TAG, "Client is null!");
            return false;
        }

        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            return mtkSoftApManager.syncUnblockClient(client);
        } else {
            return false;
        }
    }

    /**
     * Return whether all devices are allowed to connect.
     * @return {@code true} if all devices are allowed to connect else {@code false}
     * @hide
     */
    public boolean isAllDevicesAllowed() {
        enforceAccessPermission();

        Slog.d(TAG, "isAllDevicesAllowed");

        boolean result = (Settings.System.getInt(mContext.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_IS_ALL_DEVICES_ALLOWED, 1) == 1);
        return result;
    }

    /**
     * Enable or disable allow all devices.
     * @param enabled {@code true} to enable, {@code false} to disable
     * @param allowAllConnectedDevices {@code true} to add all connected devices to allowed list
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
    public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        enforceChangePermission();

        Slog.d(TAG, "setAllDevicesAllowed enabled = " + enabled
               + " allowAllConnectedDevices = " + allowAllConnectedDevices);

        Settings.System.putInt(mContext.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_IS_ALL_DEVICES_ALLOWED, enabled ? 1 : 0);
        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            mtkSoftApManager.syncSetAllDevicesAllowed(enabled, allowAllConnectedDevices);
        }

        return true;
    }

    /**
     * Allow the specified device to connect Hotspot and update the allowed list
     * with MAC address and name as well
     * @param deviceAddress the MAC address of the device
     * @param name the name of the device
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
    public boolean allowDevice(String deviceAddress, String name) {
        enforceChangePermission();

        Slog.d(TAG, "allowDevice address = " + deviceAddress + ", name = " + name
               + "is null?" + (name == null));

        if (deviceAddress == null) {
            Slog.e(TAG, "deviceAddress is null!");
            return false;
        }

        HotspotClient device = new HotspotClient(deviceAddress, false, name);
        MtkSoftApManager.addDeviceToAllowedList(device);
        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            mtkSoftApManager.syncAllowDevice(deviceAddress);
        }

        return true;
    }

    /**
     * Disallow the specified device to connect Hotspot and update the allowed list.
     * If current setting is to allow all devices, it only updates the list.
     * @param deviceAddress the MAC address of the device
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
    public boolean disallowDevice(String deviceAddress) {
        enforceChangePermission();

        Slog.d(TAG, "disallowDevice address = " + deviceAddress);

        if (deviceAddress == null) {
            Slog.e(TAG, "deviceAddress is null!");
            return false;
        }

        MtkSoftApManager.removeDeviceFromAllowedList(deviceAddress);
        MtkSoftApManager mtkSoftApManager = getMtkSoftApManager();
        if (mtkSoftApManager != null) {
            mtkSoftApManager.syncDisallowDevice(deviceAddress);
        }

        return true;
    }

    /**
     * Return the allowed devices.
     * @return a list of hotspot client in the form of a list
     * of {@link HotspotClient} objects.
     * @hide
     */
    public List<HotspotClient> getAllowedDevices() {
        enforceAccessPermission();

        Slog.d(TAG, "getAllowedDevices");

        return MtkSoftApManager.getAllowedDevices();
    }

    private MtkSoftApManager getMtkSoftApManager() {
        WifiStateMachinePrime wifiStateMachinePrime = mWifiInjector.getWifiStateMachinePrime();
        ArraySet<ActiveModeManager> activeModeManager;
        try {
            Field activeModeManagerField =
                    wifiStateMachinePrime.getClass().getSuperclass().getDeclaredField(
                            "mActiveModeManagers");
            activeModeManagerField.setAccessible(true);
            activeModeManager = (ArraySet<ActiveModeManager>) activeModeManagerField.get(
                                                                        wifiStateMachinePrime);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
        for (ActiveModeManager manager : activeModeManager) {
            if (manager instanceof MtkSoftApManager) {
                return (MtkSoftApManager) manager;
            }
        }
        return null;
    }
}
