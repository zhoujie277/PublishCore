package com.mysoft.plugin;

import android.content.pm.PackageManager;
import android.util.Log;

import com.mysoft.core.L;
import com.mysoft.core.MCordovaPlugin;
import com.mysoft.core.MicCore;
import com.mysoft.core.view.DaemonActivity;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouj04 on 2016/10/12.
 */

public class PermissionManagerPlugin extends MCordovaPlugin {
    private final static String TAG = "PermissionPlugin";
    private static final int REQ_PERMISSION = 0x12;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        List<String> permissionList = new ArrayList<>();
        Map<String, String> map = preferences.getAll();
        for (String key : map.keySet()) {
            if (key.startsWith("android_permission")) {
                permissionList.add(map.get(key));
            }
        }

        boolean result = checkPermissions(permissionList.toArray(new String[permissionList.size()]));
        Log.d(TAG, "checkPermissions [" + result + "], ");
    }

    private boolean checkPermissions(String[] permissionArray) {
        for (String str : permissionArray) {
            if (!cordova.hasPermission(str)) {
                cordova.requestPermissions(this, REQ_PERMISSION, permissionArray);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        L.e(TAG, "请求存储权限失败");
                        MicCore.callback(ERR_CODE_STORAGE_PERMISSION_REFUSE, "获取存储权限失败");
                    } else {
                        ((DaemonActivity) cordova.getActivity()).getCordovaWebView().getPluginManager().postMessage("android.permission.WRITE_EXTERNAL_STORAGE", null);
                    }
                }
            }
        }
    }
}
