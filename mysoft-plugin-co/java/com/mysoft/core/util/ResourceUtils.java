package com.mysoft.core.util;

import android.content.Context;

import com.mysoft.core.MApplication;
import com.mysoft.core.MConstant;

import java.lang.reflect.Field;

/**
 * Created by zhouj04 on 2016/3/30.
 */
public class ResourceUtils {
    public static int drawable(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_DRAWABLE, context.getPackageName());
    }

    public static int string(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_STRING, context.getPackageName());
    }

    public static int id(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_ID, context.getPackageName());
    }

    public static int layout(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_LAYOUT, context.getPackageName());
    }

    public static int color(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_COLOR, context.getPackageName());
    }

    public static int array(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_ARRAY, context.getPackageName());
    }

    public static int style(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_STYLE, context.getPackageName());
    }

    public static int dimen(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_DIMEN, context.getPackageName());
    }

    public static int raw(Context context, String name) {
        return context.getResources().getIdentifier(name, MConstant.RES_RAW, context.getPackageName());
    }

    public static int drawable(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_DRAWABLE, context.getPackageName());
    }

    public static int string(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_STRING, context.getPackageName());
    }

    public static int id(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_ID, context.getPackageName());
    }

    public static int layout(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_LAYOUT, context.getPackageName());
    }

    public static int color(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_COLOR, context.getPackageName());
    }

    public static int array(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_ARRAY, context.getPackageName());
    }

    public static int style(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_STYLE, context.getPackageName());
    }

    public static int dimen(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_DIMEN, context.getPackageName());
    }

    public static int raw(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_RAW, context.getPackageName());
    }

    public static int bool(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_BOOL, context.getPackageName());
    }

    public static int integer(String name) {
        Context context = MApplication.getApplication();
        return context.getResources().getIdentifier(name, MConstant.RES_INTEGER, context.getPackageName());
    }


    /**
     * 对于context.getResources().getIdentifier无法获取的数据,或者数组
     *
     * @param name
     * @param type
     * @return
     * @paramcontext
     */
    private static Object getResourceId(Context context, String name, String type) {
        String className = context.getPackageName() + ".R";
        try {
            Class cls = Class.forName(className);
            for (Class childClass : cls.getClasses()) {
                String simple = childClass.getSimpleName();
                if (simple.equals(type)) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(name)) {
                            System.out.println(fieldName);
                            return field.get(null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param name
     * @return
     * @paramcontext
     */

    public static int styleable(Context context, String name) {
        return ((Integer) getResourceId(context, name, MConstant.RES_STYLEABLE)).intValue();

    }

    /**
     * 获取styleable的ID号数组
     *
     * @param name
     * @return
     * @paramcontext
     */
    public static int[] styleableArray(Context context, String name) {
        return (int[]) getResourceId(context, name, MConstant.RES_STYLEABLE);

    }
}
