package com.mysoft.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.mysoft.core.exception.MArgumentException;
import com.mysoft.core.util.PrefsUtils;
import com.mysoft.core.util.SentryUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhouj04 on 2016/5/10.
 */
public class MicCore extends MCordovaPlugin {

    public static final int INFO = 1;
    public static final int ERROR = 2;
    public static final int CODE_GUIDE_CLOSE = 101;
    public static final int CODE_ADS_CLICK = 102;
    public static final int CODE_ADS_SKIP = 103;
    private static final String ACTION_CONNECT = "connect";
    private static final String ACTION_CLOSE = "close";
    private static volatile CallbackContext sWebContext;
    private static volatile boolean _initial = false;
    private static Object _lock = new Object();
    private static HandlerThread sMicHandlerThread = new HandlerThread("MicCore-Handler-Thread");
    private static Handler sHandler;

    static {
        sMicHandlerThread.start();
        sHandler = new Handler(sMicHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (!_initial) {
                    synchronized (_lock) {
                        try {
                            while (!_initial) {
                                _lock.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                callback(msg.what, msg.obj);
            }
        };
    }

    public static void asyncCallback(int code, Object obj) {
        Message message = sHandler.obtainMessage(code);
        message.obj = obj;
        sHandler.sendMessage(message);
    }

    /**
     * 回调前端Js
     */
    public static synchronized void callback(int code, Object... args) {
        if (sWebContext == null) {
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
            pr.setKeepCallback(true);
            sWebContext.sendPluginResult(pr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void pluginInitialize() {
        synchronized (_lock) {
            _initial = true;
            _lock.notifyAll();
        }
    }

    @Override
    public boolean onExecute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException,
            MArgumentException {
        if (ACTION_CONNECT.equals(action)) {
            synchronized (this) {
                sWebContext = callbackContext;
                callback(1, "connect success!");
            }
            return true;
        } else if (ACTION_CLOSE.equals(action)) {
            synchronized (this) {
                sWebContext = null;
            }
            success(callbackContext, "close connection!");
            return true;
        } else if ("keepScreenOn".equals(action)) {
            final boolean screenOn = args.optBoolean(0, false);
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    L.d(TAG, "screenOn=" + screenOn);
                    webView.getView().setKeepScreenOn(screenOn);
                    success(callbackContext);
                }
            });
            return true;
        } else if ("sentryLogSwitch".equals(action)) {
            final boolean isOff = args.optBoolean(0, false);
            JSONObject jsonObject = args.optJSONObject(1);
            SentryUtils.SWITCH_ON = !isOff;
            if (jsonObject != null) {
                SentryUtils.INFO_SWITCH = jsonObject.optBoolean("info_switch_on", false);
            }
            success(callbackContext);
            return true;
        } else if ("saveBusiness".equals(action)) {
            String saveBusiness = args.getString(0);
            PrefsUtils.putString(getContext(), MConstant.BUSINESS_ID, saveBusiness);
            success(callbackContext);
            return true;
        }
        return false;
    }
}
