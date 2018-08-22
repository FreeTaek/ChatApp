package com.example.jt.android;

import android.app.Dialog;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Chat_Data {

    private String name;
    private String user_name;
    private String key;
    private String msg;
    private String token;
    private String profile_path;
    private String badge;
    private String time;
    private int rotate;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference pathReference;
    private StorageReference storageReference;
    private String imagePath;
    private File imageFile;

    private boolean use;

    private int position = 0;

    static private Chat_Data instance = null;

    private boolean check;

    private String deleteRoom;
    private boolean deleteCheck;
    private boolean setRoomNameCheck;
    private boolean handlerCheck;

    private boolean buttonCheck;

    private int setPosition;

    private boolean longTouchCheck;
    private boolean swipeCheck;
    private boolean checkBox;
    private boolean isChecked;

    private boolean adapterCheck;
    private boolean timelineCheck;

    private boolean findChatData;
    private boolean networkCheck;
    private boolean addCheck = false;

    private boolean imageFileCheck;

    private String photoPath = "";
    private String imageFileName = "";

    private ArrayList<String> arrayList = new ArrayList<>(); //room_list delete용
    private ArrayList<String> positionData = new ArrayList<>();// chatActivity image check용

    private boolean change = false;
    private boolean room_in_check = false;
    private boolean regenerative;

    private Button button;
    private CircularImageView imageView;
    private TextView textView;
    private Dialog dialog;
    private BaseAdapterActivity adt;

    private Chat_Data() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public static Chat_Data getInstance() {
        if (instance == null) {
            instance = new Chat_Data();
        }

        return instance;
    }

    public Chat_Data(String user_name) {
        this.user_name = user_name;
    }

    public Chat_Data(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }


    public Chat_Data(String user_name, String name, String msg) // name은 공통
    {
        this.user_name = user_name;
        this.name = name;
        this.msg = msg;
    }

    public Chat_Data(String user_name, String name, String msg, String profile_path, String time, int rotate) //ChatActivity -> addChatData
    {
        this.user_name = user_name;
        this.name = name;
        this.msg = msg;
        this.profile_path = profile_path;
        this.time = time;
        this.rotate = rotate;
    }

    public Chat_Data(String name, String msg, String key, String token, String profile_path, String badge, String time) //Chat_List_Tab -> 방 생성
    {
        this.name = name;
        this.msg = msg;
        this.key = key;
        this.token = token;
        this.profile_path = profile_path;
        this.badge = badge;
        this.time = time;
    }

    public Chat_Data(String key, String user_name, int position) {
        this.key = key;
        this.user_name = user_name;
        this.position = position;
    }

    public Chat_Data(String key, String user_name, String msg , int position) {
        this.key = key;
        this.user_name = user_name;
        this.msg = msg;
        this.position = position;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }
    public String getUserName() {
        return user_name;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }

    public void setProfileImage(String profile_path) {this.profile_path = profile_path; }
    public String getProfileImage()
    {
        return profile_path;
    }

    public String getImagePath() {
        return imagePath;
    }
    public File getImageFile(String file_name) {

        saveImageFile(file_name);

        return imageFile;
    }

    public void saveImageFile(String file_name) {

        Log.i("saveImageFile", "Start");

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            File photoFile = null;

            try {
                photoFile = createImageFile(file_name);
            } catch (IOException e) {

                Log.i("saveImageFile", e.toString());
            }
        }
    }

    private File createImageFile(String file_name) throws IOException {

        imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/image/" + file_name);

        if (!imageFile.exists()) {

            // 파일 존재 x
            if (imageFile.getParentFile().mkdirs())
            {
                Log.i("ImageFile -> mkdirs", "생성 성공.");
            }
            else
            {
                Log.i("ImageFile -> mkdirs", "생성 실패");
            }

            if(imageFile.createNewFile())
            {
                Log.i("ImageFile -> createFile", "생성 성공.");
            }
            else
            {
                Log.i("ImageFile -> createFile", "생성 실패");
            }

            imagePath = null;

            imageFileCheck = false;
        }
        else
        {
            // 파일 존재.

            Log.i("file2", "exists");

            Log.i("file2", String.valueOf(imageFile.exists()));

            imageFileCheck = true;

            imagePath = imageFile.getAbsolutePath();
        }

        return imageFile;
    }

    public void setPositionData(ArrayList<String> positionData) { this.positionData = positionData; }
    public ArrayList<String> getPositionData()
    {
        return positionData;
    }

    public void setRoomInCheck(boolean room_in_check)
    {
        this.room_in_check = room_in_check;
    }
    public boolean getRoomInCheck()
    {
        return room_in_check;
    }

    public String getBadgeCount()
    {
        return badge;
    }

    public void setBadgeChanged(boolean change)
    {
        this.change = change;
    }
    public boolean getBadgeChanged()
    {
        return change;
    }

    public void setTime(String time)
    {
        this.time = time;
    }
    public String getTime()
    {
        return time;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }
    public int getRotate() {
        return rotate;
    }

    public void setTimeLineCheck(boolean timelineCheck)
    {
        this.timelineCheck = timelineCheck;
    }

    public boolean getTimeLineCheck()
    {
        return timelineCheck;
    }

    public boolean getImageFileCheck()
    {
        return imageFileCheck;
    }
    public void setImageFileCheck(boolean imageFileCheck)
    {
        this.imageFileCheck = imageFileCheck;
    }

    public StorageReference getStorageRef()
    {
        return storageRef;
    }

    public void setImageFileName(String imageFileName)
    {
        this.imageFileName = imageFileName;
    }
    public String getImageFileName()
    {
        return imageFileName;
    }

    public void setPhotoPath(String photoPath)
    {
        this.photoPath = photoPath;
    }
    public String getPhotoPath()
    {
        return photoPath;
    }

    public void setArrayList(ArrayList<String> arrayList)
    {
        this.arrayList = arrayList;
    }
    public ArrayList<String> getArrayList() {

        return arrayList;
    }

    public void setAdapterCheck(boolean adapterCheck)
    {
        this.adapterCheck = adapterCheck;
    }
    public boolean getAdapterCheck()
    {
        return adapterCheck;
    }

    public void setFindChatData(boolean findChatData)
    {
        this.findChatData = findChatData;
    }

    public void setAddDataCheck(boolean addCheck)
    {
        this.addCheck = addCheck;
    }
    public boolean getAddDataCheck()
    {
        return addCheck;
    }

    public void setNetworkCheck(boolean networkCheck)
    {
        this.networkCheck = networkCheck;
    }
    public boolean getNetworkCheck()
    {
        return networkCheck;
    }

    public void setCameraUse(boolean use) {
        this.use = use;
    }

    public void setChecked(boolean check) {
        this.check = check;
    }
    public boolean getChecked() {
        return check;
    }

    public void setButtonCheck(boolean buttonCheck) {this.buttonCheck = buttonCheck;} // 체크박스
    public boolean getButtonCheck() { return buttonCheck; } // 체크박스

    public void setAdapterPosition(int setPosition)
    {
        this.setPosition = setPosition;
    }

    public void setCheckBox(boolean checkBox)
    {
        this.checkBox = checkBox;
    }
    public boolean getCheckBox()
    {
        return checkBox;
    }

    public void setRegenerative(boolean regenerative) {this.regenerative = regenerative; }
    public boolean getRegenerative() { return regenerative;}

    public void setButton(Button button) {this.button = button;}
    public Button getButton() {return button; }

    public void setCircularImageView(CircularImageView imageView){ this.imageView = imageView; }
    public CircularImageView getCircularImageView(){return imageView;}

    public void setTextView(TextView textView){this.textView = textView;}
    public TextView getTextView() {return textView;}

    public void setDialog(Dialog dialog) {this.dialog = dialog;}
    public Dialog getDialog(){return dialog;}

    public void setBaseAdapter(BaseAdapterActivity adt){this.adt = adt;}
    public BaseAdapterActivity getBaseAdapter(){return adt;}

    public String toString()
    {
        Log.i("toString", "String");

        return getUserName();
    }
}


