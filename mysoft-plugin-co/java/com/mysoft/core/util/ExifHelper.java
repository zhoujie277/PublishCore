package com.mysoft.core.util;

import android.media.ExifInterface;

public class ExifHelper {

    public static int getOrientation(String filename) throws Exception {
        ExifInterface face = new ExifInterface(filename);
        int attr = face.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        int degree = 0;
        switch (attr) {
            case ExifInterface.ORIENTATION_NORMAL:
                degree = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                break;
        }
        return degree;
    }
}
