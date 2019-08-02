package com.magcomm.factorytest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangziran on 2017/12/25.
 */

public abstract class FactoryAdapter<T> extends BaseAdapter {
    protected Context context;
    protected List<T> entities;
    protected LayoutInflater inflater;

    public FactoryAdapter(Context context, List<T> entities) {
        this.context = context;
        this.inflater =  LayoutInflater.from(context);
        if(entities==null) {
            this.entities =  new ArrayList<>();
        }else {
            this.entities = entities;
        }
    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public Object getItem(int position) {
        return entities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
