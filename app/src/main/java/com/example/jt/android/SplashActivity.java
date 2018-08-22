package com.example.jt.android;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private TextView textView;

    private DBHandler dbHandler;

    private Handler mHandler;
    private Runnable mRunnable;
    private int seconds = 2000;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        textView = (TextView) findViewById(R.id.textView);

        dbHandler = new DBHandler(this, null, null, 1);

        boolean checkSoftKey = hasSoftMenu();

        if(checkSoftKey)
        {
            Log.e("checkSoftKey", "소프트 키 존재");

            int softKeyHeight = getSoftKeyHeight();

            Log.e("softKeyHeight", String.valueOf(softKeyHeight));
        }
        else
        {
            Log.e("checkSoftKey", "소프트 키 존재 x");
        }


        stopScheduler(seconds);
    }

    private boolean hasSoftMenu()
    {
        boolean hasMenuKey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();

        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private int getSoftKeyHeight()
    {
        Resources resources = this.getResources();

        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int deviceHeight = 0;

        if(resourceId > 0)
        {
            deviceHeight = resources.getDimensionPixelSize(resourceId);
        }

        return deviceHeight;
    }

    void stopScheduler(int seconds)
    {
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {

            @Override
            public void run() {

                try
                {
                    String login_check = dbHandler.getMyData("NAME");

                    if(login_check.length() != 0)
                    {
                        Intent intent = new Intent(SplashActivity.this, UserActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));

                        finish();
                    }
                }
                catch(Exception e)
                {
                    Log.i("MainAtivity-login","저장된 데이터 없음");

                    e.printStackTrace();
                }
            }
        };

        mHandler.postDelayed(mRunnable, seconds);
    }
}
