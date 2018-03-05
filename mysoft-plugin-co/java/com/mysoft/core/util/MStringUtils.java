package com.mysoft.core.util;

/**
 * Created by zhouj04 on 2017/7/26.
 */

public class MStringUtils {

    public static String replaceUnicode2028(String originStr) {
        StringBuilder builder = new StringBuilder();
        for (int k = 0; k < originStr.length(); k++) {
            int ch = (int) originStr.charAt(k);
            String hexString = Integer.toHexString(ch);
            if (!"2028".equals(hexString)) {
                builder.append(originStr.charAt(k));
            } else {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
