package com.mysoft.core.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.mysoft.core.L;

/**
 * 统一Activity基类
 *
 * @author Jay
 */
public class MActivity extends Activity {
    public final String TAG = getClass().getSimpleName();

    private final BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        L.i(TAG, "mNetworkReceiver onReceive");
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (!intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                L.i(TAG, "mNetworkReceiver connect");
                onNetworkConnect();
            }
        }
        }
    };

    protected void onNetworkConnect() {
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
    }
}
