package com.magcomm.factorytest.item;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by zhangziran on 2018/1/16.
 */

public class OTGFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private UsbManager usbManager;
    private BroadcastReceiver usbReceiver;
    private TextView tvOTGState;
    private HashMap<String, UsbDevice> deviceHashMap;
    private Iterator<UsbDevice> devices;

    @Override
    protected int getCurrentView() {
        return R.layout.otg_test;
    }

    @SuppressLint("ServiceCast")
    @Override
    protected void onFragmentCreat() {
        tvOTGState = (TextView) view.findViewById(R.id.tv_otg_state);
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        usbReceiver = new USBReceiver();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(usbReceiver, usbFilter);
        initOTG();
    }

    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                setBtnSuccessEnable(true);
                initOTG();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                setBtnSuccessEnable(false);
                tvOTGState.setText(getResources().getString(R.string.no_otg));
            }
        }
    }

    private void initOTG() {
        deviceHashMap = usbManager.getDeviceList();
        if (deviceHashMap == null) {
            tvOTGState.setText(getResources().getString(R.string.no_otg));
            setBtnSuccessEnable(false);
            destroyAuto(Config.FAIL);
        } else {
            devices = deviceHashMap.values().iterator();
            if (!devices.hasNext()) {
                tvOTGState.setText(getResources().getString(R.string.no_otg));
                setBtnSuccessEnable(false);
                destroyAuto(Config.FAIL);
            } else {
                while (devices.hasNext()) {
                    UsbDevice usbDevice = devices.next();
                    int vendorId = usbDevice.getVendorId();
                    int productId = usbDevice.getProductId();
                    String manufacturerName = usbDevice.getManufacturerName();
                    String serialNumber = usbDevice.getSerialNumber();
                    String version = usbDevice.getVersion();
                    tvOTGState.setText(getResources().getString(R.string.vendor_id) + vendorId + "\n" +
                            getResources().getString(R.string.product_id) + productId + "\n" +
                            getResources().getString(R.string.manufacturer_name) + manufacturerName + "\n" +
                            getResources().getString(R.string.serial_number) + serialNumber + "\n" +
                            getResources().getString(R.string.usb_version) + version);
                }
                destroyAuto(Config.SUCCESS);
            }
        }
    }

    private void destroyAuto(String result) {
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
        if (usbReceiver != null) {
            context.unregisterReceiver(usbReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        if (tvOTGState.getText().toString().equals(getResources().getString(R.string.no_otg)) && Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
