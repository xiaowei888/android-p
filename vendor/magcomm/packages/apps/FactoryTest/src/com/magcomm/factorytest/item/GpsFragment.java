package com.magcomm.factorytest.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.adapter.SatelliteAdapter;
import com.magcomm.factorytest.util.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class GpsFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ListView lvSatellite;
    private TextView tvAveSignalIntensity, tvLongitude, tvLatitude, tvTime;
    private LinearLayout llStartLocate, llLocationData;
    private Chronometer locatedTime;
    private Chronometer.OnChronometerTickListener tickListener;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GpsStatus.Listener statusListener;
    private static int current = 0;
    private List<GpsSatellite> gpsSatellites;
    private SatelliteAdapter adapter;
    private boolean isFirst = true;
    private boolean isStart = false;

    @Override
    protected int getCurrentView() {
        return R.layout.gps_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initGps();
    }


    private void initView() {
        lvSatellite = (ListView) view.findViewById(R.id.lv_satellite);
        tvAveSignalIntensity = (TextView) view.findViewById(R.id.tv_ave_signal_intensity);
        llStartLocate = (LinearLayout) view.findViewById(R.id.ll_start_locate);
        llLocationData = (LinearLayout) view.findViewById(R.id.ll_location_data);
        locatedTime = (Chronometer) view.findViewById(R.id.located_time);
        tvLongitude = (TextView) view.findViewById(R.id.tv_gps_longitude);
        tvLatitude = (TextView) view.findViewById(R.id.tv_gps_latitude);
        tvTime = (TextView) view.findViewById(R.id.tv_gps_time);
        tvAveSignalIntensity.setVisibility(View.GONE);
        llLocationData.setVisibility(View.GONE);
        locatedTime.start();
        if (Config.AUTO_TEST.equals(getMode())) {
            tickListener = new LocationChronometerTickListener();
            locatedTime.setOnChronometerTickListener(tickListener);
        }
        isStart = true;
    }

    @SuppressLint("MissingPermission")
    private void initGps() {
        openGPS();
        gpsSatellites = new ArrayList<>();
        adapter = new SatelliteAdapter(context, gpsSatellites);
        lvSatellite.setAdapter(adapter);
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationListener == null) {
            locationListener = new GPSLocationListener();
        }
        if (statusListener == null) {
            statusListener = new GPSStatusListener();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.addGpsStatusListener(statusListener);
    }

    private class GPSLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null && gpsSatellites.size() > 0) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 5000);
                llStartLocate.setVisibility(View.GONE);
                if (tickListener != null) {
                    tickListener = null;
                }
                locatedTime.setOnChronometerTickListener(null);
                locatedTime.stop();
                isStart = false;
                llLocationData.setVisibility(View.VISIBLE);
                tvLongitude.setText(getResources().getString(R.string.gps_longitude) + location.getLongitude());
                tvLatitude.setText(getResources().getString(R.string.gps_longitude) + location.getLatitude());
                tvTime.setText(getResources().getString(R.string.gps_time) + simpleDateFormat.format(location.getTime()));
                if (Config.AUTO_TEST.equals(getMode())) {
                    updateDataBase(Config.SUCCESS);
                    handler.obtainMessage(2).sendToTarget();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private class GPSStatusListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            @SuppressLint("MissingPermission")
            GpsStatus gpsStatus = locationManager.getGpsStatus(null);
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                int maxGpsSatellite = gpsStatus.getMaxSatellites();
                Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
                gpsSatellites.clear();
                int nums = 0;
                float aveSignalIntensity = 0;
                while (iterator.hasNext()) {
                    GpsSatellite satellite = iterator.next();
                    if (satellite.getSnr() > 0) {
                        tvAveSignalIntensity.setVisibility(View.VISIBLE);
                        gpsSatellites.add(satellite);
                        aveSignalIntensity += satellite.getSnr();
                        tvAveSignalIntensity.setText(getResources().getString(R.string.gps_ave_signal_intensity) + aveSignalIntensity / (nums + 1));
                        adapter.notifyDataSetChanged();
                        nums++;
                        current = nums;
                        isFirst = true;
                    }
                }
                if (nums == 0) {
                    tvAveSignalIntensity.setVisibility(View.GONE);
                    startLocate();
                }
            }
        }
    }

    private class LocationChronometerTickListener implements Chronometer.OnChronometerTickListener {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long time = SystemClock.elapsedRealtime() - chronometer.getBase();
            if (time > 90000) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startLocate();
        }
    };

    private void startLocate() {
        llLocationData.setVisibility(View.GONE);
        llStartLocate.setVisibility(View.VISIBLE);
        if (!isStart) {
            locatedTime.setBase(SystemClock.elapsedRealtime());
            locatedTime.start();
            if (Config.AUTO_TEST.equals(getMode())) {
                tickListener = new LocationChronometerTickListener();
                locatedTime.setOnChronometerTickListener(tickListener);
            }
            isStart = true;
        }
        handler.removeCallbacks(runnable);
    }

    private void openGPS() {
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
    }

    private void stopGPS() {
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
    }


    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        this.runnable = null;
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
            locationManager.removeGpsStatusListener(statusListener);
            statusListener = null;
        }
        if (tickListener != null) {
            tickListener = null;
        }
        locatedTime.stop();
        isStart = false;
        stopGPS();
    }
}
