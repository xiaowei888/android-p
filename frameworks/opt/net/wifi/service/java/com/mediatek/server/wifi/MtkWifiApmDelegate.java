/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
*/

package com.mediatek.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import com.android.server.wifi.WificondControl;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMonitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

public class MtkWifiApmDelegate {
    private static final String TAG = "MtkWifiApmDelegate";
    private static MtkWifiApmDelegate sApmDelegate = null;
    private final String mInterfaceName;
    private Handler mHandler;
    private String mLastSsid = null;
    private Calendar mLastStartScanTime;
    public static final boolean MDMI_SUPPORT =
            (SystemProperties.getInt("persist.vendor.mdmi_support", 0) == 1) ? true : false;

    private MtkWifiApmDelegate() {
        mInterfaceName = WifiInjector.getInstance().getWifiNative().getClientInterfaceName();
        mHandler = new Handler(
                WifiInjector.getInstance().getWifiStateMachineHandlerThread().getLooper(),
                new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "handleMessage --> " + msgToString(msg));
                switch (msg.what) {
                    case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                        broadcastAssociationReject((String) msg.obj);
                        break;
                }
                return true;
            }
        });
    }

    public void init() {
        if (MDMI_SUPPORT) {
            registerMessage(WifiMonitor.NETWORK_DISCONNECTION_EVENT);
            registerMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
        }
    }

    private String msgToString(Message msg) {
        String ret = "";
        switch (msg.what) {
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                ret = "ASSOCIATION_REJECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                ret = "NETWORK_DISCONNECTION_EVENT";
                break;
        }
        return ret;
    }

    private void registerMessage(int monitorMessage) {
        WifiInjector.getInstance().getWifiMonitor().registerHandler(mInterfaceName, monitorMessage,
                mHandler);
    }

    public static MtkWifiApmDelegate getInstance() {
        if (sApmDelegate == null) {
            synchronized (TAG) {
                sApmDelegate = new MtkWifiApmDelegate();
            }
            Log.d(TAG, "MDMI suuport: " + MDMI_SUPPORT);
        }
        return sApmDelegate;
    }

    public void notifyStartScanTime() {
        if (!MDMI_SUPPORT) return;
        mLastStartScanTime = Calendar.getInstance();
    }

    public void fillExtraInfo(Intent intent) {
        if (!MDMI_SUPPORT) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("scan_start", mLastStartScanTime);
        intent.putExtras(bundle);
        intent.putIntegerArrayListExtra("scan_channels", getScanChannels());
    }

    public ArrayList<Integer> getScanChannels() {
        int[] available2gChannels = WifiInjector.getInstance().getWifiNative().getChannelsForBand(
                WifiScanner.WIFI_BAND_24_GHZ);
        if (available2gChannels == null) {
            available2gChannels = new int[0];
        }
        int[] available5gChannels = WifiInjector.getInstance().getWifiNative().getChannelsForBand(
                WifiScanner.WIFI_BAND_5_GHZ);
        if (available5gChannels == null) {
            available5gChannels = new int[0];
        }
        int[] availableDfsChannels = WifiInjector.getInstance().getWifiNative().getChannelsForBand(
                WifiScanner.WIFI_BAND_5_GHZ_DFS_ONLY);
        if (availableDfsChannels == null) {
            availableDfsChannels = new int[0];
        }
        ArrayList<Integer> availableChannels = new ArrayList<Integer>();
        for (int channel : available2gChannels) {
            if (!availableChannels.contains(channel)) availableChannels.add(channel);
        }
        for (int channel : available5gChannels) {
            if (!availableChannels.contains(channel)) availableChannels.add(channel);
        }
        for (int channel : availableDfsChannels) {
            if (!availableChannels.contains(channel)) availableChannels.add(channel);
        }
        return availableChannels;
    }

    private static Context getContext() {
        WifiInjector wi = WifiInjector.getInstance();
        try {
            Field field = wi.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            return (Context) field.get(wi);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void broadcastAssociationReject(String bssid) {
        if (!MDMI_SUPPORT) return;

        Intent intent = new Intent(
                "mediatek.intent.action.WIFI_ASSOCIATION_REJECT");
        intent.putExtra("bssid", bssid);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void broadcastNetworkDisconnect(String bssid, int reason) {
        if (!MDMI_SUPPORT) return;

        Intent intent = new Intent(
                "mediatek.intent.action.WIFI_NETWORK_DISCONNECT");
        intent.putExtra("bssid", bssid);
        intent.putExtra("reason", reason);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void broadcastPowerSaveModeChanged(boolean enabled) {
        if (!MDMI_SUPPORT) return;

        Intent intent = new Intent(
                "mediatek.intent.action.WIFI_PS_CHANGED");
        intent.putExtra("ps_mode", enabled);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void broadcastProvisionFail() {
        if (!MDMI_SUPPORT) return;

        Intent intent = new Intent(
                "mediatek.intent.action.WIFI_PROVISION_FAIL");
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
