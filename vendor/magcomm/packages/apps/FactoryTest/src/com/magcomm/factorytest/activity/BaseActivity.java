package com.magcomm.factorytest.activity;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.content.SharedPreferences;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.entity.ClassEntity;
import com.magcomm.factorytest.util.Config;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private List<ClassEntity> classEntities;
    private List<Button> buttons;
    private GridLayout gridLayout;
    private int position;
    private static int ID = 0x3E8;
    private long firstClick;
    private long secondClick;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_base);
        FactoryTestApplication.getStacks().add(this);
        classEntities = FactoryTestApplication.getClasss();
        buttons = new ArrayList<>();
        mPreferences = getSharedPreferences("my_preference", MODE_PRIVATE);
        mEditor = mPreferences.edit();
        initView();
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        super.onResume();
    }

    private void initView() {
        gridLayout = (GridLayout) findViewById(R.id.gl_item_group);
        for (int i = 0; i < classEntities.size(); i++) {
            Button button = new Button(this);
            button.setText(classEntities.get(i).getItemName());
            button.setTag(classEntities.get(i).getaClass());
            button.setTransformationMethod(null);
			// magcomm songkun add avoide info loss when oncreate again 190301
            if (mPreferences.contains(button.getText().toString())) {
                Log.d("songkun", "contains");
                if (mPreferences.getBoolean(button.getText().toString(), true)) {
                    button.setBackground(getDrawable(R.drawable.btn_success_bg));
                } else {
                    button.setBackground(getDrawable(R.drawable.btn_fail_bg));
                }
            } else {
                button.setBackground(getDrawable(R.drawable.btn_normal_bg));
            }
            button.setId(ID + i);
            button.setOnClickListener(this);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1.0f);
            layoutParams.setGravity(Gravity.CENTER);
            button.setLayoutParams(layoutParams);
            buttons.add(button);
            gridLayout.addView(button);
        }
    }

    @Override
    public void onClick(View v) {
        firstClick = System.currentTimeMillis();
        if (firstClick - secondClick < 1500) return;
        position = (v.getId()) % 1000;
        Intent intent = null;
        if (v.getTag().toString().equals("class com.magcomm.factorytest.item.NFCActivity")) {
            intent = new Intent(BaseActivity.this, (Class) v.getTag());
        } else {
            intent = new Intent(BaseActivity.this, ItemTestActivity.class);
            intent.putExtra(Config.TEST_KEY, Config.MANUAL_TEST);
            intent.putExtra(Config.MANUAL_ITEM_KEY, position);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, Config.REQUEST_CODE);
        secondClick = firstClick;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Config.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case 0:
                    buttons.get(position).setBackground(getDrawable(R.drawable.btn_fail_bg));
					// magcomm songkun add avoide info loss when oncreate again 190301
                    mEditor.putBoolean(buttons.get(position).getText().toString(), false);
                    mEditor.commit();
                    break;
                case 1:
                    buttons.get(position).setBackground(getDrawable(R.drawable.btn_success_bg));
					// magcomm songkun add avoide info loss when oncreate again 190301
                    mEditor.putBoolean(buttons.get(position).getText().toString(), true);
                    mEditor.commit();
                    break;
                default:
					// magcomm songkun add back key to normal 190301
                    buttons.get(position).setBackground(getDrawable(R.drawable.btn_normal_bg));
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
        super.onStop();
    }

	// magcomm songkun add avoide info loss when oncreate again 190301 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                buttons.clear();
                buttons = null;
                mEditor.clear();
                mEditor.commit();
                mEditor = null;
                mPreferences = null;
                this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
