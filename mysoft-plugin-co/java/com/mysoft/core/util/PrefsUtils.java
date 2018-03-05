package com.mysoft.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;

import com.mysoft.core.L;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author zhouj04
 */
public class PrefsUtils {
    private static final String NAME = "mysoft";
    public static final String PREFS_GUIDE_VALUE = "guide_version_code";

    public static boolean putString(Context ctx, String key, String value) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putString(key, value);
        return edit.commit();
    }

    public static String getString(Context ctx, String key) {
        return getString(ctx, key, "");
    }

    public static String getString(Context ctx, String key, String defaultValue) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }

    public static boolean putBoolean(Context ctx, String key, boolean value) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        return edit.commit();
    }

    public static boolean getBoolean(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static boolean putInt(Context ctx, String key, int value) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putInt(key, value);
        return edit.commit();
    }

    public static int getInt(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public static int getInt(Context ctx, String key, int def) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key, def);
    }

    public static void remove(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.remove(key);
        edit.commit();
    }

    public static void putParcelableObject(Context ctx, String key, Parcelable parceable) throws Exception {
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        putString(ctx, key, Base64.encodeToString(bytes, 0));
    }

    public static Parcel getParcelableObject(Context ctx, String key) {
        byte[] bytes = Base64.decode(getString(ctx, key), 0);
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return parcel;
    }


    public static void putSerializableObject(Context ctx, String key, Object o) throws Exception {
        String var3 = writeObject(o);
        putString(ctx, key, var3);
    }


    public static Object getSerializableObject(Context ctx, String key) throws Exception {
        SharedPreferences prefs = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Object var2 = null;
        String var3 = prefs.getString(key, null);
        if (!TextUtils.isEmpty(var3)) {
            var2 = readObject(var3);
        }
        return var2;
    }


    private static String writeObject(Object o) throws Exception {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        ObjectOutputStream var2 = new ObjectOutputStream(var1);
        var2.writeObject(o);
        var2.flush();
        var2.close();
        var1.close();
        return Base64.encodeToString(var1.toByteArray(), 0);
    }

    private static Object readObject(String object) throws Exception {
        ByteArrayInputStream var1 = new ByteArrayInputStream(Base64.decode(object, 0));
        ObjectInputStream var2 = new ObjectInputStream(var1);
        Object var3 = var2.readObject();
        var1.close();
        var2.close();
        return var3;
    }

}
