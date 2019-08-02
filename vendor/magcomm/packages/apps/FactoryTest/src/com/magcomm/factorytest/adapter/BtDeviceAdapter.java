package com.magcomm.factorytest.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.entity.BluetoothEntity;

import java.util.List;

/**
 * Created by zhangziran on 2017/12/25.
 */

public class BtDeviceAdapter extends FactoryAdapter<BluetoothEntity> {
    public BtDeviceAdapter(Context context, List entities) {
        super(context, entities);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.bluetooth_item, null);
            viewHolder.tvBtName = (TextView) convertView.findViewById(R.id.tv_bt_name);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tv_bt_address);
            viewHolder.tvRssi = (TextView) convertView.findViewById(R.id.tv_bt_rssi);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothEntity entity = (BluetoothEntity) getItem(position);
        viewHolder.tvBtName.setText(entity.getName());
        viewHolder.tvAddress.setText(entity.getAddress());
        viewHolder.tvRssi.setText("Rssi:" + entity.getRssi());
        return convertView;
    }

    private class ViewHolder {
        private TextView tvBtName;
        private TextView tvAddress;
        private TextView tvRssi;
    }
}
