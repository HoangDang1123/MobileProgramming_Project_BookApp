package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //Khai báo biến firebaseAuth kiểu FirebaseAuth để xác thực người dùng
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // thiết lập giao diện bằng cách sử dụng bố cục trong file activity_splash.xml

        //start main screen after 2s
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //tạo và bắt đầu màn hình main screen
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish(); //kết thúc hoạt động hiện tại
            }
        }, 2000); // thời gian trì hoãn là 2s
    }

    //phương thức checkUser sẽ kiểm tra người dùng có đăng nhập hay không
    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); // lấy người dùng hiện tại từ FirebaseAuth
        if (firebaseUser == null){
            // Nếu không có người dùng đăng nhập, bắt đầu MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            //Kết thúc hoạt động hiện tại
            finish();
        }
        else {
            //Lấy tham chiếu đến node"Users" trong Firebase database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            //Lấy thông tin người dùng cho người dùng hiện tại
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // Lấy giá trị userType từ cơ sở dữ liệu
                            String userType = "" + snapshot.child("userType").getValue();
                            //Kiểm tra lọi của người dùng
                            if (userType.equals("user")) {
                                //Nếu userType là "user", mở DashboardUserActivity
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();
                            } else if (userType.equals("admin")) {
                                // Nếu userType là "admin", mở DashboardAdminActivity
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }

                    });

        }
    }
}