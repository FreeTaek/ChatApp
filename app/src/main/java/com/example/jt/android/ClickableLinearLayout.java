package com.example.jt.android;


import android.content.Context;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;


public class ClickableLinearLayout extends LinearLayout implements Checkable{

    public ClickableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // mIsChecked = false ;
    }

    @Override
    public boolean isChecked() {

        CheckBox cb = (CheckBox) findViewById(R.id.checkbox) ;

        return cb.isChecked() ;
        // return mIsChecked ;
    }

    @Override
    public void toggle() {
        CheckBox cb = (CheckBox) findViewById(R.id.checkbox) ;

        setChecked(cb.isChecked() ? false : true) ;
        // setChecked(mIsChecked ? false : true) ;
    }

    @Override
    public void setChecked(boolean checked) {
        CheckBox cb = (CheckBox) findViewById(R.id.checkbox) ;

        if (cb.isChecked() != checked) {
            cb.setChecked(checked) ;
        }

        // CheckBox 가 아닌 View의 상태 변경.
    }
}
