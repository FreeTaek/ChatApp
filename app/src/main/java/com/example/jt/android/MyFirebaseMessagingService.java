package com.example.jt.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String Tag = "MyFirebaseMsgService";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;

    private DBHandler dbHandler;

    public List<Chat_Data> items;
    private ArrayList<HashMap<String, String>> mapList;
    private ArrayList<String> arrayList;

    private BaseAdapterActivity adt;
    private RecyclerView recyclerView;
    public Chat_Data chat_room;

    private ArrayList<Integer> badge = new ArrayList<Integer>();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Log.d(Tag, "From:"+remoteMessage.getFrom());
        Log.i("onMessageReceived", remoteMessage.getData().get("message"));

        if(remoteMessage.getData().size() > 0) { // Check if message contains a data payload
            Log.d(Tag, "Message data payload:" + remoteMessage.getData());

            // In this case the XMPP Server sends a payload data
            String user_name = remoteMessage.getData().get("name"); //user name 받는 사람 입장에서
            String my_name = remoteMessage.getData().get("user_name"); //my_name 받는 사람 입장에서
            String token = remoteMessage.getData().get("token"); // 상대방 token
            String key = remoteMessage.getData().get("key"); // 상대방 token
            String message = remoteMessage.getData().get("message");
            String list_msg = remoteMessage.getData().get("list_message");
            String profile_image = remoteMessage.getData().get("profile_image");
            String time = remoteMessage.getData().get("time");

            adt = new BaseAdapterActivity(this, items, R.layout.tab_list);
            items = new ArrayList<>();

            //==================================================

            chat_room = new Chat_Data(user_name, my_name, message);

            dbHandler = new DBHandler(this, null, null, 1);
            dbHandler.addChatData(user_name, my_name, message, time);

            badge = dbHandler.getBadgeCount(user_name); // user_name Count 가져오기
            int badgeCount = 0;

            if (badge.size() != 0) {
                badgeCount = badge.get(0);
            }


            ShortcutBadger.applyCount(this, badgeCount + 1); //for 1.1.4+ // add badge count

            if (list_msg != null)
            {
                Log.i("list_Msg", "Data not null");

                message = list_msg;
            }
            else
            {
                Log.i("list_msg", "Data Null");
            }

            dbHandler.updateRoomData(user_name, message, key, token, profile_image, badgeCount + 1, time); // update

            if(Chat_Data.getInstance().getRoomInCheck()) // ChatActivity에 들어가 있을 경우 실행 x
            {
                Log.e("ChatActivity", "들어와있음");

                Chat_Data.getInstance().setBadgeChanged(false);
            }
            else
            {
                Log.e("ChatActivity", "들어와 있지 않음");

                Chat_Data.getInstance().setBadgeChanged(true);

                showNotification(user_name, message, key, token, profile_image); // 알림 표시
            }
        }
    }


    private void showNotification(String name, String message, String key, String token, String profile_image)
    {
        Log.i("sendNotification", "Start");

        dbHandler = new DBHandler(this, null, null, 1);

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("user_name", name);
        intent.putExtra("key", key);
        intent.putExtra("token", token);
        intent.putExtra("profile_image", profile_image);
        intent.putExtra("token", token);
        intent.putExtra("check", "true");

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // intent 로 유저 이름을 ChatRoomActivity로 보낸다.

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(name)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());
    }
}
