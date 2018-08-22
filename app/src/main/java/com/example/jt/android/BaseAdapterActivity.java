package com.example.jt.android;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseAdapterActivity extends RecyclerView.Adapter<BaseAdapterActivity.ViewHolder> {

    private Context context;
    private List<Chat_Data> items;
    private int item_layout;
    private int checkPosition;
    private ArrayList arrayList = new ArrayList();
    private DBHandler dbHandler;

    private Handler mHandler;
    private Runnable mRunnable;
    private int seconds =1000;

    private String imageFileName;
    private String imageFilePath;
    private File imageFile;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference storageReference;

    private int items_size = 0;


    public BaseAdapterActivity(Context context, List<Chat_Data> items, int item_layout) {
        this.context=context;
        this.items=items;
        this.item_layout=item_layout;
    }

    @Override
    public BaseAdapterActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list, null);

        return new ViewHolder(v);

    }

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    @Override
    public void onBindViewHolder(BaseAdapterActivity.ViewHolder holder, int position) {

        Log.i("onBindBiewHolder", "Start");

        final int eposition = position;

        holder.chat_name.setText(items.get(position).getName());
        holder.chat_text.setText(items.get(position).getMsg());
        holder.chat_time.setText(items.get(position).getTime());

        if(!items.get(position).getBadgeCount().equals("0"))
        {
            Log.i("BaseAdapter", "badgeCount"+items.get(position).getBadgeCount());

            holder.chat_badge.setText(items.get(position).getBadgeCount());
        }
        else
        {
            Log.i("BaseAdapter", "badgeCount 0 표시 x");

            holder.chat_badge.setVisibility(View.INVISIBLE);
        }

        if(!items.get(position).getProfileImage().equals("None"))
        {
            Log.e("BaseAdapter", "profile_image -> Not Null");

            imageFileName = items.get(position).getProfileImage();

            storageRef = storage.getReference();
            storageReference = storageRef.child("images/" + imageFileName);

            imageFile = items.get(position).getImageFile(imageFileName); // msg로 파일 체크, 없으면 생성.
            imageFilePath = items.get(position).getImagePath(); // 체크한 파일 경로 가져오기.

            if(imageFilePath != null)
            {
                // 내가 찍은 사진. 내부에 존재

                Log.e("BaseAdapter", "FBDwonload start");

                Chat_Data.getInstance().setImageFileCheck(false);

                internalStorageImage(holder, position);
            }
            else
            {
                // 상대방에게 받은 사진. fb에서 다운로드

                firebaseImageDownload(holder, position);

                Log.e("BaseAdapter", "imageFIle null");
            }
        }

        if(Chat_Data.getInstance().getCheckBox())
        {
            // checkBox Visible

            holder.chat_time.setVisibility(View.GONE);

            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.VISIBLE);

            holder.chat_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(holder.checkBox.isChecked())
                    {
                        Log.i("BaseAdater", "isChecked normal");

                        Log.i("BaseAdapter-name = ",items.get(position).getName());

                        String name = items.get(position).getName();

                        arrayList.remove(name);

                        Log.i("BaseAdapter-arrayList", " = " + String.valueOf(arrayList.size()));

                        holder.checkBox.setChecked(false);
                    }
                    else
                    {
                        Log.i("BaseAdater", "isChecked checked ");

                        Log.i("BaseAdapter-name = "," = " + items.get(position).getName());

                        String name = items.get(position).getName();

                        arrayList.add(name);

                        Chat_Data.getInstance().setArrayList(arrayList);

                        Log.i("BaseAdapter-arrayList", "checkd" +  String.valueOf(arrayList.size()));

                        holder.checkBox.setChecked(true);
                    }
                }
            });
        }
        else
        {
            // checkBox InVisible

            Log.i("setChecked", "false");

            holder.checkBox.setVisibility(View.GONE);
            holder.chat_time.setVisibility(View.VISIBLE);
        }
    }


    public void internalStorageImage(BaseAdapterActivity.ViewHolder holder, int position)
    {
        Log.i("internalStorageImage", "Start");

        imageFile = items.get(position).getImageFile(imageFileName);
        imageFilePath = items.get(position).getImagePath();

        setCameraImage(holder);
    }


    private void firebaseImageDownload(final BaseAdapterActivity.ViewHolder holder, final int position)
    {
        Log.i("FirebaseImageDownload", "Start");

        Log.e("imageFile", String.valueOf(imageFile));

        storageReference.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created

                Log.i("FIie Download", "Success");

                imageFile = items.get(position).getImageFile(imageFileName); // // 다시 한번 더 검색 함으로써
                imageFilePath = items.get(position).getImagePath(); // 다운로드 된 파일을 확인한다.

                setCameraImage(holder);

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


    private void setCameraImage(BaseAdapterActivity.ViewHolder holder) // centerCrop okkk // Camera image
    {
        Log.e("setCameraImage", "Start");

        Glide.with(context.getApplicationContext()).load(imageFilePath)
                .asBitmap()
                .override(300, 300)
                .fitCenter()
                .centerCrop()
                .into(holder.imageView);

        Log.e("setCameraImage", "End");
    }


    @Override
    public int getItemCount() {

        items_size = items.size();

        return this.items.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView chat_name;
        TextView chat_text;
        TextView chat_badge;
        TextView chat_time;

        CheckBox checkBox;
        CircularImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            chat_name = (TextView)itemView.findViewById(R.id.chat_name);
            chat_text = (TextView)itemView.findViewById(R.id.chat_text);
            chat_badge = (TextView)itemView.findViewById(R.id.badge);
            chat_time = (TextView)itemView.findViewById(R.id.time);
            imageView = (CircularImageView)itemView.findViewById(R.id.imageView);
            checkBox = (CheckBox)itemView.findViewById(R.id.checkbox);
        }
    }
}


