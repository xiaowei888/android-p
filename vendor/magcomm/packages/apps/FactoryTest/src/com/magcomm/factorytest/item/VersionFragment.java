package com.magcomm.factorytest.item;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import com.android.internal.telephony.PhoneConstants;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class VersionFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private TelephonyManager telephonyManager;
    private TextView tvModel, tvSoftwareVersion, tvHardwareVersion, tvBasebandVersion,
            tvSnVersion, tvWcdmaBT, tvWcdmaFT, tvGsmBT, tvGsmFT, tvIMEI1, tvIMEI2, tvMEID,tvDeviceInfo;
    private static final String MODEL = "ro.product.model";
    private static final String SOFTWARE_VERSION = "ro.build.display.id";
    private static final String HARDWARE_VERSION = "ro.board.platform";
    private static final String BASEBAND_VERSION = "gsm.version.baseband";
    private static final String SN = "gsm.serial";

    private static final int SN_LEN = 10;
    @Override
    protected int getCurrentView() {
        return R.layout.version_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initVersion();
    }

    private void initView() {
        tvModel = (TextView) view.findViewById(R.id.tv_model);
        tvSoftwareVersion = (TextView) view.findViewById(R.id.tv_software_version);
        tvHardwareVersion = (TextView) view.findViewById(R.id.tv_hardware_version);
        tvBasebandVersion = (TextView) view.findViewById(R.id.tv_baseband_version);
        tvSnVersion = (TextView) view.findViewById(R.id.tv_sn_version);
        tvWcdmaBT = (TextView) view.findViewById(R.id.tv_wcdma_bt);
        tvWcdmaFT = (TextView) view.findViewById(R.id.tv_wcdma_ft);
        tvGsmBT = (TextView) view.findViewById(R.id.tv_gsm_bt);
        tvGsmFT = (TextView) view.findViewById(R.id.tv_gsm_ft);
        tvIMEI1 = (TextView) view.findViewById(R.id.tv_imei1);
        tvIMEI2 = (TextView) view.findViewById(R.id.tv_imei2);
        tvMEID = (TextView) view.findViewById(R.id.tv_meid);
        tvDeviceInfo = (TextView) view.findViewById(R.id.tv_device_info);
    }

    private void initVersion() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //model
        String model = getSystemproString(MODEL);
        tvModel.setText(getResources().getString(R.string.version_model) + model);
        //software version
        String softwareVersion = getSystemproString(SOFTWARE_VERSION);
        tvSoftwareVersion.setText(getResources().getString(R.string.software_version) + softwareVersion);
        //hardware version
        String hardwareVersion = getSystemproString(HARDWARE_VERSION);
        tvHardwareVersion.setText(getResources().getString(R.string.hardware_version) + hardwareVersion);
        //baseband_version
        String baseband_version = getSystemproString(BASEBAND_VERSION);
        tvBasebandVersion.setText(getResources().getString(R.string.baseband_version) + baseband_version);
        //SN
        //String sn = SystemProperties.get("gsm.serial"); //Build.SERIAL;
        String sn = SystemProperties.get("vendor.gsm.serial");
        //modified by Yar begin
        String calSN = sn.replaceAll("\\s*", "");
        int plen = sn.length();
        int clen = calSN.length();
        int flag = 0;
        if (plen > 0) {
            flag = sn.indexOf(" ");
        }
        android.util.Log.i("Yar", " sn = (" + sn + "), flag = " + flag);
        android.util.Log.i("Yar", " calSN = (" + calSN + "), plen = " + plen + ", clen = " + clen);
        if ((sn != null) && (sn.length() >= SN_LEN)) {
            sn = sn.substring(0, SN_LEN);
        }
        if (sn == null) {
            tvSnVersion.setText(getResources().getString(R.string.sn_version) + getResources().getString(R.string.sn_error));
            tvWcdmaBT.setVisibility(View.GONE);
            tvWcdmaFT.setVisibility(View.GONE);
            tvGsmBT.setVisibility(View.GONE);
            tvGsmFT.setVisibility(View.GONE);
        } else {
            if (clen >= 2 && plen >= clen) {
                char char1 = calSN.charAt(clen-2);
                char char2 = calSN.charAt(clen-1);
                String wcdmaBt = getResources().getString(R.string.WCDMA_BT);
                String wcdmaFt = getResources().getString(R.string.WCDMA_FT);
                String gsmBt = getResources().getString(R.string.GSM_BT);
                String gsmFt = getResources().getString(R.string.GSM_FT);
                if (char1 == '1' && char2 == '0' && flag > 0 && flag < clen) {
                    wcdmaBt += "WCDMA BT pass";
                    wcdmaFt += "WCDMA FT pass";
                    gsmBt += "GSM BT pass";
                    gsmFt += "GSM FT pass";
                } else {
                    wcdmaBt = "BT/FT not pass";
                    //wcdmaFt += "WCDMA BT not pass";
                    //gsmBt += "GSM BT not pass";
                    //gsmFt += "GSM FT not pass";
                    tvWcdmaFT.setVisibility(View.GONE);
                    tvGsmBT.setVisibility(View.GONE);
                    tvGsmFT.setVisibility(View.GONE);
                }
                tvWcdmaBT.setText(wcdmaBt);
                tvWcdmaFT.setText(wcdmaFt);
                tvGsmBT.setText(gsmBt);
                tvGsmFT.setText(gsmFt);
            } else {
                tvWcdmaBT.setText(getResources().getString(R.string.sn_notice));
                tvWcdmaFT.setVisibility(View.GONE);
                tvGsmBT.setVisibility(View.GONE);
                tvGsmFT.setVisibility(View.GONE);
            }
        //modified by Yar end
        /*String sn = SystemProperties.get("gsm.serial"); //Build.SERIAL;
        sn = sn.replaceAll("\\s*", "");
        if ((sn != null) && (sn.length() >= 17)) {
            sn = sn.substring(0, 17);
        }
        if (sn == null) {
            tvSnVersion.setText(getResources().getString(R.string.sn_version) + getResources().getString(R.string.sn_error));
            tvWcdmaBT.setVisibility(View.GONE);
            tvWcdmaFT.setVisibility(View.GONE);
            tvGsmBT.setVisibility(View.GONE);
            tvGsmFT.setVisibility(View.GONE);
        } else {
            if (sn.length() >= 0 && sn.length() < 17) {
                tvWcdmaBT.setText(getResources().getString(R.string.sn_notice));
                tvWcdmaFT.setVisibility(View.GONE);
                tvGsmBT.setVisibility(View.GONE);
                tvGsmFT.setVisibility(View.GONE);
            } else if (sn.length() >= 17) {
                char char1 = sn.charAt(15);
                char char2 = sn.charAt(16);
                String wcdmaBt = getResources().getString(R.string.WCDMA_BT);
                String wcdmaFt = getResources().getString(R.string.WCDMA_FT);
                String gsmBt = getResources().getString(R.string.GSM_BT);
                String gsmFt = getResources().getString(R.string.GSM_FT);
                if (char1 == '3' || char1 == '4' || char1 == '5' || char1 == '6' || char1 == '7') {
                    if (char1 == '5' || char1 == '6' || char1 == '7') {
                        wcdmaBt += "WCDMA BT pass";
                        wcdmaFt += "WCDMA FT pass";
                    } else {
                        wcdmaBt += "WCDMA BT pass";
                        wcdmaFt += "WCDMA FT not pass";
                    }
                } else {
                    wcdmaBt += "WCDMA FT not pass";
                    wcdmaFt += "WCDMA BT not pass";
                }
                if (char2 == '1' || char2 == '2' || char2 == '3' || char2 == '4' || char2 == '5') {
                    if (char2 == '2' || char2 == '3' || char2 == '4' || char2 == '5') {
                        gsmBt += "GSM BT pass";
                        gsmFt += "GSM FT pass";
                    } else {
                        gsmBt += "GSM BT pass";
                        gsmFt += "GSM FT not pass";
                    }
                } else {
                    gsmBt += "GSM BT not pass";
                    gsmFt += "GSM FT not pass";
                }
                tvWcdmaBT.setText(wcdmaBt);
                tvWcdmaFT.setText(wcdmaFt);
                tvGsmBT.setText(gsmBt);
                tvGsmFT.setText(gsmFt);
            }*/
            tvSnVersion.setText(getResources().getString(R.string.sn_version) + Build.getSerial());
        }
        //MEID && IMEI
        if (SystemProperties.get("persist.radio.multisim.config").equals("dsds")) {
            String imei1 = telephonyManager.getDeviceId(PhoneConstants.SIM_ID_1);
            String imei2 = telephonyManager.getDeviceId(PhoneConstants.SIM_ID_2);
            tvIMEI1.setText("IMEI1:" + imei1);
            tvIMEI2.setText("IMEI2:" + imei2);
        } else {
            String imei1 = telephonyManager.getDeviceId(PhoneConstants.SIM_ID_1);
            tvIMEI1.setText("IMEI:" + imei1);
        }
        if (Config.AUTO_TEST.equals(getMode())) {
            updateDataBase(Config.SUCCESS);
            handler.obtainMessage(2).sendToTarget();
        }

        //device info
        tvDeviceInfo.setText(readDeviceInfo());
    }

    private String readDeviceInfo() {
        StringBuffer stringBuffer = new StringBuffer();
        File file = new File("/proc/driver","deviceinfo");
        if(!file.exists()) return "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while (( line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line+"\n");
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return  stringBuffer.toString();
    }

    private static String getSystemproString(String property) {
        return SystemProperties.get(property, "unknown");
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
    }
}

