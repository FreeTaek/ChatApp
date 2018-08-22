package com.example.jt.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static com.example.jt.android.Chat_List_Tab.setting_button;
import static com.example.jt.android.Chat_List_Tab.setting_button_check;
import static com.example.jt.android.Chat_List_Tab.textItem;

public class UserActivity extends AppCompatActivity {


    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_ALBUM = 2;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    public static PhotoManager manager = new PhotoManager();
    public static Chat_List_Tab chat_list;
    public static Context mContext;

    private TabLayout tabLayout;
    private ViewPager viewPager;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mContext = this;

        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager)findViewById(R.id.viewPager);


        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        int[] tabIcons = { R.drawable.android, R.drawable.chat_list};

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    public static Context getContext()
    {
        return mContext;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {

                case KeyEvent.KEYCODE_BACK:
                    // 단말기의 BACK버튼

                    Log.i("KeyEvent", "Back");

                    // ------------- Chat_List_Tab BackPreseed ---------------//

                    textItem.setVisible(false);
                    setting_button.setVisible(true);

                    Chat_Data.getInstance().setChecked(false);
                    Chat_Data.getInstance().setCheckBox(false);
                    Chat_Data.getInstance().setButtonCheck(true); // -

                    Chat_Data.getInstance().getBaseAdapter().notifyDataSetChanged();

                    setting_button_check = false; // back button 시에 다시 방 입장 가능.
                    // -------------------------------------------------------//

                    // 두 번 눌러야 꺼진다.

                    long tempTime = System.currentTimeMillis();
                    long intervalTime = tempTime - backPressedTime;

                    if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
                    {
                        super.onBackPressed();

                        FirebaseAuth.getInstance().signOut();

                        finish();
                    }
                    else
                    {
                        backPressedTime = tempTime;
                        Toast.makeText(this, "one more", Toast.LENGTH_SHORT).show();
                    }


                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            switch(requestCode)
            {
                case REQUEST_CAMERA :

                    Log.i("Random_Chat_Tab", "galleryAddPic Start");

                    manager.galleryAddPic(this);

                    break;


                case REQUEST_ALBUM :

                    if(data.getData() != null)
                    {
                        try
                        {
                            Log.i("Random_Chat_Tab", "getAlbumData Start");

                            manager.getAlbumData(data);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            Log.i("REQUEST_ALBUM", "Album Failed");
                        }
                    }
                    else
                    {
                        Log.i("DATA - NULL", "Album Failed2");
                    }

                    break;
            }
        }
        else
        {
            Toast.makeText(this, "RandomChatActivityUser - RESULT_OK Null", Toast.LENGTH_SHORT).show();

            Chat_Data.getInstance().getDialog().dismiss();
        }
    }
}