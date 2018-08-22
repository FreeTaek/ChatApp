package com.example.jt.android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter
{
    private int tabCount;
    private Chat_Profile_Tab random_chat_tab = new Chat_Profile_Tab();
    private Chat_List_Tab chat_list_tab = new Chat_List_Tab();

    private FragmentTest fragmentTest = new FragmentTest();

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        //Returning the current tabs
        switch (position){
            case 0:
                return random_chat_tab;
            case 1:
                return chat_list_tab;

            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return tabCount;
    }
}
