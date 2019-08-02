package com.magcomm.factorytest.entity;

/**
 * Created by Administrator on 2017/12/21.
 */

public class ClassEntity {
    private Class aClass;
    private String itemName;

    public ClassEntity(Class aClass, String itemName) {
        this.aClass = aClass;
        this.itemName = itemName;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
