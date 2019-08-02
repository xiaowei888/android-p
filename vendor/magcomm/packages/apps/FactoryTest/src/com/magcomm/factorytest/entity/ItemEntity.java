package com.magcomm.factorytest.entity;

import com.magcomm.factorytest.item.BaseFragment;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class ItemEntity {
    private BaseFragment fragment;
    private String name;

    public ItemEntity(BaseFragment fragment, String name) {
        this.fragment = fragment;
        this.name = name;
    }

    public BaseFragment getFragment() {
        return fragment;
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
