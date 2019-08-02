package com.magcomm.factorytest.item;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.adapter.BtDeviceAdapter;
import com.magcomm.factorytest.entity.BluetoothEntity;
import com.magcomm.factorytest.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class BluetoothFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private ListView lvBtDevice;
    private TextView tvBtTitle;
    private Button btRefresh;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver broadcastReceiver;
    private List<BluetoothEntity> bluetoothEntities;
    private BtDeviceAdapter adapter;
    private static final short DEFAULTVALUE = -999;

    @Override
    protected int getCurrentView() {
        return R.layout.bluetooth_test;
    }

    @Override
    protected void onFragmentCreat() {
        lvBtDevice = (ListView) view.findViewById(R.id.lv_bt_device);
        tvBtTitle = (TextView) view.findViewById(R.id.tv_bt_title);
        btRefresh = (Button) view.findViewById(R.id.bt_bt_refresh);
        btRefresh.setEnabled(false);
        btRefresh.setOnClickListener(this);
        tvBtTitle.setText(getResources().getString(R.string.open_bt));
        initReceiver();
        initBluetooth();
    }

    private void initReceiver() {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BluetoothReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(broadcastReceiver, filter);
    }

    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        bluetoothEntities = new ArrayList<>();
        adapter = new BtDeviceAdapter(context, bluetoothEntities);
        lvBtDevice.setAdapter(adapter);
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.enable()) {
            bluetoothAdapter.enable();
        }
        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int state = bluetoothAdapter.getState();
            if (state == BluetoothAdapter.STATE_ON) {
                handler.removeCallbacks(this);
                runnable = null;
                bluetoothAdapter.startDiscovery();
            } else {
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    tvBtTitle.setText(getResources().getString(R.string.scan_bt_device));
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, DEFAULTVALUE);
                    if (btDevice != null) {
                        if (rssi == DEFAULTVALUE) {
                            rssi = 0;
                        }
                        String name = btDevice.getName() == null ? getResources().getString(R.string.unknow_device) : btDevice.getName();
                        BluetoothEntity entity = new BluetoothEntity(name, btDevice.getAddress(), rssi);
                        bluetoothEntities.add(entity);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    tvBtTitle.setText(getResources().getString(R.string.used_bt_device));
                    btRefresh.setEnabled(true);
                    if (Config.AUTO_TEST.equals(getMode())) {
                        if (bluetoothEntities.size() > 0) {
                            handler.obtainMessage(2).sendToTarget();
                            updateDataBase(Config.SUCCESS);
                        } else {
                            handler.obtainMessage(2).sendToTarget();
                            updateDataBase(Config.FAIL);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        btRefresh.setEnabled(false);
        if (v.getId() == R.id.bt_bt_refresh) {
            for (int i = 0; i < bluetoothEntities.size(); i++) {
                BluetoothEntity entity = bluetoothEntities.get(i);
                if (entity != null) {
                    entity = null;
                }
            }
            bluetoothEntities.clear();
            adapter.notifyDataSetChanged();
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        this.runnable = null;
        bluetoothAdapter.cancelDiscovery();
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
        }
        if (bluetoothAdapter.enable()) {
            bluetoothAdapter.disable();
        }
    }
}
