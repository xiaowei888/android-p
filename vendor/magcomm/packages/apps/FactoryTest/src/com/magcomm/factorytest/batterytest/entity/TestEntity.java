package com.magcomm.factorytest.batterytest.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangziran on 2017/12/4.
 */

public class TestEntity implements Parcelable{
    private List<Integer> ids;
    private int time;

    public TestEntity(List<Integer> ids, int time) {
        this.ids = ids;
        this.time = time;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(time);
        dest.writeList(ids);
    }

    public static final Creator<TestEntity> CREATOR = new Creator<TestEntity>() {
        @Override
        public TestEntity createFromParcel(Parcel in) {
            return new TestEntity(in);
        }

        @Override
        public TestEntity[] newArray(int size) {
            return new TestEntity[size];
        }
    };

    protected TestEntity(Parcel in) {
        time = in.readInt();
        if(ids==null) {
            ids = new ArrayList<>();
        }
        in.readList(ids,Integer.class.getClassLoader());
    }

}
