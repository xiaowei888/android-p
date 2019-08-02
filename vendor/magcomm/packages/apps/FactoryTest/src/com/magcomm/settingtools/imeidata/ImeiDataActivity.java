/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.magcomm.settingtools.imeidata;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.AsyncResult;
import java.io.UnsupportedEncodingException;
import com.android.internal.util.HexDump;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface; 
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.magcomm.factorytest.R;

/**
 * Created by hucheng on 2018/3/2.
 */

public class ImeiDataActivity extends Activity {
    private ListView mPhoneView;
    private PhoneAdapter mAdapter;
    private TelephonyManager mTelephonyManager;
    private int mPhoneCount = 0;
    private Phone mGsmPhone = null;
    private List<String> mDataList;
    private List<EditText> mEditList;
    private List<Phone> mPhoneList;
	private int mResult;
	private StringBuilder mSb;
	private int num;
	private static final int EVENT_QUERY_NEIGHBORING_CIDS_DONE = 1001;
    private static final int EVENT_AT_CMD_DONE                 = 1003;
	private static final String INFO_TITLE = "RESULT INFO.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_layout);
        mPhoneView = (ListView) findViewById(R.id.lv_phone);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mPhoneCount = mTelephonyManager.getPhoneCount();
        mDataList = new ArrayList<String>();
        mEditList=new ArrayList<EditText>();
		mPhoneList=new ArrayList<Phone>();
		mSb=new StringBuilder();
        for (int i = 0; i < mPhoneCount; i++) {
            mDataList.add(mTelephonyManager.getDeviceId(i));
			mPhoneList.add(PhoneFactory.getPhone(i));
        }
        mAdapter=new PhoneAdapter();
        mPhoneView.setAdapter(mAdapter);
        ((Button)findViewById(R.id.send_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i=0;i<mEditList.size();i++){
                    String text=mEditList.get(i).getText().toString();
                    if (text==null||text.length()<15){
						showInfo("Input fail ( length < 15 )",i);
                    }else {
                        setImei(i,text);
                    }
                }
            }
        });
    }
    class PhoneAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view==null){
                view= LayoutInflater.from(ImeiDataActivity.this).inflate(R.layout.phone_info_view,null);
            }
            TextView imeiName=(TextView)view.findViewById(R.id.imei_name);
            TextView imeiInfo=(TextView)view.findViewById(R.id.imei_info);
            EditText imeiEdit=(EditText)view.findViewById(R.id.imei_edit);
            imeiName.setText("PHONE"+(i+1));
            imeiInfo.setText("IMEI: "+mDataList.get(i));
			if(i<mEditList.size()){
				mEditList.set(i,imeiEdit);
			}else{
				mEditList.add(imeiEdit);
			}
            return view;
        }
    }
	private void setImei(int index,String text){
		String atCmdLine = "";

            if (0 == index){
                atCmdLine = "AT+EGMR=1,7,\"" + text + "\"";
            }else if (1 == index){
                atCmdLine = "AT+EGMR=1,10,\"" + text + "\"";
            }

            try {
                byte[] rawData = atCmdLine.getBytes();
                byte[] cmdByte = new byte[rawData.length + 1];
                System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
                cmdByte[cmdByte.length - 1] = 0;
				mPhoneList.get(index).invokeOemRilRequestRaw(cmdByte,mHandler.obtainMessage(EVENT_AT_CMD_DONE,index,index));
            } catch (NullPointerException ee) {
                ee.printStackTrace();
            }
	}	
	private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;

            switch (msg.what) {

            case EVENT_QUERY_NEIGHBORING_CIDS_DONE:
                //updateNeighboringCids();
                break;

            case EVENT_AT_CMD_DONE:
                ar = (AsyncResult) msg.obj;
                handleAtCmdResponse(ar,msg.arg1);
                break;
            default:
                break;

            }
        }
    };
	void handleAtCmdResponse(AsyncResult ar,int index) {
        if (ar.exception != null) {
            showInfo("AT failed to send",index);
        } else {
            try {
                byte[] rawData = (byte[]) ar.result;
                String txt = new String(rawData, "UTF-8");
                showInfo("AT command : " + txt.trim(),index);
            } catch (NullPointerException e) {
                showInfo("Something is wrong",index);
                e.printStackTrace();
            } catch (UnsupportedEncodingException ee) {
                ee.printStackTrace();
            }
        }
    }
	private void showInfo(String info,int index) {
		if(index==0&&num>0){
			mSb.insert(index,"phone"+(index+1)+" : "+info+"\n");
		}else{
			mSb.append("phone"+(index+1)+" : "+info+"\n");
		}
		num++;
		if(num==mPhoneList.size()){
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle(INFO_TITLE);
        infoDialog.setMessage(mSb.toString());
        infoDialog.setIcon(android.R.drawable.ic_dialog_alert);
        infoDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) { 
                    }  
                });  
        infoDialog.show();
		num=0;
		mSb.setLength(0);
		}
    }
}
