package com.mysoft.core;

import android.content.res.Resources;

import com.mysoft.core.util.ResourceUtils;

/**
 * 自定义编译配置文件
 * Created by zhouj04 on 2016/4/11.
 */
public class MBuildConfig {
    // remote debug
    private static final String REMOTE_DEBUG = "remote";
    // local debug
    private static final String LOCAL_DEBUG = "debug";
    // beta
    private static final String BEAT = "beta";
    // release
    private static final String RELEASE = "release";
    private static String DEBUG_MODE = RELEASE;
    private static final String TAG = "MBuildConfig";

    static {
        try {
            int debug_mode_id = ResourceUtils.string(MApplication.getApplication(), "debug_mode");
            DEBUG_MODE = MApplication.getApplication().getString(debug_mode_id);
        } catch (Resources.NotFoundException e) {
            L.i(TAG, "获取debug异常:" + e.getMessage());
        }

        L.i(TAG, "DEBUG_MODE:" + DEBUG_MODE);
    }

    public static boolean isDebug() {
        return LOCAL_DEBUG.equals(DEBUG_MODE);
    }

    public static boolean isRemote() {
        return REMOTE_DEBUG.equals(DEBUG_MODE);
    }

    public static boolean isBeta() {
        return BEAT.equals(DEBUG_MODE);
    }

    public static boolean isRelease() {
        return RELEASE.equals(DEBUG_MODE);
    }
}
