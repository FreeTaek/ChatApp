package com.example.jt.android;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import static com.example.jt.android.UserActivity.manager;

public class CustomDialog extends AppCompatActivity {

    private Context context;
    private DBHandler dbHandler;
    private Chat_Profile_Tab chat_profile_tab;

    public CustomDialog()
    {
        // Structure
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(Context ct) {

        this.context = ct;

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.show();

        final Button camera = (Button) dialog.findViewById(R.id.camera);
        final Button album = (Button) dialog.findViewById(R.id.album);
        final Button base = (Button) dialog.findViewById(R.id.base);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("button", "Camera");

                chat_profile_tab = new Chat_Profile_Tab();
                chat_profile_tab.dialogProgress();

                manager.Camera(context);

                dialog.dismiss();
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("button", "Album");

                chat_profile_tab = new Chat_Profile_Tab();
                chat_profile_tab.dialogProgress();

                manager.Album(context);

                dialog.dismiss();
            }
        });

        base.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.e("base Clicked", "Start");

                dbHandler = new DBHandler(context, null, null, 1);

                String my_name = dbHandler.getMyData("NAME");

                if(!my_name.equals("None"))
                {
                    dbHandler.setProfileImage(my_name, "None", "MY_PROFILE");

                    manager.setProfileData("None");

                    Chat_Data.getInstance().getCircularImageView().setImageResource(R.drawable.profile_base);
                }

                dialog.dismiss();
            }
        });
    }
}