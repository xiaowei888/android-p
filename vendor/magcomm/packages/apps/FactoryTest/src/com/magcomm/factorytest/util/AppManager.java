package com.magcomm.factorytest.util;

import android.content.Context;
import android.util.Log;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.entity.ClassEntity;
import com.magcomm.factorytest.entity.ItemEntity;
import com.magcomm.factorytest.item.AccelerometerFragment;
import com.magcomm.factorytest.item.BackCameraFragment;
import com.magcomm.factorytest.item.BackFlashFragment;
import com.magcomm.factorytest.item.BaseFragment;
import com.magcomm.factorytest.item.BluetoothFragment;
import com.magcomm.factorytest.item.BrightnessFragment;
import com.magcomm.factorytest.item.ChargeFragment;
import com.magcomm.factorytest.item.FingerPrintFragment;
import com.magcomm.factorytest.item.FrontCameraFragment;
import com.magcomm.factorytest.item.FrontFlashFragment;
import com.magcomm.factorytest.item.GpsFragment;
import com.magcomm.factorytest.item.GyroscopeFragment;
import com.magcomm.factorytest.item.HDMIFragment;
import com.magcomm.factorytest.item.HeadsetFragment;
import com.magcomm.factorytest.item.KeyboardFragment;
import com.magcomm.factorytest.item.LCDFragment;
import com.magcomm.factorytest.item.LEDFragment;
import com.magcomm.factorytest.item.MagneticFragment;
import com.magcomm.factorytest.item.MikeFragment;
import com.magcomm.factorytest.item.OTGFragment;
import com.magcomm.factorytest.item.LightSensorFragment;
import com.magcomm.factorytest.item.PressureFragment;
import com.magcomm.factorytest.item.ProximityFragment;
import com.magcomm.factorytest.item.RadioFragment;
import com.magcomm.factorytest.item.ReceiverFragment;
import com.magcomm.factorytest.item.SDCardFragment;
import com.magcomm.factorytest.item.SpeakerFragment;
import com.magcomm.factorytest.item.TPFragment;
import com.magcomm.factorytest.item.UIMFragment;
import com.magcomm.factorytest.item.VersionFragment;
import com.magcomm.factorytest.item.VibrateFragment;
import com.magcomm.factorytest.item.ViceCameraFragment;
import com.magcomm.factorytest.item.WlanFragment;
//added by Yar begin
import com.magcomm.factorytest.item.PhoneFragment;
//added by Yar end

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class AppManager {
    static {
        AccelerometerFragment accelerometerFragment;
        BackCameraFragment backCameraFragment;
        BackFlashFragment backFlashFragment;
        BluetoothFragment bluetoothFragment;
        BrightnessFragment brightnessFragment;
        ChargeFragment chargeFragment;
        FingerPrintFragment fingerPrintFragment;
        FrontCameraFragment frontCameraFragment;
        FrontFlashFragment frontFlashFragment;
        GpsFragment gpsFragment;
        GyroscopeFragment gyroscopeFragment;
        HeadsetFragment headsetFragment;
        KeyboardFragment keyboardFragment;
        LCDFragment lcdFragment;
        MagneticFragment magneticFragment;
        MikeFragment mikeFragment;
        LightSensorFragment lightSensorFragment;
        ProximityFragment proximityFragment;
        RadioFragment radioFragment;
        ReceiverFragment receiverFragment;
        SDCardFragment sdCardFragment;
        SpeakerFragment speakerFragment;
        TPFragment tpFragment;
        UIMFragment uimFragment;
        VersionFragment versionFragment;
        VibrateFragment vibrateFragment;
        WlanFragment wlanFragment;
        OTGFragment otgFragment;
        HDMIFragment hdmiFragment;
        PressureFragment pressureFragment;
        ViceCameraFragment viceCameraFragment;
        LEDFragment ledFragment;
        //added by Yar begin
        PhoneFragment phoneFragment;
        //added by Yar end
    }

    private static final String TAG = "zhangziran";
    private static Context context;
    private Properties properties;
    private static List<ItemEntity> itemEntities;
    private static List<ClassEntity> classEntities;
    private static ProviderUtil providerUtil;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AppManager() {
    }

    private static class AppManagerHelper {
        private static final AppManager appManager = new AppManager();
    }

    public static AppManager getInstance(Context mContext) {
        context = mContext;
        init();
        return AppManagerHelper.appManager;
    }

    private static void init() {
        providerUtil = ProviderUtil.getInstance();
        itemEntities = new ArrayList<>();
        classEntities = new ArrayList<>();
    }

    //Auto test
    public List<ItemEntity> getItems() {
        itemEntities.clear();
        String[] items = context.getResources().getStringArray(R.array.items);
        for (int i = 0; i < items.length; i++) {
            String itme = items[i];
            boolean isLast = (i == (items.length - 1));
            boolean isEnable = isEnable(itme, 1, isLast);
            int id = context.getResources().getIdentifier(itme, "string", context.getPackageName());
            String chinaItemName = ResourcesUtil.getItem(id, context)[0];
            String englishItemName = ResourcesUtil.getItem(id, context)[1];
            //Log.i(TAG, "getItems: chinaItemName="+chinaItemName+"--englishItemName="+englishItemName);
            /*if (providerUtil.queryItem(chinaItemName).equals("null") || providerUtil.queryItem(englishItemName).equals("null")) {
                providerUtil.insert(chinaItemName, Config.NOT_TEST, Config.ZH);
                providerUtil.insert(englishItemName, Config.NOT_TEST, Config.EN);
            }*/
            if (isEnable) {
                //modified by Yar for not add test item when 'auto.cfg' set default value no @20180508
                if (providerUtil.queryItem(chinaItemName).equals("null") || providerUtil.queryItem(englishItemName).equals("null")) {
                    providerUtil.insert(chinaItemName, Config.NOT_TEST, Config.ZH);
                    providerUtil.insert(englishItemName, Config.NOT_TEST, Config.EN);
                }
                String className = Config.FACTORY_ITEM_PACKAGE + itme + Config.FRAGMENT;
                try {
                    Class itmeClass = Class.forName(className);
                    Log.i(TAG, "zhangya getItems: " + itmeClass.getSimpleName());
                    if (itmeClass != null) {
                        Object object = itmeClass.newInstance();
                        SharepreferenceUtil.putTime(simpleDateFormat.format(System.currentTimeMillis()));
                        String itmeName = context.getResources().getString(id);
                        ItemEntity itemEntity = new ItemEntity((BaseFragment) object, itmeName);
                        itemEntity.getFragment().setMode(Config.AUTO_TEST);
                        itemEntities.add(itemEntity);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (!(providerUtil.queryItem(chinaItemName).equals(Config.NOT_TEST)) || !(providerUtil.queryItem(englishItemName).equals(Config.NOT_TEST))) {
                    providerUtil.update(chinaItemName, Config.NOT_TEST);
                    providerUtil.update(englishItemName, Config.NOT_TEST);
                }
            }
        }
        return itemEntities;
    }


    //Manual test
    public List<ClassEntity> getClasss() {
        String[] items = context.getResources().getStringArray(R.array.items);
        for (int i = 0; i < items.length; i++) {
            String itme = items[i];
            boolean isLast = (i == (items.length - 1));
            boolean isEnable = isEnable(itme, 0, isLast);
            if (isEnable) {
                //Log.i(TAG, "getClasss: itme="+itme);
                String className = "";
                if(itme.equals("NFC")) {
                    className = Config.FACTORY_ITEM_PACKAGE + itme + Config.ACTIVITY;
                }else {
                    className = Config.FACTORY_ITEM_PACKAGE + itme + Config.FRAGMENT;
                }
                try {
                    Class itmeClass = Class.forName(className);
                    if (itmeClass != null) {
                        int id = context.getResources().getIdentifier(itme, "string", context.getPackageName());
                        String itmeName = context.getResources().getString(id);
                        Log.i(TAG, "getClasss: itmeClass=" + itmeClass);
                        ClassEntity classEntity = new ClassEntity(itmeClass, itmeName);
                        classEntities.add(classEntity);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classEntities;
    }


    private boolean isEnable(String item, int mode, boolean isLast) {
        boolean result = false;
        InputStream inputStream = null;
        try {
            if (mode == 0) {
                inputStream = context.getAssets().open("manualtest.cfg");
            } else if (mode == 1) {
                inputStream = context.getAssets().open("autotest.cfg");
            }
            if (properties == null) {
                properties = new Properties();
                properties.load(inputStream);
            }
            String ret = properties.getProperty(Config.FACTORY_ITEM_PACKAGE + item);
            //android.util.Log.i("zhangya", " isEnable mode = " + mode + ", item = " + item + ", ret = " + ret);
            if ("yes".equals(ret)) {
                result = true;
            } else {
                result = false;
            }
            if (isLast) {
                if (properties != null) {
                    inputStream.close();
                    inputStream = null;
                    properties.clear();
                    properties = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
