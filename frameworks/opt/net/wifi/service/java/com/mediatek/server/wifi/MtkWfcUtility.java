/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
*/

package com.mediatek.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanSettings;
import android.os.Process;
import android.os.WorkSource;
import android.util.Log;

import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MtkWfcUtility {
    private static final String TAG = "MtkWfcUtility";
    private static final WorkSource WIFI_WORK_SOURCE = new WorkSource(Process.WIFI_UID);
    private static Set<Integer> mSavedNetworkChannelSet = new HashSet<>();

    public static void init(Context context) {
        context.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.v(TAG, "onReceive: WFC_REQUEST_PARTIAL_SCAN");
                        startPartialScanForSavedChannel();
                    }
                },
                new IntentFilter("com.mediatek.intent.action.WFC_REQUEST_PARTIAL_SCAN"));
        context.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int state = intent.getIntExtra(
                            WifiManager.EXTRA_SCAN_AVAILABLE, WifiManager.WIFI_STATE_DISABLED);
                        if (state == WifiManager.WIFI_STATE_DISABLED) {
                            Log.v(TAG, "Clear Saved Network Channel due to Wi-Fi disabled");
                            clearSavedNetworkChannel();
                        }
                    }
                },
                new IntentFilter(WifiManager.WIFI_SCAN_AVAILABLE));
    }

    public static void updateSavedNetworkChannel(List<ScanDetail> scanDetails) {
        WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            WifiConfiguration associatedConfiguration =
                    wifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail);
            if (associatedConfiguration == null) {
                continue;
            } else {
                mSavedNetworkChannelSet.add(scanResult.frequency);
            }
        }
    }

    public static void clearSavedNetworkChannel() {
        mSavedNetworkChannelSet.clear();
    }

    public static Set<Integer> startPartialScanForSavedChannel() {
        WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        ScanSettings settings = new ScanSettings();
        settings.reportEvents = WifiScanner.REPORT_EVENT_FULL_SCAN_RESULT
                            | WifiScanner.REPORT_EVENT_AFTER_EACH_SCAN;
        settings.type = WifiScanner.TYPE_HIGH_ACCURACY;
        List<ScanSettings.HiddenNetwork> hiddenNetworkList =
            wifiConfigManager.retrieveHiddenNetworkList();
        settings.hiddenNetworks =
            hiddenNetworkList.toArray(new ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
        Set<Integer> freqs = mSavedNetworkChannelSet;
        settings.channels = new WifiScanner.ChannelSpec[freqs.size()];
        if (freqs != null && freqs.size() != 0) {
            int index = 0;
            settings.channels = new WifiScanner.ChannelSpec[freqs.size()];
            for (Integer freq : freqs) {
                settings.channels[index++] = new WifiScanner.ChannelSpec(freq);
            }
            settings.band = WifiScanner.WIFI_BAND_UNSPECIFIED;
        } else {
            settings.band = WifiScanner.WIFI_BAND_BOTH_WITH_DFS;
        }
        WifiScanner.ScanListener nativeScanListener = new WifiScanner.ScanListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason, String description) {
            }
            @Override
            public void onResults(WifiScanner.ScanData[] results) {
            }
            @Override
            public void onFullResult(ScanResult fullScanResult) {
            }
            @Override
            public void onPeriodChanged(int periodInMs) {
            }
        };
        WifiInjector.getInstance().getWifiScanner()
                .startScan(settings, nativeScanListener, WIFI_WORK_SOURCE);
        if (settings.band == WifiScanner.WIFI_BAND_UNSPECIFIED) {
            Log.v(TAG, "Start partial scan for channels " + freqs);
        } else {
            Log.v(TAG, "Start full scan since no saved channel available");
        }
        return freqs;
    }
}
