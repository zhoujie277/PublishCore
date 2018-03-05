/**
 *
 */
package com.mysoft.core.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * 文件工具类
 *
 * @author gaob
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    private FileUtils() {
    }

    public static final String getAbsolutePath(String path) {
        if (!TextUtils.isEmpty(path) && path.startsWith("file://")) {
            return Uri.parse(path).getPath();
        }
        return path;
    }

    public static final boolean legalPath(String path) {
        return legalPath(path, false);
    }

    public static final boolean legalPath(String path, boolean createParent) {
        String absolutePath = getAbsolutePath(path);
        if (!TextUtils.isEmpty(absolutePath) && absolutePath.startsWith("/")) {
            boolean result = true;
            if (createParent) {
                File file = new File(absolutePath);
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    result = parentFile.mkdirs();
                }
            }
            return result;
        }
        return false;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(File file) {
        boolean result;
        try {
            if (file != null && file.exists()) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null && files.length >= 1) {
                        Log.v(TAG, "deleteFile  文件夹 包含" + files.length + "个File");
                        for (int i = 0; i < files.length; i++) {
                            deleteFile(files[i]);
                        }
                    }
                    file.delete();
                } else {
                    file.delete();
                }
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;

    }

    /**
     * 写文件
     */
    public static void generateFile(File file, String content) throws IOException {
        FileWriter writer = null;
        StringReader reader = null;
        try {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);
            char[] charbuf = new char[1024];
            reader = new StringReader(content);
            int len = 0;
            while ((len = reader.read(charbuf)) != -1) {
                writer.write(charbuf, 0, len);
            }
            writer.flush();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static boolean copy(String srcPath, String dstPath) throws IOException {
        File srcFile = new File(srcPath);
        if (srcFile.exists()) {
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                for (File file : files) {
                    String path = file.getAbsolutePath();
                    String dstFilePath = dstPath + File.separator + file.getName();
                    copy(path, dstFilePath);
                }
            } else if (srcFile.isFile()) {
                copyFile(srcPath, dstPath);
            }
            return true;
        } else {
            return false;
        }
    }

    public static void copyFile(String srcPath, String dstPath) throws IOException {
        File dstFile = new File(dstPath);
        if (!dstFile.getParentFile().exists()) {
            dstFile.getParentFile().mkdirs();
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcPath);
            out = new FileOutputStream(dstPath);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 判断指定路径文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean isExistFile(String path) {
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            return true;
        }
        return false;
    }


    /**
     * 根据filePath创建相应的目录
     *
     * @param filePath 要创建的文件路经
     * @return file        文件
     * @throws IOException
     */
    public static File mkdirFiles(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        return file;
    }

    //获取文件或文件夹大小
    public static double getDirOrFileSize(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children) {
                    size += getDirOrFileSize(f);
                }
                return size;
            } else {
                return file.length();
            }
        } else {
            return 0.0;
        }
    }
}
