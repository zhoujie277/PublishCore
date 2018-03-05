package com.mysoft.core;

public class MConstant {
    // 房客代码
    public static final String TENANT_CODE = "tenantCode";
    // 用户代码
    public static final String USER_CODE = "userCode";
    //resource
    public static final String RES_STRING = "string";
    public static final String RES_DRAWABLE = "drawable";
    public static final String RES_LAYOUT = "layout";
    public static final String RES_COLOR = "color";
    public static final String RES_ID = "id";
    public static final String RES_ARRAY = "array";
    public static final String RES_STYLE = "style";
    public static final String RES_STYLEABLE = "styleable";
    public static final String RES_DIMEN = "dimen";
    public static final String RES_RAW = "raw";

    public static final String RES_BOOL = "bool";
    public static final String RES_INTEGER = "integer";
    // prefs
    public static final String PREFS_VERSION = "version";
    public static final String PREFS_SPLASH = "splash";

    // ads
    public static final String PREFS_KEY_SAVEPATH = "ads_path";
    public static final String PREFS_KEY_ADS_OPT = "ads_opt";
    public static final String ADS_OPT_DURATION = "duration";
    public static final String ADS_OPT_SHOW_DURATION = "showDuration";
    public static final String ADS_OPT_SKIPPABLE = "skippable";
    public static final String ADS_OPT_SHOW_DATE = "showDate";
    public static final String ADS_OPT_END_DATE = "endDate";
    public static final String ADS_OPT_PARAMS = "params";
    public static final String ADS_EVENT = "ads_event";

    public static final String BUSINESS_ID = "businessID";

    // requestCodeForResult
    //相机拍照请求码
    public static final int TAKEPHOTO_REQUEST_CODE = 10010;
    //相册选择请求码
    public static final int PHOTOALBUM_REQUEST_CODE = 10011;
    //涂鸦请求码
    public static final int GRAFFITI_REQUEST_CODE = 10012;
    //从引导页进入应用请求码
    public static final int REQ_CLOSE_GUIDE = 10014;
    //二维码扫描请求码
    public static final int REQ_SCAN_QR_CODE = 10015;
    //有道云笔记授权请求码
    public static final int REQ_YNOTE_CODE = 10016;

    // intent extra
    public static final String LAUNCH_URL = "launchUrl";
    public static final String ADS_PATH = "ads_path";

    // intent action
    public static final String ACTION_ADS = "com.mysoft.plugin.action.ads";
    public static final String ACTION_GUIDE = "com.mysoft.plugin.action.guide";
    public static final String ACTION_SPLASH_CLOSE = "com.mysoft.plugin.action.splash.close";
}
