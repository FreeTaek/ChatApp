package com.example.jt.android;


import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TokenService {

    private static final String TAG = "TokenService";
    private static final String BACKEND_SERVER_IP = "-----"; // ip7
    private static final String BACKEND_URL_BASE = "http://" + BACKEND_SERVER_IP;

    private DatabaseReference mDatabase;

    private Context context;
    private RequestListener listener;

    public TokenService(Context context, RequestListener listener)
    {
        this.context = context;
        this.listener = listener;
    }

    public void registerTokenInDB(final String token)
    {
        // The Call should have a back off strategy

        // Instantiate the RequestQueue
        Log.i("registerTokenInDB", "Start////////////");

        String url = BACKEND_URL_BASE+ "/register.php";

        Log.i("//////////////////", url);

        OkHttpClient client;

        //getSafeOkHttpClient();

        client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();

        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        new Thread()
        {
            public void run()
            {
                try
                {
                    client.newCall(request).execute();

                    Log.i("okhttp", "Success");
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }

        }.start();
    }
}