/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static com.android.server.wifi.util.ApConfigUtil.ERROR_GENERIC;
import static com.android.server.wifi.util.ApConfigUtil.ERROR_NO_CHANNEL;
import static com.android.server.wifi.util.ApConfigUtil.SUCCESS;

import android.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IState;
import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.SoftApManager;
import com.android.server.wifi.SoftApModeConfiguration;
import com.android.server.wifi.WifiApConfigStore;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.InterfaceCallback;
import com.android.server.wifi.WifiNative.SoftApListener;
import com.android.server.wifi.util.ApConfigUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import mediatek.net.wifi.HotspotClient;
import mediatek.net.wifi.WifiHotspotManager;

/**
 * Manage WiFi in AP mode.
 * The internal state machine runs under "WifiStateMachine" thread context.
 */
public class MtkSoftApManager extends SoftApManager {
    private static final String TAG = "MtkSoftApManager";

    // Minimum limit to use for timeout delay if the value from overlay setting is too small.
    private static final int MIN_SOFT_AP_TIMEOUT_DELAY_MS = 600_000;  // 10 minutes

    @VisibleForTesting
    public static final String SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG = TAG
            + " Soft AP Send Message Timeout";

    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final WifiNative mWifiNative;

    private final String mCountryCode;

    private final SoftApStateMachine mStateMachine;

    private final WifiManager.SoftApCallback mCallback;

    private String mApInterfaceName;
    private boolean mIfaceIsUp;

    private final WifiApConfigStore mWifiApConfigStore;

    private final WifiMetrics mWifiMetrics;

    private final int mMode;
    private WifiConfiguration mApConfig;

    private int mReportedFrequency = -1;
    private int mReportedBandwidth = -1;

    private int mNumAssociatedStations = 0;
    private boolean mTimeoutEnabled = false;

    /**
     * Listener for soft AP events.
     */
    private final SoftApListener mSoftApListener = new SoftApListener() {
        @Override
        public void onNumAssociatedStationsChanged(int numStations) {
            mStateMachine.sendMessage(
                    SoftApStateMachine.CMD_NUM_ASSOCIATED_STATIONS_CHANGED, numStations);
        }

        @Override
        public void onSoftApChannelSwitched(int frequency, int bandwidth) {
            mStateMachine.sendMessage(
                    SoftApStateMachine.CMD_SOFT_AP_CHANNEL_SWITCHED, frequency, bandwidth);
        }
    };

    // M: Wi-Fi Hotspot Manager
    static final int BASE = Protocol.BASE_WIFI;
    public static final int M_CMD_BLOCK_CLIENT                 = BASE + 300;
    public static final int M_CMD_UNBLOCK_CLIENT               = BASE + 301;
    public static final int M_CMD_GET_CLIENTS_LIST             = BASE + 302;
    public static final int M_CMD_START_AP_WPS                 = BASE + 303;
    public static final int M_CMD_IS_ALL_DEVICES_ALLOWED       = BASE + 304;
    public static final int M_CMD_SET_ALL_DEVICES_ALLOWED      = BASE + 305;
    public static final int M_CMD_ALLOW_DEVICE                 = BASE + 306;
    public static final int M_CMD_DISALLOW_DEVICE              = BASE + 307;
    public static final int M_CMD_GET_ALLOWED_DEVICES          = BASE + 308;

    private HashMap<String, HotspotClient> mHotspotClients =
                                                    new HashMap<String, HotspotClient>();

    private static LinkedHashMap<String, HotspotClient> sAllowedDevices;
    private static final String ALLOWED_LIST_FILE =
            Environment.getDataDirectory() + "/misc/wifi/allowed_list.conf";

