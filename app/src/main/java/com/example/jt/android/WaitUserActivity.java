package com.example.jt.android;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class WaitUserActivity extends AppCompatActivity {

    private String db_user;

    private String check_user;
    private String chat_user;

    private Chat_Data chat_room;

    private DatabaseReference mDatabase;

    private int user_size = 0;

    private TextView textView;
    private ProgressBar progressBar;

    private String random_user;
    private String my_name;
    private String my_profile;

    private boolean check = true;
    private boolean check2 = true;

    private Handler mHandler;
    private Runnable mRunnable;
    private int seconds =1000;

    private DBHandler dbHandler;
    private ArrayList<HashMap<String, String>> mapList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wait);

        my_name = getIntent().getExtras().get("my_name").toString();
        my_profile = getIntent().getExtras().get("my_profile").toString();

        textView = (TextView) findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        dbHandler = new DBHandler(this, null, null, 1);

        random_user();
    }


    private void random_user() {

        mDatabase = FirebaseDatabase.getInstance().getReference("random_user");
        final DatabaseReference data = mDatabase.child("uid");

        data.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long allNum = dataSnapshot.getChildrenCount(); // 저장된 데이터 갯수 체크
                int maxNum = (int) allNum;
                int minNum = 0;
                int count = 0;

                Iterable<DataSnapshot> ds = dataSnapshot.getChildren();
                Iterator<DataSnapshot> ids = ds.iterator();

                ArrayList<String> room = new ArrayList<String>();
                ArrayList<String> list = new ArrayList<String>();

                String room_name; // user_name

                String [] arr;
                String user_name = "";
                String user_profile = "";
                String my_profile= "";


                mapList = dbHandler.findRoomData();

                for(int i = 0; mapList.size() > i; i++) {

                    HashMap<String, String> map = mapList.get(i);

                    room_name = map.get("chat_name");

                    room.add(room_name);
                }


                while (ids.hasNext() && count < maxNum) {

                    db_user = (String) ids.next().getValue();

                    String [] arr2 = db_user.split(":");
                    String user_name2 = arr2[0]; // name

                    if (!room.contains(user_name2))
                    {
                        // 저장된 유저와 다른 유저들 배열에 추가

                        list.add(db_user);

                        count++;
                    }
                }

                user_size = list.size();

                if (user_size != 0) {

                    int randomNum = new Random().nextInt(user_size) + minNum; // 사용자들 중 한명 랜덤으로 결정.

                    if (user_size >= 1 && user_size != 0) //db의 유저가 하나가 아닐경우, 랜덤으로 돌린다.
                    {
                        // list의 저장된 데이터를 get을 이용하여 랜덤한 수의 위치에 있는 데이터를 가져옴.

                        random_user = list.get(randomNum);

                        arr = random_user.split(":"); // 0은 name, 1은 key
                        user_name = arr[0]; // name
                        user_profile = arr[1];
                        my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE"); // profile_image

                        if(!my_name.equals(user_name))
                        {
                            if(check)
                            {
                                Chat_Data.getInstance().setUserName(user_name);

                                String key = getRandomKey().toString(); // public key

                                if(user_profile.length() == 0)
                                {
                                    user_profile = "None";
                                }

                                data.child(user_name).setValue(my_name+":"+key+":"+my_profile); // 나의 이름과 공통의 key를 만들어서 fb에 저장한다.// WaitUser에서 찾은 후 제거

                                startActivity(user_name, key, user_profile); // ChatActivity Start
                            }
                            else
                            {
                                WaitUser();// WaitUser에서 재검색을 통해 random_user가 식별될 경우 실행
                            }
                        }
                        else
                        {
                            check = false;
                            WaitUser(); // 최초 사용자가 없다면 wait 상태가 된다.
                            Toast.makeText(WaitUserActivity.this, "duplicate", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    check = false;
                    WaitUser();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(WaitUserActivity.this, "onCancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void WaitUser()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference("random_user");
        final DatabaseReference data = mDatabase.child("uid");

        my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

        if (check2)
        {
            if(my_profile.length() == 0)
            {
                my_profile = "None";
            }

            data.child(my_name).setValue(my_name+":"+my_profile); // 자기의 닉네임을 db에 저장.

            check2  = false;
        }

        stopScheduler(seconds);
    }

    void stopScheduler(int seconds)
    {
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {

            @Override
            public void run()
            {
                String [] arr;
                String user_name = "";
                String key = "";
                String profile_image = "";

                if (random_user == null)
                {
                    random_user();
                }
                else
                {
                    arr = random_user.split(":"); // 0은 name, 1은 key
                    user_name = arr[0]; // name

                    if(my_name.equals(user_name))
                    {
                        random_user();
                    }
                    else if(!my_name.equals(user_name)) // DB 대기자 name과 본인 이름 비교
                    {
                        key = arr[1]; // key
                        profile_image = arr[2]; // profile_image

                        mHandler.removeCallbacksAndMessages(mRunnable);

                        mDatabase = FirebaseDatabase.getInstance().getReference("random_user");
                        final DatabaseReference data = mDatabase.child("uid");

                        data.child(my_name).setValue(null);

                        startActivity(user_name, key, profile_image);
                    }
                }
            }
        };

        mHandler.postDelayed(mRunnable, seconds);
    }

    private StringBuffer getRandomKey()
    {
        Random random = new Random();

        StringBuffer randomKey = new StringBuffer();

        int num = 20;
        int randomNum = 0;

        for(int i =0; i<num; i++)
        {
            String randomKey1 = String.valueOf((char) ((int) (random.nextInt(26)) + 65));//대문자 아스키 65~122
            String randomKey2 = String.valueOf((char) ((int) (random.nextInt(26)) + 97));//소문자 아스키 97~122
            String randomKey3 = String.valueOf(random.nextInt(10));

            randomNum = random.nextInt(3);

            switch(String.valueOf(randomNum))
            {
                case "1" : randomKey.append(randomKey1); break;
                case "2" : randomKey.append(randomKey2); break;
                case "3" : randomKey.append(randomKey3); break;

                default : Log.e("RandomChatActivityTwo", "getRandomKey Default !!");
            }
        }

        Log.e("getRandomKey", "value: "+ randomKey);

        return randomKey;
    }

    public String getTime()
    {
        long now = System.currentTimeMillis();

        Date date = new Date(now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        String getTime = sdf.format(date);

        return getTime;
    }


    private void setProfileImageData(String user_name, String profile_image)
    {
        // profile 이미지 관리

        mDatabase = FirebaseDatabase.getInstance().getReference("profile_image");
        final DatabaseReference data = mDatabase;

        HashMap<String, String> map = new HashMap<String, String>();

        String key = data.push().getKey();

        map.put("name", user_name);
        map.put("profile_image", profile_image);

        data.child(my_name).child(key).setValue(map);
    }

    private void startActivity(String user_name, String key, String profile_image)
    {

        Log.e("user_name", user_name);
        Log.e("key", key);
        Log.e("profile_image", profile_image);

        String time = getTime();
        Log.e("time = ", time);

        setProfileImageData(user_name, profile_image);

        mDatabase = FirebaseDatabase.getInstance().getReference("Token");
        final DatabaseReference data2 = mDatabase.child(user_name);

        data2.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String token = dataSnapshot.getValue().toString();

                Intent intent = new Intent(WaitUserActivity.this, ChatActivity.class);

                intent.putExtra("user_name", user_name);
                intent.putExtra("key", key);
                intent.putExtra("token", token);
                intent.putExtra("profile_image", profile_image);
                intent.putExtra("boolean", false);// 처음 대화 인증

                //dbHandler.setUserData(user_name, token, key);

                startActivity(intent);

                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Toast.makeText(this, "BackPressed", Toast.LENGTH_SHORT).show();

        DatabaseReference dataBase = FirebaseDatabase.getInstance().getReference("random_user").child("uid");

        dataBase.child(my_name).setValue(null);

        finish();

    }
}


