package com.example.jt.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;

public class NickNameDialog implements RequestListener
{
    private Context context;


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Button button1;
    private Button button2;
    private TextView textView;
    private EditText editText;
    private boolean check;

    private TokenService tokenService;
    private DBHandler dbHandler;

    private boolean TEXT = false;

    public NickNameDialog()
    {
        //...
    }

    public void nickName(Context ct)
    {
        this.context = ct;

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_nickname);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        //params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //params.height = WindowManager.LayoutParams.MATCH_PARENT;

        params.width = 900;
        params.height = 1200;

        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        dialog.show();

        button1 = (Button)dialog.findViewById(R.id.button1);
        button2 = (Button)dialog.findViewById(R.id.button2);
        editText = (EditText)dialog.findViewById(R.id.editText);
        textView = (TextView)dialog.findViewById(R.id.checkName);

        dbHandler = new DBHandler(context, null, null, 1);


        mDatabase = FirebaseDatabase.getInstance().getReference("test_chat");
        final DatabaseReference data = mDatabase.child("uid");

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (editText.getText().toString().matches("^[가-힣|a-z|A-Z|0-9|*]+$")) {

                    mDatabase = FirebaseDatabase.getInstance().getReference("users"); // 추가된 uid == Boo 값을 가져온다.
                    final DatabaseReference data = mDatabase.child("name");  //dataSnapshot은 전체의 경로이다, 즉 전체의 경로를 입력해주어야하고,

                    data.addValueEventListener(new ValueEventListener() {// 연결해주어야한다.
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String name = "";

                            Iterable<DataSnapshot> ds = dataSnapshot.getChildren();
                            Iterator<DataSnapshot> ids = ds.iterator();

                            ArrayList<String> list = new ArrayList<String>();

                            while (ids.hasNext())
                            {
                                name = (String) ids.next().getValue();

                                list.add(name);
                            }

                          if(!check)
                          {
                              if(list.contains(editText.getText().toString()))
                              {
                                  Log.e("name ", "포함되어있음");
                                  Log.e("name ", name);

                                  Toast.makeText(context, "존재하는 아이디 입니다.", Toast.LENGTH_LONG).show();

                                  textView.setText("닉네임을 다시 설정해주세요");
                              }
                              else
                              {
                                  Log.e("name ", "포함 안되어 있음");

                                  check = true;

                                  setName();
                              }
                          }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (TEXT) {

                                String my_name = editText.getText().toString();
                                String token = FirebaseInstanceId.getInstance().getToken(); // TOKEN Firebase 에서 받아오기
                                String key = mDatabase.child("name").push().getKey();

                                mDatabase.child("name").child(key).setValue(my_name); // name을 Firebase에 저장
                                dbHandler.setMyData(my_name, token, "None"); // name, token, profile_image 내부 db에 저장, login 인증

                                setTokenData(my_name, token); // Token을 서버로 전송

                                Intent intent = new Intent(context, UserActivity.class);
                                intent.putExtra("name", my_name);

                                context.startActivity(intent);

                            }
                            else
                            {
                                Toast.makeText(context, "중복체크를 해주세요", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setName() {

        textView.setText("사용이 가능합니다.");

        TEXT = true;
    }

    public void setTokenData(String my_name, String token)
    {
        Log.i("setTokenData","Start//////////////////////////////////////////////");

        mDatabase = FirebaseDatabase.getInstance().getReference("Token"); // 추가된 uid == Boo 값을 가져온다.

        mDatabase.child(my_name).setValue(token); // Token 저장

        tokenService = new TokenService(context, this);
        tokenService.registerTokenInDB(token);
    }


    @Override // RequestListener Override
    public void onComplete() {
        Log.d("NickName", "Token registered successfully in the DB");

    }

    @Override // RequestListener Override
    public void onError(String message) {
        Log.d("NickName", "Error trying to register the token in the DB: " + message);
    }
}
