package com.mediatek.server.wifi;

import android.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;

import com.android.internal.app.IBatteryStats;
import com.android.internal.util.Protocol;
import com.android.server.wifi.DefaultModeManager;
import com.android.server.wifi.SoftApModeConfiguration;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachinePrime;

import java.util.ArrayList;

public class MtkWifiStateMachinePrime extends WifiStateMachinePrime implements Handler.Callback {
    private static final String TAG = "WifiStateMachinePrime";
    public boolean mShouldDeferDisableWifi = false;
    private ArrayList<Message> mDeferredMsgInQueue;
    /* WFC status:
     * 0: Indicate Fwk no need to defer disable wifi process
     * 1: Indicate Fwk to defer disable wifi process if needed
     * 2: Indicate Fwk good to go
    */
    private static final int NO_NEED_DEFER = 0;
    private static final int NEED_DEFER = 1;
    private static final int WFC_NOTIFY_GO = 2;
    static final int MTK_BASE = Protocol.BASE_WIFI + 400;
    /* used to indicate that the notification from WFC */
    private static final int CMD_WFC_NOTIFY_GO            = MTK_BASE + 1;
    private static final int CMD_WFC_DEFER_OFF_TIMEOUT    = MTK_BASE + 2;
    private static final int CMD_WFC_GO_WIFI_OFF          = MTK_BASE + 3;
    private static final int CMD_WFC_GO_SCAN_MODE         = MTK_BASE + 4;
    private static final int CMD_WFC_GO_SOFT_AP           = MTK_BASE + 5;
    private static final int CMD_WFC_GO_SHUT_DOWN         = MTK_BASE + 6;
    private static final int CMD_WFC_DEFER_WIFI_ON        = MTK_BASE + 7;
    private static final int WFC_TIMEOUT = 3000;
    private final Context mContext;
    private Handler mEventHandler;
    private static boolean mWaitForEvent = false;
    private SoftApModeConfiguration mWifiConfig;

