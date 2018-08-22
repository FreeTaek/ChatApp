package com.example.jt.android;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

public class DBHandler extends SQLiteOpenHelper {


    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "chatRoom";

    private String LOGIN = "login";
    private String CHAT = "chat";
    private String ROOM = "room";
    private String DATA = "data";

    private String CHAT_LIST = "chat_list";
    private String CHAT_ROOM = "chat_room";
    private String CHAT_NAME = "chat_name";
    private String CHAT_TEXT = "chat_text";
    private String CHAT_KEY = "chat_key";
    private String CHAT_TOKEN = "chat_token";
    private String CHAT_PROFILE_IMAGE = "chat_profile_image";
    private String CHAT_BADGE = "chat_badge";
    private String CHAT_TIME = "chat_time";

    private String MY_NAME = "my_name";
    private String MY_TOKEN = "my_token";
    private String MY_PROFILE_IMAGE = "my_profile_image";

    public ArrayList<String> arrayList;
    public ArrayList<Integer> deleteList;

    private String room_name;
    private String chat_text;



    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String DATA_TABLE =

                "CREATE TABLE " +

                        DATA + "(" +

                        CHAT_LIST + " INTEGER PRIMARY KEY, " +

                        MY_NAME + " TEXT, " + // my_name
                        MY_TOKEN + " TEXT, " + // my_token
                        MY_PROFILE_IMAGE + " TEXT" + ")"; // user profile_image name

        db.execSQL(DATA_TABLE);

        // CHAT DATA
        String CREATE_CHAT_TABLE =

                "CREATE TABLE " +

                 CHAT + "(" +

                        CHAT_LIST + " INTEGER PRIMARY KEY, " +

                        CHAT_ROOM + " TEXT," +

                        CHAT_NAME + " TEXT," +

                        CHAT_TEXT + " TEXT," +

                        CHAT_TIME + " TEXT" + ")";

        db.execSQL(CREATE_CHAT_TABLE);


        // ROOM DATA
        String CHAT_ROOM_TABLE =

                "CREATE TABLE " +

                        ROOM + "(" +

                        CHAT_LIST + " INTEGER PRIMARY KEY, " +

                        CHAT_NAME + " TEXT, " + // room_name(=user_name)

                        CHAT_TEXT + " TEXT, " +

                        CHAT_KEY + " TEXT, " +

                        CHAT_TOKEN + " TEXT, " + // user_token

                        CHAT_PROFILE_IMAGE + " TEXT, " +

                        CHAT_BADGE + " TEXT, " +

                        CHAT_TIME + " TEXT" + ")"; // profile image

        db.execSQL(CHAT_ROOM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + DATA);
        db.execSQL("DROP TABLE IF EXISTS " + CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + ROOM);

