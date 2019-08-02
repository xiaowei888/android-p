package com.magcomm.factorytest.util;

import android.net.Uri;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class Config {
    public static final String FACTORY_ITEM_PACKAGE = "com.magcomm.factorytest.item.";
    public static final String FRAGMENT = "Fragment";
    public static final String ACTIVITY = "Activity";
    public static final int SUCCESS_RESULT = 1;
    public static final int FAIL_RESULT = 0;
    public static final int NO_RESULT = 2;
    public static final int REQUEST_CODE = 0;
    public static final String MANUAL_TEST = "manual";
    public static final String AUTO_TEST = "auto";
    public static final String VERSION_TEST = "version";
    public static final String TEST_KEY = "test_mode";
    public static final String MANUAL_ITEM_KEY = "item";
    public static final String LOCK = "locked";

    public static final Uri FACTORY_URI = Uri.parse("content://com.magcomm.factorytest.provider/factory");
    public static final String FACTORY_TABLE = "factory";
    public static final String FACTORY_ITEM = "item";
    public static final String FACTORY_RESULT = "result";
    public static final String FACTORY_LANGUAGE = "language";

    public static final String NOT_TEST = "not test";
    public static final String SUCCESS = "pass";
    public static final String FAIL = "fail";

    public static final String ZH = "zh";
    public static final String EN = "en";


}
