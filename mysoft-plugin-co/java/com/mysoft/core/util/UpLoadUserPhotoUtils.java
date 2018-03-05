package com.mysoft.core.util;

import android.app.ProgressDialog;
import android.content.Context;

import com.mysoft.core.L;
import com.mysoft.core.http.OkHttpUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 调用上传头像方法
 */

public class UpLoadUserPhotoUtils {

    private static final String TAG = "UpLoadUserPhotoUtils";

    private CallbackContext callbackContext;

    private String uploadUrl;
    private String infoSaveUrl;
    private String filePath;
    private ProgressDialog progressDialog;
    private Context mContext;

    public UpLoadUserPhotoUtils(String urlPath, String infoSaveUrl, String Filepath, CallbackContext callbackContext,
                                Context context) {
        this.mContext = context;
        this.uploadUrl = urlPath;
        this.infoSaveUrl = infoSaveUrl;
        this.filePath = Filepath;
        this.callbackContext = callbackContext;
    }

    public void upLoadHead() {
        L.i(TAG, "upLoadHead");
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("上传中");
        progressDialog.show();
        OkHttpUtil.postAsync(uploadUrl, "file", new File(filePath), "image/jpeg", new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                PluginResult r = new PluginResult(PluginResult.Status.ERROR, e.toString());
                r.setKeepCallback(false);
                callbackContext.sendPluginResult(r);
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonOb = new JSONObject(result);
                    String status = jsonOb.optString("status");
                    // 图片上传状态，1表示成功，0表示失败
                    if ("1".equals(status)) {
                        String original = jsonOb.getString("original");
                        updataHeadPortrait(original);
                    } else if ("0".equals(status)) {
                        String msg = jsonOb.getString("msg");
                        PluginResult r = new PluginResult(PluginResult.Status.ERROR, msg.toString());
                        r.setKeepCallback(false);
                        callbackContext.sendPluginResult(r);
                    }
                } catch (JSONException e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    public void updataHeadPortrait(final String orig) {
        Map<String, String> params = new HashMap<>();
        params.put("headimg_url", orig);
        OkHttpUtil.postAsync(infoSaveUrl, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                PluginResult r = new PluginResult(PluginResult.Status.ERROR, e.toString());
                r.setKeepCallback(false);
                callbackContext.sendPluginResult(r);
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    JSONObject jsonOb = new JSONObject(result);
                    jsonOb.put("original", orig);
                    jsonOb.put("localPath", filePath);
                    PluginResult r = new PluginResult(PluginResult.Status.OK, jsonOb);
                    r.setKeepCallback(false);
                    callbackContext.sendPluginResult(r);
                } catch (JSONException e) {
                    PluginResult r = new PluginResult(PluginResult.Status.ERROR, "JSONException");
                    r.setKeepCallback(false);
                    callbackContext.sendPluginResult(r);
                }
                progressDialog.dismiss();
            }

        });
    }
}
