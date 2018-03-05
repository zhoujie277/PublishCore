/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.mysoft.core.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mysoft.core.L;
import com.mysoft.core.MBuildConfig;
import com.mysoft.core.MConstant;
import com.mysoft.core.MicCore;
import com.mysoft.core.util.PackageUtils;
import com.mysoft.core.util.PrefsUtils;
import com.mysoft.core.util.ResourceUtils;
import com.mysoft.core.util.StorageUtils;
import com.mysoft.core.util.ZipUtils;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.engine.SystemWebView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DaemonActivity extends CordovaActivity {
    private static final String TAG = "DaemonActivity";
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private DaemonReceiver mReceiver;
    private boolean isMainScan = false;
    public static boolean sPageFinished = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(TAG, "onCreate");
        boolean secure_protect = getResources().getBoolean(ResourceUtils.bool("secure_protect"));
        if (secure_protect) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }
        //个推
        if (getIntent().getBooleanExtra("isPushLaunch", false)) {
            L.d(TAG, "pushData: " + getIntent().getStringExtra("pushData"));
            PrefsUtils.putString(this, "pushData", getIntent().getStringExtra("pushData"));
        }
        super.init();
        //开发环境时,显示调试布局
        if (MBuildConfig.isRemote()) {
            LOG.setLogLevel(LOG.VERBOSE);
//            String wwwUrl = getIntent().getStringExtra(MConstant.LAUNCH_URL);
//            if (TextUtils.isEmpty(wwwUrl)) {
//                Intent intent = new Intent();
//                intent.setClass(getApplicationContext(), RemoteActivity.class);
//                startActivity(intent);
//                finish();
//            } else {
//                startActivity(new Intent(this, SplashActivity.class));
//                showWebContent(wwwUrl);
//            }
            View debugView = LayoutInflater.from(this).inflate(ResourceUtils.layout("debug_layout"), null);
            addContentView(debugView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            setDebugListener(debugView);
//            return;
        }

        // 如果不是官方的webview引擎，则不处理pageFinish事件
        if (!(appView.getView() instanceof SystemWebView)) {
            sPageFinished = true;
        }
        if (savedInstanceState == null) {
            startActivity(new Intent(this, SplashActivity.class));
        }
        mReceiver = new DaemonReceiver();
        mReceiver.register(getApplicationContext());
        check();
    }

    public CordovaWebView getCordovaWebView() {
        return appView;
    }

    private void setDebugListener(View debugView) {
        debugView.findViewById(ResourceUtils.id("img_debug_back")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SystemWebView) appView.getView()).canGoBack()) {
                    ((SystemWebView) appView.getView()).goBack();
                }
            }
        });
        debugView.findViewById(ResourceUtils.id("img_debug_forward")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SystemWebView) appView.getView()).canGoForward()) {
                    ((SystemWebView) appView.getView()).goForward();
                }

            }
        });
        debugView.findViewById(ResourceUtils.id("img_debug_refresh")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SystemWebView) appView.getView()).reload();
            }
        });
        debugView.findViewById(ResourceUtils.id("img_debug_url")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(DaemonActivity.this);
                et.setHint("以http或https为前缀");
                et.setText(PrefsUtils.getString(DaemonActivity.this, "debug_url", ""));
                new AlertDialog.Builder(DaemonActivity.this).setTitle("服务器地址")
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String url = et.getText().toString();
                                if (!TextUtils.isEmpty(url) && url.trim().length() > 0 && url.startsWith("http")) {
                                    showWebContent(url);
                                    PrefsUtils.putString(DaemonActivity.this, "debug_url", url);
                                } else {
                                    Toast.makeText(DaemonActivity.this, "输入格式不正确", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        debugView.findViewById(ResourceUtils.id("img_debug_scan")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isMainScan = true;
                    Class cls = Class.forName("com.com.obsessive.zbar.CaptureActivity");
                    Intent intent = new Intent(DaemonActivity.this, cls);
                    startActivityForResult(intent, MConstant.REQ_SCAN_QR_CODE);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(DaemonActivity.this, "请添加二维码插件", Toast.LENGTH_SHORT).show();
                    L.d(TAG, "CaptureActivity not found");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d(TAG, "onResume  " + getIntent().toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.d(TAG, "onPause  " + getIntent().toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.d(TAG, "onStop  " + getIntent().toString());
    }

    public void showWebContent(String wwwUrl) {
        if (!TextUtils.isEmpty(wwwUrl) && (wwwUrl.startsWith("http") || wwwUrl.startsWith("file://"))) {
            loadUrl(wwwUrl);
        } else {
            Toast.makeText(this, "请输入加载有效的地址", Toast.LENGTH_SHORT).show();
        }

    }

    private void check() {
        final String lastVersion = PrefsUtils.getString(this, MConstant.PREFS_VERSION);
        final String currentVersion = PackageUtils.getAppVersion(this);
        L.d(TAG, "lastVersion:" + lastVersion + ", currentVersion:" + currentVersion);
        String wwwDirPath = StorageUtils.getWwwDir(getApplicationContext());
        final String wwwUrl = "file://" + wwwDirPath + "/index.html";
        File file = new File(wwwDirPath, "index.html");
        if (isFirstEnter(lastVersion, currentVersion) || !file.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        unzipWWW();
                        PrefsUtils.putString(getApplicationContext(), MConstant.PREFS_VERSION, currentVersion);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showWebContent(wwwUrl);
                            }
                        });
                    } catch (Exception e) {
                        L.e(TAG, "www.zip不存在", e);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showWebContent(launchUrl);
                            }
                        });
                    }
                }
            }).start();
        } else {
            showWebContent(wwwUrl);
        }
    }

    private boolean isFirstEnter(String lastVersion, String currentVersion) {
        if (TextUtils.isEmpty(lastVersion) || !lastVersion.equals(currentVersion)) {
            return true;
        }
        return false;
    }

    private void unzipWWW() throws Exception {
        long start = System.currentTimeMillis();
        InputStream input;
        String filePath = StorageUtils.getWwwDir(getApplicationContext());
        input = getAssets().open("www.zip");
        ZipUtils.unZip(input, filePath);
        L.i(TAG, "unzipWWW used time:" + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            mReceiver.unRegister(getApplicationContext());
        }
    }

    private class DaemonReceiver extends BroadcastReceiver {

        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(MConstant.ACTION_GUIDE);
            filter.addAction(MConstant.ACTION_ADS);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        }

        public void unRegister(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MConstant.ACTION_GUIDE.equals(intent.getAction())) {
                L.d(TAG, "asyncCallback GUIDE_CLOSE!");
                MicCore.asyncCallback(MicCore.CODE_GUIDE_CLOSE, "引导页已关闭");
            } else if (MConstant.ACTION_ADS.equals(intent.getAction())) {
                int event = intent.getIntExtra(MConstant.ADS_EVENT, MicCore.CODE_ADS_CLICK);
                String params = intent.getStringExtra(MConstant.ADS_OPT_PARAMS);
                L.d(TAG, "asyncCallback CODE_ADS_CLICK!" + event + ", params=" + params);
                MicCore.asyncCallback(event, params);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (isMainScan) {
            isMainScan = false;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == MConstant.REQ_SCAN_QR_CODE) {
                    String url = intent.getStringExtra("msg");
                    showWebContent(url);
                }
            }
        }
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("onPageFinished".equals(id)) {
            L.d(TAG, "onMessage() called with: id = [" + id + "], data = [" + data + "]");
            sPageFinished = true;
            mHandler.post(new CheckJSLoadTask());
        }
        return super.onMessage(id, data);
    }

    private class CheckJSLoadTask implements Runnable {
        private final int maxTime = 30 * 1000;
        private int currentTime = 0;

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            Bitmap bitmap = Bitmap.createBitmap(appView.getView().getWidth(), appView.getView().getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            appView.getView().draw(canvas);
            if (bitmap != null) {
                boolean isColorGragh = isColorGragh(bitmap);
                if (isColorGragh) {
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(MConstant.ACTION_SPLASH_CLOSE));
                } else {
                    if (currentTime < maxTime) {
                        mHandler.postDelayed(this, 600);
                        currentTime += 600;
                    } else {
                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(MConstant.ACTION_SPLASH_CLOSE));
                    }
                }
                L.d(TAG, "run() called=" + (System.currentTimeMillis() - start) + "ms");
            } else {
                L.d(TAG, "run() called don't support!");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(MConstant.ACTION_SPLASH_CLOSE));
                    }
                }, 2000);
            }
        }

        // 是一张彩图
        private boolean isColorGragh(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 保存所有的像素的数组，图片宽×高
            int[] pixels = new int[width * height / 4];
            Log.d(TAG, "isColorGragh() called with: pixels.length = [" + pixels.length + "]");
            bitmap.getPixels(pixels, 0, width / 2, width / 4, height / 4, width / 2, height / 2);
//            FileOutputStream out = null;
//            try {
//                Bitmap mBitmap3 = Bitmap.createBitmap(pixels, 0, width / 2, width / 2, height / 2,
//                        Bitmap.Config.ARGB_8888);
//                out = new FileOutputStream(new File("/sdcard/mysoft/ss.jpg"));
//                mBitmap3.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            for (int i = 0; i < pixels.length; i += 50) {
                int clr = pixels[i];
                int red = (clr & 0x00ff0000) >> 16; // 取高两位
                int green = (clr & 0x0000ff00) >> 8; // 取中两位
                int blue = clr & 0x000000ff; // 取低两位
                Log.d("tag", "r=" + red + ",g=" + green + ",b=" + blue);
                if (red != 0xFF || green != 0xFF || blue != 0xFF) {
                    return true;
                }
            }
            return false;
        }
    }
}
