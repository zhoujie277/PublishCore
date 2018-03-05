package com.mysoft.core.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mysoft.core.L;
import com.mysoft.core.MConstant;
import com.mysoft.core.MHandler;
import com.mysoft.core.MicCore;
import com.mysoft.core.util.PrefsUtils;
import com.mysoft.core.util.ResourceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import static com.mysoft.core.util.MDateUtils.arriveDay;
import static com.mysoft.core.util.MDateUtils.parseString;

/**
 * 启动页
 */
public class SplashActivity extends Activity implements MHandler.MHandlerListener {

    public static final String TAG = "SplashActivity";
    private static final int MSG_FINISH = 0x10;
    private static final int MSG_ADS = 0x11;
    private MHandler mHandler = new MHandler(Looper.getMainLooper(), this);
    private ClosePageReceiver mClosePageReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int layoutId = ResourceUtils.layout(this, "activity_splash");
        setContentView(layoutId);
        ImageView imageView = (ImageView) findViewById(ResourceUtils.id(this, "iv_splash"));
        imageView.setImageResource(ResourceUtils.drawable(this, "screen"));
        final String adsPath = PrefsUtils.getString(this, MConstant.PREFS_KEY_SAVEPATH, "");
        L.d(TAG, "adsPath:" + adsPath);
        if (!showAds(adsPath)) {
            Log.d(TAG, "onCreate() called with: DaemonActivity.sPageFinished = [" + DaemonActivity.sPageFinished + "]");
            if (!DaemonActivity.sPageFinished) {
                mClosePageReceiver = new ClosePageReceiver();
                mClosePageReceiver.register(this);
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_FINISH, 3000);
            }
        } else {
            File file = new File(getFilesDir(), adsPath);
            Message msg = mHandler.obtainMessage(MSG_ADS);
            msg.obj = file.getAbsolutePath();
            mHandler.sendMessageDelayed(msg, 3500);
        }
    }

    private boolean showAds(String adsPath) {
        if (TextUtils.isEmpty(adsPath)) {
            return false;
        }
        File file = new File(getFilesDir(), adsPath);
        if (!file.exists()) {
            return false;
        }
        final String options = PrefsUtils.getString(this, MConstant.PREFS_KEY_ADS_OPT, "{}");
        try {
            JSONObject optionObj = new JSONObject(options);
            String showDateString = optionObj.optString(MConstant.ADS_OPT_SHOW_DATE);
            String endDateString = optionObj.optString(MConstant.ADS_OPT_END_DATE);
            Date showDate = parseString(showDateString);
            Date endDate = parseString(endDateString);
            if (showDate != null && endDate != null) {
                return arriveDay(showDate) && !arriveDay(endDate);
            } else if (showDate == null && endDate != null) {
                return !arriveDay(endDate);
            } else if (endDate == null && showDate != null) {
                return arriveDay(showDate);
            } else {
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "checkAds: options not a json string.", e);
        }
        return true;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ADS:
                launchAds((String) msg.obj);
                break;
            case MSG_FINISH:
                launchGuide();
                break;
        }
    }

    public void launchGuide() {
        try {
            String guideCodeValue = getString(ResourceUtils.string(this, "guide_version_code"));
            int guideCode = Integer.valueOf(guideCodeValue);
            int value = PrefsUtils.getInt(this, PrefsUtils.PREFS_GUIDE_VALUE, 0);
            if (guideCode > value) {
                Intent intent = new Intent(MConstant.ACTION_GUIDE);
                intent.setComponent(new ComponentName(getPackageName(), "com.mysoft.plugin.guide.GuideActivity"));
                startActivity(intent);
            }
        } catch (Exception e) {
            L.w(TAG, "guideCodeValue must integer");
        } finally {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void launchAds(String adsPath) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), "com.mysoft.plugin.view.AdsActivity"));
        intent.putExtra(MConstant.ADS_PATH, adsPath);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClosePageReceiver != null) {
            mClosePageReceiver.unRegister(this);
        }
    }

    @Override
    public void onBackPressed() {
        // nothing
    }

    private class ClosePageReceiver extends BroadcastReceiver {

        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(MConstant.ACTION_SPLASH_CLOSE);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        }

        public void unRegister(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MConstant.ACTION_SPLASH_CLOSE.equals(intent.getAction())) {
                L.d(TAG, "ClosePageReceiver ACTION_SPLASH_CLOSE!");
                mHandler.sendEmptyMessageDelayed(MSG_FINISH, 500);
            }
        }
    }

}