    public MtkWifiStateMachinePrime(WifiInjector wifiInjector,
                             Context context,
                             Looper looper,
                             WifiNative wifiNative,
                             DefaultModeManager defaultModeManager,
                             IBatteryStats batteryStats) {
        super(wifiInjector, context, looper, wifiNative, defaultModeManager, batteryStats);

        mDeferredMsgInQueue = new ArrayList();
        mEventHandler = new Handler(looper, this);
        mContext = context;
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int wfcStatus = intent.getIntExtra("wfc_status", NO_NEED_DEFER);
                        switch (wfcStatus) {
                            case NO_NEED_DEFER:
                                Log.d(TAG, "Received WFC_STATUS_CHANGED, status: NO_NEED_DEFER");
                                mShouldDeferDisableWifi = false;
                                break;
                            case NEED_DEFER:
                                Log.d(TAG, "Received WFC_STATUS_CHANGED, status: NEED_DEFER");
                                mShouldDeferDisableWifi = true;
                                break;
                            case WFC_NOTIFY_GO:
                                Log.d(TAG, "Received WFC_STATUS_CHANGED, status: WFC_NOTIFY_GO");
                                if (mShouldDeferDisableWifi) {
                                    mEventHandler.sendMessage(
                                            mEventHandler.obtainMessage(CMD_WFC_NOTIFY_GO));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                },
                new IntentFilter("com.mediatek.intent.action.WFC_STATUS_CHANGED"));
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CMD_WFC_DEFER_OFF_TIMEOUT:
                Log.d(TAG, "WFC Defer Wi-Fi off timeout");
            case CMD_WFC_NOTIFY_GO:
                for (int i = 0; i < mDeferredMsgInQueue.size(); i++) {
                    Log.d(TAG, "mDeferredMsgInQueue: " + mDeferredMsgInQueue.get(i));
                    Message copyMsg = mEventHandler.obtainMessage();
                    copyMsg.copyFrom(mDeferredMsgInQueue.get(i));
                    mEventHandler.sendMessage(copyMsg);
                }
                mDeferredMsgInQueue.clear();
                mWaitForEvent = false;
                break;
            case CMD_WFC_GO_WIFI_OFF:
                super.disableWifi();
                break;
            case CMD_WFC_GO_SCAN_MODE:
                super.enterScanOnlyMode();
                break;
            case CMD_WFC_GO_SOFT_AP:
                super.enterSoftAPMode(mWifiConfig);
                break;
            case CMD_WFC_GO_SHUT_DOWN:
                super.shutdownWifi();
                break;
            case CMD_WFC_DEFER_WIFI_ON:
                super.enterClientMode();
                break;
            default:
                Log.e(TAG, "Unhandle message");
                break;
        }
        return true;
    }

    @Override
    public void enterClientMode() {
        if (!mWaitForEvent) {
            super.enterClientMode();
            return;
        }
        Log.d(TAG, "enterClientMode, mWaitForEvent " + mWaitForEvent);
        mDeferredMsgInQueue.add(mEventHandler.obtainMessage(CMD_WFC_DEFER_WIFI_ON));
    }

    @Override
    public void disableWifi() {
        if (!mShouldDeferDisableWifi) {
            super.disableWifi();
            return;
        }
        Log.d(TAG, "disableWifi, mShouldDeferDisableWifi: " + mShouldDeferDisableWifi);
        mWaitForEvent = true;
        //Set wifi state disabling to notify WFC
        updateWifiState(WifiManager.WIFI_STATE_DISABLING, WifiManager.WIFI_STATE_ENABLED);
        mDeferredMsgInQueue.add(mEventHandler.obtainMessage(CMD_WFC_GO_WIFI_OFF));
        mEventHandler.sendMessageDelayed(
                mEventHandler.obtainMessage(CMD_WFC_DEFER_OFF_TIMEOUT), WFC_TIMEOUT);
    }

    @Override
    public void enterScanOnlyMode() {
        if (!mShouldDeferDisableWifi) {
            super.enterScanOnlyMode();
            return;
        }
        Log.d(TAG, "enterScanOnlyMode, mShouldDeferDisableWifi: " + mShouldDeferDisableWifi);
        mWaitForEvent = true;
        //Set wifi state disabling to notify WFC
        updateWifiState(WifiManager.WIFI_STATE_DISABLING, WifiManager.WIFI_STATE_ENABLED);
        mDeferredMsgInQueue.add(mEventHandler.obtainMessage(CMD_WFC_GO_SCAN_MODE));
        mEventHandler.sendMessageDelayed(
                mEventHandler.obtainMessage(CMD_WFC_DEFER_OFF_TIMEOUT), WFC_TIMEOUT);
    }

    @Override
    public void enterSoftAPMode(@NonNull SoftApModeConfiguration wifiConfig) {
        if (!mShouldDeferDisableWifi) {
            super.enterSoftAPMode(wifiConfig);
            return;
        }
        Log.d(TAG, "enterSoftAPMode, mShouldDeferDisableWifi: " + mShouldDeferDisableWifi);
        mWaitForEvent = true;
        mWifiConfig = wifiConfig;
        //Set wifi state disabling to notify WFC
        updateWifiState(WifiManager.WIFI_STATE_DISABLING, WifiManager.WIFI_STATE_ENABLED);
        mDeferredMsgInQueue.add(mEventHandler.obtainMessage(CMD_WFC_GO_SOFT_AP));
        mEventHandler.sendMessageDelayed(
                mEventHandler.obtainMessage(CMD_WFC_DEFER_OFF_TIMEOUT), WFC_TIMEOUT);
    }

    @Override
    public void shutdownWifi() {
        if (!mShouldDeferDisableWifi) {
            super.shutdownWifi();
            return;
        }
        Log.d(TAG, "shutdownWifi, mShouldDeferDisableWifi: " + mShouldDeferDisableWifi);
        mWaitForEvent = true;
        //Set wifi state disabling to notify WFC
        updateWifiState(WifiManager.WIFI_STATE_DISABLING, WifiManager.WIFI_STATE_ENABLED);
        mDeferredMsgInQueue.add(mEventHandler.obtainMessage(CMD_WFC_GO_SHUT_DOWN));
        mEventHandler.sendMessageDelayed(
                mEventHandler.obtainMessage(CMD_WFC_DEFER_OFF_TIMEOUT), WFC_TIMEOUT);
    }

    private void updateWifiState(int newState, int currentState) {
        final Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_WIFI_STATE, newState);
        intent.putExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, currentState);
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
