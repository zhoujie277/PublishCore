package com.mysoft.core;

import com.mysoft.core.annotation.Action;
import com.mysoft.core.exception.MArgumentException;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;

/**
 * Created by zhouj04 on 2016/8/4.
 */
public class BaseCordovaPlugin extends MCordovaPlugin {

    private static Method getMethod(Object o, String action) {
        Method[] methods = o.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Action annotation = methods[i].getAnnotation(Action.class);
            if (annotation != null) {
                String name = annotation.value();
                if (action.equals(name)) {
                    return methods[i];
                }
            }
        }
        return null;
    }

    @Override
    public boolean onExecute(final String action, final JSONArray args, final CallbackContext callbackContext) throws
            JSONException,
            MArgumentException {
        final Method method = getMethod(this, action);
        if (method == null) {
            return false;
        }
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    method.invoke(BaseCordovaPlugin.this, args, callbackContext);
                } catch (Exception e) {
                    L.e(TAG, "onExecute Exception.", e);
                    error(callbackContext, "onExecute Exception !" + e.getMessage());
                }
            }
        });
        return true;
    }
}
