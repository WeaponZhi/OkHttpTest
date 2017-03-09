package com.weaponzhi.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    OkHttpClient okHttpClient;
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    private TextView mTvResult;
    private String mBaseUrl = "http://192.168.0.101:8080/imooc_okhttp/";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.id_iv_result);
        mTvResult = (TextView) findViewById(R.id.id_tv_result);
        //初始化okHttpClient并进行Cookies策略设置
        okHttpClient = clientBuilder.cookieJar(new CookiesManager(getApplicationContext())).build();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            //
        }
    }

    public void doGet(View view) throws IOException {
        //1.拿到okHttpClient对象

        //2.构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder.get()
                .url(mBaseUrl + "login?username=hyman&password=1234")
                .build();
        //3.将Request封装为Call
        executeRequest(request);
    }


    public void doPost(View view) {

        FormBody.Builder formBody = new FormBody.Builder();
        RequestBody requestBody = formBody.add("username", "hyman").add("password", "123").build();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "login").post(requestBody).build();

        executeRequest(request);
    }

    public void doPostString(View view) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "{username:hyman,password:123}");
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "postString").post(requestBody).build();

        executeRequest(request);
    }

    public void doPostFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "banner2.jpg");
        if (!file.exists()) {
            LogUtil.e(file.getAbsolutePath() + " not exist!");
            return;
        }
        //mime type
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "postFile").post(requestBody).build();

        executeRequest(request);
    }

    public void doUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "banner2.jpg");
        if (!file.exists()) {
            LogUtil.e(file.getAbsolutePath() + " not exist!");
            return;
        }
        MultipartBody.Builder multiBuilder = new MultipartBody.Builder();
        RequestBody requestBody = multiBuilder.setType(MultipartBody.FORM)
                .addFormDataPart("password", "123")
                .addFormDataPart("username", "hyman")
                .addFormDataPart("mPhoto", "hyman.jpg", RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesWritten, long contentLength) {
                LogUtil.e(bytesWritten+" / "+contentLength);
            }
        });
        //mime type
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "uploadInfo").post(countingRequestBody).build();

        executeRequest(request);
    }

    public void doDownload(View view) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get()
                .url(mBaseUrl + "files/hyman.jpg")
                .build();
        //3.将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //4.执行call
//        Response response = call.execute();//同步
        //异步
        //回调还是在子线程里
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtil.e("onResponse:");

                final long total = response.body().contentLength();
                long sum = 0;
                InputStream is = response.body().byteStream();
                int len = 0;
                File file = new File(Environment.getExternalStorageDirectory(), "hyman12306.jpg");
                byte[] buf = new byte[128];
                FileOutputStream fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum += len;
                    LogUtil.e(sum + " / " + total);
                    final long finalSum = sum;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(finalSum+" / "+total);
                        }
                    });
                }
                fos.flush();
                fos.close();
                is.close();

                LogUtil.e("download success!");
            }
        });
    }

    public void doDownloadImg(View view) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get()
                .url(mBaseUrl + "files/hyman.jpg")
                .build();
        //3.将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //4.执行call
//        Response response = call.execute();//同步
        //异步
        //回调还是在子线程里
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtil.e("onResponse:");

                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });

                LogUtil.e("download success!");
            }
        });
    }

    private void executeRequest(Request request) {
        Call call = okHttpClient.newCall(request);
        //4.执行call
//        Response response = call.execute();//同步
        //异步
        //回调还是在子线程里
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtil.e("onResponse:");
                final String res = response.body().string();
                LogUtil.e(res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText(res);
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "权限申请通过！", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
