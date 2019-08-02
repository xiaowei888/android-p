package com.magcomm.factorytest.item;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/27.
 */

public class SDCardFragment extends BaseFragment {
    private TextView tvSd;
    private StorageManager storageManager;
    private String SD1Path, SD2Path;

    @Override
    protected int getCurrentView() {
        return R.layout.sd_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvSd = (TextView) view.findViewById(R.id.tv_sd);
        storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] storagePathList = storageManager.getVolumePaths();
        if (storagePathList != null) {
            if (storagePathList.length >= 2) {
                if (SystemProperties.get("ro.mount.swap").equals("1")) {
                    SD1Path = storagePathList[1];        // emmc card
                    SD2Path = storagePathList[0];        // SD card
                } else {
                    SD1Path = storagePathList[0];        // emmc card
                    SD2Path = storagePathList[1];        // SD card
                }
            } else if (storagePathList.length == 1) {
                SD1Path = storagePathList[0];
            }
        }
        if (SystemProperties.get("ro.mount.fs").equals("EXT4")) {
            if (!checkSDCardMount(SD2Path)) {
                tvSd.setText(R.string.no_sd);
                destoryByAuto(Config.FAIL);
            } else {
                tvSd.setText(R.string.sdcard_test_success);
                destoryByAuto(Config.SUCCESS);
            }
        } else {
            if (!checkSDCardMount(SD1Path)) {
                tvSd.setText(R.string.no_sd);
                destoryByAuto(Config.FAIL);
            } else {
                tvSd.setText(R.string.sdcard_test_success);
                destoryByAuto(Config.SUCCESS);
            }
        }
    }

    public boolean checkSDCardMount(String mountPoint) {
        if (mountPoint == null) {
            return false;
        }
        String state = storageManager.getVolumeState(mountPoint);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void destoryByAuto(String result) {
        if (Config.AUTO_TEST.equals(getMode())) {
            updateDataBase(result);
            handler.obtainMessage(2).sendToTarget();
        }
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
