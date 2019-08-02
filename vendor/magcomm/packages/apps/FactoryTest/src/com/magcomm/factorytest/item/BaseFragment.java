package com.magcomm.factorytest.item;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magcomm.factorytest.activity.ItemTestActivity;
import com.magcomm.factorytest.util.Config;
import com.magcomm.factorytest.util.ProviderUtil;
import com.magcomm.factorytest.util.ResourcesUtil;

/**
 * Created by zhangziran on 2017/12/21.
 */

public abstract class BaseFragment extends Fragment implements ItemTestActivity.OnTouchListener, ItemTestActivity.OnButtonClickListener {
    private static final String TAG = "zhangziran";
    protected Handler handler;
    protected Context context;
    protected View view;
    protected String mode;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ItemTestActivity) {
            this.context = context;
            handler = ((ItemTestActivity) context).getHandler();
            ((ItemTestActivity) context).setOnTouchListener(this);
            ((ItemTestActivity) context).setOnButtonClickListener(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(getCurrentView(), null);
        onFragmentCreat();
        return view;
    }

    protected void setBtnSuccessEnable(boolean enable) {
        ((ItemTestActivity) context).setBtnSuccessEnable(enable);
    }

    protected void setBtnFailEnable(boolean enable) {
        ((ItemTestActivity) context).setBtnFailEnable(enable);
    }

    protected void setLLResultVisibility(boolean enable) {
        ((ItemTestActivity) context).setLLResultVisibility(enable);
    }

    protected abstract int getCurrentView();

    protected abstract void onFragmentCreat();

    @Override
    public void onDown(int x, int y) {

    }

    @Override
    public void onMove(int x, int y) {

    }

    @Override
    public void onUp(int x, int y) {

    }

    @Override
    public void onKeyDown(int keyCode) {

    }

    @Override
    public void onClick(String result) {

    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    protected abstract String getTotalName();


    protected void updateDataBase(String result) {
        //Log.i(TAG, "updateDataBase:result=" + result + "--getMode()=" + getMode());
        if (Config.AUTO_TEST.equals(getMode())) {
            String totalName = getTotalName();
            //Log.i("zhangziran", "updateDataBase: " + totalName);
            String name = totalName.substring(0, totalName.length() - 8);
            int id = context.getResources().getIdentifier(name, "string", context.getPackageName());
            String chinaItemName = ResourcesUtil.getItem(id, context)[0];
            String englishItemName = ResourcesUtil.getItem(id, context)[1];
            ProviderUtil.getInstance().update(chinaItemName, result);
            ProviderUtil.getInstance().update(englishItemName, result);
        }
    }

    public abstract void destroy();

}
