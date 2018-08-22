package com.example.jt.android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoManager extends AppCompatActivity {

    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_ALBUM = 2;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference storageReference;

    private Context context;

    private String my_name;
    private String my_token;
    private String user_name;
    private String user_token;
    private String key;
    private String text;
    private String list_msg;
    private String token;
    private String fbKey;

    public String mCurrentPhotoPath;
    public String mCurrentAlbumPath;;

    private String imageFileName;
    private String albumFileName;

    private String getImageFileName;
    private String getAlbumFileName;

    private Uri contentUri;
    private File imageFile;

    private Button btn_send_msg;
    private ImageButton imageButton;
    private ImageButton imageFrame;
    private ImageButton imageFrame2;
    private ImageButton imageFrame3;
    private ImageButton imageFrame4;
    private ImageButton imageFrame5;

    private EditText input_msg;
    private TextView chat_conversation;

    private List<Chat_Data> items;
    private AdapterActivity adt;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private Chat_Data chat_room;
    private Chat_Data_Sub chatData;

    private boolean del = true;
    private boolean del2 = true;
    private boolean check;
    private boolean data_check;

    private boolean cameraUsedCheck;
    private boolean cameraCheck;
    private boolean albumCheck;

    private InputMethodManager imm;
    private DBHandler dbHandler;
    private ArrayList<HashMap<String, String>> mapList;
    private ArrayList<String> arrayList;
    private NetworkCheck networkCheck;

    private int rotate = 0;

    private String chat = "";
    private String type = "";

    private Handler mHandler;
    private Runnable mRunnable;
    private int seconds = 1000;

    private int count = 0;
    private int num = 0;

    public PhotoManager()
    {
        // Structure
    }


    public void Camera(Context ct)
    {
        this.context = ct;

        type = "camera"; // meta data에 넣어야함.
        Log.i("type", type);

        Chat_Data.getInstance().setCameraUse(true);

        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(intent.resolveActivity(context.getPackageManager()) != null)
            {
                File photoFile = null;

                try
                {
                    photoFile = createImageFile();
                }
                catch(IOException e)
                {
                    Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show();
                }

                if(photoFile != null)
                {
                    Uri providerUri = FileProvider.getUriForFile(this, context.getPackageName()+".fileprovider", photoFile);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerUri);

                    ((Activity)context).startActivityForResult(intent, REQUEST_CAMERA);
                }
                else
                {
                    Toast.makeText(this, "photoFile Failed", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "None createImageFile", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "저장 공간 접근 불가", Toast.LENGTH_SHORT).show();

            return;
        }

        Log.i("setCamera", "Start");
    }

    public void Album(Context ct)
    {
        this.context = ct;

        albumCheck = true;
        type = "album";
        Log.i("type", type);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");

        //((Activity)context).startActivityForResult(intent, REQUEST_ALBUM);

        ((Activity)context).startActivityForResult(intent, REQUEST_ALBUM);
    }



    private File createImageFile() throws IOException
    {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = timeStamp + "qweasdqwesadfdsa" + ".jpg";

        imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/image/"+imageFileName);

        if(!imageFile.exists())
        {
            imageFile.getParentFile().mkdirs();
            imageFile.createNewFile();
        }

        mCurrentPhotoPath = imageFile.getAbsolutePath();

        Chat_Data.getInstance().setImageFileName(imageFileName);
        Chat_Data.getInstance().setPhotoPath(mCurrentPhotoPath);

        return imageFile;
    }

    public void galleryAddPic(Context ct)
    {
        this.context = ct;

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        //File file = new File(mCurrentPhotoPath);

        File file = new File(Chat_Data.getInstance().getPhotoPath());

        Log.i("PhotoManager","galleryAddPic" + " " + String.valueOf(file));

        contentUri = Uri.fromFile(file);
        intent.setData(contentUri);
        //context.sendBroadcast(intent);

        context.sendBroadcast(intent);

        uploadStorage();
    }

    public void getAlbumData(Intent intent)
    {
        Uri imgUri = intent.getData();

        Log.i("getAlbumData/////////", String.valueOf(imgUri));

        mCurrentPhotoPath = getRealPathFromURI(imgUri); // path 경로
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = timeStamp + "qweasdqwesadfdsa" + ".jpg";

        Chat_Data.getInstance().setCameraUse(true);

        Chat_Data.getInstance().setImageFileName(imageFileName);
        Chat_Data.getInstance().setPhotoPath(mCurrentPhotoPath);

        uploadStorage();
    }


    // Album path
    public String getRealPathFromURI(Uri uri) {

        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = ((Activity)context).managedQuery(uri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        Chat_Data.getInstance().setCameraUse(true);

        return cursor.getString(column_index);
    }



    public void uploadStorage()
    {
        imageFileName = Chat_Data.getInstance().getImageFileName();
        mCurrentPhotoPath = Chat_Data.getInstance().getPhotoPath();

        Log.i("Upload", "Upload Start");

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        bmOptions.inSampleSize = 8;
        bmOptions.inPurgeable = true;

        bmOptions.inJustDecodeBounds = false;

        rotate = getRotate(mCurrentPhotoPath);
        Log.e("getRotate", String.valueOf(rotate));
        Log.e("getRotate", String.valueOf(rotate));

        bitmap = rotate(bitmap, rotate);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data = baos.toByteArray();

        Log.i("PhotoManager", imageFileName);

        storageReference = Chat_Data.getInstance().getStorageRef();
        storageReference = storage.getReferenceFromUrl("gs://chatapp-b6d7f.appspot.com").child("images/"+imageFileName);

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads

                Log.i("Upload", "Failed");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                Log.i("Upload", "Success");

                addMetaData();
            }
        });
    }

    public int getRotate(String imageFilePath)
    {
        Log.e("PhotoManager-getRotate", "Start");

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageFilePath);
        } catch (IOException e) {

            Log.i("ExifInterface", "NUll");

            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Log.i("exifDegree", String.valueOf(exifDegree));

        return exifDegree;
    }

    public Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    // 가로일 때 rotate 값이 뭔지 확인한 후 chat_room 에 저장시켜서 imageView를 구분시켜줘야 한다.

    public int exifOrientationToDegrees(int exifOrientation) {

        Log.i("rotate", "Start");

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

            return 270;
        }

        return 0;
    }

    public void addMetaData()
    {
        Log.i("addMetaData", "Start");
        Log.i("type", type);

        storageReference = storage.getReference();
        StorageReference forestRef = storageReference.child("images/"+imageFileName);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("rotate", String.valueOf(rotate))
                .setCustomMetadata("type", type)
                .build();

        forestRef.updateMetadata(metadata)
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        // Updated metadata is in storageMetadata

                        if(context.getClass() == ChatActivity.class) // ChatActivity
                        {
                            Log.i("PhotoManager", "Class -> ChatActivity");
                            Log.i("PhotoManager", "addFileName() - Start");

                            addFileName();
                        }
                        else if(context.getClass() == UserActivity.class)
                        {

                            Log.i("PhotoManager", "Class -> UserActivity");
                            Log.i("PhotoManager", "userProfile Start");

                            userProfile(imageFileName);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                    }
                });
    }

    public void addFileName()
    {
        dbHandler = new DBHandler(context, null, null, 1);
        my_name = dbHandler.getMyData("NAME");

        mDatabase = FirebaseDatabase.getInstance().getReference("test_chat");
        final DatabaseReference data = mDatabase.child("uid");

        Map<String, Object> map = new HashMap<String, Object>();
        String fbKey = data.push().getKey();

        ChatActivity chatActivity = new ChatActivity();
        String time = chatActivity.getTime();

        map.put("name", my_name);
        map.put("msg", imageFileName);
        map.put("time", time);

        list_msg = "사진";

        check = false;

        Log.i("addFileName", "S T A R T !!!!");

        key = Chat_Data.getInstance().getKey();

        data.child(key).child(fbKey).updateChildren(map);
    }

    public void userProfile(String imageFileName)
    {
        Log.i("userProfile", "Start22");

        setProfileData(imageFileName);

        Chat_Profile_Tab chat_tab = new Chat_Profile_Tab();
        chat_tab.profile_Image();
    }

    public void setProfileData(String imageFileName)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference("profile_image");
        final DatabaseReference data = mDatabase;

        dbHandler = new DBHandler(UserActivity.getContext(), null, null, 1);
        String my_name = dbHandler.getMyData("NAME");

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

        arrayList = dbHandler.findRoomData();

        if(arrayList.size() != 0)
        {
            for(int i = 0; arrayList.size() > i; i++) {

                // db 에 저장된 룸 데이터 가져오기

                HashMap<String, String> map = arrayList.get(i);

                String user_name = map.get("chat_name");

                // 하나씩 새로 바뀐 데이터 알려주기

                Map<String, Object> map2 = new HashMap<String, Object>();

                String key = data.push().getKey();

                Log.e("setProfileData-username", user_name);
                Log.e("setProfileData-name", my_name);
                Log.e("setProfileData-filename", imageFileName);

                map2.put("name", my_name);
                map2.put("msg", imageFileName);

                data.child(user_name).child(key).updateChildren(map2); // 상대에게 알린다.
            }
        }
        else
        {
            Log.i("저장된 데이터 없음", " 서버 전송 x");
        }
    }

//사진 다운로드 할 때만 이용,

    public void downloadImageFile(String imageFileName, Context context)
    {

        storageRef = storage.getReferenceFromUrl("gs://chatapp-b6d7f.appspot.com");
        storageReference = storageRef.child("images").child(imageFileName);

        try
        {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/TackAlbum");

            if(!file.exists())
            {
                Log.e("path = ", "not exists 생성");

                file.mkdirs();
            }

            //File localFile = File.createTempFile("images", "jpg");

            Log.e("file = ", String.valueOf(file));

            File localFile = new File(file, imageFileName);

            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Log.e("download = ", "Start");

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DATA, localFile.getAbsolutePath());
                            values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
                            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);


                            Log.e("download fromFIle = ", String.valueOf(Uri.fromFile(localFile)));
                            Log.e("download filePath = ", String.valueOf(localFile.getAbsolutePath()));

                            Log.e("download = ", "Successs");

                            Toast.makeText(context, "Download Success!!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...

                    Log.e("downloadImageFile", "download failed");

                    Log.e("downloadimageFile", String.valueOf(exception));
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

