package com.mysoft.core.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.mysoft.core.L;
import com.mysoft.core.MApplication;
import com.mysoft.core.MBuildConfig;
import com.mysoft.core.exception.NoSdcardException;
import com.mysoft.core.exception.SdcardNoSpaceException;

import java.io.File;

/**
 * 存储工具
 *
 * @author zhouj04
 */
public class StorageUtils {

    private static final String SUB_PATH = "/Android/data/" + MApplication.getApplication().getPackageName();

    // 20M
    private static final long IMAGE_NEED_SPACE = 20 * 1024 * 1024;
    // 5M
    private static final long CAMERA_NEED_SPACE = 5 * 1024 * 1024;

    private static final String TAG = "StorageUtils";


    public static String getAvaliableImagesDir(Context context) {
        String dir;
        try {
            dir = getImagesDir(context, false);
        } catch (Exception e) {
            dir = context.getCacheDir().toString();
        }
        return dir;
    }

    /**
     * 只读形式获取图片目录
     */
    public static String getImagesDirOnlyRead(Context context) throws NoSdcardException {
        try {
            return getImagesDir(context, false);
        } catch (SdcardNoSpaceException e) {
            L.e(TAG, "SdcardException", e);
        }
        return null;
    }

    public static String getImagesDir(Context context) throws NoSdcardException, SdcardNoSpaceException {
        return getImagesDir(context, true);
    }

    public static String getImagesDir(Context context, boolean write) throws NoSdcardException, SdcardNoSpaceException {
        return getImagesDir(context, null, write);
    }

    /**
     * 获取图片目录，并且是否需要写入 <br/>
     * 如果需要写入，则会校验存储空间是否足够
     *
     * @throws SdcardNoSpaceException
     */
    public static String getImagesDir(Context context, String subDir, boolean write)
            throws NoSdcardException, SdcardNoSpaceException {
        File cache = null;
//        String sdcardPath = NewSdcardUtils.getMaxAvaliableSdcardPath();
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!TextUtils.isEmpty(sdcardPath)) {
            if (write) {
                long availableSize = NewSdcardUtils.calculateAvailableSize(sdcardPath);
                if (availableSize < IMAGE_NEED_SPACE) {
                    throw new SdcardNoSpaceException(availableSize);
                }
            }
            String dir = sdcardPath + SUB_PATH + "/images/";
            if (!TextUtils.isEmpty(subDir)) {
                dir += subDir;
            }
            cache = new File(dir);
        } else {
            // sdcard 不存在
            throw new NoSdcardException(context);
        }
        // Create the cache directory if it doesn't exist
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache.getAbsolutePath();
    }

    /**
     * 得到拍照需要的最小空间
     */
    public static boolean enoughSpaceToCamera() {
        // 取得SD卡文件路径
//        String sdcardPath = NewSdcardUtils.getMaxAvaliableSdcardPath();
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        long availableSize = NewSdcardUtils.calculateAvailableSize(sdcardPath);
        return availableSize > CAMERA_NEED_SPACE;
    }

    /**
     * 创建数据库文件路径
     */
    public static String getDatabaseDir(Context context) {
        // /data/data/com.mysoft.mobilecheckroom/files
        File baseDir = getBaseDir(context);
        return baseDir.getAbsolutePath();
    }

    public static String getWwwDir(Context context) {
        File filesDir = new File(context.getFilesDir(), "www");
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }
        return filesDir.getAbsolutePath();
    }

    public static String getExceptionFile(Context context) {
        File file = new File(getBaseDir(context), "exception.txt");
        return file.getAbsolutePath();
    }

    public static String getCrashFile(Context context) {
        File file = new File(getBaseDir(context), "crash.txt");
        return file.getAbsolutePath();
    }

    public static File getBaseDir(Context context) {
        String baseDir;
        if (MBuildConfig.isRelease()) {
            baseDir = context.getFilesDir().getAbsolutePath();
        } else {
            String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            baseDir = sdcardPath + File.separator + "mysoft" + File.separator + context.getPackageName();
        }
        File baseFile = new File(baseDir);
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        return baseFile;
    }

    /**
     * 获取临时的下载目录
     */
    public static String getTempDownloadDir(Context context) {
        File tempDir = context.getExternalCacheDir();
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                tempDir = context.getCacheDir();
                tempDir.mkdirs();
            }
        }
        return tempDir.getAbsolutePath();
    }

    public static String getOssDir(Context context) {
        File file = new File(context.getFilesDir(), "oss_record");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

}
