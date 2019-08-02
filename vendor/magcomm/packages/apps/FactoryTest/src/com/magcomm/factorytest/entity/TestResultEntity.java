package com.magcomm.factorytest.entity;

import com.magcomm.factorytest.FactoryTestApplication;

import java.util.Locale;

/**
 * Created by zhangziran on 2018/1/19.
 */

public class TestResultEntity {
    private String item;
    private String result;
    private int mode = -999;

    public TestResultEntity(String item, String result) {
        this.item = item;
        if (FactoryTestApplication.isZH()) {
            if (item.length() > 3) {
                mode = 2;
            } else {
                mode = 1;
            }
        } else {
            if (item.length() > 5) {
                mode = 2;
            } else {
                mode = 1;
            }
        }
        this.result = result;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "TestResultEntity{" +
                "item='" + item + '\'' +
                ", result='" + result + '\'' +
                '}';
    }


}
