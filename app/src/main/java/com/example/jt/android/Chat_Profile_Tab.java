package com.example.jt.android;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class Chat_Profile_Tab extends Fragment {

    public Button button;
    public TextView textView;
    public CircularImageView imageView;
    public Dialog dialog;
    public String name;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference storageReference;

    private String my_name;
    private String my_profile;
    private String imagePath = "";

    private File imageFile;

    private DBHandler dbHandler;

    public MenuItem setting_button;
    public MenuItem textItem;

    private LinearLayout layout;

    public Chat_Profile_Tab()
    {
        // ...
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        layout = (LinearLayout) inflater.inflate(R.layout.tab_profile, container, false);

        button = (Button) layout.findViewById(R.id.button);
        textView = (TextView) layout.findViewById(R.id.textView);
        imageView = (CircularImageView) layout.findViewById(R.id.profile_image);
        dialog = new Dialog(UserActivity.getContext(), R.style.MyDialog);

        Chat_Data.getInstance().setButton(button);
        Chat_Data.getInstance().setTextView(textView);
        Chat_Data.getInstance().setCircularImageView(imageView);
        Chat_Data.getInstance().setDialog(dialog);

        dbHandler = new DBHandler(getActivity(), null, null, 1);

        my_name = dbHandler.getMyData("NAME");
        my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

        if(my_profile != null)
        {
            if(!my_profile.equals("None"))
            {
                // 저장된 이미지 있음.
                Log.i("my_profile", "저장된 파일 있음");

                my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

                setProfileImage(my_profile);
            }
            else
            {
                imageView.post(new Runnable() {
                    @Override
                    public void run() {

                        imageView.setImageResource(R.drawable.profile_base);
                    }
                });
            }
        }

        textView.post(new Runnable() {
            @Override
            public void run() {

                textView.setText(my_name);

            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

                if(my_profile != null)
                {
                    Log.e("my_profile =", my_profile);

                    if(!my_profile.equals("None"))
                    {
                        // 저장된 이미지가 있다면
                        Log.i("my_profile", "저장된 파일 있음");

                        Intent intent = new Intent(getActivity(), AllImageActivity.class);

                        intent.putExtra("imageFileName" ,my_profile);
                        intent.putExtra("type", "PROFILE");

                        startActivity(intent);
                    }
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

                Intent intent = new Intent(getActivity(), WaitUserActivity.class);

                intent.putExtra("my_name", my_name);
                intent.putExtra("my_profile", my_profile);

                startActivity(intent);
            }
        });


        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.i("onActivityCreated", "Start");

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i("RandomChatTab", "Start");

        Chat_Data.getInstance().setChecked(false);
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

                Log.i("RandomChatTab - setting", "Clicked");

                CustomDialog dialog = new CustomDialog();

                dialog.callFunction(getActivity());

                return true;

            case R.id.textItem :

                Log.i("RandomChatTab - text", "Clicked");


                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void profile_Image()
    {
        Log.i("profile_Image", "Start");

        String imageFileName = Chat_Data.getInstance().getImageFileName();

        imageFile = Chat_Data.getInstance().getImageFile(imageFileName);
        imagePath = Chat_Data.getInstance().getImagePath();

        if(imagePath != null)
        {
            Log.i("progile_Image", "imagePath Not Null");

            setProfileImage(imageFileName);
        }
        else
        {
            // 데이터 없으면 내려받기, 최초에는 업로드 후 내려 받아야 한다..

            Log.i("progile_Image", "imagePath Null Download Start");

            storageRef = storage.getReference();
            storageReference = storageRef.child("images/" + imageFileName);

            storageReference.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created

                    Log.i("FIie Download", "Success");

                    setProfileImage(imageFileName);

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
    }

    public void dialogProgress()
    {
        Log.e("dialogProgress", "Start");

       try
       {
           dialog = Chat_Data.getInstance().getDialog();

           dialog.setCanceledOnTouchOutside(false);
           dialog.setCancelable(true);
           dialog.addContentView(new ProgressBar(UserActivity.getContext()) ,new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
           dialog.show();

           dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

               @Override
               public void onDismiss(DialogInterface listener) {

               }
           });
       }
       catch (Exception e)
       {
           e.toString();
       }
    }

    public void setProfileImage(String imageFileName)
    {
        Log.i("setProfileImage", "Start");

        imageFile = Chat_Data.getInstance().getImageFile(imageFileName);
        imagePath = Chat_Data.getInstance().getImagePath();

        //dialog.cancel();

        if(Chat_Data.getInstance().getDialog() != null)
        {
            Log.e("dismiss", "Start");

            Chat_Data.getInstance().getDialog().dismiss();
        }

        Glide.with(UserActivity.getContext()).load(imagePath)
                .asBitmap()
                .override(300, 300)
                .fitCenter()
                .centerCrop()
                .into(Chat_Data.getInstance().getCircularImageView());

        dbHandler = new DBHandler(UserActivity.getContext(), null, null, 1);

        my_name = dbHandler.getMyData("NAME");
        my_profile = dbHandler.getMyData("MY_PROFILE_IMAGE");

        if(!my_profile.equals(imageFileName))
        {
            Log.e("데이터 저장 ", "...Start");

            dbHandler.setProfileImage(my_name, imageFileName, "MY_PROFILE");
        }

        Log.i("setProfileImage", "End");
    }
}