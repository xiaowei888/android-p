package com.magcomm.factorytest.batterytest.entity;


import com.magcomm.factorytest.batterytest.fragment.BaseFragment;

/**
 * Created by Administrator on 2017/12/4.
 */

public class FragmentEntity {
    private int model;
    private BaseFragment fragment;

    public FragmentEntity(int model, BaseFragment fragment) {
        this.model = model;
        this.fragment = fragment;
        this.fragment.setModel(model);
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public BaseFragment getFragment() {
        return fragment;
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }
}
