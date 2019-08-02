package com.magcomm.factorytest.activity;

import android.app.StatusBarManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.TextView;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.adapter.TestResultAdapter;
import com.magcomm.factorytest.entity.TestResultEntity;
import com.magcomm.factorytest.util.ProviderUtil;
import com.magcomm.factorytest.util.SharepreferenceUtil;

import java.util.List;

public class TestResultActivity extends AppCompatActivity {
    private static final String TAG = "zhangziran";
    private TextView tvTestDate;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private List<TestResultEntity> resultEntities;
    private TestResultAdapter resultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test_result);
        FactoryTestApplication.getStacks().add(this);
        recyclerView = (RecyclerView) findViewById(R.id.rv_result);
        tvTestDate = (TextView) findViewById(R.id.tv_test_date);
        tvTestDate.setText(getResources().getString(R.string.test_date) + SharepreferenceUtil.getTime());
        resultEntities = ProviderUtil.getInstance().queryAll();
        if (resultEntities.size() == 0) {
            tvTestDate.setText("No test report");
        }else {
            initRecycler();
        }
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        super.onResume();
    }

    private void initRecycler() {
        resultAdapter = new TestResultAdapter(this);
        resultAdapter.notify(resultEntities);
        gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return resultAdapter.getItemViewType(position);
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(resultAdapter);
    }

    @Override
    protected void onStop() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
        super.onStop();
    }
}
