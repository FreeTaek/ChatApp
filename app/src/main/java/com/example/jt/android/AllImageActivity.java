package com.example.jt.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;


public class AllImageActivity extends AppCompatActivity {

    private FrameLayout linearLayout;
    private ImageView imageView;
    private Button download;

    private FrameLayout.LayoutParams layoutParams;
    private ViewGroup.LayoutParams viewParams;

    private File imageFile;
    private String imagePath = "";

    private boolean click;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_image);

        linearLayout = (FrameLayout) findViewById(R.id.linearLayout);

        imageView = (ImageView) findViewById(R.id.allimageView);
        download = (Button) findViewById(R.id.download);

        layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();

        String imageFileName = getIntent().getExtras().get("imageFileName").toString();
        String type = getIntent().getExtras().get("type").toString();

        imageFile = Chat_Data.getInstance().getImageFile(imageFileName);
        imagePath = Chat_Data.getInstance().getImagePath();

        setImageView(imagePath);

        download.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                Log.e("저장", "Click");

                Toast.makeText(AllImageActivity.this, "저장하기!!", Toast.LENGTH_SHORT).show();

                PhotoManager photoManager = new PhotoManager();

                photoManager.downloadImageFile(imageFileName, AllImageActivity.this);

            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!type.equals("PROFILE"))
                {
                    if(!click)
                    {
                        click = true;

                        Log.e("imageView onClick", "false 기본");

                        download.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        click = false;

                        Log.e("imageView onClick", "true ");

                        download.setVisibility(View.INVISIBLE);
                    }
                }
                else
                {
                    download.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setImageView(String imagePath)
    {
        Log.e("AliImageVeiew", "setIamgeView Start");

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        int width = bmOptions.outWidth;
        int height = bmOptions.outHeight;

        int width2 = bmOptions.outWidth/2;
        int height2 = bmOptions.outHeight/2;

        int width4 = bmOptions.outWidth/4;
        int height4 = bmOptions.outHeight/4;

        Log.i("imageWidth", String.valueOf(width));
        Log.i("imageHeight", String.valueOf(height));

        Log.i("imageWidth2", String.valueOf(width2));
        Log.i("imageHeight2", String.valueOf(height2));

        Log.i("imageWidth4", String.valueOf(width4));
        Log.i("imageHeight4", String.valueOf(height4));

        // 이미지 크기가 4000이 넘어가면 반으로 줄인 것을 사용한다.

        bmOptions.inJustDecodeBounds = false;

        if(width < 2000 && height < 2000)
        {
            Log.e("width check", String.valueOf(width));
            Log.e("height check", String.valueOf(height));
        }

        if(width > 2000 && width < 4500 && height > 2000 && height < 4500)
        {
            width = width2;
            height = height2;

            Log.e("width check", String.valueOf(width));
            Log.e("height check", String.valueOf(height));
        }

        if(width > 4500 || height > 4500)
        {
            width = width4;
            height = height4;

            Log.e("width check", String.valueOf(width));
            Log.e("height check", String.valueOf(height));
        }

        Glide.with(this).load(imagePath)
                .override(width, height)
                .fitCenter()
                //.centerCrop()
                .into(imageView);

        ImageSizeCheck(width, height); // imageView 재설정..
    }

    private void ImageSizeCheck(int width, int height)
    {
        Log.i("SetImageSizeCheck", "Start");

        int rotate = setExifDegree();

        switch(rotate)
        {
            case 90:

                Log.e("getRotate_switch", "rotate - 90");

                layoutParams.gravity = Gravity.CENTER;

                viewParams = imageView.getLayoutParams();

                viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                imageView.requestLayout();

                break;

            case 180 :

                Log.e("getRotate_switch", "rotate - 180");

                //layoutParams.topMargin = 550;

                layoutParams.gravity = Gravity.CENTER;

                viewParams = imageView.getLayoutParams();

                viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewParams.height = 700;

                imageView.requestLayout();

                 break;


            case 270 :

                Log.e("getRotate_switch", "rotate - 270");

                layoutParams.gravity = Gravity.CENTER;

                viewParams = imageView.getLayoutParams();

                viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                imageView.requestLayout();

                break;

            default:

                if (width == height)
                {
                    Log.e("width == height", "Start");

                    layoutParams.gravity = Gravity.CENTER;

                    viewParams = imageView.getLayoutParams();

                    viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewParams.height = 1000;

                    imageView.requestLayout();

                    break;
                }

                if(width > height)
                {
                    Log.e("width > height", "Start");

                    layoutParams.gravity = Gravity.CENTER;

                    viewParams = imageView.getLayoutParams();

                    viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewParams.height = 700;

                    imageView.requestLayout();

                    break;
                }

                if(width < height)
                {
                    Log.e("width < height", "Start");


                    if(height < 2500)
                    {
                        Log.e("height", "3000 이하");

                        layoutParams.gravity = Gravity.CENTER;

                        viewParams = imageView.getLayoutParams();

                        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                        imageView.requestLayout();
                    }

                   if(height > 2500)
                   {
                       Log.e("height", "3000 이상");

                       layoutParams.gravity = Gravity.CENTER;

                       viewParams = imageView.getLayoutParams();

                       viewParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                       viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

                       imageView.requestLayout();

                       break;
                   }
                }
        }
    }

    private int setExifDegree()
    {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {

            Log.i("ExifInterface", "NUll");

            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = 0;

        exifDegree = exifOrientationToDegrees(exifOrientation);
        Log.i("exifDegree", String.valueOf(exifDegree));

        return exifDegree;
    }

    private int exifOrientationToDegrees(int exifOrientation) {

        Log.i("exifOrientation", "Start");

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

            return 270;
        }

        return 0;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        Log.e("onBackPressed-allImage", "Start");

        Chat_Data.getInstance().setRegenerative(false);

        this.finish();
    }
}
