package com.example.jt.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkCheck {

    private boolean internet;
    private Context context;

    public NetworkCheck()
    {
        //
    }

    public void networkConnectionCheck(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(wifi.isConnected() || mobile.isConnected())
        {
            internet = true;

            Log.i("internet", "True");
        }
        else
        {
            internet = false;

            Log.i("internet", "False");
        }

        Chat_Data.getInstance().setNetworkCheck(internet);
    }
}
