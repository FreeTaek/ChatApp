
package com.example.jt.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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



public class AdapterActivity extends RecyclerView.Adapter<AdapterActivity.ViewHolder> {

    public static Context context;
    public static ArrayList<String> arrayList = new ArrayList<>();

    private List<Chat_Data> items;
    private int item_layout;
    private String name;
    private String yyyyMMdd;
    private String time;
    private String time_data;

    private String data;
    private String imageFilePath;
    private File imageFile;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference storageReference;

    private int imageWidth = 0;
    private int imageHeight = 0;
    private int rotate = 0;

    private boolean imageSizeCheck;
    private boolean imageFileCheck;

    private Bitmap bitmap;
    private DBHandler dbHandler;

    private int position = 0;
    private int items_size = 0;


    public AdapterActivity(Context context, List<Chat_Data> items, int item_layout, String name) {

        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
        this.name = name;
    }

    public int getItemViewType(int position) {

        if (items.get(position).getName().equals(name)) {

            return 1;

        } else {

            return 2;
        }
    }

    @Override
    public AdapterActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;

        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items_user, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterActivity.ViewHolder holder, int position) {

        //holder.setIsRecyclable(false);

        Log.i("onBindBiewHolder", "Start");

        this.position = position;

        data = items.get(position).getMsg();

        time_data = items.get(position).getTime();

        Log.e("AdapterActivity ", "time_data = "+time_data);

        if(!TextUtils.isEmpty(time_data))
        {
            Log.e("isEmpty is", "true");

            String arr [] = time_data.split("/");

            String yyyy = arr[0];
            String MM = arr[1];
            String dd = arr[2];

            yyyyMMdd = yyyy+"/"+MM+"/"+dd;

            time = arr[3];

            Log.e("yyyyMMdd - 1", yyyyMMdd);
        }
        else
        {
            Log.e("isEmpty is", "false");
            Log.e("yyyyMMdd - 2", yyyyMMdd);
        }

        addChatData(name, data, holder, position); // 데이터 DB 저장.

        // ------- 프로필 이미지

        if(!Chat_Data.getInstance().getProfileImage().contains("None"))
        {
            imageFile = items.get(position).getImageFile(data);
            imageFilePath = items.get(position).getImagePath();

            setCircularImage(holder, imageFilePath);
        }
        else
        {
            holder.circleImage.setImageResource(R.drawable.profile_base);
        }


        // ------- 이미지 및 텍스트 구별

        if(!data.contains("qweasdqwesadfdsa"))
        {
            // msg
            Chat_Data.getInstance().setChecked(false);

            holder.msg.setText(items.get(position).getMsg());
            holder.time.setText(time);

            holder.msg.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);
            holder.rectangle.setVisibility(View.VISIBLE);

            holder.imageView.setVisibility(View.GONE);
            holder.image_time.setVisibility(View.GONE);
            holder.imageForm.setVisibility(View.GONE);
        }
        else
        {
            holder.msg.setVisibility(View.GONE);
            holder.time.setVisibility(View.GONE);
            holder.rectangle.setVisibility(View.GONE);

            imageFile = items.get(position).getImageFile(data); // msg로 파일 체크, 없으면 생성.
            imageFilePath = items.get(position).getImagePath(); // 체크한 파일 경로 가져오기.

            if(imageFilePath != null)
            {
                // 내가 찍은 사진. 내부에 존재

                Log.i("imageFilePath", "Not Null");

                internalStorageImage(holder, position);
            }
            else
            {
                Log.i("imageFilePath", "Null");

                Chat_Data.getInstance().setImageFileCheck(false);
            }
        }
    }

    private void addChatData(String name, String msg, ViewHolder holder, int position)
    {
        try
        {
            Log.i("Adpater-addChatData", "Start");

            dbHandler = new DBHandler(context, null, null, 1);

            String user_name = items.get(position).getUserName();

            ArrayList<HashMap<String, String>> arrayList;

          if(Chat_Data.getInstance().getAdapterCheck())
          {
              // true 시 실행 -> list를 통해 들어온 경우.

              // 또는 두 번째 데이터가 들어온 경우.

              arrayList = dbHandler.findChatData(user_name);

              int count = arrayList.size(); // 최초 데이터 중첩 저장 막음용.// <- db에 저장된 item 갯수.

              if(items_size>count) //<-
              {
                  Log.e("adapter-addChatData", "The data is saved");

                  imageForm(holder);

                  String addTime="";

                  if(dbHandler.getLastTime(user_name).length() != 0)
                  {
                      Log.e("TEST = getLastTimeCheck", "Start");

                      // 현재 시간
                      String arr[] = getTime().split("/");
                      String yyyy = arr[0];
                      String MM = arr[1];
                      String dd = arr[2];

                      //마지막으로 저장된 데이터 시간
                      String last_time = dbHandler.getLastTime(user_name);
                      String last_arr[] = last_time.split("/");
                      String last_yyyy = last_arr[0];
                      String last_MM = last_arr[1];
                      String last_dd = last_arr[2];

                      Log.e("LastTime = ", last_yyyy+"/"+last_MM+"/"+last_dd);

                      if(Integer.parseInt(yyyy+MM+dd) > Integer.parseInt(last_yyyy+last_MM+last_dd))
                      {
                          holder.linearLayout.setVisibility(View.VISIBLE);

                          holder.time_line.setText(yyyy+"년 "+MM+"월 "+dd+"일");

                          addTime = yyyy+"/"+MM+"/"+dd;
                      }
                      else
                      {
                          Log.e("linearLayout =", "GONE");

                          holder.linearLayout.setVisibility(View.GONE);

                          addTime = yyyyMMdd;

                          Log.e("addTime 1 =", addTime);
                      }
                  }
                  else
                  {
                      addTime = yyyyMMdd;

                      Log.e("addTime 11 =", addTime);
                  }

                  Log.e("addTime 2 =", addTime);

                  dbHandler.addChatData(user_name, name, msg, addTime);

                  Chat_Data.getInstance().setTimeLineCheck(true);
              }
              else
              {
                  Log.i("recycle", "재활용 중 저장 X");

                  Chat_Data.getInstance().setTimeLineCheck(false);
              }
          }
          else
          {
              // false -> 처음 상대방과 대화하는 경우.

              Log.i("Adapter-check", "Adapter 바로저장");

              dbHandler.addChatData(user_name, name, msg, yyyyMMdd);

              Chat_Data.getInstance().setTimeLineCheck(true);
              Chat_Data.getInstance().setAdapterCheck(true);
          }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void imageForm(ViewHolder holder)
    {
        if(data.contains("qweasdqwesadfdsa"))
        {
            Log.e("imageForm", "Start");

            imageFile = items.get(position).getImageFile(data); // msg로 파일 체크, 없으면 생성.
            imageFilePath = items.get(position).getImagePath(); // 체크한 파일 경로 가져오기.

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFilePath, bmOptions);

            int width = bmOptions.outWidth;
            int height = bmOptions.outHeight;

            bmOptions.inJustDecodeBounds = false;

            // -----

            Log.e("imageForm width", String.valueOf(width));
            Log.e("imageForm height", String.valueOf(height));

            RelativeLayout.LayoutParams layoutParams;

            if(height < 500)
            {
                Log.e("500","이하");

                height = 550;
            }
            else if(height >= 500)
            {
                Log.e("500","이상");

                if(2000>height)
                {
                    Log.e("width > height", "height = 600");

                    height = 550;
                }
                else if(4000 < height)
                {
                    Log.e("width < height", "height = 1000");

                    height = 1000;
                }
                else if(width == height)
                {
                    Log.e("width == height", "height = 700");

                    height = 700;
                }
            }

            holder.imageForm.setVisibility(View.INVISIBLE);

            holder.imageForm.getLayoutParams().width = width;
            holder.imageForm.getLayoutParams().height = height;
            holder.imageForm.requestLayout();
        }
        else
        {
            holder.imageForm.setVisibility(View.GONE);
        }
    }



    private String getTime()
    {
        long now = System.currentTimeMillis();

        Date date = new Date(now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        String getTime = sdf.format(date);

        return getTime;
    }


    private void internalStorageImage(ViewHolder holder, int position)
    {
        Log.i("internalStorageImage", "Start");

        int exifDegree = items.get(position).getRotate();

        if(exifDegree != 0)
        {
            Log.i("setCameraImage", "Start");

            getRotate(exifDegree, holder);
            setCameraImage(holder);
        }
        else
        {
            Log.i("exifDegree", "0 -> SetImageCheck start");

            // album 의 경우 rotate를 구할 수 없다.

            SetImageSizeCheck(holder);
        }
    }

    private void setCircularImage(ViewHolder holder, String imageFilePath)
    {
        Log.e("setCircularImage", "Start");

        Glide.with(context.getApplicationContext()).load(imageFilePath)
                .asBitmap()
                .override(300, 300)
                .fitCenter()
                .centerCrop()
                .into(holder.circleImage);
    }


    private void setCameraImage(ViewHolder holder) // centerCrop okkk // Camera image
    {
        //holder.imageView.setImageBitmap(bitmap);

        Log.i("setCameraImage", "Start");

        Glide.with(context.getApplicationContext()).load(imageFilePath)
                .override(imageWidth, imageHeight)
                .fitCenter()
                .centerCrop()
                .into(holder.imageView);

        holder.imageView.setVisibility(View.VISIBLE);
        holder.image_time.setVisibility(View.VISIBLE);
        holder.image_time.setText(time);

        Chat_Data.getInstance().setChecked(false);

        Log.i("setCameraImage", "End");
    }

    private void setAlbumImage(ViewHolder holder) // centerCrop xxxxxx // album image width, height 1000, 800 이하.
    {
        Log.i("setAlbumImage", "Start");

        Glide.with(context.getApplicationContext()).load(imageFilePath)
                .override(700, 700)
                .fitCenter()
                //.centerCrop()
                .into(holder.imageView);

        holder.imageView.setVisibility(View.VISIBLE);
        holder.image_time.setVisibility(View.VISIBLE);
        holder.image_time.setText(time);

        Chat_Data.getInstance().setChecked(false);
    }


    private void SetImageSizeCheck(ViewHolder holder)
    {
        Log.i("SetImageSizeCheck", "Start");

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFilePath, bmOptions);

        int width = bmOptions.outWidth;
        int height = bmOptions.outHeight;

        Log.i("imageWidth", String.valueOf(width));
        Log.i("imageHeight", String.valueOf(height));

        bmOptions.inJustDecodeBounds = false;


        if(width < 2000 && height < 2000)
        {
            // width  height가 1000 이하일 경우.

            Log.i("setAlbum", "Start");

            if(width == height)
            {
                Log.i("2000 이하", "width == height");

                imageWidth = 600;
                imageHeight = 500;

                setCameraImage(holder);

                return;
            }

            if(width > height)
            {
                imageWidth = 800;
                imageHeight = 500;
            }
            else if(width < height)
            {
                imageWidth = 700;
                imageHeight = 1000;
            }

            setAlbumImage(holder);
        }
         else if(width > 2000 || height > 2000)
        {
            // width , heigth 둘 중 하나가 2000 이상일 경우.

            if(width ==  height)
            {
                imageWidth = 800;
                imageHeight = 500;

                Log.i("setCamera", "width = "+String.valueOf(imageWidth));
                Log.i("setCamera", "heigth = "+String.valueOf(imageHeight));

                setCameraImage(holder);

                Log.i("값 같음", " 종료");

                return;
            }

            if(width > height)
            {
                imageWidth = 800;
                imageHeight = 500;
            }
            else if(width < height)
            {
                imageWidth = 700;
                imageHeight = 1000;
            }

            Log.i("setCamera", "Start");

            Log.i("setCamera", "width = "+String.valueOf(imageWidth));
            Log.i("setCamera", "heigth = "+String.valueOf(imageHeight));

            setCameraImage(holder);
        }
    }

    private void getRotate(int rotate, ViewHolder holder)
    {
        switch(rotate)
        {
            case 90 :

                Log.i("getRotate_switch", "rotate - 90");

                imageWidth = 700;
                imageHeight = 1000;
                break;

            case 180 :

                Log.i("getRotate_switch", "rotate - 180");

                imageWidth = 800;
                imageHeight = 500;
                break;

            case 270 :

                Log.i("getRotate_switch", "rotate - 270");

                imageWidth = 800;
                imageHeight = 500;
                break;

            case 0 :

                SetImageSizeCheck(holder);

                Log.i("getRotate_switch", "rotate - 0");
        }

        Log.i("imageWidth", String.valueOf(imageWidth));
        Log.i("imageHeight", String.valueOf(imageHeight));
    }


    @Override
    public int getItemCount()
    {
        Log.i("items-size", String.valueOf(items.size()));

        items_size = items.size();

        return this.items.size();
    }

    @Override
    public long getItemId(int position)
    {
        super.getItemId(position);

        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView msg;
        TextView time;
        TextView image_time;

        TextView time_line;
        LinearLayout linearLayout;

        CircularImageView circleImage;
        ImageView imageView;
        ImageView rectangle;

        ImageView imageForm;
        RelativeLayout relative;

        private ViewHolder(View itemView) {
            super(itemView);

            relative = (RelativeLayout)itemView.findViewById(R.id.relative);
            imageForm = (ImageView)itemView.findViewById(R.id.form);

            msg = (TextView)itemView.findViewById(R.id.chat_name);
            time = (TextView)itemView.findViewById(R.id.time);
            image_time = (TextView)itemView.findViewById(R.id.image_time);

            time_line = (TextView)itemView.findViewById(R.id.time_line);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);

            this.circleImage = (CircularImageView)itemView.findViewById(R.id.circle_image);
            this.imageView = (ImageView)itemView.findViewById(R.id.imageView);

            rectangle = (ImageView)itemView.findViewById(R.id.rectangle);

            this.circleImage.setOnClickListener(this);
            this.imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView)
        {
            Log.e("AdapterActivity", "itemsView = "+String.valueOf(itemView.getId()));


            if(itemView.getId() == imageView.getId())
            {
                Log.e("AdapterActivity", "imageView Clicked");

                int position = getAdapterPosition();

                arrayList = Chat_Data.getInstance().getPositionData();

                if(arrayList.get(position).contains("qweasdqwesadfdsa"))
                {
                    if(!Chat_Data.getInstance().getRegenerative()) // 액티비티 재생성 방지.
                    {
                        Chat_Data.getInstance().setRegenerative(true);

                        Intent intent = new Intent(context, AllImageActivity.class);
                        intent.putExtra("imageFileName", arrayList.get(position));
                        intent.putExtra("type", "PICTURE");

                        context.startActivity(intent);
                    }
                }
                else
                {
                    Log.e("AdapterActivity", "image key 포함 안되어있음");
                }
            }

            if(itemView.getId() == circleImage.getId())
            {
               Log.e("AdapterActivity", "CircleimageView Clicked");

                arrayList = Chat_Data.getInstance().getPositionData();

                if(!Chat_Data.getInstance().getProfileImage().contains("None"))
                {
                    if(!Chat_Data.getInstance().getRegenerative()) // 액티비티 재생성 방지.
                    {
                        Chat_Data.getInstance().setRegenerative(true);

                        Intent intent = new Intent(context, AllImageActivity.class);
                        intent.putExtra("imageFileName", Chat_Data.getInstance().getProfileImage());
                        intent.putExtra("type", "PICTURE");

                        context. startActivity(intent);
                    }
                }
                else
                {
                    Log.e("AdapterActivity", "circle image key 포함 안되어있음");
                }
            }
        }
    }
}
