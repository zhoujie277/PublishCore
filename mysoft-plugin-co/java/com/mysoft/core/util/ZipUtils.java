package com.mysoft.core.util;

import com.mysoft.core.L;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 解压zip文件工具类
 */
public class ZipUtils {

    private static int totalSize;
    private static UnZipProgressListener mUnZipProgressListener;

    public interface UnZipProgressListener {
        void onProgress(int progress);
    }

    private ZipUtils() {

    }

    public static void setUnZipProgressListener(UnZipProgressListener unZipProgressListener) {
        mUnZipProgressListener = unZipProgressListener;
    }

    /**
     * DeCompress the ZIP to the path
     */
    public static void unZip(String zipPath, String outPath) throws IOException {
        ZipFile f = new ZipFile(zipPath);
        Enumeration<? extends ZipEntry> en = f.entries();
        while (en.hasMoreElements()) {
            totalSize += en.nextElement().getSize();
        }
        unZip(new FileInputStream(zipPath), outPath);
    }

    /**
     * DeCompress the ZIP to the path
     */
    public static void unZip(InputStream inStream, String outPath) throws IOException {
        ZipInputStream inZip = new ZipInputStream(inStream);
        ZipEntry zipEntry;
        String entryName;
        float totalWrite = 0;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            entryName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                entryName = entryName.substring(0, entryName.length() - 1);
                File folder = new File(outPath, entryName);
                folder.mkdirs();
            } else {
                File file = new File(outPath, entryName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    totalWrite += len;
                    out.write(buffer, 0, len);
                    if (mUnZipProgressListener != null) {
                        mUnZipProgressListener.onProgress((int) (totalWrite / totalSize * 100));
                    }
                }
                out.flush();
                out.close();
            }
        }
        if (mUnZipProgressListener != null) {
            mUnZipProgressListener.onProgress(100);
        }

        inZip.close();
    }

    /**
     * 压缩指定路径的集合
     */
    public static void zip(File zip, File... files) throws IOException {
        if (files == null) {
            return;
        }
        ZipOutputStream zos = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(zip);
            zos = new ZipOutputStream(out);
            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            List<ZFile> subFiles = getSubFiles(files);
            for (int j = 0; j < subFiles.size(); j++) {
                ZFile zfile = subFiles.get(j);
                File file = zfile.file;
                ze = new ZipEntry(zfile.entryName);
                ze.setSize(file.length());
                ze.setTime(file.lastModified());
                zos.putNextEntry(ze);
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(file));
                    int readLen = 0;
                    while ((readLen = is.read(buf)) != -1) {
                        zos.write(buf, 0, readLen);
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (zos != null) {
                        zos.closeEntry();
                    }
                }
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 构造ZFile文件列表
     *
     * @param baseDir File... 指定的目录数组
     * @return 按照指定格式返回的ZFile
     */
    private static List<ZFile> getSubFiles(File... baseDir) {
        List<ZFile> ret = new ArrayList<ZFile>();
        if (baseDir == null)
            return ret;
        for (int j = 0; j < baseDir.length; j++) {
            if (baseDir[j].isFile()) {
                ZFile zfile = new ZFile(baseDir[j], baseDir[j].getName());
                ret.add(zfile);
            } else if (baseDir[j].isDirectory()) {
                File[] tmp = baseDir[j].listFiles();
                ret.addAll(get(tmp, baseDir[j]));
            }
        }
        return ret;
    }

    /**
     * 取得指定目录下的所有文件列表，包括子目录
     */
    private static List<ZFile> get(File[] tmp, File root) {
        List<ZFile> ret = new ArrayList<ZFile>();
        if (tmp != null) {
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isFile()) {
                    String entryName = getAbsFileName(root, tmp[i]);
                    ZFile zfile = new ZFile(tmp[i], entryName);
                    ret.add(zfile);
                }
                if (tmp[i].isDirectory())
                    ret.addAll(get(tmp[i].listFiles(), root));
            }
        }
        return ret;
    }

    /**
     * 给定根目录，返回另一个文件名的相对路径，用于zip文件中的路径.
     */
    private static String getAbsFileName(File root, File file) {
        if (file == null) {
            return "";
        }
        if (root == null) {
            return file.getName();
        }
        String parent = file.getParent();
        String rootName = root.getName();
        String rootPath = root.getAbsolutePath();
        if (parent != null) {
            if (parent.equals(rootPath)) {
                return rootName + File.separator + file.getName();
            }
            if (parent.contains(rootPath)) {
                int indexOf = parent.lastIndexOf(rootPath);
                String result = rootName + File.separator + parent.substring(indexOf + rootPath.length() + 1)
                        + File.separator + file.getName();
                return result;
            }
        }
        return file.getName();
    }

    private static class ZFile {
        public final File file;
        public final String entryName;

        public ZFile(File file, String entryName) {
            this.file = file;
            this.entryName = entryName;
        }
    }
}
