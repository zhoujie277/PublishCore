package com.mysoft.core.util;

/**
 * Created by yuw on 2017/4/15.
 */

import java.util.Stack;

import android.app.Activity;

public class ActivityStackManagerUtils {
    private static Stack<Activity> activityStack;
    private static ActivityStackManagerUtils instance;

    private ActivityStackManagerUtils() {
    }

    public static ActivityStackManagerUtils getActivityStackManager() {
        if (instance == null) {
            instance = new ActivityStackManagerUtils();
        }
        return instance;
    }


    public void popActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
            activityStack.remove(activity);
            activity = null;
        }
    }

    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack();
        }
        activityStack.add(activity);
    }

    public Activity currentActivity() {
        if (activityStack.size() != 0) {
            Activity activity = activityStack.lastElement();
            return activity;
        }
        return null;
    }


}
