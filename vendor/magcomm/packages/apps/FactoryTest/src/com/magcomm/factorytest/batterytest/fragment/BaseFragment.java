package com.magcomm.factorytest.batterytest.fragment;

import android.app.Fragment;

/**
 * Created by zhangziran on 2017/12/4.
 */

public abstract class BaseFragment extends Fragment{
    protected int model=-999;
    public void setModel(int model) {
        this.model = model;
    }
    public int getModel() {
        return this.model;
    }
    public abstract void reset(boolean fail);
}
