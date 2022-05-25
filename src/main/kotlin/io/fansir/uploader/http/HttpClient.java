package io.fansir.uploader.http;

import com.google.gson.Gson;
import io.fansir.uploader.util.Utils;
import kotlin.Pair;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static OkHttpClient                                               client;
    private static Gson                                                       gson;
    private String                                                            url;
    private Map<String,Object>                                                params;
    private Map<String, Pair<File, ProgressListener>> files;
    private int method = 0;//0:post 1:put 3:get

    public HttpClient(String url){
        this.url = url;
    }

    public HttpClient addParams(String key,Object value){
        if (params == null){
            params = new HashMap<>();
        }
        params.put(key,value);
        return this;
    }

    public HttpClient addFile(String key,File file){
        return addFile(key,file,null);
    }

    public HttpClient addFile(String key,File file, ProgressListener listener){
        if (files == null){
            files = new HashMap<>();
        }
        files.put(key,new Pair(file,listener));
        return this;
    }

    public HttpClient setPostMethod(){
        method = 0;
        return this;
    }

    public HttpClient setPutMethod(){
        method = 1;
        return this;
    }

    public HttpClient setGetMethod(){
        method = 3;
        return this;
    }

    private void init(){
        if (gson == null){
            gson = new Gson();
        }
        if (client == null){
            client = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.MINUTES)
                    .connectTimeout(10,TimeUnit.MINUTES)
                    .readTimeout(10,TimeUnit.MINUTES)
                    .build();
        }
    }

    private RequestBody createRequestBody(){
        MultipartBody.Builder body = new MultipartBody.Builder();
        body.setType(MultipartBody.FORM);
        if (files != null){
            for (Map.Entry<String,Pair<File, ProgressListener>> entry:files.entrySet()){
                body.addFormDataPart(entry.getKey(), entry.getValue().getFirst().getName(), new FileProgressRequestBody(entry.getValue().getFirst(),entry.getValue().getSecond()));
            }
        }
        if (params != null){
            for (Map.Entry<String,Object> entry : params.entrySet()){
                body.addFormDataPart(entry.getKey(),String.valueOf(entry.getValue()));
            }
        }
        return body.build();
    }

    private String saveFile(Response response,File file) {
        String filePath = null;
        InputStream is = null;
        long current = 0L;
        byte[] buf = new byte[3072];
        FileOutputStream fos = null;

        try {
            is = response.body().byteStream();
            long total = response.body().contentLength();
            fos = new FileOutputStream(file);

            int len;
            while((len = is.read(buf)) != -1) {
                current += (long)len;
                fos.write(buf, 0, len);
                final float percent = total == 0L ? 0.0F : (float)current * 100.0F / (float)total;
                if (percent % 10 ==0){
                    Utils.info("下载进度："+percent+"%");
                }
            }

            fos.flush();
            filePath = file.getAbsolutePath();
        } catch (Exception var14) {
            new RuntimeException(var14);
        } finally {
            Utils.closeIO(new Closeable[]{is, fos});
            return filePath;
        }
    }

    public void download(File file){
        if (client == null){
            client = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.MINUTES)
                    .connectTimeout(10,TimeUnit.MINUTES)
                    .readTimeout(10,TimeUnit.MINUTES)
                    .build();
        }

        Request.Builder builder = new Request.Builder();
        builder.url(url);

        Response response = null;
        try {
            response = client.newCall(builder.build()).execute();
            if (response.isSuccessful()){
                saveFile(response,file);
            }else {
                throw new RuntimeException(response.code()+"-"+response.message());
            }
        }catch (Exception e){
            Utils.info(e.getMessage());
            throw new RuntimeException(e);
        }finally {
            if (response != null){
                response.close();
            }
        }
    }

    public <T> T start(Class<T> clazz){
        init();

        Request.Builder builder = new Request.Builder();
        builder.url(url);

        if (method == 1){
            builder.put(createRequestBody());
        }else {
            builder.post(createRequestBody());
        }

        Response response = null;
        T t = null;
        try {
            response = client.newCall(builder.build()).execute();
            if (response.isSuccessful()){
                String uploadResult = response.body().string();
                Utils.info(uploadResult);
                Utils.info("result:"+uploadResult);
                t = gson.fromJson(uploadResult, clazz);
            }else {
                throw new RuntimeException(response.code()+"-"+response.message());
            }
        }catch (Exception e){
            Utils.info(e.getMessage());
            throw new RuntimeException(e);
        }finally {
            if (response != null){
                response.close();
            }
            return t;
        }
    }


}
