package com.example.jt.android;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements RequestListener {

    private static final String TAG = "MyFirebaseIDService";
    private TokenService tokenService;

    @Override
    public void onTokenRefresh()
    {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refresh Token:" + refreshToken);

        tokenService = new TokenService(this, this);
        tokenService.registerTokenInDB(refreshToken);
        //sendRegistrationToServer(refreshToken);
    }

    @Override
    public void onComplete()
    {
        Log.d(TAG, "Token registered successfully in the DB");
    }

    @Override
    public void onError(String message)
    {
        Log.d(TAG, "Error trying to register the token in the DB:" + message);
    }


    private void sendRegistrationToServer(String token)
    {
        Log.i("sendRegistration", "Start");

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("Token", token).build();

        Request request = new Request.Builder().url("----").post(body).build();

        try
        {
            client.newCall(request).execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
