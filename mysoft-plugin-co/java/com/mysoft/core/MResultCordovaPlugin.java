package com.mysoft.core;

import android.os.Bundle;

import com.mysoft.core.exception.MArgumentException;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 带有基本状态保存的插件。
 * 建议需要onActivityResult的CordovaPlugin都继承这个类。
 * 会保证CallbackContext任何时候都有效
 * Created by zhouj04 on 2016/4/22.
 */
public abstract class MResultCordovaPlugin extends MCordovaPlugin {
    private static final String ACTION = "save_action";
    protected final String TAG = getClass().getSimpleName();
    private AtomicReference<CallbackContext> mCallbackContext = new AtomicReference<>();
    private AtomicReference<String> mAction = new AtomicReference<>();

    @Override
    public boolean onExecute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException,
            MArgumentException {
        mAction.getAndSet(action);
        mCallbackContext.getAndSet(callbackContext);
        return onExecute(action, args);
    }

    public abstract boolean onExecute(String action, JSONArray args) throws JSONException,
            MArgumentException;

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        mCallbackContext.getAndSet(callbackContext);
        String action = state.getString(ACTION);
        mAction.getAndSet(action);
        onRestoreInstanceState(state);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, mAction.get());
        onSaveInstanceState(bundle);
        return bundle;
    }

    public CallbackContext getCallbackContext() {
        return mCallbackContext.get();
    }

    public String getAction() {
        return mAction.get();
    }

    /**
     * 恢复数据
     */
    protected void onRestoreInstanceState(Bundle state) {}

    /**
     * 保存需要重建的数据
     */
    protected void onSaveInstanceState(Bundle state) {}

    /**
     * 回调前端Js
     */
    protected void callback(int code, boolean keepCallback, Object... args) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            callback(code, context, keepCallback, args);
        }
    }

    public void success(String string) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            success(context, string);
        }
    }

    public void success() {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            success(context);
        }
    }

    protected void success(boolean value) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            success(context, value);
        }
    }

    protected void success(int value) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            success(context, value);
        }
    }

    public void error(String errMsg) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            error(context, errMsg);
        }
    }

    public void error(int code, String errMsg) {
        final CallbackContext context = mCallbackContext.get();
        if (context != null) {
            error(context, code, errMsg);
        }
    }
}
