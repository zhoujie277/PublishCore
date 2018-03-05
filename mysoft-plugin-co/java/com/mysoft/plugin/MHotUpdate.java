package com.mysoft.plugin;

import android.text.TextUtils;

import com.mysoft.core.L;
import com.mysoft.core.MCordovaPlugin;
import com.mysoft.core.MResultCordovaPlugin;
import com.mysoft.core.exception.MArgumentException;
import com.mysoft.core.http.OkHttpUtil;
import com.mysoft.core.util.StorageUtils;
import com.mysoft.core.util.ZipUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import okhttp3.Response;

/**
 * This class echoes a string called from JavaScript.
 */
public class MHotUpdate extends MResultCordovaPlugin {

    private final int INPROGRESS_CODE = 1;
    private final int SUCCESS_CODE = 2;


    @Override
    public boolean onExecute(String action, JSONArray args) throws JSONException, MArgumentException {
        if ("downloadWebContent".equals(action)) {// 更新app的web内容
            String url = args.getString(0);
            if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                downLoadWebContent(url, getCallbackContext());
            } else {
                error(getCallbackContext(), "不合法的url地址");
            }
            return true;
        }
        return false;
    }

    /**
     * 下载webContent zip文件并覆盖www文件夹
     *
     * @param zipUrl
     */
    private void downLoadWebContent(final String zipUrl, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String tmpDir = StorageUtils.getTempDownloadDir(getContext());
                    int endIndex = zipUrl.indexOf("?");
                    String filePath = "";
                    if (endIndex == -1) {
                        filePath = tmpDir + File.separator + zipUrl.substring(zipUrl.lastIndexOf("/") + 1, zipUrl.length());
                    } else {
                        filePath = tmpDir + File.separator + zipUrl.substring(zipUrl.lastIndexOf("/") + 1, endIndex);
                    }

                    Response response = OkHttpUtil.get(zipUrl);
                    OkHttpUtil.handleDownloadFile(filePath, response, new OkHttpUtil.DownloadProgress() {
                        @Override
                        public void inProgress(int currSize, int totalSize) {
                            callback(INPROGRESS_CODE, getCallbackContext(), true, String.format("%.2f", (float) currSize / (float) totalSize));
                        }
                    });
                    // 覆盖www文件目录中的文件
                    ZipUtils.unZip(filePath, StorageUtils.getWwwDir(getContext()));
                    // 覆盖成功后删除zip文件
                    new File(filePath).delete();
                    callback(SUCCESS_CODE, true);
                } catch (Exception e) {
                    L.e(TAG, "downLoadWebContent Error.", e);
                    callbackContext.error(getErrJson("下载失败:" + e.getMessage()));
                }

            }
        });
    }

}
