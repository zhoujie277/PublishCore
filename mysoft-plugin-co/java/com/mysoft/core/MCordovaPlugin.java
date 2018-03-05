package com.mysoft.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mysoft.core.exception.MArgumentException;
import com.mysoft.core.util.SentryUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 明源公共cordova插件
 *
 * @author zhouj04
 */
public class MCordovaPlugin extends CordovaPlugin {
    public static final String ERR_CODE = "errCode";
    public static final String ERR_MSG = "errMsg";
    public static final int ERR_CODE_CAMERA_PERMISSION_REFUSE = 1100;
    public static final int ERR_CODE_STORAGE_PERMISSION_REFUSE = 1200;
    public static final int ERR_CODE_PARAMS_CHECK = 1001;
    public static final int ERR_CODE_CONNECT_TIMEOUT = 2102;
    public static final int ERR_CODE_CANCEL = 999;
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    protected final String TAG = getClass().getSimpleName();
    private Context mContext;
    private volatile String mActionString;
    private volatile String mActionParams;

    public static Handler getUiHandler() {
        return uiHandler;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        L.i(TAG, "initialize...");
        mContext = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        L.i(TAG, "execute ACTION:" + action + ", args:" + args);
        mActionString = action;
        mActionParams = args.toString();
        try {
            if (!onExecute(action, args, callbackContext)) {
                error(callbackContext, "INVALID ACTION! " + action);
            }
        } catch (JSONException e) {
            L.e(TAG, "JSON params not match args:" + args, e);
            error(callbackContext, ERR_CODE_PARAMS_CHECK, "JSON params not match! args:" + args);
        } catch (MArgumentException e) {
            L.e(TAG, "PARAMS ERROR args:" + args, e);
            error(callbackContext, ERR_CODE_PARAMS_CHECK, "PARAMS ERROR args:" + args);
        } catch (Exception e) {
            L.e(TAG, "onExecute Exception.", e);
            error(callbackContext, "onExecute Exception !" + e.getMessage());
        }
        return true;
    }

    public boolean onExecute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException,
            MArgumentException {
        return false;
    }

    public Context getContext() {
        return mContext;
    }

    public JSONObject getErrJson(String errMsg) {
        return getErrJson(-1, errMsg);
    }

    public JSONObject getErrJson(int code, String errMsg) {
        JSONObject json = new JSONObject();
        try {
            json.put(ERR_CODE, code);
            json.put(ERR_MSG, errMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 回调前端Js
     */
    public void callback(int code, CallbackContext callbackContext, boolean keepCallback, Object... args) {
        L.i(TAG, "success code:" + code + ", keepCallback:" + keepCallback);
        if (callbackContext == null) {
            // 此处为了兼容验房的老代码插件，避免崩溃，所以加个空判断
            return;
        }
        try {
            JSONArray arr = new JSONArray();
            arr.put(0, code);
            if (args != null) {
                for (int j = 0; j < args.length; j++) {
                    arr.put(j + 1, args[j]);
                }
            }
            PluginResult pr = new PluginResult(PluginResult.Status.OK, arr);
            pr.setKeepCallback(keepCallback);
            callbackResult(callbackContext, pr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void success(CallbackContext callbackContext, JSONArray value) {
        callbackResult(callbackContext, new PluginResult(PluginResult.Status.OK, value));
    }

    public void success(CallbackContext callbackContext, JSONObject value) {
        callbackResult(callbackContext, new PluginResult(PluginResult.Status.OK, value));
    }

    public void success(CallbackContext callbackContext, boolean value) {
        callbackResult(callbackContext, new PluginResult(PluginResult.Status.OK, value));
    }

    public void success(CallbackContext callbackContext, int value) {
        callbackResult(callbackContext, new PluginResult(PluginResult.Status.OK, value));
    }

    public void success(CallbackContext callbackContext, String value) {
        callbackResult(callbackContext, new PluginResult(PluginResult.Status.OK, value));
    }

    public void success(CallbackContext context) {
        callbackResult(context, new PluginResult(PluginResult.Status.OK));
    }

    public void error(CallbackContext context, String errMsg) {
        callbackResult(context, new PluginResult(PluginResult.Status.ERROR, errMsg));
    }

    public void error(CallbackContext context, int code, String errMsg) {
        callbackResult(context, new PluginResult(PluginResult.Status.ERROR, getErrJson(code, errMsg)));
    }

    public void error(CallbackContext context, JSONObject err) {
        callbackResult(context, new PluginResult(PluginResult.Status.ERROR, err));
    }

    public void error(CallbackContext context, JSONArray err) {
        callbackResult(context, new PluginResult(PluginResult.Status.ERROR, err));
    }

    public void callbackResult(CallbackContext callbackContext, PluginResult pr) {
        if (callbackContext != null) {
            callbackContext.sendPluginResult(pr);
            if (!TextUtils.isEmpty(mActionString)) {
                String prefix = getPrefixString();
                if (pr.getStatus() == PluginResult.Status.ERROR.ordinal()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(pr.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //ERR_CODE_CANCEL操作不需要上传
                    if (jsonObject == null || (jsonObject != null && jsonObject.optInt(ERR_CODE) != ERR_CODE_CANCEL)) {
                        SentryUtils.reportError(prefix, callbackContext.getCallbackId(), pr.getMessage(), mActionParams);
                    }
                } else {
                    SentryUtils.reportInfo(prefix, callbackContext.getCallbackId(), pr.getMessage(), mActionParams);
                }
            }
        }
    }

    @NonNull
    private String getPrefixString() {
        return getClass().getSimpleName() + "_" + mActionString;
    }
}
