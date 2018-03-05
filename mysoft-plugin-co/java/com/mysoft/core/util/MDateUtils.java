package com.mysoft.core.util;

import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhouj04 on 2016/12/19.
 */

public class MDateUtils {
    private static final String TAG = "MDateUtils";

    public static boolean arriveDay(Date date) {
        if (new Date().getTime() > date.getTime()) {
            return true;
        }
        return false;
    }

    public static Date parseString(String dateString) {
        if (!TextUtils.isEmpty(dateString)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                Log.e(TAG, "parseString: " + dateString, e);
            }
        }
        return null;
    }

}
