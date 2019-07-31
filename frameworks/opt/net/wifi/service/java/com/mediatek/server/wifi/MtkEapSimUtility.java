/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
*/

package com.mediatek.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;

import android.telephony.ImsiEncryptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiCountryCode;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.util.TelephonyUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class MtkEapSimUtility {
    private static final String TAG = "MtkEapSimUtility";
    private static boolean mVerboseLoggingEnabled = false;
    public static final int GET_SUBID_NULL_ERROR = -1;
    private static TelephonyManager mTelephonyManager;
    private static WifiConfigManager mWifiConfigManager;
    private static WifiNative mWifiNative;
    private static WifiStateMachine mWifiStateMachine;
    private static String mSim1IccState = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
    private static String mSim2IccState = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
    private static boolean mSim1Present = false;
    private static boolean mSim2Present = false;

    public static void init() {
        if (mTelephonyManager == null) {
            mTelephonyManager = WifiInjector.getInstance().makeTelephonyManager();
        }
        mWifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        mWifiNative = WifiInjector.getInstance().getWifiNative();
        mWifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
    }

    ///M: EAP-SIM for extending to dual sim
    public static boolean setSimSlot(int networkId, String slotId) {
        if (mVerboseLoggingEnabled) {
            Log.v(TAG, "Set network sim slot " + slotId + " for netId " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null || !TelephonyUtil.isSimConfig(config)) {
            return false;
        }
        config.enterpriseConfig.setSimNum(WifiInfo.removeDoubleQuotes(slotId));

        mWifiConfigManager.saveToStore(true);
        return true;
    }

    /**
     * Resets all sim networks state.
     * M: EAP-SIM for extending to dual sim
     */
    public static void resetSimNetworks(boolean simPresent, int simSlot) {
        if (mVerboseLoggingEnabled) {
            Log.v(TAG, "resetSimNetworks, simPresent: " + simPresent + ", simSlot: " + simSlot);
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config) && getIntSimSlot(config) == simSlot) {
                if (mVerboseLoggingEnabled) {
                    Log.v(TAG, "Reset SSID " + config.SSID + " with simSlot " + simSlot);
                }
                Pair<String, String> currentIdentity = null;
                if (simPresent) {
                    currentIdentity = getSimIdentity(mTelephonyManager,
                            new TelephonyUtil(), config);
                }
                // Update the loaded config
                if (currentIdentity == null) {
                    Log.d(TAG, "Identity is null");
                    return;
                }
                config.enterpriseConfig.setIdentity(currentIdentity.first);
                if (config.enterpriseConfig.getEapMethod() != WifiEnterpriseConfig.Eap.PEAP) {
                    config.enterpriseConfig.setAnonymousIdentity("");
                }
            }
        }
    }

    public static boolean isSim1Present() { return mSim1Present; }
    public static boolean isSim2Present() { return mSim2Present; }

    /**
     * M: EAP-SIM for extending to dual sim
     * Convert simSlot from string to integer
     */
    public static int getIntSimSlot(WifiConfiguration config) {
        if (config == null || !TelephonyUtil.isSimConfig(config)) {
            return -1;
        }
        int slotId = 0;
        String simSlot = config.enterpriseConfig.getSimNum();
        if (simSlot != null) {
            String[] simSlots = simSlot.split("\"");
            //simSlot = "\"1\"";
            if (simSlots.length > 1) {
                slotId = Integer.parseInt(simSlots[1]);
                //simSlot = "1";
            } else if (simSlots.length == 1) {
                if (simSlots[0].length() > 0) {
                    slotId = Integer.parseInt(simSlots[0]);
                }
            }
        }
        return slotId;
    }

    /**
     * Get the identity for the current SIM or null if the SIM is not available
     *
     * @param tm TelephonyManager instance
     * @param config WifiConfiguration that indicates what sort of authentication is necessary
     * @return Pair<identify, encrypted identity> or null if the SIM is not available
     * or config is invalid
     */
    public static Pair<String, String> getSimIdentity(TelephonyManager tm,
                                                      TelephonyUtil telephonyUtil,
                                                      WifiConfiguration config) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        String imsi = tm.getSubscriberId();
        String mccMnc = "";

        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            mccMnc = tm.getSimOperator();
        }

        ///M: EAP-SIM for extending to dual sim @{
        int slotId = getIntSimSlot(config);
        int subId = getSubId(slotId);
        if (tm.getDefault().getPhoneCount() >= 2 && subId != GET_SUBID_NULL_ERROR) {
            imsi = tm.getSubscriberId(subId);
            mccMnc = "";
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mccMnc = tm.getSimOperator(subId);
            }
        }
        //@}

        ImsiEncryptionInfo imsiEncryptionInfo;
        try {
            imsiEncryptionInfo = tm.getCarrierInfoForImsiEncryption(TelephonyManager.KEY_TYPE_WLAN);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get imsi encryption info: " + e.getMessage());
            return null;
        }

        String identity = buildIdentity(getSimMethodForConfig(config), imsi, mccMnc, false);
        if (identity == null) {
            Log.e(TAG, "Failed to build the identity");
            return null;
        }

        String encryptedIdentity = buildEncryptedIdentity(telephonyUtil,
                getSimMethodForConfig(config), imsi, mccMnc, imsiEncryptionInfo);
        // In case of failure for encryption, set empty string
        if (encryptedIdentity == null) encryptedIdentity = "";
        return Pair.create(identity, encryptedIdentity);
    }

    ///M: GET_SUBID_NULL_ERROR means phone doesn't insert any card
    public static int getSubId(int simSlot) {
        int[] subIds = SubscriptionManager.getSubId(simSlot);
        if (subIds != null) {
            return subIds[0];
        } else {
            return GET_SUBID_NULL_ERROR;
        }
    }

    ///M: EAP-SIM for extending to dual sim
    public static int getDefaultSim() {
        return SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultSubscriptionId());
    }

    ///M: EAP-SIM for extending to dual sim
    public static String getIccAuthentication(int appType, int authType, String base64Challenge) {
        String tmResponse = null;
        int slotId = getIntSimSlot(getTargetWificonfiguration());
        if (slotId != GET_SUBID_NULL_ERROR) {
            if (mTelephonyManager.getDefault().getPhoneCount() >= 2) {
                int subId = getSubId(slotId);
                if (subId != GET_SUBID_NULL_ERROR) {
                    Log.d(TAG, "subId: " + subId + ", appType: " + appType
                            + ", authType: " + authType + ", challenge: " + base64Challenge);
                    Log.d(TAG, "getIccAuthentication for specified subId");
                    tmResponse = mTelephonyManager.getIccAuthentication(subId, appType,
                            authType, base64Challenge);
                    return tmResponse;
                }
            }
        }
        Log.d(TAG, "getIccAuthentication for the default subscription");
        tmResponse = mTelephonyManager.getIccAuthentication(appType, authType, base64Challenge);
        return tmResponse;
    }

    public static boolean isConfigSimCardPresent(WifiConfiguration config) {
        int simSlot = getIntSimSlot(config);
        // If simSlot is unspecified (-1)
        if (simSlot == GET_SUBID_NULL_ERROR) {
            Log.d(TAG, "simSlot is unspecified, check sim state: ("
                    + mSim1Present + ", " + mSim2Present + ")");
            if (mSim1Present || mSim2Present) {
                return true;
            } else {
                return false;
            }
        }
        return (simSlot == 0) ? mSim1Present : mSim2Present;
    }

    public static boolean isDualSimAbsent() {
        return !mSim1Present && !mSim2Present;
    }

    //M: If slotId is unspecified(-1), set default sim from TelepohonyManager
    public static void setDefaultSimToUnspecifiedSimSlot() {
        WifiConfiguration targetWificonfiguration = getTargetWificonfiguration();
        if (targetWificonfiguration == null ||
                !TelephonyUtil.isSimConfig(targetWificonfiguration)) {
            Log.e(TAG, "Empty config or invalid config to set sim slot");
            return;
        }
        int slotId = getIntSimSlot(targetWificonfiguration);
        int subId = getSubId(slotId);
        if (subId == GET_SUBID_NULL_ERROR) {
            Log.d(TAG, "config.simSlot is unspecified(-1), set to"
                    + " default sim slot selected by telephony manager");
            int defaultSim = getDefaultSim();
            // Update config's simSlot
            targetWificonfiguration.enterpriseConfig.setSimNum("\"" + defaultSim + "\"");
            if (!setSimSlot(targetWificonfiguration.networkId,
                    targetWificonfiguration.enterpriseConfig.getSimNum())) {
                Log.e(TAG, "Fail to set sim slot for config, networkId="
                        + targetWificonfiguration.networkId);
            }
        }
    }

    public static boolean isSimConfigSameAsCurrent(WifiConfiguration config,
            WifiConfiguration curConfig) {
        if (config == null || curConfig == null) {
            Log.e(TAG, "Null config");
            return false;
        }
        if (!TelephonyUtil.isSimConfig(config)) {
            return false;
        }
        if (config.enterpriseConfig.getEapMethod() !=
            curConfig.enterpriseConfig.getEapMethod()) {
            Log.d(TAG, "EAP method changed, skip checking");
            return false;
        }
        Log.d(TAG, "config sim: " + getIntSimSlot(config) + " current sim: "
            + getIntSimSlot(curConfig) + " default sim: " + getDefaultSim());
        if (getIntSimSlot(config) == -1 && getIntSimSlot(curConfig) == getDefaultSim()) {
            if (!setSimSlot(config.networkId, "\"" + getDefaultSim() + "\"")) {
                Log.e(TAG, "Fail to set sim slot for config, networkId=" + config.networkId);
            }
            return true;
        } else if (getIntSimSlot(curConfig) == getIntSimSlot(config)) {
            return true;
        }
        return false;
    }

    /// get function or variable by reflection @{
    private static WifiConfiguration getTargetWificonfiguration() {
        try {
            Field field = mWifiStateMachine.getClass()
                    .getDeclaredField("targetWificonfiguration");
            field.setAccessible(true);
            return (WifiConfiguration) field.get(mWifiStateMachine);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Collection<WifiConfiguration> getInternalConfiguredNetworks() {
        try {
            Method method = mWifiConfigManager.getClass()
                    .getDeclaredMethod("getInternalConfiguredNetworks");
            method.setAccessible(true);
            return (Collection<WifiConfiguration>) method.invoke(mWifiConfigManager);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static WifiConfiguration getInternalConfiguredNetwork(int networkId) {
        try {
            Method method = mWifiConfigManager.getClass()
                    .getDeclaredMethod("getInternalConfiguredNetwork", int.class);
            method.setAccessible(true);
            return (WifiConfiguration) method.invoke(mWifiConfigManager, networkId);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String buildIdentity(int eapMethod, String imsi, String mccMnc,
                                      boolean isEncrypted) {
        try {
            Method method = TelephonyUtil.class.getDeclaredMethod(
                    "buildIdentity", int.class, String.class, String.class, boolean.class);
            method.setAccessible(true);
            return (String) method.invoke(null, eapMethod, imsi, mccMnc, isEncrypted);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String buildEncryptedIdentity(TelephonyUtil telephonyUtil, int eapMethod,
                                                 String imsi, String mccMnc,
                                                 ImsiEncryptionInfo imsiEncryptionInfo) {
        try {
            Method method = TelephonyUtil.class.getDeclaredMethod(
                    "buildEncryptedIdentity", TelephonyUtil.class, int.class, String.class,
                            String.class, ImsiEncryptionInfo.class);
            method.setAccessible(true);
            return (String) method.invoke(null, telephonyUtil, eapMethod,
                    imsi, mccMnc, imsiEncryptionInfo);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static int getSimMethodForConfig(WifiConfiguration config) {
        try {
            Method method = TelephonyUtil.class.getDeclaredMethod(
                    "getSimMethodForConfig", WifiConfiguration.class);
            method.setAccessible(true);
            return (Integer) method.invoke(null, config);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return -1;
        }
    }
    /// }@

    public static void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            mVerboseLoggingEnabled = true;
        } else {
            mVerboseLoggingEnabled = false;
        }
    }

    public static class MtkSimBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            ///M: EAP-SIM for extending to dual sim
            int simSlot = intent.getIntExtra(PhoneConstants.SLOT_KEY, -1);
            Log.d(TAG, "onReceive ACTION_SIM_STATE_CHANGED iccState: " + state
                    + ", simSlot: " + simSlot);
            int CMD_RESET_SIM_NETWORKS = 0;
            try {
                Field field = mWifiStateMachine.getClass()
                        .getDeclaredField("CMD_RESET_SIM_NETWORKS");
                field.setAccessible(true);
                CMD_RESET_SIM_NETWORKS = (Integer) field.get(mWifiStateMachine);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(state)) {
                if (0 == simSlot || -1 == simSlot) { /// simSlot = -1 means single sim
                    mSim1Present = false;
                } else if (1 == simSlot) {
                    mSim2Present = false;
                }
                Log.d(TAG, "resetting networks because SIM" + simSlot + " was removed");
                mWifiStateMachine.sendMessage(CMD_RESET_SIM_NETWORKS, 0, simSlot);
            } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(state)) {
                if (0 == simSlot || -1 == simSlot) { /// simSlot = -1 means single sim
                    mSim1Present = true;
                } else if (1 == simSlot) {
                    mSim2Present = true;
                }
                Log.d(TAG, "resetting networks because SIM" + simSlot + " was loaded");
                mWifiStateMachine.sendMessage(CMD_RESET_SIM_NETWORKS, 1, simSlot);
            }
        }
    }
}
