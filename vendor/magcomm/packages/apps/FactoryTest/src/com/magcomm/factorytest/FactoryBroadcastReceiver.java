package com.magcomm.factorytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.magcomm.factorytest.activity.MainActivity;

/**
 * Created by zhangziran on 2018/2/6.
 */

public class FactoryBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SECRET_CODE";
    private static final Uri factory = Uri.parse("android_secret_code://8080");
    private static final Uri factorySecond = Uri.parse("android_secret_code://66");
    private static final Uri imeiData = Uri.parse("android_secret_code://06");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            if (factory.equals(intent.getData()) || factorySecond.equals(intent.getData())) {
                Intent intentFactory = new Intent(context, MainActivity.class);
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
            }else if(imeiData.equals(intent.getData())){
				Intent intentFactory = new Intent(context, com.magcomm.settingtools.imeidata.ImeiDataActivity.class);
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
			}
        }
    }
}
