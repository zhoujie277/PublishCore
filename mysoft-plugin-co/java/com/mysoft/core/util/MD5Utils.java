package com.mysoft.core.util;


import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zhouj04 on 2016/9/29.
 */

public class MD5Utils {
    static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private MD5Utils() {}

    public static byte[] calculateMd5(String filePath) throws IOException {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];
            FileInputStream is = new FileInputStream(new File(filePath));

            int lent;
            while ((lent = is.read(buffer)) != -1) {
                e.update(buffer, 0, lent);
            }

            is.close();
            return e.digest();
        } catch (NoSuchAlgorithmException var5) {
            throw new RuntimeException("MD5 algorithm not found.");
        }
    }

    public static String calculateBase64Md5(String filePath) throws IOException {
        return toBase64String(calculateMd5(filePath));
    }

    public static String toBase64String(byte[] binaryData) {
//        return Base64.encodeToString(binaryData, android.util.Base64.DEFAULT);
        return new String(Base64.encodeBase64(binaryData));
    }

    public static String calculateHexStringMd5(String filePath) throws IOException {
        byte[] bytes = calculateMd5(filePath);
        // 把密文转换成十六进制的字符串形式
        int j = bytes.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = bytes[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }
}
