package com.magcomm.factorytest.entity;

/**
 * Created by zhangziran on 2017/12/25.
 */

public class BluetoothEntity {
    private String name;
    private String address;
    private int rssi;

    public BluetoothEntity(String name, String address, int rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