        onCreate(db);
    }

    public void setMyData(String name, String token, String profile_image)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MY_NAME, name); // user name
        contentValues.put(MY_TOKEN, token);
        contentValues.put(MY_PROFILE_IMAGE, profile_image);

        SQLiteDatabase sqLiteDatabase =this.getWritableDatabase();

        sqLiteDatabase.insert(DATA, null, contentValues);
        sqLiteDatabase.close();
    }

    public String getMyData(String type)
    {
        String query = "SELECT * FROM " + DATA;

        String data = "";


        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);


        if(cursor.moveToFirst()) {

            switch(type)
            {
                case "NAME" : data = cursor.getString(cursor.getColumnIndex("my_name")); break;

                case "TOKEN" :  data = cursor.getString(cursor.getColumnIndex("my_token")); break;

                case "MY_PROFILE_IMAGE" :  data = cursor.getString(cursor.getColumnIndex("my_profile_image")); break;

                default: Log.i("DBHandler-getMyData", "DEFAULT");
            }
        }

        cursor.close();

        sqLiteDatabase.close();

        return data;
    }

    public void setUserData(String userName, String token, String key)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(CHAT_NAME, userName); // user name
        contentValues.put(CHAT_TOKEN, token); // user name
        contentValues.put(CHAT_KEY, key); // user name

        SQLiteDatabase sqLiteDatabase =this.getWritableDatabase();

        sqLiteDatabase.insert(DATA, null, contentValues);
        sqLiteDatabase.close();
    }

    public String getUserData(String name, String type)
    {
        //String query = "SELECT * FROM " + DATA + " WHERE " + CHAT_NAME + " = '"+ name +"'";

        String query = "SELECT * FROM " + ROOM + " WHERE " + CHAT_NAME + " = '"+ name +"'";

        String data = "";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Log.i("getUserData", DatabaseUtils.dumpCursorToString(cursor));

        if(cursor.moveToFirst()) {

            switch(type)
            {
                case "NAME" : data = cursor.getString(cursor.getColumnIndex("chat_name")); break;

                case "KEY" : data = cursor.getString(cursor.getColumnIndex("chat_key")); break;

                case "TOKEN" : data = cursor.getString(cursor.getColumnIndex("chat_token")); break;

                case "PROFILE_IMAGE" : data = cursor.getString(cursor.getColumnIndex("chat_profile_image")); break;

                default: Log.i("DBHandler", "DEFAULT");
            }
        }

        cursor.close();

        sqLiteDatabase.close();

        Log.e("DBHandler - key", data);

        return data;
    }

    public void setProfileImage(String name, String imageFileName, String type)
    {
        Log.e("setProfileImage - name", name);
        Log.e("setProfileImage - file", imageFileName);

        ContentValues contentValues = new ContentValues();
        SQLiteDatabase sqLiteDatabase =this.getWritableDatabase();

        switch(type)
        {
            case "MY_PROFILE" :

                contentValues.put(MY_PROFILE_IMAGE, imageFileName);

                sqLiteDatabase.update(DATA, contentValues, MY_NAME + " = ?", new String[]{name}); break;

            case "USER_PROFILE" :

                contentValues.put(CHAT_PROFILE_IMAGE, imageFileName);

                sqLiteDatabase.update(ROOM, contentValues, CHAT_NAME + " = ?", new String[]{name}); break;

            default : Log.i("setProfileImage", "DEFAULT");
        }

        sqLiteDatabase.close();
    }

    public ArrayList<Integer> getBadgeCount(String name)
    {
        String query = "";

        if(!TextUtils.isEmpty(name))
        {
            // 데이터 들어있음.

            Log.i("getBadgeCount", "name is not zero");

            query = "SELECT * FROM " + ROOM + " WHERE " + CHAT_NAME + " = '"+ name +"'";
        }
        else
        {
            // 데이터 안들어있음.

            Log.i("getBadgeCount", "name is zero");

            query = "SELECT * FROM " + ROOM;
        }

        ArrayList<Integer> count = new ArrayList<Integer>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Log.i("getBadgeCount", DatabaseUtils.dumpCursorToString(cursor));

        if(cursor.moveToFirst())
        {
            int badge = cursor.getInt(cursor.getColumnIndex("chat_badge"));

            count.add(badge);

            //cursor.moveToNext();
        }

        Log.i("getBadgeCount", String.valueOf(count));

        cursor.close();

        sqLiteDatabase.close();

        return count;
    }

    public String getLastTime(String name)
    {
        Log.e("getLastTime Start", "Start");
        Log.e("getLastTime name =", name);

        String query = "";

        query = "SELECT * FROM " + CHAT + " WHERE " + CHAT_ROOM + " = '"+ name +"'";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Log.i("getLastTime ", DatabaseUtils.dumpCursorToString(cursor));

        String time = "";

        if(cursor.moveToFirst())
        {
            time = cursor.getString(cursor.getColumnIndex("chat_time"));
        }

        cursor.close();

        sqLiteDatabase.close();

        return time;
    }

     public ArrayList<HashMap<String,String>> findRoomData() {

        String query = "SELECT * FROM " + ROOM;

        //String query = "SELECT * FROM " + ROOM + " WHERE " + CHAT_ROOM + " = '"+ room_name +"'";

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Log.i("findRoomData", DatabaseUtils.dumpCursorToString(cursor));

        Log.i("cursor_true", "true");

         if(cursor.moveToFirst())
         {
             int count = cursor.getCount();

             for(int i = 0; count>i; i++)
             {
                 String chat_name = cursor.getString(cursor.getColumnIndex("chat_name"));
                 String chat_text = cursor.getString(cursor.getColumnIndex("chat_text"));
                 String chat_key = cursor.getString(cursor.getColumnIndex("chat_key"));
                 String chat_token = cursor.getString(cursor.getColumnIndex("chat_token"));
                 String chat_profile_image = cursor.getString(cursor.getColumnIndex("chat_profile_image"));
                 int chat_badge = cursor.getInt(cursor.getColumnIndex("chat_badge"));
                 String chat_time = cursor.getString(cursor.getColumnIndex("chat_time"));

                 HashMap<String, String> map = new HashMap<String, String>();

                 map.put("chat_name", chat_name);
                 map.put("chat_text", chat_text);
                 map.put("chat_key", chat_key);
                 map.put("chat_token", chat_token);
                 map.put("chat_profile_image", chat_profile_image);
                 map.put("chat_badge", String.valueOf(chat_badge));
                 map.put("chat_time", chat_time);

                 arrayList.add(map);

                 cursor.moveToNext();
             }
         }

         cursor.close();

         sqLiteDatabase.close();

         return arrayList;
    }


    public ArrayList<HashMap<String, String>> findChatData(String room_name)
    {
        Log.i("findChatData", "Start");

        Log.i("room_name", room_name);


        String query = "SELECT * FROM " + CHAT + " WHERE " + CHAT_ROOM + " = '"+ room_name +"'";

        //query = "SELECT * FROM " + CHAT + " WHERE " + CHAT_ROOM + " LIKE " + " '"+ room_name +"'";

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Log.i("Chat Data", DatabaseUtils.dumpCursorToString(cursor));

        if(cursor.moveToFirst())
        {
            int count = cursor.getCount();

            Log.i("count", String.valueOf(count));

            for(int i = 0; i < count; i++)
            {
                String name = cursor.getString(cursor.getColumnIndex("chat_name"));
                String text = cursor.getString(cursor.getColumnIndex("chat_text"));

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("chat_name", name);
                map.put("chat_text", text);

                arrayList.add(map);

                cursor.moveToNext();
            }

            Log.i("arrayList", String.valueOf(arrayList.size()));
        }
        else
        {
            Log.i("chat", "Error");
        }


        cursor.close();

        sqLiteDatabase.close();

        return arrayList;
    }

    public void addRoomData(String user_name, String msg, String key, String token, String profile_Image, int badge, String time)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(CHAT_NAME, user_name); // 방 이름
        contentValues.put(CHAT_TEXT, msg); // 채팅 내용
        contentValues.put(CHAT_KEY, key);
        contentValues.put(CHAT_TOKEN, token); // 방 이름
        contentValues.put(CHAT_PROFILE_IMAGE, profile_Image); // 채팅 내용
        contentValues.put(CHAT_BADGE, badge); // 채팅 내용
        contentValues.put(CHAT_TIME, time); // 채팅 내용

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.insert(ROOM, null, contentValues);
        sqLiteDatabase.close();
    }

    public void addChatData(String user_name, String name, String msg, String time)
    {
        Log.i("addChatData", "Start");

        ContentValues contentValues = new ContentValues();

        //contentValues.put(CHAT_KEY, chat_room.getKey());
        contentValues.put(CHAT_ROOM, user_name); // DB 방 이름
        contentValues.put(CHAT_NAME, name);
        contentValues.put(CHAT_TEXT, msg);
        contentValues.put(CHAT_TIME, time);

        Log.i("getUserName", user_name);
        Log.i("getName", name);
        Log.i("getMsg", msg);
        Log.i("getTime", time);


        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.insert(CHAT, null, contentValues);
        sqLiteDatabase.close();

        //checkData();
    }


    public void updateRoomData(String user_name, String msg, String key, String token, String profile_Image, int badge, String time)
    {
        Log.i("DBHandler ->", "updateRoomData Start");

        ArrayList<String> arrayList = new ArrayList<String>();

        arrayList.add(user_name);

        deleteRoomData(arrayList);

        addRoomData(user_name, msg, key, token, profile_Image, badge, time);
    }



    public void deleteRoomData(ArrayList arrayList)
    {
        Log.i("DBHandler ->", "deleteRoomData Start");

        String query = "SELECT * FROM " + ROOM;

        Cursor cursor = null;

        String item = "";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Iterator<String> iterator = arrayList.iterator();

        while(iterator.hasNext()) {

            item = iterator.next();

            Log.i("item", item);

            cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                //sqLiteDatabase.delete(ROOM, CHAT_LIST + " =?", new String[]{ });

                sqLiteDatabase.delete(ROOM, CHAT_NAME + " = " + "'" + item + "'", new String[]{});

                cursor.moveToNext();
            }

        }

        cursor.close();

        sqLiteDatabase.close();
    }


    public void deleteChatData(ArrayList arrayList)
    {
        Log.i("DBHandler ->", "deleteChatData Start");

        String query = "SELECT * FROM " + CHAT;

        String item = "";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        Iterator<String> iterator = arrayList.iterator();

        while(iterator.hasNext()) {

            item = iterator.next();

            Log.i("item", item);

            if (cursor.moveToFirst()) {
                //sqLiteDatabase.delete(ROOM, CHAT_LIST + " =?", new String[]{ });

                sqLiteDatabase.delete(CHAT, CHAT_ROOM + " = " + "'" + item + "'", new String[]{});

                cursor.moveToNext();
            }
        }

        //Log.i("deleteChatData", DatabaseUtils.dumpCursorToString(cursor));

        cursor.close();

        sqLiteDatabase.close();
    }

    public void deleteUserChatKey(ArrayList arrayList)
    {
        Log.i("DBHandler ->", "deleteUserCheckKey Start");

        Iterator<String> iterator = arrayList.iterator();

        Cursor cursor = null;
        SQLiteDatabase sqLiteDatabase = null;
        String query = "";

        String name = "";

        while(iterator.hasNext())
        {
            name = iterator.next();

            //query = "SELECT * FROM " + DATA + " WHERE " + CHAT_KEY + " LIKE " + "'"+ name +"%'";

            query = "SELECT * FROM " + DATA;

            sqLiteDatabase = this.getWritableDatabase();

            cursor = sqLiteDatabase.rawQuery(query, null);


            if(cursor.moveToFirst())
            {
                sqLiteDatabase.delete(DATA, CHAT_NAME + " = " + "'" + name + "'", new String[]{});

                cursor.moveToNext();
            }
        }

        cursor.close();

        sqLiteDatabase.close();
    }

}