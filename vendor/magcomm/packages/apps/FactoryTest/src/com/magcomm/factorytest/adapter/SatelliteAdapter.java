package com.magcomm.factorytest.adapter;

import android.content.Context;
import android.location.GpsSatellite;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.magcomm.factorytest.R;

import java.util.List;

/**
 * Created by zhangziran on 2017/12/25.
 */

public class SatelliteAdapter extends FactoryAdapter<GpsSatellite> {

    public SatelliteAdapter(Context context, List<GpsSatellite> entities) {
        super(context, entities);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.gps_item, null);
            viewHolder.tvID = (TextView) convertView.findViewById(R.id.tv_gps_id);
            viewHolder.tvSNR = (TextView) convertView.findViewById(R.id.tv_gps_snr);
            viewHolder.tvPRN = (TextView) convertView.findViewById(R.id.tv_gps_prn);
            viewHolder.tvAzimuth = (TextView) convertView.findViewById(R.id.tv_gps_azimuth);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        GpsSatellite gpsSatellite = (GpsSatellite) getItem(position);
        viewHolder.tvID.setText(position + "");
        viewHolder.tvSNR.setText(gpsSatellite.getSnr() + "");
        viewHolder.tvPRN.setText(gpsSatellite.getPrn() + "");
        viewHolder.tvAzimuth.setText(gpsSatellite.getAzimuth() + "");
        return convertView;
    }

    private class ViewHolder {
        private TextView tvID;
        private TextView tvSNR;
        private TextView tvPRN;
        private TextView tvAzimuth;
    }
}
