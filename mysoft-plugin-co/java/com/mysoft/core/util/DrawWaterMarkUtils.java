package com.mysoft.core.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.mysoft.core.L;


/**
 * Created by zhouj04 on 2016/11/11.
 */

public class DrawWaterMarkUtils {
    public static final int ORIENT_LEFT = 1;
    public static final int ORIENT_CENTER = 2;
    public static final int ORIENT_RIGHT = 3;
    public static final int ORIENT_TOP = 1;
    public static final int ORIENT_MIDDLE = 2;
    public static final int ORIENT_BOTTOM = 3;
    private static final String TAG = "DrawWaterMarkUtils";

    private TextPaint mTextPaint;
    private String mTextInfo;
    private int mTextColor = Color.WHITE;
    private int mTextRectColor = Color.parseColor("#40000000");
    private int mTextHorizontalOrientation = ORIENT_RIGHT;
    private int mTextVerticalOrientation = ORIENT_MIDDLE;
    private Paint mTextRectPaint;

    private int viewWidth;
    private int viewHeight;

    public static void draw(String text, int text_vertical_orient, String textColor, int text_horizontal_orient, String textBackground, Bitmap bitmap) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        DrawWaterMarkUtils drawWaterMarkUtils = new DrawWaterMarkUtils();
        drawWaterMarkUtils.init();
        drawWaterMarkUtils.setTextInfo(text);
        drawWaterMarkUtils.setTextVerticalOrientation(text_vertical_orient);
        drawWaterMarkUtils.setTextColor(textColor);
        drawWaterMarkUtils.setTextHorizontalOrientation(text_horizontal_orient);
        drawWaterMarkUtils.setTextRectColor(textBackground);
        drawWaterMarkUtils.setViewRect(bitmap.getWidth(), bitmap.getHeight());
        Log.d(TAG, "draw() called with: bitmap.getWidth() = [" + bitmap.getWidth() + "], bitmap.getHeight() = [" +
                bitmap.getHeight() + "]");
        drawWaterMarkUtils.draw(bitmap);
    }

    public void init() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextRectPaint = new Paint();
        mTextRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setViewRect(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }

    public void setTextInfo(String text) {
        mTextInfo = text;
    }

    public void setTextRectColor(String color) {
        try {
            this.mTextRectColor = Color.parseColor(color);
        } catch (Exception e) {
            L.e(TAG, "文本背景颜色设置不符合要求:" + color);
            this.mTextRectColor = Color.parseColor("#40000000");
        }
    }

    public void setTextColor(String color) {
        try {
            this.mTextColor = Color.parseColor(color);
        } catch (Exception e) {
            L.e(TAG, "文本颜色设置不符合要求:" + color);
            this.mTextColor = Color.WHITE;
        }
    }

    public void setTextHorizontalOrientation(int orientation) {
        this.mTextHorizontalOrientation = orientation;
    }

    public void setTextVerticalOrientation(int orientation) {
        this.mTextVerticalOrientation = orientation;
    }

    public void drawWaterMark(Canvas canvas) {
        if (TextUtils.isEmpty(mTextInfo)) {
            return;
        }
        setAdapterTextSize(28);
        mTextPaint.setColor(mTextColor);
        Paint.FontMetricsInt fontMetricsInt = mTextPaint.getFontMetricsInt();
        int height = Math.abs(fontMetricsInt.bottom) + Math.abs(fontMetricsInt.top);
        int startX;
        int startY;
        int rectStartY;
        if (ORIENT_RIGHT == mTextHorizontalOrientation) {
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            startX = viewWidth - 10;
        } else if (ORIENT_LEFT == mTextHorizontalOrientation) {
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            startX = 10;
        } else {
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            startX = viewWidth / 2;
        }
        if (ORIENT_BOTTOM == mTextVerticalOrientation) {
            startY = viewHeight - Math.abs(fontMetricsInt.bottom);
            rectStartY = viewHeight - height;
        } else if (ORIENT_TOP == mTextVerticalOrientation) {
            startY = Math.abs(fontMetricsInt.top);
            rectStartY = 0;
        } else {
            int fontHeight = (int) (mTextPaint.ascent() + mTextPaint.descent());
            startY = viewHeight / 2 - fontHeight / 2;
            rectStartY = viewHeight / 2 - Math.abs(fontHeight);
        }
        mTextRectPaint.setColor(mTextRectColor);
        canvas.drawRect(0, rectStartY, viewWidth, rectStartY + height, mTextRectPaint);
        canvas.drawText(mTextInfo, startX, startY, mTextPaint);
    }

    public void draw(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        drawWaterMark(canvas);
    }


    private void setAdapterTextSize(int size) {
        float defalutSize = Math.round(size * ((float) viewWidth / 640));
        mTextPaint.setTextSize(defalutSize);
        while (mTextPaint.measureText(mTextInfo) > viewWidth) {
            defalutSize--;
            mTextPaint.setTextSize(defalutSize);
        }
    }

}
