package com.example.jt.android;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ChatActivity extends AppCompatActivity {

    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_ALBUM = 2;
    //private static final String FCM_ID = "----"; // token
    private final String FCM_SENDER_ID = "-----"; // 발신자 id
    private final String FCM_SERVER = "@gcm.googleapis.com";
    //public static final String BACKEND_ACTION_ECHO = "ECHO"; TEST 용
    public final String BACKEND_ACTION_MESSAGE = "MESSAGE";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference storageReference;

    private String name;
    private String text = "";
    private String my_name;
    private String my_token;
    private String user_name;
    private String user_token;
    private String key;
    private String list_msg;
    private String token;
    private String fbKey;
    private String profile_image;
    private String time_data;
    private String time = "";
    private String mCurrentPhotoPath;
    private String imageFileName;
    private String albumFileName;
    private int rotate = 0;

    private Button btn_send_msg;
    private ImageButton imageButton;
    private ImageButton camera;
    private ImageButton album;
    private EditText input_msg;
    private TextView chat_conversation;
    private TextView time_line;



    private List<Chat_Data> items;
    private AdapterActivity adt;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private LinearLayout linearLayout;


    private Chat_Data chat_room;
    private Chat_Data_Sub chatData;

    private boolean hideButton = true;
    private boolean check;
    private boolean cameraUsedCheck;

    private InputMethodManager imm;
    private DBHandler dbHandler;
    private ArrayList<HashMap<String, String>> mapList;
    private ArrayList<String> arrayList;
    private NetworkCheck networkCheck;

    private String type = "";

    public PhotoManager manager = new PhotoManager();

    private String imageFilePath;
    private String imagePath;
    private File imageFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.i("onCreate", "Start");

        user_name = getIntent().getExtras().get("user_name").toString();
        key = getIntent().getExtras().get("key").toString();
        token = getIntent().getExtras().get("token").toString();
        profile_image = getIntent().getExtras().get("profile_image").toString();
        check = getIntent().getBooleanExtra("check", false);

        setProfileImage(profile_image);

        Chat_Data.getInstance().setKey(key);
        Chat_Data.getInstance().setAdapterCheck(check); /// <- list 통해 들어옴.


        ShortcutBadger.removeCount(this); //for 1.1.4+ // Badge Delete <- 다른 채팅방에 badge 카운트가 0일시에 발동하게끔 변경


        dbHandler = new DBHandler(this, null, null, 1);

        my_name = dbHandler.getMyData("NAME");

        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        time_line = (TextView) findViewById(R.id.time_line);

        recyclerView = (RecyclerView) findViewById(R.id.activity_recycler);

        linearLayout = (LinearLayout) findViewById(R.id.hide);

        imageButton = (ImageButton) findViewById(R.id.image_button);
        camera = (ImageButton) findViewById(R.id.camera);
        album = (ImageButton) findViewById(R.id.album);

        imm = (InputMethodManager) getSystemService(ChatActivity.INPUT_METHOD_SERVICE);

        arrayList = new ArrayList<>();
        items = new ArrayList<>();

        adt = new AdapterActivity(ChatActivity.this, items, R.layout.activity_chat, my_name);
        adt.setHasStableIds(true);
        
        chatData = new Chat_Data_Sub();

        layoutManager = new LinearLayoutManager(ChatActivity.this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setFocusable(false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adt);

        adt.notifyDataSetChanged();

        if(!Chat_Data.getInstance().getNetworkCheck())
        {
            // 인터넷 연결 x

            Log.i("인터넷 연결 x", "items.clear");
            Log.i("인터넷 연결 x", "findChatData-Start");

            items.clear();

            findChatData();
        }

        recyclerView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                hideKeyboard();

                return false;
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hideButton)
                {
                    On();
                    hideButton = false;
                }
                else
                {
                    Off();
                    hideButton = true;
                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                linearLayout.setVisibility(View.GONE);


                manager.Camera(ChatActivity.this);

                }
        });

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                linearLayout.setVisibility(View.GONE);

                manager.Album(ChatActivity.this);

            }
        });


        //------------------- message button ----- Firebase --------------------------//

        mDatabase = FirebaseDatabase.getInstance().getReference("test_chat");
        final DatabaseReference data = mDatabase.child("uid");

        btn_send_msg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                hideKeyboard();

                if (cameraUsedCheck)
                {
                    Log.i("image Loading....", " image Loading....");

                    btn_send_msg.setEnabled(true);
                    input_msg.setEnabled(true);
                }
                else
                {
                    if (input_msg.getText().length() == 0)
                    {
                        btn_send_msg.setEnabled(true);
                    }
                    else
                    {
                        Map<String, Object> map = new HashMap<String, Object>();
                        fbKey = data.push().getKey();

                        text = input_msg.getText().toString();
                        time_data = getTime();

                        map.put("name", my_name);
                        map.put("msg", text);
                        map.put("time", time_data);

                        data.child(key).child(fbKey).updateChildren(map);

                        Log.i("updateChildren", user_name);

                        Chat_Data.getInstance().setAddDataCheck(true); // Send Check

                        sendNotification();
                    }
                }
            }
        });

        data.child(key).limitToLast(100).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.e("onChildAdded", "//////////onChildAdded Start/////////");

                chatData = dataSnapshot.getValue(Chat_Data_Sub.class);

                name = chatData.getName();
                text = chatData.getMsg();
                time_data = chatData.getTime();

                String arr[] = time_data.split("/");

                time = arr[3];

                // yyyy+"/"+MM+"/"+dd+"/"+time

                NetworkCheck networkCheck = new NetworkCheck();
                networkCheck.networkConnectionCheck(ChatActivity.this);

                if(Chat_Data.getInstance().getNetworkCheck())
                {
                    Log.i("internet 연결", "데이터 저장");

                    imageAndMessageCheck(name, text, time_data);
                }
                else
                {
                    Log.i("internet 연결 x", "데이터 저장 x");
                }

                if (input_msg.length() > 0)
                {
                    input_msg.setText("");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("data", mCurrentPhotoPath);
        savedInstanceState.putString("image", imageFileName);
        savedInstanceState.putString("album", albumFileName);
        savedInstanceState.putString("type", type);

        savedInstanceState.putBoolean("cameraUsedCheck", cameraUsedCheck);

        if (storageRef != null)
        {
            savedInstanceState.putString("reference", storageRef.toString());
        }

    }

    /**/
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentPhotoPath = savedInstanceState.getString("data");
        imageFileName = savedInstanceState.getString("image");
        albumFileName = savedInstanceState.getString("album");
        type = savedInstanceState.getString("type");

        cameraUsedCheck = savedInstanceState.getBoolean("cameraUsedCheck");


        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List<FileDownloadTask> tasks = storageRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot state) {
                    //handleSuccess(state); //call a user defined function to handle the event.

                }
            });
        }
    }

    private void On()
    {
        linearLayout.setVisibility(View.VISIBLE);
    }
    private void Off()
    {
        linearLayout.setVisibility(View.GONE);
    }

    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(input_msg.getWindowToken(), 0);
    }

    private void setProfileImage(String profile_image)
    {
        if(!profile_image.contains("None"))
        {
            imageFile = Chat_Data.getInstance().getImageFile(profile_image);
            imageFilePath = Chat_Data.getInstance().getImagePath();

            if(imageFilePath == null)
            {
                firebaseImageDownload(profile_image, "profile"); // 다운로드 후 이미지 이름 저장
            }
        }
        else
        {
            Chat_Data.getInstance().setProfileImage(profile_image); // None 저장
        }
    }

    private void imageAndMessageCheck(String name, String msg, String time_data)
    {
        if(!msg.contains("qweasdqwesadfdsa"))
        {
            // msg
            Chat_Data.getInstance().setChecked(false);

            addChatData(name, msg, time_data);
        }
        else
        {
            imageFile = Chat_Data.getInstance().getImageFile(msg);
            imageFilePath = Chat_Data.getInstance().getImagePath();

            if(imageFilePath != null)
            {
                // 내가 찍은 사진. 내부에 존재

                Log.i("imageFilePath", "Not Null");

                addChatData(name, msg, time_data);
            }
            else
            {
                // 사진. fb에서 다운로드

                Log.i("imageFilePath", "Null 2");

                firebaseImageDownload(msg, "picture");

                Chat_Data.getInstance().setImageFileCheck(false);
            }
        }
    }

    private void firebaseImageDownload(String data, String type)
    {
        Log.i("FirebaseImageDownload", "Start");

        storageRef = storage.getReference();
        storageReference = storageRef.child("images/" + data);

        storageReference.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created

                Log.i("FIie Download", "Success");

                imageFile = Chat_Data.getInstance().getImageFile(data);
                imageFilePath = Chat_Data.getInstance().getImagePath();

                switch(type)
                {
                    case "picture" :

                        setDownloadImage(); break;

                    case "profile" :

                        Chat_Data.getInstance().setProfileImage(data);

                        break;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors

                Log.i("FIie Download", "Faled");

                Log.i("FIie Download", String.valueOf(exception));
            }
        });
    }

    private void setDownloadImage()
    {
        Log.i("setDownloadImage", "Start");

        storageRef = storage.getReference();
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'

                String data = storageMetadata.getCustomMetadata("rotate");
                String type = storageMetadata.getCustomMetadata("type");

                rotate = Integer.parseInt(data);

                addChatData(name, text, time_data);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!

                Log.i("getMetadata","Failed");
            }
        });
    }

    private void addChatData(String name, String msg, String time_data)
    {
        Log.i("addChatData", "Start");

        chat_room = new Chat_Data(user_name, name, msg, profile_image, time_data, rotate);

        items.add(chat_room);

        arrayList.add(msg);
        Chat_Data.getInstance().setPositionData(arrayList); // image touch 용

        int position = adt.getItemCount() - 1;

        recyclerView.scrollToPosition(position);

        adt.notifyItemInserted(position);
    }


    private void findChatData()
    {
        Log.i("findChatData", "//////////findChatData Start/////////");

        items.clear();
        arrayList.clear();

        Chat_Data.getInstance().setFindChatData(true); // data db 저장 막기.

        try
        {
            mapList = dbHandler.findChatData(user_name);

            for(int i=0; mapList.size()> i; i++)
            {
                HashMap<String, String> map = mapList.get(i);

                String name = map.get("chat_name");
                String msg = map.get("chat_text");

                Log.i("map-name", name);
                Log.i("map-msg", msg);

                chat_room = new Chat_Data(user_name, name, msg);

                items.add(chat_room);
            }

            adt.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            Log.i("Exception", e.toString());
        }

        Chat_Data.getInstance().setFindChatData(false); // 다시 저장 가능.

        Log.i("findChatData", "finish");
    }

    public String getTime()
    {
        long now = System.currentTimeMillis();

        Date date = new Date(now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        String getTime = sdf.format(date);
        Log.e("time = ", getTime);

        String arr[] = getTime.split("-");

        String yyyy = arr[0];
        String MM = arr[1];
        String dd = arr[2];
        String HH = arr[3];
        String mm = arr[4];

        String time ="";
        String time_data ="";

        if(Integer.parseInt(HH) > 12)
        {
            int hour = Integer.parseInt(HH) - 12;

            time = "오후 " + String.valueOf(hour) + ":" +mm;
        }
        else
        {
            if(Integer.parseInt(HH) == 12)
            {
                time = "오후 " + HH+":"+mm;
            }
            else
            {
                time = "오전 " + HH+":"+mm;
            }
        }


        time_data = yyyy+"/"+MM+"/"+dd+"/"+time;

        return time_data;
    }


    private void add_Room_List()
    {
        ArrayList<HashMap<String,String>> room_list; // room data 가져올 arraylist
        ArrayList<String> room_name = new ArrayList<>(); // room data에서 name값 분리할 arraylist
        String room_check = ""; // 분리한 name 값을 arraylist에 담을 변수
        String room_msg = ""; // room msg

        try {
            Log.e("add_Room_List", "//////////////////START/////////////////");

            if(text.contains("qweasdqwesadfdsa"))
            {
                text = "사진";
            }

            room_list = dbHandler.findRoomData();

            if (room_list.size() != 0)
            {
                Log.i("room_list - size", String.valueOf(room_list.size()));

                for (int i = 0; room_list.size() > i; i++) // db에서 가져온 데이터를 1차적으로 String 으로 사용하기 위해 가공한다.
                {
                    HashMap<String, String> map = room_list.get(i);

                    room_check = map.get("chat_name");

                    room_name.add(room_check); // room_name 값만 String 에 넣어준다.
                }

                if (!room_name.contains(user_name)) // 저장된 데이터가 없다면 저장
                {
                    dbHandler.addRoomData(user_name, text, key, token, profile_image, 0, time);
                }
                else // 저장된 데이터가 있다면
                {
                    Log.i("updateRoomData", "//////////////////START/////////////////");

                    Log.i("Add_room_data ->", String.valueOf(Chat_Data.getInstance().getAddDataCheck()));

                    if(Chat_Data.getInstance().getAddDataCheck())
                    {
                        // update room data.

                        Log.i("ChatActivity->", "updateRoomData Start");

                        Log.e("ChatActivity->text = ", text);

                        dbHandler.updateRoomData(user_name, text, key, token, profile_image, 0, time);

                        Chat_Data.getInstance().setAddDataCheck(false);
                    }
                    else
                    {
                        // 저장된 데이터가 없을 시 실행 x

                        Log.i("ChatActivity-roomdata", "새로 저장한 데이터 x");
                    }
                }
            }
            else
            {
                // 저장된 데이터가 아예 없을 시 최초 저장

                Log.i("add_Room_List", "바로 저장");

                dbHandler.addRoomData(user_name, text, key, token, profile_image, 0, time);
            }
        }
        catch (Exception e)
        {
            Log.i("add_Room_List","ERROR 발생 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }
    }

    private void sendNotification()
    {
        my_token = dbHandler.getMyData("TOKEN");

        AtomicInteger msgId = new AtomicInteger();

        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_SENDER_ID + FCM_SERVER)
                .setMessageId(Integer.toString(msgId.incrementAndGet())) // 보내는 상대의 토큰
                .addData ("recipient", token)
                .addData("name", my_name) // 내 이름
                .addData("user_name", user_name) // 상대 이름
                .addData("token",my_token) // 내 토큰
                .addData("key",key) // 내 토큰
                .addData("message", text) // 실제 메세지
                .addData("list_message", list_msg) // 리스트에 표시할 메세지
                .addData("profile_image", profile_image) // 프로필 이미지
                .addData("time", time) // 프로필 이미지
                .addData("action", BACKEND_ACTION_MESSAGE) // ECHO TEST, MESSAGE 실제 상대
                .build());

        Log.i("FirebaseMessaging", "End");
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

                    Chat_Data.getInstance().setAddDataCheck(true); // Send Check

                    manager.galleryAddPic(ChatActivity.this);
                    sendNotification();

                    break;


                case REQUEST_ALBUM :

                    if(data.getData() != null)
                    {
                        try
                        {
                            Log.i("REQUEST_ALBUM", String.valueOf(data));

                            Chat_Data.getInstance().setAddDataCheck(true); // Send Check

                            manager.getAlbumData(data);
                            sendNotification();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            Toast.makeText(this, "Album Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "Album Failed2", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }
        else
        {
            Toast.makeText(this, "RESULT_OK Null", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

        Log.i("onResume", "Start");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        adt.notifyDataSetChanged();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.i("onPause", "Start");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.i("onBackPressed", "Start");

        Chat_Data.getInstance().setRoomInCheck(false);
        Chat_Data.getInstance().setBadgeChanged(true);

        add_Room_List();

        Intent intent = new Intent(this, UserActivity.class);

        startActivity(intent);

        finish();
    }
}


