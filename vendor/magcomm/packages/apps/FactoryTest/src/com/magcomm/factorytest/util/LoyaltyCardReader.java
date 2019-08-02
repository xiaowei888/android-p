 /* Copyright (C) 2013 The Android Open Source Project
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

 package com.magcomm.factorytest.util;

 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.IsoDep;
 import android.nfc.tech.MifareClassic;
 import android.nfc.tech.MifareUltralight;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NfcA;
 import android.nfc.tech.NfcB;
 import android.nfc.tech.NfcBarcode;
 import android.nfc.tech.NfcF;
 import android.nfc.tech.NfcV;
 import android.util.Log;

 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.concurrent.CopyOnWriteArrayList;


 public class LoyaltyCardReader implements NfcAdapter.ReaderCallback {
     private static final String TAG = "zhangziran";
     private WeakReference<AccountCallback> mAccountCallback;
     private CopyOnWriteArrayList<String> results;

     public interface AccountCallback {
         public void onAccountReceived(CopyOnWriteArrayList<String> results);
     }

     public LoyaltyCardReader(AccountCallback accountCallback) {
         mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
         results = new CopyOnWriteArrayList<>();
     }

     @Override
     public void onTagDiscovered(Tag tag) {
         results.clear();
         if (tag != null) {
             String[] techs = tag.getTechList();
             String tagInfo = "";
             for (String tech : techs) {
                 Log.i(TAG, "onTagDiscovered: tech=" + tech);
                 if (tech == IsoDep.class.getName()) {
                     tagInfo = "IsoDep:" + parseIsoDep(tag);
                 } else if (tech == MifareClassic.class.getName()) {
                     tagInfo = "MifareClassic:" + parseMifareClassic(tag);
                 } else if (tech == MifareUltralight.class.getName()) {
                     tagInfo = "MifareUltralight:" + parseMifareUltralight(tag);
                 } else if (tech == Ndef.class.getName()) {
                     tagInfo = "Ndef:" + parseNdef(tag);
                 } else if (tech == NfcA.class.getName()) {
                     tagInfo = "NfcA:" + parseNfcA(tag);
                 } else if (tech == NfcB.class.getName()) {
                     tagInfo = "NfcB:" + parseNfcB(tag);
                 } else if (tech == NfcBarcode.class.getName()) {
                     tagInfo = "NfcBarcode:" + parseNfcBarcode(tag);
                 } else if (tech == NfcF.class.getName()) {
                     tagInfo = "NfcF:" + parseNfcF(tag);
                 } else if (tech == NfcV.class.getName()) {
                     tagInfo = "NfcV:" + parseNfcV(tag);
                 }else{
                     tagInfo= "Unknow Tech";
                 }
                 results.add(tagInfo);
             }
             mAccountCallback.get().onAccountReceived(results);
         }
     }

     private boolean lockIsoDep = true;

     private synchronized String parseIsoDep(Tag tag) {
         String info = null;
         if (lockIsoDep) {
             IsoDep isoDep = IsoDep.get(tag);
             if (isoDep != null) {
                 try {
                     lockIsoDep = false;
                     isoDep.connect();
                     byte[] response = isoDep.getHiLayerResponse();
                     if (response != null) {
                         info = "HiLayerResponse=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         isoDep.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockIsoDep = true;
                 }
             }
         }
         return info;
     }

     private boolean lockMifareClassic = true;

     private synchronized String parseMifareClassic(Tag tag) {
         String info = null;
         if (lockMifareClassic) {
             MifareClassic mifareClassic = MifareClassic.get(tag);
             if (mifareClassic != null) {
                 try {
                     lockMifareClassic = false;
                     mifareClassic.connect();
                     byte[] response = mifareClassic.readBlock(0);
                     if (response != null) {
                         info = "Block[0]=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         mifareClassic.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockMifareClassic = true;
                 }
             }
         }
         return info;
     }


     private boolean lockMifareUltralight = true;

     private synchronized String parseMifareUltralight(Tag tag) {
         String info = null;
         if (lockMifareUltralight) {
             MifareUltralight mifareUltralight = MifareUltralight.get(tag);
             if (mifareUltralight != null) {
                 try {
                     lockMifareUltralight = false;
                     mifareUltralight.connect();
                     byte[] response = mifareUltralight.readPages(0);
                     if (response != null) {
                         info = "Pages[0]=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         mifareUltralight.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockMifareUltralight = true;
                 }
             }
         }
         return info;
     }

     private boolean lockNdef = true;

     private synchronized String parseNdef(Tag tag) {
         String info = null;
         if (lockNdef) {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 try {
                     lockNdef = false;
                     ndef.connect();
                     byte[] response = ndef.getCachedNdefMessage() == null ? null : ndef.getCachedNdefMessage().toByteArray();
                     if (response != null) {
                         info = "CachedNdefMessage=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         ndef.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNdef = true;
                 }
             }
         }
         return info;
     }

     private boolean lockNfcA = true;

     private synchronized String parseNfcA(Tag tag) {
         String info = null;
         if (lockNfcA) {
             NfcA nfcA = NfcA.get(tag);
             if (nfcA != null) {
                 try {
                     lockNfcA = false;
                     nfcA.connect();
                     byte[] response = nfcA.getAtqa();
                     if (response != null) {
                         info = "Atqa=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         nfcA.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNfcA = true;
                 }
             }
         }
         return info;
     }

     private boolean lockNfcB = true;

     private synchronized String parseNfcB(Tag tag) {
         String info = null;
         if (lockNfcB) {
             NfcB nfcB = NfcB.get(tag);
             if (nfcB != null) {
                 try {
                     lockNfcB = false;
                     nfcB.connect();
                     byte[] response = nfcB.getApplicationData();
                     if (response != null) {
                         info = "ApplicationData=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         nfcB.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNfcB = true;
                 }
             }
         }
         return info;
     }

     private boolean lockNfcBarcode = true;

     private synchronized String parseNfcBarcode(Tag tag) {
         String info = null;
         if (lockNfcBarcode) {
             NfcBarcode nfcBarcode = NfcBarcode.get(tag);
             if (nfcBarcode != null) {
                 try {
                     lockNfcBarcode = false;
                     nfcBarcode.connect();
                     byte[] response = nfcBarcode.getBarcode();
                     if (response != null) {
                         info = "HiLayerResponse:" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         nfcBarcode.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNfcBarcode = true;
                 }
             }
         }
         return info;
     }


     private boolean lockNfcF = true;

     private synchronized String parseNfcF(Tag tag) {
         String info = null;
         if (lockNfcF) {
             NfcF nfcF = NfcF.get(tag);
             if (nfcF != null) {
                 try {
                     lockNfcF = false;
                     nfcF.connect();
                     byte[] response = nfcF.getManufacturer();
                     if (response != null) {
                         info = "Manufacturer=" + ByteArrayToHexString(response);
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         nfcF.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNfcF = true;
                 }
             }
         }
         return info;
     }

     private boolean lockNfcV = true;

     private synchronized String parseNfcV(Tag tag) {
         String info = null;
         if (lockNfcV) {
             NfcV nfcV = NfcV.get(tag);
             if (nfcV != null) {
                 try {
                     lockNfcV = false;
                     nfcV.connect();
                     byte response = nfcV.getDsfId();
                     if (response != 0) {
                         info = "DsfId=" + ByteArrayToHexString(new byte[]{response});
                     }
                 } catch (Exception e) {
                     Log.i(TAG, "parseIsoDep: e=" + e.toString());
                 } finally {
                     try {
                         nfcV.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     lockNfcV = true;
                 }
             }
         }
         return info;
     }

     public static String ByteArrayToHexString(byte[] bytes) {
         final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
         char[] hexChars = new char[bytes.length * 2];
         int v;
         for (int j = 0; j < bytes.length; j++) {
             v = bytes[j] & 0xFF;
             hexChars[j * 2] = hexArray[v >>> 4];
             hexChars[j * 2 + 1] = hexArray[v & 0x0F];
         }
         return new String(hexChars);
     }
 }
