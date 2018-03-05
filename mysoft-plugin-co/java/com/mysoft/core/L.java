package com.mysoft.core;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 日志封装类
 *
 * @author Jay
 */
public final class L {
    private final static boolean debug = !MBuildConfig.isRelease();

    public final static void i(String TAG, String msg) {
        if (debug) {
            Log.i(TAG, msg);
        }
    }

    public final static void d(String TAG, String msg) {
        if (debug) {
            Log.d(TAG, msg);
        }
    }

    public final static void e(String TAG, String msg) {
        if (debug) {
            Log.e(TAG, msg);
        }
    }

    public static final void v(String TAG, String msg) {
        if (debug) {
            Log.v(TAG, msg);
        }
    }

    public static final void w(String TAG, String msg) {
        if (debug) {
            Log.w(TAG, msg);
        }
    }

    public final static void e(String TAG, String msg, Throwable ex) {
        if (debug) {
            Log.e(TAG, msg, ex);
        }
    }

    public final static void ef(String TAG, String path, String msg, Throwable ex) {
        ef(TAG, path, msg, ex, false);
    }

    public final static void ef(String TAG, String path, String msg, Throwable ex, boolean append) {
        if (debug) {
            Log.e(TAG, msg, ex);
            String traceString = Log.getStackTraceString(ex);
            f(TAG, path, msg + '\n' + traceString, append);
        }
    }

    public final static void f(String TAG, String path, String msg) {
        f(TAG, path, msg, false);
    }

    public final static void f(String TAG, String path, String msg, boolean append) {
        if (debug) {
            FileWriter fw = null;
            try {
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                fw = new FileWriter(file, append);
                fw.write(TAG + "=" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
