package com.mysoft.core;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.getsentry.raven.android.Raven;
import com.mysoft.core.util.ResourceUtils;
import com.mysoft.core.util.StorageUtils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;

public class MApplication extends Application implements UncaughtExceptionHandler {

    public static String TAG = MApplication.class.getName();

    private static MApplication instance;
    private UncaughtExceptionHandler mDefaultExceptionHandler;// 获取crash的缺省handler

    public static MApplication getApplication() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 暂时先放在主线程。遇到这类问题，第一还是尽可能剔除没用到的第三方库，减少方法数
        // 如果依赖第三方库很多，该方法需在子线程中进行。此后需注意空指针错误。
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
//        NewSdcardUtils.init(true);
        loadDefaultFontSize();
        loadModuleApp();
        Raven.init(getApplicationContext());
    }

    private void loadModuleApp() {
        try {
            String[] moduleApps = getResources().getStringArray(ResourceUtils.array(this, "module_apps"));
            for (String moduleApp : moduleApps) {
                Class<?> aClass = Class.forName(moduleApp);
                Object o = aClass.newInstance();
                Method onCreate = aClass.getMethod("onCreate");
                onCreate.invoke(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        L.ef(TAG, StorageUtils.getCrashFile(instance), "uncaughtException", ex, true);
        if (mDefaultExceptionHandler != null) {
            mDefaultExceptionHandler.uncaughtException(thread, ex);
        }
    }

    // 加载系统默认设置，字体不随用户设置变化
    private void loadDefaultFontSize() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.fontScale = 1;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
