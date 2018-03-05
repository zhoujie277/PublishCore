package com.mysoft.core.util;

import android.os.Build;
import android.text.TextUtils;

import com.getsentry.raven.android.Raven;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.mysoft.core.MApplication;
import com.mysoft.core.MConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhouj04 on 2017/2/22.
 */

public class SentryUtils {

    public static volatile boolean SWITCH_ON = true;
    public static volatile boolean INFO_SWITCH = false;

    public static void reportRequest(String prefix, JSONArray args, String callbackId) {
        if (!SWITCH_ON) {
            return;
        }
        EventBuilder eventBuilder = new EventBuilder()
                .withExtra("Request", args.toString())
                .withLevel(Event.Level.INFO);
        buildCommon(eventBuilder, prefix, callbackId);
    }

    public static void reportResponse(String prefix, String callbackId, String value) {
        if (!SWITCH_ON) {
            return;
        }
        EventBuilder eventBuilder = new EventBuilder()
                .withExtra("Response", value)
                .withLevel(Event.Level.INFO);
        buildCommon(eventBuilder, prefix, callbackId);
    }

    public static void reportInfo(String prefix, String callbackId, String value, String requestParams) {
        if (!SWITCH_ON || !INFO_SWITCH) {
            return;
        }
        EventBuilder eventBuilder = new EventBuilder()
                .withExtra("Response", value)
                .withExtra("Request", requestParams)
                .withLevel(Event.Level.INFO);
        buildCommon(eventBuilder, prefix, callbackId);
    }

    public static void reportError(String prefix, String callbackId, String errorInfo, String requestParams) {
        if (!SWITCH_ON) {
            return;
        }
        EventBuilder eventBuilder = new EventBuilder()
                .withExtra("Response", errorInfo)
                .withExtra("Request", requestParams)
                .withLevel(Event.Level.ERROR);
        buildCommon(eventBuilder, prefix, callbackId);
    }

    private static EventBuilder buildCommon(EventBuilder eventBuilder, String prefix, String callbackId) {
        eventBuilder.withMessage(prefix + "_Android")
                .withCulprit(callbackId).withTag("packageName", MApplication.getApplication().getPackageName())
                .withTag("transaction", callbackId)
                .withPlatform("android");
        eventBuilder.withExtra("BusinessID", PrefsUtils.getString(MApplication.getApplication(), MConstant.BUSINESS_ID));
        buildDevice(eventBuilder);
        buildApp(eventBuilder);
        Raven.capture(eventBuilder.build());
        return eventBuilder;
    }

    private static EventBuilder buildDevice(EventBuilder eventBuilder) {
        JSONObject deviceJson = new JSONObject();
        try {
            deviceJson.put("name", Build.PRODUCT);
            deviceJson.put("family", Build.MANUFACTURER);
            deviceJson.put("model", Build.MODEL);
            deviceJson.put("arch", Build.CPU_ABI);
            deviceJson.put("sdkVersion", Build.VERSION.RELEASE);
            deviceJson.put("sdkInt", Build.VERSION.SDK_INT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventBuilder.withExtra("Device", deviceJson.toString());
        return eventBuilder;
    }

    private static EventBuilder buildApp(EventBuilder eventBuilder) {
        JSONObject appJson = new JSONObject();
        try {
            appJson.put("versionName", PackageUtils.getAppVersion(MApplication.getApplication()));
            appJson.put("versionCode", PackageUtils.getAppVersionCode(MApplication.getApplication()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventBuilder.withExtra("App", appJson.toString());
        return eventBuilder;
    }


}
