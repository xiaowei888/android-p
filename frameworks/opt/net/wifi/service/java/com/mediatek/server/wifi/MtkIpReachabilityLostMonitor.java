/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
*/

package com.mediatek.server.wifi;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.internal.util.IState;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiStateMachine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
 * IpReachabilityMonitor in AOSP is too sensitive to lead network disconnect,
 * MtkIpReachabilityLostMonitor only monitor IpReachabilityLost for 10s since
 * driver roaming occurred.
*/
public class MtkIpReachabilityLostMonitor implements Handler.Callback {

    private static final String TAG = "WifiStateMachine";

    private static final int CMD_IP_REACHABILITY_TIMEOUT = 1;

    /* IpReachabilityLost monitor for 10s since driver romaing */
    private static final int IP_REACHABILITY_TIMEOUT = 10 * 1000;

    // used to indicate that MtkIpReachabilityLostMonitor is under monitoring for ip reachability
    // lost
    private boolean mIsMonitoring = false;
    private String mLastBssid = null;

    private WifiStateMachine mWifiStateMachine;
    private WifiMonitor mWifiMonitor;
    private Handler mEventHandler;

    public MtkIpReachabilityLostMonitor(WifiStateMachine wsm, WifiMonitor wm, Looper looper) {
        mWifiStateMachine = wsm;
        mWifiMonitor = wm;

        mEventHandler = new Handler(looper, this);

        mWifiStateMachine.setIpReachabilityDisconnectEnabled(false);
    }

    public void registerForWifiMonitorEvents() {
        /* Register NETWORK_CONNECTION_EVENT to monitor driver roaming*/
        mWifiMonitor.registerHandler(getInterfaceName(), WifiMonitor.NETWORK_CONNECTION_EVENT,
                mEventHandler);

        /* Register NETWORK_DISCONNECTION_EVENT to monitor driver roaming*/
        mWifiMonitor.registerHandler(getInterfaceName(), WifiMonitor.NETWORK_DISCONNECTION_EVENT,
                mEventHandler);
    }

    /// get function or variable by reflection @{
    private String getInterfaceName() {
        try {
            Field field = mWifiStateMachine.getClass().getDeclaredField("mInterfaceName");
            field.setAccessible(true);
            return (String) field.get(mWifiStateMachine);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getLogRecString(Message msg) {
        try {
            Method method = mWifiStateMachine
                    .getClass().getDeclaredMethod("getLogRecString", Message.class);
            method.setAccessible(true);
            return (String) method.invoke(mWifiStateMachine, msg);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return "";
        }

    }

    private IState getCurrentState() {
        try {
            Method method = StateMachine.class.getDeclaredMethod("getCurrentState");
            method.setAccessible(true);
            return (IState) method.invoke(mWifiStateMachine);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }
    /// }@

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, " MtkIpReachabilityLostMonitor " + getLogRecString(msg)
                + " IRM enable = " + mWifiStateMachine.getIpReachabilityDisconnectEnabled());
        switch (msg.what) {
            case WifiMonitor.NETWORK_CONNECTION_EVENT:
                // Check if the current state of WifiStateMachine is in ConnectedState
                boolean isInConnectedState = getCurrentState().getName().equals("ConnectedState");
                // If current state is in ConnectedState and current BSSID is different from
                // previous BSSID, it means that drvier roaming event occured
                if (isInConnectedState && mLastBssid != null && !mLastBssid.equals(msg.obj)) {
                    // If receives another driver roaming event in the monitoring period, cancel the
                    // current timer and restart new timer for IRM
                    if (mIsMonitoring) mEventHandler.removeMessages(CMD_IP_REACHABILITY_TIMEOUT);
                    mWifiStateMachine.setIpReachabilityDisconnectEnabled(true);
                    mEventHandler.sendMessageDelayed(
                            mEventHandler.obtainMessage(CMD_IP_REACHABILITY_TIMEOUT),
                                IP_REACHABILITY_TIMEOUT);
                    mIsMonitoring = true;
                    Log.d(TAG, "MtkIpReachabilityLostMonitor: enable IRM for driver roaming,"
                            + " mIsMonitoring = " + mIsMonitoring + ", mLastBssid = " + mLastBssid
                            + ", Current Bssid = " + (String) msg.obj);
                }
                mLastBssid = (String) msg.obj;
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                mLastBssid = null;
            case CMD_IP_REACHABILITY_TIMEOUT:
                mWifiStateMachine.setIpReachabilityDisconnectEnabled(false);
                mIsMonitoring = false;
                Log.d(TAG, "MtkIpReachabilityLostMonitor: disable IRM,"
                        + " mIsMonitoring = " + mIsMonitoring);
                break;
            default:
                // ignore unknown event
                break;
        }
        return true;
    }
}
