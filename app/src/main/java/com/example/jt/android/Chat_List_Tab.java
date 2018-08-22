package com.example.jt.android;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class Chat_List_Tab extends Fragment {

    private View view;
    private ArrayList<HashMap<String, String>> mapList;
    private ArrayList<String> arrayList;

    private List<Chat_Data> items = new ArrayList<>();;
    private BaseAdapterActivity adt = new BaseAdapterActivity(UserActivity.getContext(), items, R.layout.tab_list);
    private RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    private String my_name;
    private String user_name;
    private String key;

    public static boolean setting_button_check;

    private Chat_Data chat_room;
    private Chat_Data_Sub chatData;

    private DBHandler dbHandler;

    public static MenuItem setting_button;
    public static MenuItem textItem;

    private DatabaseReference mDatabase;

    private Handler mHandler;
    private Runnable mRunnable;
    private int seconds = 2000;


    public Chat_List_Tab()
    {
        // ...
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i("Chat_List_Tab", "Start");

        setHasOptionsMenu(true);

        dbHandler = new DBHandler(getActivity(), null, null, 1);
        my_name = dbHandler.getMyData("NAME");

        chatData = new Chat_Data_Sub();

        mDatabase = FirebaseDatabase.getInstance().getReference("profile_image");
        final DatabaseReference data = mDatabase.child(my_name);

        data.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.e("Chat_List_Tab profile", "Start 반응함");

                try
                {
                    Log.e("chatData check : ", my_name);

                    chatData = dataSnapshot.getValue(Chat_Data_Sub.class);

                    String user_name = chatData.getName();
                    String profile_image = chatData.getMsg();


                    if(profile_image != null)
                    {
                        switch(profile_image)
                        {
                            case "None" : Log.i("Chat_List_Tab file", "None");

                                dbHandler.setProfileImage(user_name, "None","USER_PROFILE");

                            break;

                            default:

                                Log.i("Chat_List_Tab - file", "Not None");

                                String user_profile = dbHandler.getUserData(user_name,"PROFILE_IMAGE");

                                if(!user_profile.equals(profile_image))
                                {
                                    Log.e("Chat_List_Tab","if문 조건 true");

                                    // 저장된 파일 이름과 새로 들어오는 파일 이름이 같다면 실행 x
                                    dbHandler.setProfileImage(user_name, profile_image,"USER_PROFILE");
                                }
                                else
                                {
                                    Log.e("Chat_List_Tab","else문 실행");
                                }

                                dbHandler.getUserData(user_name, "NAME");

                                items.clear();

                                findRoomData();
                        }
                    }
                    else
                    {
                        Log.e("profile_image check", "Chat_List_Tab"+"= profile image NULL");
                    }
                }
                catch(Exception e)
                {
                    Log.i("Chat_List_Tab", "데이터 없음");

                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("onCreateView", "Start");

        view = inflater.inflate(R.layout.tab_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.tab_recyclerview);
        recyclerView.addItemDecoration(new ListDecoration(getActivity()));
        arrayList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);// 역순
        layoutManager.setStackFromEnd(true); // 역순
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adt);

        Chat_Data.getInstance().setBaseAdapter(adt);

        findRoomData();

        WaitUser();


        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                Log.i("Touch", "Start");

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e)
            {
                super.onLongPress(e);

                Log.i("onLongPress", "Touch");
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
        {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e)
            {
                View view = recyclerView.findChildViewUnder(e.getX(), e.getY());

                int position = recyclerView.getChildAdapterPosition(view);
                Log.i("position_List", String.valueOf(position));
                Log.i("onLongPress-position", String.valueOf(position));

                Chat_Data.getInstance().setAdapterPosition(position);

                if(view!=null&&gestureDetector.onTouchEvent(e))
                {
                    Log.i("onInterceptTouchEvent", "Touch");

                    TextView chat_name = (TextView)recyclerView.getChildViewHolder(view).itemView.findViewById(R.id.chat_name);

                    user_name = chat_name.getText().toString();

                    key = dbHandler.getUserData(user_name, "KEY");
                    String token = dbHandler.getUserData(user_name, "TOKEN");
                    String user_profile = dbHandler.getUserData(user_name, "PROFILE_IMAGE");


                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

                    String time = sdf.format(date);
                    Log.e("time = ", time);


                    Log.i("user_name", user_name);
                    Log.i("token", token);
                    Log.i("key", key);

                   if(!setting_button_check)
                   {
                       // Network 연결 체크.
                       NetworkCheck networkCheck = new NetworkCheck();
                       networkCheck.networkConnectionCheck(getActivity());

                       Intent intent = new Intent(getActivity(), ChatActivity.class);

                       intent.putExtra("user_name", user_name);
                       intent.putExtra("key", key);
                       intent.putExtra("token", token);
                       intent.putExtra("profile_image", user_profile);
                       intent.putExtra("time", time);
                       intent.putExtra("check", true);// list -> chat

                       Log.i("Chat_List_Tab", "----- Chat_List_Tab Check -----");

                       Log.i("user_name", user_name);
                       Log.i("key", key);
                       Log.i("token", token);

                       Chat_Data.getInstance().setRoomInCheck(true); // notification 알림 안받음.

                       startActivity(intent);

                       getActivity().finish();
                   }
                   else
                   {
                       Log.i("Chat_List_Tab ->", "setting_button_check 발동");

                   }

                    Log.i("Touched!!!", "Touch///////////////////");

                    if(Chat_Data.getInstance().getChecked())
                    {
                        Chat_Data.getInstance().setCheckBox(true);
                    }
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent e)
            {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
            {

            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i("Chat_List_tab", "Start");

        Chat_Data.getInstance().setButtonCheck(true);
        Chat_Data.getInstance().setCheckBox(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu,inflater);

        setting_button = (MenuItem) menu.findItem(R.id.setting_button);
        textItem = (MenuItem) menu.findItem(R.id.textItem);

        setting_button.setVisible(true);
        textItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id)
        {
            case R.id.setting_button:

                Log.i("Chat_List_Tab - setting", "Clicked");

                setting_button_check = true;

                if(Chat_Data.getInstance().getButtonCheck())
                {
                    Log.i("getButtonCheck", "true");

                    arrayList.clear();

                    textItem.setVisible(true);
                    setting_button.setVisible(false);

                    Chat_Data.getInstance().setChecked(true);
                    Chat_Data.getInstance().setCheckBox(true);

                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    View itemView = inflater.inflate(R.layout.chat_list, null, false);

                    TextView chat_badge = (TextView)itemView.findViewById(R.id.badge);
                    chat_badge.setVisibility(View.INVISIBLE); // badge count 가리기

                    adt.notifyDataSetChanged();

                    //refresh();
                }
                else
                {
                    Log.i("getButtonCheck", "false");
                }

                return true;

            case R.id.textItem :

                Log.i("Chat_List_Tab - text", "Clicked");

                textItem.setVisible(false);
                setting_button.setVisible(true);

                Chat_Data.getInstance().setChecked(false);
                Chat_Data.getInstance().setCheckBox(false);
                Chat_Data.getInstance().setButtonCheck(true);

               if(Chat_Data.getInstance().getArrayList().size() != 0)
               {
                   Log.e("Chat_List_Tab", "room size not Zero");

                   dbHandler.deleteRoomData(Chat_Data.getInstance().getArrayList());
                   dbHandler.deleteChatData(Chat_Data.getInstance().getArrayList());
               }

                refresh();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findRoomData()
    {
        Log.e("findRoomData", "Chat_List_Tab -> Start");

        items.clear();

        String chat_name; // user_name
        String chat_text;
        String chat_key;
        String chat_token;
        String chat_profile_image;
        String chat_badge;
        String chat_time;

        int badgeCount = 0;

        mapList = dbHandler.findRoomData();

        for(int i = 0; mapList.size() > i; i++) {

            HashMap<String, String> map = mapList.get(i);

            chat_name = map.get("chat_name");
            chat_text = map.get("chat_text");
            chat_key = map.get("chat_key");
            chat_token = map.get("chat_token");
            chat_profile_image = map.get("chat_profile_image");
            chat_badge = map.get("chat_badge");
            chat_time = map.get("chat_time");

            badgeCount = Integer.parseInt(chat_badge);
            badgeCount += badgeCount;


            if (chat_name != null && chat_name != "")
            {
                Log.i("Chat_List_Tab", "Start");

                chat_room = new Chat_Data(chat_name, chat_text, chat_key, chat_token, chat_profile_image, chat_badge, chat_time);

                items.add(chat_room);
            }
            else
            {
                Log.i("Chat_List_Tab", "데이터 없음");
            }

            adt.notifyDataSetChanged();

            arrayList.clear();
        }

        Log.e("badgeCOunt = ", String.valueOf(badgeCount));

        ShortcutBadger.applyCount(getActivity(), badgeCount); //for 1.1.4+
    }

    public void refresh()
    {
        Log.e("Chat_List_Tab", "refresh -> Start");

       try
       {
           Log.e("refresh", "Not null");

           getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();

       }
       catch (Exception e)
       {
           e.printStackTrace();

           Log.e("refresh", "Null");
       }
    }

    public void WaitUser()
    {
        stopScheduler(seconds);
    }

    void stopScheduler(int seconds)
    {
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {

            @Override
            public void run() {

                if(Chat_Data.getInstance().getBadgeChanged())
                {
                    Log.e("badge", "BadgeCount Change");

                    Chat_Data.getInstance().setBadgeChanged(false);

                    refresh();
                }
                else
                {
                    WaitUser();
                }
            }
        };

        mHandler.postDelayed(mRunnable,seconds);
    }


    @Override
    public void onResume()
    {
        super.onResume();

        Log.i("onResume", "Start");

        getActivity().invalidateOptionsMenu();
    }
}
