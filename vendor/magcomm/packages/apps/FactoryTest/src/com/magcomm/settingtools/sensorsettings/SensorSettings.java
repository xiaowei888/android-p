package com.magcomm.settingtools.sensorsettings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import com.magcomm.factorytest.R;


/**
 * Created by hucheng on 2018/1/29.
 */

public class SensorSettings extends Activity implements AdapterView.OnItemClickListener{
    private ListView mListView;
    private ImageButton mBackButton;
    private final static int G_SENSOR=0;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_activity);
        mListView= (ListView) findViewById(R.id.sensor_list);
        mBackButton= (ImageButton) findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CharSequence[] items=getResources().getStringArray(R.array.sensor_list);
        ArrayAdapter<CharSequence> adapter=new ArrayAdapter<CharSequence>(this,android.R.layout.simple_expandable_list_item_1,items);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (position){
            case G_SENSOR:
                intent.setClass(SensorSettings.this, GravityActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
