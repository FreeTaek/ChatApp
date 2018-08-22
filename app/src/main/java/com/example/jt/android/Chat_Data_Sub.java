package com.example.jt.android;

import android.util.Log;

public class Chat_Data_Sub {

    private String name;
    private String msg;
    private String time;

    public Chat_Data_Sub()
    {

    }

    public Chat_Data_Sub(String name, String msg, String time)
    {
        this.name = name;
        this.msg = msg;
        this.time = time;

        Log.e("ChatData", "check ------------------//");

        Log.e("ChatData-name", name);
        Log.e("ChatData-msg", msg);
        Log.e("ChatData-time", time);
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    public void setTime(String time) {this.time = time;}

    public String getName()
    {
        return name;
    }
    public String getMsg()
    {
        return msg;
    }
    public String getTime()
    {
        return time;
    }

}