    // M: Need to stop softap when p2p is connected for STA+SAP case.
    private Looper mLooper;
    private final BroadcastReceiver mWifiP2pReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                boolean p2pIsConnected = networkInfo.isConnected();
                Log.d(TAG, "[STA+SAP] Received WIFI_P2P_CONNECTION_CHANGED_ACTION: isConnected = "
                        + p2pIsConnected);
                if (p2pIsConnected) {
                    Log.d(TAG, "[STA+SAP] Stop softap due to p2p is connected");
                    WifiManager wifiManager =
                            (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.stopSoftAp();
                }
            }
        }
    };

    public MtkSoftApManager(@NonNull Context context,
                            @NonNull Looper looper,
                            @NonNull FrameworkFacade framework,
                            @NonNull WifiNative wifiNative,
                            String countryCode,
                            @NonNull WifiManager.SoftApCallback callback,
                            @NonNull WifiApConfigStore wifiApConfigStore,
                            @NonNull SoftApModeConfiguration apConfig,
                            @NonNull WifiMetrics wifiMetrics) {
        // M: Wi-Fi Hotspot Manager
        super(context, looper, framework, wifiNative, countryCode, callback, wifiApConfigStore,
                apConfig, wifiMetrics);
        mLooper = looper;

        mContext = context;
        mFrameworkFacade = framework;
        mWifiNative = wifiNative;
        mCountryCode = countryCode;
        mCallback = callback;
        mWifiApConfigStore = wifiApConfigStore;
        mMode = apConfig.getTargetMode();
        WifiConfiguration config = apConfig.getWifiConfiguration();
        if (config == null) {
            mApConfig = mWifiApConfigStore.getApConfiguration();
        } else {
            mApConfig = config;
        }
        mWifiMetrics = wifiMetrics;
        mStateMachine = new SoftApStateMachine(looper);
    }

    /**
     * Start soft AP with the supplied config.
     */
    public void start() {
        mStateMachine.sendMessage(SoftApStateMachine.CMD_START, mApConfig);
    }

    /**
     * Stop soft AP.
     */
    public void stop() {
        Log.d(TAG, " currentstate: " + getCurrentStateName());
        if (mApInterfaceName != null) {
            if (mIfaceIsUp) {
                updateApState(WifiManager.WIFI_AP_STATE_DISABLING,
                        WifiManager.WIFI_AP_STATE_ENABLED, 0);
            } else {
                updateApState(WifiManager.WIFI_AP_STATE_DISABLING,
                        WifiManager.WIFI_AP_STATE_ENABLING, 0);
            }
        }
        mStateMachine.quitNow();
    }

    /**
     * Dump info about this softap manager.
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of SoftApManager--");

        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mApInterfaceName: " + mApInterfaceName);
        pw.println("mIfaceIsUp: " + mIfaceIsUp);
        pw.println("mMode: " + mMode);
        pw.println("mCountryCode: " + mCountryCode);
        if (mApConfig != null) {
            pw.println("mApConfig.SSID: " + mApConfig.SSID);
            pw.println("mApConfig.apBand: " + mApConfig.apBand);
            pw.println("mApConfig.hiddenSSID: " + mApConfig.hiddenSSID);
        } else {
            pw.println("mApConfig: null");
        }
        pw.println("mNumAssociatedStations: " + mNumAssociatedStations);
        pw.println("mTimeoutEnabled: " + mTimeoutEnabled);
        pw.println("mReportedFrequency: " + mReportedFrequency);
        pw.println("mReportedBandwidth: " + mReportedBandwidth);
    }

    private String getCurrentStateName() {
        IState currentState = mStateMachine.getCurrentState();

        if (currentState != null) {
            return currentState.getName();
        }

        return "StateMachine not active";
    }

    // M: Wi-Fi Hotspot Manager
    public List<HotspotClient> getHotspotClientsList() {
        List<HotspotClient> clients = new ArrayList<HotspotClient>();
        synchronized (mHotspotClients) {
            for (HotspotClient client : mHotspotClients.values()) {
                clients.add(new HotspotClient(client));
            }
        }
        return clients;
    }

    public boolean syncBlockClient(HotspotClient client) {
        boolean result;
        synchronized (mHotspotClients) {
            result = MtkHostapdHal.blockClient(client.deviceAddress);
            if (result) {
                HotspotClient cli =
                        mHotspotClients.get(client.deviceAddress);
                if (cli != null) {
                    cli.isBlocked = true;
                } else {
                    Log.e(TAG, "Failed to get " + client.deviceAddress);
                }
                sendClientsChangedBroadcast();
            } else {
                Log.e(TAG, "Failed to block " + client.deviceAddress);
            }
        }
        return result;
    }

    public boolean syncUnblockClient(HotspotClient client) {
        boolean result;
        synchronized (mHotspotClients) {
            result = MtkHostapdHal.unblockClient(client.deviceAddress);
            if (result) {
                mHotspotClients.remove(client.deviceAddress);
                sendClientsChangedBroadcast();
            } else {
                Log.e(TAG, "Failed to unblock " + client.deviceAddress);
            }
        }
        return result;
    }

    public boolean syncSetAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        if (!enabled) {
            synchronized (mHotspotClients) {
                initAllowedListIfNecessary();
                if (allowAllConnectedDevices && mHotspotClients.size() > 0) {
                    String content = "";
                    for (HotspotClient client : mHotspotClients.values()) {
                        if (!client.isBlocked &&
                                !sAllowedDevices.containsKey(client.deviceAddress)) {
                            sAllowedDevices.put(client.deviceAddress, new HotspotClient(client));
                            content += client.deviceAddress + "\n";
                        }
                    }

                    if (!content.equals("")) {
                        writeAllowedList();
                        updateAcceptMacFile(content);
                    }
                }
            }
        }

        return MtkHostapdHal.setAllDevicesAllowed(enabled);
    }

    public static void addDeviceToAllowedList(HotspotClient device) {
        Log.d(TAG, "addDeviceToAllowedList device = " + device +
            ", is name null?" + (device.name == null));
        initAllowedListIfNecessary();
        if (!sAllowedDevices.containsKey(device.deviceAddress)) {
            sAllowedDevices.put(device.deviceAddress, device);
        }
        writeAllowedList();
    }

    public void syncAllowDevice(String address) {
        updateAcceptMacFile(address);
    }

    public static void removeDeviceFromAllowedList(String address) {
        Log.d(TAG, "removeDeviceFromAllowedList address = " + address);
        initAllowedListIfNecessary();
        sAllowedDevices.remove(address);
        writeAllowedList();
    }

    public void syncDisallowDevice(String address) {
        updateAcceptMacFile("-" + address);
    }

    public static List<HotspotClient> getAllowedDevices() {
        Log.d(TAG, "getAllowedDevices");
        initAllowedListIfNecessary();
        List<HotspotClient> devices = new ArrayList<HotspotClient>();
        for (HotspotClient device : sAllowedDevices.values()) {
            devices.add(new HotspotClient(device));
            Log.d(TAG, "device = " + device);
        }
        return devices;
    }

    private static void initAllowedListIfNecessary() {
        if (sAllowedDevices == null) {
            sAllowedDevices = new LinkedHashMap<String, HotspotClient>();

            try {
                BufferedReader br = new BufferedReader(new FileReader(ALLOWED_LIST_FILE));
                String line = br.readLine();
                while (line != null) {
                    String[] result = line.split("\t");
                    if (result == null) {
                        continue;
                    }
                    String address = result[0];
                    boolean blocked = result[1].equals("1") ? true : false;
                    String name = result.length == 3 ? result[2] : "";
                    sAllowedDevices.put(address, new HotspotClient(address, blocked, name));
                    line = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString(), new Throwable("initAllowedListIfNecessary"));
            }
        }
    }

    private static void writeAllowedList() {
        String content = "";
        for (HotspotClient device : sAllowedDevices.values()) {
            String blocked = device.isBlocked == true ? "1" : "0";
            if (device.name != null) {
                content += device.deviceAddress + "\t" + blocked + "\t" + device.name + "\n";
            } else {
                content += device.deviceAddress + "\t" + blocked + "\n";
            }
        }

        Log.d(TAG, "writeAllowedLis content = " + content);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ALLOWED_LIST_FILE));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), new Throwable("writeAllowedList"));
        }
    }

    private void updateAcceptMacFile(String content) {
        Log.d(TAG, "updateAllowedList content = " + content);
        MtkHostapdHal.updateAllowedList(content);
    }

    /**
     * Update AP state.
     * @param newState new AP state
     * @param currentState current AP state
     * @param reason Failure reason if the new AP state is in failure state
     */
    private void updateApState(int newState, int currentState, int reason) {
        mCallback.onStateChanged(newState, reason);

        //send the AP state change broadcast
        final Intent intent = new Intent(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_WIFI_AP_STATE, newState);
        intent.putExtra(WifiManager.EXTRA_PREVIOUS_WIFI_AP_STATE, currentState);
        if (newState == WifiManager.WIFI_AP_STATE_FAILED) {
            //only set reason number when softAP start failed
            intent.putExtra(WifiManager.EXTRA_WIFI_AP_FAILURE_REASON, reason);
        }

        intent.putExtra(WifiManager.EXTRA_WIFI_AP_INTERFACE_NAME, mApInterfaceName);
        intent.putExtra(WifiManager.EXTRA_WIFI_AP_MODE, mMode);
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /**
     * Start a soft AP instance with the given configuration.
     * @param config AP configuration
     * @return integer result code
     */
    private int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return ERROR_GENERIC;
        }

        // Make a copy of configuration for updating AP band and channel.
        WifiConfiguration localConfig = new WifiConfiguration(config);

        // M: Move setCountryCodeHal forward for getting the correct 5G channel list
        // Setup country code if it is provided.
        if (mCountryCode != null) {
            // Country code is mandatory for 5GHz band, return an error if failed to set
            // country code when AP is configured for 5GHz band.
            if (!mWifiNative.setCountryCodeHal(
                    mApInterfaceName, mCountryCode.toUpperCase(Locale.ROOT))
                    && config.apBand == WifiConfiguration.AP_BAND_5GHZ) {
                Log.e(TAG, "Failed to set country code, required for setting up "
                        + "soft ap in 5GHz");
                return ERROR_GENERIC;
            }
        }

        // M: Need to config channel when starting softap for STA+SAP case.
        WifiManager wifiManager =
                (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getCurrentNetwork() != null) {
            int staChannel = ApConfigUtil.convertFrequencyToChannel(
                    wifiManager.getConnectionInfo().getFrequency());
            Log.e(TAG, "[STA+SAP] Need to config channel for STA+SAP case"
                    + ", getCurrentNetwork = " + wifiManager.getCurrentNetwork()
                    + ", staChannel = " + staChannel
                    + ", Build.HARDWARE = " + Build.HARDWARE);
            if (Build.HARDWARE.equals("mt6779")) {
                if ((staChannel >= 1 && staChannel <= 14
                            && localConfig.apBand == WifiConfiguration.AP_BAND_2GHZ)
                        || (staChannel >= 34
                            && localConfig.apBand == WifiConfiguration.AP_BAND_5GHZ)) {
                    // M: SCC
                    localConfig.apChannel = staChannel;
                }
                // M: DBDC
            } else {
                // M: SCC
                if (staChannel >= 1 && staChannel <= 14) {
                    localConfig.apBand = WifiConfiguration.AP_BAND_2GHZ;
                    localConfig.apChannel = staChannel;
                } else if (staChannel >= 34) {
                    localConfig.apBand = WifiConfiguration.AP_BAND_5GHZ;
                    localConfig.apChannel = staChannel;
                }
            }
            Log.e(TAG, "[STA+SAP] apBand = " + localConfig.apBand
                    + ", apChannel = " + localConfig.apChannel);
        }

        // M: Need to disconnect p2p when starting softap for STA+SAP case.
        if (mWifiNative.getClientInterfaceName() != null) {
            WifiP2pManager wifiP2pManager =
                    (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
            Channel wifiP2pChannel = wifiP2pManager.initialize(mContext, mLooper, null);
            wifiP2pManager.removeGroup(wifiP2pChannel, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "[STA+SAP] Disconnect p2p successfully");
                }

                @Override
                public void onFailure(int reason) {
                    Log.i(TAG, "[STA+SAP] Disconnect p2p failed, reason = " + reason);
                }
            });
            wifiP2pManager.cancelConnect(wifiP2pChannel, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "[STA+SAP] Cancel connect p2p successfully");
                }

                @Override
                public void onFailure(int reason) {
                    Log.i(TAG, "[STA+SAP] Cancel connect p2p failed, reason = " + reason);
                }
            });
        }

        int result = ApConfigUtil.updateApChannelConfig(
                mWifiNative, mCountryCode,
                mWifiApConfigStore.getAllowed2GChannel(), localConfig);

        if (result != SUCCESS) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        }

        if (localConfig.hiddenSSID) {
            Log.d(TAG, "SoftAP is a hidden network");
        }

        // M: Fix channel for testing
        String fixChannelString = SystemProperties.get("wifi.tethering.channel");
        int fixChannel = -1;
        if (fixChannelString != null && fixChannelString.length() > 0) {
            fixChannel = Integer.parseInt(fixChannelString);
            if (fixChannel >= 0) {
                localConfig.apChannel = fixChannel;
            }
        }

        if (!mWifiNative.startSoftAp(mApInterfaceName, localConfig, mSoftApListener)) {
            Log.e(TAG, "Soft AP start failed");
            return ERROR_GENERIC;
        }

        // M: Wi-Fi Hotspot Manager
        MtkHostapdHalCallback callback = new MtkHostapdHalCallback();
        if (!MtkHostapdHal.registerCallback(callback)) {
            Log.d(TAG, "Failed to register MtkHostapdHalCallback");
            return ERROR_GENERIC;
        }

        Log.d(TAG, "Soft AP is started");

        return SUCCESS;
    }

    /**
     * Teardown soft AP and teardown the interface.
     */
    private void stopSoftAp() {
        mWifiNative.teardownInterface(mApInterfaceName);
        Log.d(TAG, "Soft AP is stopped");
    }

    // M: Wi-Fi Hotspot Manager
    private void sendClientsChangedBroadcast() {
        Intent intent = new Intent(WifiHotspotManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendClientsIpReadyBroadcast(String mac, String ip, String deviceName) {
        Intent intent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_IP_READY");
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiHotspotManager.EXTRA_DEVICE_ADDRESS, mac);
        intent.putExtra(WifiHotspotManager.EXTRA_IP_ADDRESS, ip);
        intent.putExtra(WifiHotspotManager.EXTRA_DEVICE_NAME, deviceName);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private class SoftApStateMachine extends StateMachine {
        // Commands for the state machine.
        public static final int CMD_START = 0;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_NUM_ASSOCIATED_STATIONS_CHANGED = 4;
        public static final int CMD_NO_ASSOCIATED_STATIONS_TIMEOUT = 5;
        public static final int CMD_TIMEOUT_TOGGLE_CHANGED = 6;
        public static final int CMD_INTERFACE_DESTROYED = 7;
        public static final int CMD_INTERFACE_DOWN = 8;
        public static final int CMD_SOFT_AP_CHANNEL_SWITCHED = 9;

        // M: Wi-Fi Hotspot Manager
        public static final int CMD_POLL_IP_ADDRESS = 100;

        /* Should be the same with WifiStateMachine */
        private static final int Wifi_SUCCESS = 1;
        private static final int Wifi_FAILURE = -1;

        private static final int POLL_IP_ADDRESS_INTERVAL_MSECS = 2000;
        private static final int POLL_IP_TIMES = 15;

        private final WifiManager mWifiManager;

        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();

        private final InterfaceCallback mWifiNativeInterfaceCallback = new InterfaceCallback() {
            @Override
            public void onDestroyed(String ifaceName) {
                if (mApInterfaceName != null && mApInterfaceName.equals(ifaceName)) {
                    sendMessage(CMD_INTERFACE_DESTROYED);
                }
            }

            @Override
            public void onUp(String ifaceName) {
                if (mApInterfaceName != null && mApInterfaceName.equals(ifaceName)) {
                    sendMessage(CMD_INTERFACE_STATUS_CHANGED, 1);
                }
            }

            @Override
            public void onDown(String ifaceName) {
                if (mApInterfaceName != null && mApInterfaceName.equals(ifaceName)) {
                    sendMessage(CMD_INTERFACE_STATUS_CHANGED, 0);
                }
            }
        };

        SoftApStateMachine(Looper looper) {
            super(TAG, looper);

            addState(mIdleState);
            addState(mStartedState);

            setInitialState(mIdleState);
            start();

            // M: Wi-Fi Hotspot Manager
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }

        private class IdleState extends State {
            @Override
            public void enter() {
                mApInterfaceName = null;
                mIfaceIsUp = false;
            }

            @Override
            public boolean processMessage(Message message) {
                switch (message.what) {
                    case CMD_START:
                        mApInterfaceName = mWifiNative.setupInterfaceForSoftApMode(
                                mWifiNativeInterfaceCallback);
                        if (TextUtils.isEmpty(mApInterfaceName)) {
                            Log.e(TAG, "setup failure when creating ap interface.");
                            updateApState(WifiManager.WIFI_AP_STATE_FAILED,
                                    WifiManager.WIFI_AP_STATE_DISABLED,
                                    WifiManager.SAP_START_FAILURE_GENERAL);
                            mWifiMetrics.incrementSoftApStartResult(
                                    false, WifiManager.SAP_START_FAILURE_GENERAL);
                            break;
                        }
                        updateApState(WifiManager.WIFI_AP_STATE_ENABLING,
                                WifiManager.WIFI_AP_STATE_DISABLED, 0);
                        int result = startSoftAp((WifiConfiguration) message.obj);
                        if (result != SUCCESS) {
                            int failureReason = WifiManager.SAP_START_FAILURE_GENERAL;
                            if (result == ERROR_NO_CHANNEL) {
                                failureReason = WifiManager.SAP_START_FAILURE_NO_CHANNEL;
                            }
                            updateApState(WifiManager.WIFI_AP_STATE_FAILED,
                                          WifiManager.WIFI_AP_STATE_ENABLING,
                                          failureReason);
                            stopSoftAp();
                            mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                            break;
                        }

                        // M: Wi-Fi Hotspot Manager
                        MtkWifiApMonitor.registerHandler(
                                mApInterfaceName,
                                MtkWifiApMonitor.AP_STA_CONNECTED_EVENT,
                                getHandler());
                        MtkWifiApMonitor.registerHandler(
                                mApInterfaceName,
                                MtkWifiApMonitor.AP_STA_DISCONNECTED_EVENT,
                                getHandler());
                        MtkWifiApMonitor.startMonitoring(mApInterfaceName);

                        transitionTo(mStartedState);
                        break;
                    default:
                        // Ignore all other commands.
                        break;
                }

                return HANDLED;
            }
        }

        private class StartedState extends State {
            private int mTimeoutDelay;
            private WakeupMessage mSoftApTimeoutMessage;
            private SoftApTimeoutEnabledSettingObserver mSettingObserver;

            /**
            * Observer for timeout settings changes.
            */
            private class SoftApTimeoutEnabledSettingObserver extends ContentObserver {
                SoftApTimeoutEnabledSettingObserver(Handler handler) {
                    super(handler);
                }

                public void register() {
                    mFrameworkFacade.registerContentObserver(mContext,
                            Settings.Global.getUriFor(Settings.Global.SOFT_AP_TIMEOUT_ENABLED),
                            true, this);
                    mTimeoutEnabled = getValue();
                }

                public void unregister() {
                    mFrameworkFacade.unregisterContentObserver(mContext, this);
                }

                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    mStateMachine.sendMessage(SoftApStateMachine.CMD_TIMEOUT_TOGGLE_CHANGED,
                            getValue() ? 1 : 0);
                }

                private boolean getValue() {
                    boolean enabled = mFrameworkFacade.getIntegerSetting(mContext,
                            Settings.Global.SOFT_AP_TIMEOUT_ENABLED, 1) == 1;
                    return enabled;
                }
            }

            private int getConfigSoftApTimeoutDelay() {
                int delay = mContext.getResources().getInteger(
                        R.integer.config_wifi_framework_soft_ap_timeout_delay);
                if (delay < MIN_SOFT_AP_TIMEOUT_DELAY_MS) {
                    delay = MIN_SOFT_AP_TIMEOUT_DELAY_MS;
                    Log.w(TAG, "Overriding timeout delay with minimum limit value");
                }
                Log.d(TAG, "Timeout delay: " + delay);
                return delay;
            }

            private void scheduleTimeoutMessage() {
                if (!mTimeoutEnabled) {
                    return;
                }
                mSoftApTimeoutMessage.schedule(SystemClock.elapsedRealtime() + mTimeoutDelay);
                Log.d(TAG, "Timeout message scheduled");
            }

            private void cancelTimeoutMessage() {
                mSoftApTimeoutMessage.cancel();
                Log.d(TAG, "Timeout message canceled");
            }

            /**
             * Set number of stations associated with this soft AP
             * @param numStations Number of connected stations
             */
            private void setNumAssociatedStations(int numStations) {
                if (mNumAssociatedStations == numStations) {
                    return;
                }
                mNumAssociatedStations = numStations;
                Log.d(TAG, "Number of associated stations changed: " + mNumAssociatedStations);

                if (mCallback != null) {
                    mCallback.onNumClientsChanged(mNumAssociatedStations);
                } else {
                    Log.e(TAG, "SoftApCallback is null. Dropping NumClientsChanged event.");
                }
                mWifiMetrics.addSoftApNumAssociatedStationsChangedEvent(mNumAssociatedStations,
                        mMode);

                if (mNumAssociatedStations == 0) {
                    scheduleTimeoutMessage();
                } else {
                    cancelTimeoutMessage();
                }
            }

            private void onUpChanged(boolean isUp) {
                if (isUp == mIfaceIsUp) {
                    return;  // no change
                }
                mIfaceIsUp = isUp;
                if (isUp) {
                    Log.d(TAG, "SoftAp is ready for use");
                    updateApState(WifiManager.WIFI_AP_STATE_ENABLED,
                            WifiManager.WIFI_AP_STATE_ENABLING, 0);
                    mWifiMetrics.incrementSoftApStartResult(true, 0);
                    if (mCallback != null) {
                        mCallback.onNumClientsChanged(mNumAssociatedStations);
                    }
                } else {
                    // the interface was up, but goes down
                    sendMessage(CMD_INTERFACE_DOWN);
                }
                mWifiMetrics.addSoftApUpChangedEvent(isUp, mMode);
            }

            @Override
            public void enter() {
                mIfaceIsUp = false;
                onUpChanged(mWifiNative.isInterfaceUp(mApInterfaceName));

                mTimeoutDelay = getConfigSoftApTimeoutDelay();
                Handler handler = mStateMachine.getHandler();
                mSoftApTimeoutMessage = new WakeupMessage(mContext, handler,
                        SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG,
                        SoftApStateMachine.CMD_NO_ASSOCIATED_STATIONS_TIMEOUT);
                mSettingObserver = new SoftApTimeoutEnabledSettingObserver(handler);

                if (mSettingObserver != null) {
                    mSettingObserver.register();
                }
                Log.d(TAG, "Resetting num stations on start");
                mNumAssociatedStations = 0;
                scheduleTimeoutMessage();

                // M: STA+SAP
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                mContext.registerReceiver(mWifiP2pReceiver, intentFilter);
            }

            @Override
            public void exit() {
                if (mApInterfaceName != null) {
                    stopSoftAp();
                }
                if (mSettingObserver != null) {
                    mSettingObserver.unregister();
                }
                Log.d(TAG, "Resetting num stations on stop");
                mNumAssociatedStations = 0;
                cancelTimeoutMessage();
                // Need this here since we are exiting |Started| state and won't handle any
                // future CMD_INTERFACE_STATUS_CHANGED events after this point
                mWifiMetrics.addSoftApUpChangedEvent(false, mMode);
                updateApState(WifiManager.WIFI_AP_STATE_DISABLED,
                        WifiManager.WIFI_AP_STATE_DISABLING, 0);

                // M: Wi-Fi Hotspot Manager
                MtkWifiApMonitor.deregisterAllHandler();
                MtkWifiApMonitor.stopMonitoring(mApInterfaceName);
                synchronized (mHotspotClients) {
                    mHotspotClients.clear();
                }
                sendClientsChangedBroadcast();

                // M: STA+SAP
                mContext.unregisterReceiver(mWifiP2pReceiver);

                mApInterfaceName = null;
                mIfaceIsUp = false;
                mStateMachine.quitNow();
            }

            @Override
            public boolean processMessage(Message message) {
                switch (message.what) {
                    case CMD_NUM_ASSOCIATED_STATIONS_CHANGED:
                        if (message.arg1 < 0) {
                            Log.e(TAG, "Invalid number of associated stations: " + message.arg1);
                            break;
                        }
                        Log.d(TAG, "Setting num stations on CMD_NUM_ASSOCIATED_STATIONS_CHANGED");
                        setNumAssociatedStations(message.arg1);
                        break;
                    case CMD_SOFT_AP_CHANNEL_SWITCHED:
                        mReportedFrequency = message.arg1;
                        mReportedBandwidth = message.arg2;
                        Log.d(TAG, "Channel switched. Frequency: " + mReportedFrequency
                                + " Bandwidth: " + mReportedBandwidth);
                        mWifiMetrics.addSoftApChannelSwitchedEvent(mReportedFrequency,
                                mReportedBandwidth, mMode);
                        int[] allowedChannels = new int[0];
                        if (mApConfig.apBand == WifiConfiguration.AP_BAND_2GHZ) {
                            allowedChannels =
                                    mWifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_24_GHZ);
                        } else if (mApConfig.apBand == WifiConfiguration.AP_BAND_5GHZ) {
                            allowedChannels =
                                    mWifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_5_GHZ);
                        } else if (mApConfig.apBand == WifiConfiguration.AP_BAND_ANY) {
                            int[] allowed2GChannels =
                                    mWifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_24_GHZ);
                            int[] allowed5GChannels =
                                    mWifiNative.getChannelsForBand(WifiScanner.WIFI_BAND_5_GHZ);
                            allowedChannels = Stream.concat(
                                    Arrays.stream(allowed2GChannels).boxed(),
                                    Arrays.stream(allowed5GChannels).boxed())
                                    .mapToInt(Integer::valueOf)
                                    .toArray();
                        }
                        if (!ArrayUtils.contains(allowedChannels, mReportedFrequency)) {
                            Log.e(TAG, "Channel does not satisfy user band preference: "
                                    + mReportedFrequency);
                            mWifiMetrics.incrementNumSoftApUserBandPreferenceUnsatisfied();
                        }
                        break;
                    case CMD_TIMEOUT_TOGGLE_CHANGED:
                        boolean isEnabled = (message.arg1 == 1);
                        if (mTimeoutEnabled == isEnabled) {
                            break;
                        }
                        mTimeoutEnabled = isEnabled;
                        if (!mTimeoutEnabled) {
                            cancelTimeoutMessage();
                        }
                        if (mTimeoutEnabled && mNumAssociatedStations == 0) {
                            scheduleTimeoutMessage();
                        }
                        break;
                    case CMD_INTERFACE_STATUS_CHANGED:
                        boolean isUp = message.arg1 == 1;
                        onUpChanged(isUp);
                        break;
                    case CMD_START:
                        // Already started, ignore this command.
                        break;
                    case CMD_NO_ASSOCIATED_STATIONS_TIMEOUT:
                        if (!mTimeoutEnabled) {
                            Log.wtf(TAG, "Timeout message received while timeout is disabled."
                                    + " Dropping.");
                            break;
                        }
                        if (mNumAssociatedStations != 0) {
                            Log.wtf(TAG, "Timeout message received but has clients. Dropping.");
                            break;
                        }
                        Log.i(TAG, "Timeout message received. Stopping soft AP.");
                        updateApState(WifiManager.WIFI_AP_STATE_DISABLING,
                                WifiManager.WIFI_AP_STATE_ENABLED, 0);
                        transitionTo(mIdleState);
                        break;
                    case CMD_INTERFACE_DESTROYED:
                        Log.d(TAG, "Interface was cleanly destroyed.");
                        updateApState(WifiManager.WIFI_AP_STATE_DISABLING,
                                WifiManager.WIFI_AP_STATE_ENABLED, 0);
                        mApInterfaceName = null;
                        transitionTo(mIdleState);
                        break;
                    case CMD_INTERFACE_DOWN:
                        Log.w(TAG, "interface error, stop and report failure");
                        updateApState(WifiManager.WIFI_AP_STATE_FAILED,
                                WifiManager.WIFI_AP_STATE_ENABLED,
                                WifiManager.SAP_START_FAILURE_GENERAL);
                        updateApState(WifiManager.WIFI_AP_STATE_DISABLING,
                                WifiManager.WIFI_AP_STATE_FAILED, 0);
                        transitionTo(mIdleState);
                        break;
                    // M: Wi-Fi Hotspot Manager
                    case CMD_POLL_IP_ADDRESS:
                        String deviceAddress = (String) message.obj;
                        int count = message.arg1;
                        String ipAddress =
                            mWifiManager.getWifiHotspotManager().getClientIp(deviceAddress);
                        String deviceName =
                            mWifiManager.getWifiHotspotManager().getClientDeviceName(deviceAddress);
                        Log.d(TAG, "CMD_POLL_IP_ADDRESS ,deviceAddress = " +
                              message.obj + " ipAddress = " + ipAddress + ", count = " + count);
                        if (ipAddress == null && count < POLL_IP_TIMES) {
                            sendMessageDelayed(CMD_POLL_IP_ADDRESS, ++count, 0, deviceAddress,
                                               POLL_IP_ADDRESS_INTERVAL_MSECS);
                        } else if (ipAddress != null) {
                            sendClientsIpReadyBroadcast(deviceAddress, ipAddress, deviceName);
                        }
                        break;
                    case MtkWifiApMonitor.AP_STA_CONNECTED_EVENT:
                        Log.d(TAG, "AP STA CONNECTED:" + message.obj);
                        String address = (String) message.obj;
                        synchronized (mHotspotClients) {
                            if (!mHotspotClients.containsKey(address)) {
                                mHotspotClients.put(address, new HotspotClient(address, false));
                            }
                        }

                        int start = 1;
                        sendMessageDelayed(CMD_POLL_IP_ADDRESS, start, 0, address,
                                           POLL_IP_ADDRESS_INTERVAL_MSECS);

                        sendClientsChangedBroadcast();
                        break;
                    case MtkWifiApMonitor.AP_STA_DISCONNECTED_EVENT:
                        Log.d(TAG, "AP STA DISCONNECTED:" + message.obj);
                        address = (String) message.obj;
                        synchronized (mHotspotClients) {
                            HotspotClient client = mHotspotClients.get(address);
                            if (client != null && !client.isBlocked) {
                                mHotspotClients.remove(address);
                            }
                        }

                        sendClientsChangedBroadcast();
                        break;
                    default:
                        return NOT_HANDLED;
                }
                return HANDLED;
            }
        }
    }
}
