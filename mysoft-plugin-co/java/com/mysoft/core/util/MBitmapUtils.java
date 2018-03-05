package com.mysoft.core.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import com.mysoft.core.L;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 图片处理工具
 *
 * @author zhouj04
 */
public class MBitmapUtils {

    private static final int IMAGE_WIDTH = 720;
    private static final int IMAGE_HEIGHT = 1280;
    private static final String TAG = "MBitmapUtils";

    public final static Bitmap decodeFile(String filePath) {
        return decodeFile(filePath, false);
    }

    public final static Bitmap decodeFile(String filePath, boolean mutable) {
        BitmapEntity entity = decodeFileEntity(filePath, IMAGE_WIDTH, IMAGE_HEIGHT, mutable);
        return entity.bitmap;
    }

    public final static Bitmap decodeFile(String filePath, int reqWidth, int reqHeight) {
        return decodeFileEntity(filePath, reqWidth, reqHeight).bitmap;
    }

    public final static Bitmap decodeFile(String filePath, int reqWidth, int reqHeight, boolean mutable) {
        return decodeFileEntity(filePath, reqWidth, reqHeight, mutable).bitmap;
    }

    public final static BitmapEntity decodeFileEntity(String filePath, int reqWidth, int reqHeight) {
        return decodeFileEntity(filePath, reqWidth, reqHeight, false);
    }

    public final static BitmapEntity decodeFileEntity(String filePath) {
        return decodeFileEntity(filePath, IMAGE_WIDTH, IMAGE_HEIGHT, false);
    }

    public final static BitmapEntity decodeFileEntity(String filePath, int reqWidth, int reqHeight, boolean mutable) {
        BitmapEntity entity = new BitmapEntity();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        L.v(TAG, "decodeFile inJustDecodeBounds true.");
        BitmapFactory.decodeFile(filePath, options);
        entity.outWidth = options.outWidth;
        entity.outHeight = options.outHeight;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        L.v(TAG, "decodeFile inSampleSize:" + options.inSampleSize);
        options.inJustDecodeBounds = false;
        options.inMutable = mutable;
        entity.bitmap = BitmapFactory.decodeFile(filePath, options);
        return entity;
    }

    public final static Bitmap decodeResource(Resources res, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);
        options.inSampleSize = calculateInSampleSize(options, IMAGE_WIDTH, IMAGE_HEIGHT);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, id, options);
    }

    public final static Bitmap decodeByteArray(byte[] data) {
        return decodeByteArray(data, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    public final static Bitmap decodeByteArray(byte[] data, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public final static Bitmap decodeStream(URL url) throws IOException {
        return decodeStream(url, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    public final static Bitmap decodeStream(URL url, int width, int height) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(url.openStream(), null, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(url.openStream(), null, options);
    }

    public final static int calculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0)
            return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio <= widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        Bitmap dstBitmap = Bitmap.createBitmap(source, x, y, width, height);
        if (dstBitmap != source) {
            source.recycle();
        }
        return dstBitmap;
    }


    /**
     * 旋转图片
     */
    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotateBitamp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotateBitamp != bitmap) {
            recycle(bitmap);
        }
        return rotateBitamp;
    }


    /**
     * 根据bitmap生成file
     */
    public static void bitmapSaveToFile(Bitmap bitmap, String savePath, int quality) throws IOException {
        FileOutputStream fos = null;
        try {
            long start = System.currentTimeMillis();
            fos = new FileOutputStream(new File(savePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            L.i(TAG, "bitmapSaveToFile used time:" + (System.currentTimeMillis() - start) + "ms");
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                L.e(TAG, "IOException", e);
            }
        }
    }

    /**
     * 根据图片uri生成file
     *
     * @param context
     * @param uri
     * @param savePath
     */
    public static void uriSaveToFile(Context context, Uri uri, String savePath) throws IOException {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            byte[] b = new byte[1024];
            inputStream = context.getContentResolver().openInputStream(uri);
            fileOutputStream = new FileOutputStream(savePath);
            int length;
            while ((length = inputStream.read(b)) != -1) {
                fileOutputStream.write(b, 0, length);
            }
        } finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 将bitmap根据原来的角度重置方向
     */
    public static Bitmap cutAndRotateBitmap(String path, int reqWidth, int reqHeight) throws Exception {
        Bitmap scaledBitmap = decodeFile(path, reqWidth, reqHeight, true);
        Bitmap rotateBitmap = MBitmapUtils.rotateBitmap(ExifHelper.getOrientation(path), scaledBitmap);
        if (scaledBitmap != rotateBitmap) {
            recycle(scaledBitmap);
        }
        return rotateBitmap;
    }

    /**
     * 得到bitmap指定物理大小的byte[]
     *
     * @param bitmap
     * @param targetSize
     * @return
     */
    public static byte[] getBitmapTargetSize(Bitmap bitmap, int targetSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 >= targetSize) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        recycle(bitmap);
        L.d(TAG, "" + baos.toByteArray().length);
        return baos.toByteArray();
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            L.d(TAG, "bitmap recycled");
        }
    }

    public static class BitmapEntity {
        public Bitmap bitmap;
        public int outHeight;
        public int outWidth;
    }
}
