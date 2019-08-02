package com.magcomm.factorytest.item;

import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;
import com.magcomm.factorytest.util.LoyaltyCardReader;

import java.util.concurrent.CopyOnWriteArrayList;

public class NFCActivity extends AppCompatActivity implements View.OnClickListener, LoyaltyCardReader.AccountCallback {
    private Button btnSuccess, btnFail;
    private TextView tvTest;
    private LoyaltyCardReader mLoyaltyCardReader;
    private String[][] techList;
    private IntentFilter[] intentFilters;
    private PendingIntent pendingIntent;
    private boolean isOpen = false;
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_nfc);
        initview();
        initNFC();
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        enableForegroundDispatch();
        enableReaderMode();
        super.onResume();
    }

    private void initview() {
        btnSuccess = (Button) findViewById(R.id.btn_nfc_success);
        btnFail = (Button) findViewById(R.id.btn_nfc_fail);
        tvTest = (TextView) findViewById(R.id.card_account_field);
        btnSuccess.setOnClickListener(this);
        btnFail.setOnClickListener(this);
        tvTest.setText("Waiting...");
        mLoyaltyCardReader = new LoyaltyCardReader(this);
    }

    private void initNFC() {
        techList = new String[][]{new String[]{IsoDep.class.getName()},
                new String[]{MifareClassic.class.getName()},
                new String[]{MifareUltralight.class.getName()},
                new String[]{Ndef.class.getName()},
                new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcBarcode.class.getName()},
                new String[]{NfcF.class.getName()},
                new String[]{NfcV.class.getName()}};
        intentFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void enableForegroundDispatch() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            isOpen = nfc.isEnabled();
            if(!isOpen) {
                nfc.enable();
            }
            nfc.enableForegroundDispatch(this, pendingIntent, intentFilters, techList);
        }
    }

    private void enableReaderMode() {
        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 50);
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.enableReaderMode(this, mLoyaltyCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }

    private void disableForegroundDispatch() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableForegroundDispatch(this);
            if(!isOpen && nfc.isEnabled()) {
                nfc.disable();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nfc_success:
                setResult(Config.SUCCESS_RESULT);
                finish();
                break;
            case R.id.btn_nfc_fail:
                setResult(Config.FAIL_RESULT);
                finish();
                break;
        }
    }

    @Override
    public void onAccountReceived(CopyOnWriteArrayList<String> results) {
        runOnUiThread(() -> show(results));
    }

    private void show(CopyOnWriteArrayList<String> results) {
        tvTest.setText("");
        if (results != null) {
            for (String result : results) {
                tvTest.append(result + "\n");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        disableReaderMode();
        disableForegroundDispatch();
        super.onPause();
    }

    @Override
    protected void onStop() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
        super.onStop();
    }

}
