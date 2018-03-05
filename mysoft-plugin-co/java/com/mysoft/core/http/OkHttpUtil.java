package com.mysoft.core.http;

import com.mysoft.core.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    private static final OkHttpClient sOkHttpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES).writeTimeout(5, TimeUnit.MINUTES).build();


    /**
     * 该不会开启异步线程。
     */
    public static Response execute(Request request) throws IOException {
        return sOkHttpClient.newCall(request).execute();
    }

    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public static Response get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return sOkHttpClient.newCall(request).execute();
    }

    public static void getAsync(String url, Callback callback) {
        Request request = new Request.Builder().url(url).get().build();
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void getAsync(String url, String tag, Callback callback) {
        Request request = new Request.Builder().url(url).tag(tag).get().build();
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url, Map<String, String> params, Callback callback) {
        Request request = getMultipartFormRequest(url, params, null, null);
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url, Map<String, String> params, Map<String, String> head, Callback callback) {
        Request request = getMultipartFormRequest(url, params, head, null);
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url, Map<String, String> params, Map<String, String> head, String tag, Callback callback) {
        Request request = getMultipartFormRequest(url, params, head, tag);
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postFormBodyAsync(String url, FormBody body, Callback callback) {
        Request request = (new Request.Builder()).url(url).post(body).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static Response postFormBody(String url, FormBody body) throws IOException {
        Request request = (new Request.Builder()).url(url).post(body).build();
        Call call = sOkHttpClient.newCall(request);
        return call.execute();
    }

    public static OkHttpClient getOkhttpClient() {
        return sOkHttpClient;
    }

    public static Response postString(String url, String contentType, String params) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), params);
        Request request = new Request.Builder().url(url).post(body).build();
        return sOkHttpClient.newCall(request).execute();
    }

    private static Request getMultipartFormRequest(String url, Map<String, String> params, Map<String, String> head, String tag) {
        Request.Builder reqBuilder = new Request.Builder().url(url);
        reqBuilder.tag(tag);
        if (params != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }

            RequestBody requestBody = builder.build();
            reqBuilder.post(requestBody);
            if (head != null) {
                Set<Map.Entry<String, String>> headSet = head.entrySet();
                for (Map.Entry<String, String> entry : headSet) {
                    reqBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        return reqBuilder.build();
    }

    public static Response post(String url, Map<String, String> params) throws IOException {
        Request request = getMultipartFormRequest(url, params, null, null);
        return sOkHttpClient.newCall(request).execute();
    }

    public static Response post(String url, Map<String, String> params, Map<String, String> head) throws IOException {
        Request request = getMultipartFormRequest(url, params, head, null);
        return sOkHttpClient.newCall(request).execute();
    }

    public static Response post(String url, String filekey, File file, String mime) throws IOException {
        return post(url, filekey, file, mime, null);
    }

    public static void postAsync(String url, String filekey, File file, String mime, Map<String, String> params,
                                 Callback callback) {
        Request request = getPostRequest(url, filekey, file, mime, params, null, null);
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url, String filekey, File file, String mime, Map<String, String> params, Map<String, String> head, Callback callback) {
        Request request = getPostRequest(url, filekey, file, mime, params, head, null);
        sOkHttpClient.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url, String filekey, File file, String mime, Map<String, String> params, Map<String, String> head, String tag, Callback callback) {
        Request request = getPostRequest(url, filekey, file, mime, params, head, tag);
        sOkHttpClient.newCall(request).enqueue(callback);
    }


    public static void postAsync(String url, String filekey, File file, String mime, Callback callback) {
        postAsync(url, filekey, file, mime, null, callback);
    }

    public static Response post(String url, String filekey, File file, String mime, Map<String, String> params)
            throws IOException {
        Request request = getPostRequest(url, filekey, file, mime, params, null, null);
        return sOkHttpClient.newCall(request).execute();
    }

    private static Request getPostRequest(String url, String filekey, File file, String mime, Map<String, String> params, Map<String, String> head, String tag) {
        Request.Builder reqBuilder = new Request.Builder().url(url);
        reqBuilder.tag(tag);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        builder.addFormDataPart(filekey, file.getName(), RequestBody.create(MediaType.parse(mime), file));
        RequestBody requestBody = builder.build();
        reqBuilder.post(requestBody);
        if (head != null) {
            Set<Map.Entry<String, String>> headSet = head.entrySet();
            for (Map.Entry<String, String> entry : headSet) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return reqBuilder.build();
    }

    public static void cancelCallWithTag(String tag) {
        for (Call call : getOkhttpClient().dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag))
                call.cancel();
        }
        for (Call call : getOkhttpClient().dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag))
                call.cancel();
        }
    }

    public static void handleDownloadFile(String path, Response response) throws IOException {
        download(path, response, null);
    }


    public static void handleDownloadFile(String path, Response response, DownloadProgress downloadProgress) throws
            IOException {
        download(path, response, downloadProgress);
    }

    private static void download(String path, Response response, DownloadProgress downloadProgress) throws IOException {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        InputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = response.body().byteStream();
            outStream = new FileOutputStream(new File(path));
            byte[] buf = new byte[1024];
            int len;
            int currSize = 0;
            int totalSize = (int) response.body().contentLength();
            while ((len = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, len);
                currSize += len;
                if (downloadProgress != null) {
                    L.d("okhttputils", "currSize: " + currSize + "totalSize: " + totalSize);
                    downloadProgress.inProgress(currSize, totalSize);
                }
            }
            outStream.flush();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface DownloadProgress {
        void inProgress(int currSize, int totalSize);
    }

}