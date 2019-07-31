/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Listens for events from the hostapd server, and passes them on
 * to the MtkSoftApManager for handling.
 *
 * @hide
 */
public class MtkWifiApMonitor {
    private static final String TAG = "MtkWifiApMonitor";

    /* Supplicant events reported to a state machine */
    private static final int BASE = Protocol.BASE_WIFI_MONITOR;

    /* Connection to supplicant established */
    public static final int SUP_CONNECTION_EVENT                 = BASE + 1;
    /* Connection to supplicant lost */
    public static final int SUP_DISCONNECTION_EVENT              = BASE + 2;

    /* hostap events */
    public static final int AP_STA_DISCONNECTED_EVENT            = BASE + 41;
    public static final int AP_STA_CONNECTED_EVENT               = BASE + 42;

    // TODO(b/27569474) remove support for multiple handlers for the same event
    private static final Map<String, SparseArray<Set<Handler>>> sHandlerMap = new HashMap<>();

    /**
     * Registers a callback handler for the provided event.
     */
    public static synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = sHandlerMap.get(iface);
        if (ifaceHandlers == null) {
            ifaceHandlers = new SparseArray<>();
            sHandlerMap.put(iface, ifaceHandlers);
        }
        Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(what);
        if (ifaceWhatHandlers == null) {
            ifaceWhatHandlers = new ArraySet<>();
            ifaceHandlers.put(what, ifaceWhatHandlers);
        }
        ifaceWhatHandlers.add(handler);
    }

    /**
     * Deregisters all callback handlers.
     */
    public static synchronized void deregisterAllHandler() {
        sHandlerMap.clear();
    }

    private static final Map<String, Boolean> sMonitoringMap = new HashMap<>();
    private static boolean isMonitoring(String iface) {
        Boolean val = sMonitoringMap.get(iface);
        if (val == null) {
            return false;
        } else {
            return val.booleanValue();
        }
    }

    /**
     * Enable/Disable monitoring for the provided iface.
     *
     * @param iface Name of the iface.
     * @param enabled true to enable, false to disable.
     */
    @VisibleForTesting
    public static void setMonitoring(String iface, boolean enabled) {
        sMonitoringMap.put(iface, enabled);
    }

    private static void setMonitoringNone() {
        for (String iface : sMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }

    /**
     * Start Monitoring for hostapd events.
     *
     * @param iface Name of iface.
     */
    public static synchronized void startMonitoring(String iface) {
        Log.d(TAG, "startMonitoring(" + iface + ")");
        setMonitoring(iface, true);
        broadcastSupplicantConnectionEvent(iface);
    }

    /**
     * Stop Monitoring for hostapd events.
     *
     * @param iface Name of iface.
     */
    public static synchronized void stopMonitoring(String iface) {
        Log.d(TAG, "stopMonitoring(" + iface + ")");
        setMonitoring(iface, true);
        broadcastSupplicantDisconnectionEvent(iface);
        setMonitoring(iface, false);
    }

    /**
     * Stop Monitoring for hostapd events.
     */
    public static synchronized void stopAllMonitoring() {
        setMonitoringNone();
    }

    /**
     * Similar functions to Handler#sendMessage that send the message to the registered handler
     * for the given interface and message what.
     * All of these should be called with the WifiMonitor class lock
     */
    private static void sendMessage(String iface, int what) {
        sendMessage(iface, Message.obtain(null, what));
    }

    private static void sendMessage(String iface, int what, Object obj) {
        sendMessage(iface, Message.obtain(null, what, obj));
    }

    private static void sendMessage(String iface, int what, int arg1) {
        sendMessage(iface, Message.obtain(null, what, arg1, 0));
    }

    private static void sendMessage(String iface, int what, int arg1, int arg2) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2));
    }

    private static void sendMessage(String iface, int what, int arg1, int arg2, Object obj) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2, obj));
    }

    private static void sendMessage(String iface, Message message) {
        SparseArray<Set<Handler>> ifaceHandlers = sHandlerMap.get(iface);
        if (iface != null && ifaceHandlers != null) {
            if (isMonitoring(iface)) {
                Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(message.what);
                if (ifaceWhatHandlers != null) {
                    for (Handler handler : ifaceWhatHandlers) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            } else {
                Log.d(TAG, "Dropping event because (" + iface + ") is stopped");
            }
        } else {
            Log.d(TAG, "Sending to all monitors because there's no matching iface");
            for (Map.Entry<String, SparseArray<Set<Handler>>> entry : sHandlerMap.entrySet()) {
                if (isMonitoring(entry.getKey())) {
                    Set<Handler> ifaceWhatHandlers = entry.getValue().get(message.what);
                    for (Handler handler : ifaceWhatHandlers) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        }

        message.recycle();
    }

    private static void sendMessage(Handler handler, Message message) {
        message.setTarget(handler);
        message.sendToTarget();
    }

    /**
     * Broadcast the connection to wpa_supplicant event to all the handlers registered for
     * this event.
     *
     * @param iface Name of iface on which this occurred.
     */
    public static void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, SUP_CONNECTION_EVENT);
    }

    /**
     * Broadcast the loss of connection to wpa_supplicant event to all the handlers registered for
     * this event.
     *
     * @param iface Name of iface on which this occurred.
     */
    public static void broadcastSupplicantDisconnectionEvent(String iface) {
        sendMessage(iface, SUP_DISCONNECTION_EVENT);
    }

    /**
     * Broadcast AP STA connection event.
     *
     * @param iface Name of iface on which this occurred.
     * @param macAddress STA MAC address.
     */
    public static void broadcastApStaConnected(String iface, String macAddress) {
        sendMessage(iface, AP_STA_CONNECTED_EVENT, macAddress);
    }

    /**
     * Broadcast AP STA disconnection event.
     *
     * @param iface Name of iface on which this occurred.
     * @param macAddress STA MAC address.
     */
    public static void broadcastApStaDisconnected(String iface, String macAddress) {
        sendMessage(iface, AP_STA_DISCONNECTED_EVENT, macAddress);
    }
}
