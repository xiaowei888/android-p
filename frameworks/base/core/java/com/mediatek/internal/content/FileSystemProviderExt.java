/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2017. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.internal.content;

import android.content.ContentResolver;
import android.content.Context;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * MTK DRM helper class for {@link android.provider.DocumentsProvider}
 * to perform file operations on local
 * files.
 */

 public class FileSystemProviderExt {

    private static final String TAG = "FileSystemProviderExt";
    private static final boolean DEBUG = "eng".equals(Build.TYPE);

    private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    private static final String classFileSystemProviderHelper
        = "com.mediatek.internal.content.MtkFileSystemProviderHelper";

    private static FileSystemProviderExt sInstance = null;
    private Context mContext;
    private String[] mDefaultProjection;
    private static Object sFSPHelper = null;
    private static Method sAddSupportDRMMethod = null;
    private static Method sGetTypeForNameMethod = null;
    private static Method sGetDefaultProjectionMethod = null;
    private static final Uri BASE_URI =
    new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
      .authority("com.android.externalstorage.documents").build();

    /**
     * Connects MTK DRM Methods.
     *
     * @param context - context of current application.
     */
    public static FileSystemProviderExt getInstance(Context context) {

        if(context == null) {
            return null;
        }

        if(sInstance == null) {
            sInstance = new FileSystemProviderExt(context);
        }
        return sInstance;
    }

    private FileSystemProviderExt(Context context) {
        if (DEBUG) Log.d(TAG, "[DRM]- Contructor is called FileSystemProviderExt");
        mContext = context;
        onCreateMTKHelper();
    }

    private void onCreateMTKHelper() {
        Constructor constructorPlus = null;
        Class mFSPHelperClass = null;
        if (DEBUG) {
            Log.d(TAG, "onCreateMTKHelper");
        }
        try {
            mFSPHelperClass = Class.forName(classFileSystemProviderHelper);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "[DRM]- onCreate Helper Class not found");
            return;
        }
        Class constructorParaClass[] = { Context.class };
        try {
            constructorPlus = mFSPHelperClass.getConstructor(constructorParaClass);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "[DRM]- onCreate Helper Class constructor not found");
            return;
        }
        if (constructorPlus != null) {
            try {
                sFSPHelper = constructorPlus.newInstance(mContext);
            } catch (InvocationTargetException
                     | IllegalAccessException | InstantiationException e) {
                Log.d(TAG, "[DRM] - Failed to create constructor for helper class.");
                return;
            }
        }

        if (DEBUG) {
            Log.d(TAG, "onCreate sFSPHelper : " + sFSPHelper);
        }
        Class supportDrmParaClass[] = { File.class,
                                         RowBuilder.class, String.class, String.class, File.class };

        Class getTypeForNameParaClass[] = { File.class, String.class };

        Class getDefaultProjectionParaClass[] = {};
        try {
            sAddSupportDRMMethod = mFSPHelperClass.getDeclaredMethod("supportDRM",
                                   supportDrmParaClass);
        } catch (NoSuchMethodException e) {
            sAddSupportDRMMethod = null;
        }

        try {
            sGetTypeForNameMethod = mFSPHelperClass.getDeclaredMethod("getTypeForNameMtk",
                                    getTypeForNameParaClass);
        } catch (NoSuchMethodException e) {
            sGetTypeForNameMethod = null;
        }

        try {
            sGetDefaultProjectionMethod = mFSPHelperClass.getDeclaredMethod("getDefaultProjection",
                                          getDefaultProjectionParaClass);
        } catch (NoSuchMethodException e) {
            sGetDefaultProjectionMethod = null;
        }

        String[] retProj = null;
        if (sGetDefaultProjectionMethod != null) {
            try {
                retProj = (String[]) sGetDefaultProjectionMethod.invoke(sFSPHelper);
            } catch (InvocationTargetException | IllegalAccessException e) {
                Log.d(TAG, "[DRM]-Unable to access GetDefaultProjectionMethod()");
                retProj = null;
            }
            mDefaultProjection = retProj;
        }
    }

    /**
     * Gets actual mimetype of DRM files for the current file.
     *
     * @param projection - default projection.
     * @ return - DRM's projection if it is supported otherwise returns the default projections
     */
    public String[] resolveProjection(String[] projection) {
        return mDefaultProjection == null ? projection : mDefaultProjection;
    }

    /**
     * Adds DRM details for current file.
     *
     * @param file - current file
     * @param row a new row create for the current file in file system provider's db
     * @param docId -
     * @param mimeType file's mimetype like audio/image/video
     * @param visibleFile path for DRM files
     */
    public static void addSupportDRMMethod(File file,
        RowBuilder row, String docId, String mimeType, File visibleFile) {
        if (sAddSupportDRMMethod != null) {
            try {
                sAddSupportDRMMethod.invoke(sFSPHelper, file, row, docId, mimeType, visibleFile);
            } catch (InvocationTargetException | IllegalAccessException e) {
                Log.d(TAG, "[DRM]-Unable to access AddSupportDRMMethod()");
            }
        }
    }

    /**
     * Gets actual mimetype of DRM files for the current file.
     *
     * @param file - current file .
     */
    public static String getTypeForNameMethod(File file) {
        String fileName = file.getName();
        String retMethodName = MIMETYPE_OCTET_STREAM;

        if (sGetTypeForNameMethod != null) {
            try {
                retMethodName = (String) sGetTypeForNameMethod.invoke(sFSPHelper, file, fileName);
                Log.d(TAG, "getTypeForNameMethod" + retMethodName);
                if (retMethodName == null) {
                    retMethodName = MIMETYPE_OCTET_STREAM;
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                Log.d(TAG, "[DRM]-Unable to access GetTypeForNameMethod()");
                retMethodName = MIMETYPE_OCTET_STREAM;
            }
        } else {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot >= 0) {
                String extension = fileName.substring(lastDot + 1).toLowerCase();
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) {
                    return mime;
                }
            }

        }
        return retMethodName;
    }

}
