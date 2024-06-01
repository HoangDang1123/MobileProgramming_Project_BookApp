package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Phan Thị Ngọc Mai - 21110238
public class LoginActivity extends AppCompatActivity {

    // Khai báo biến binding kiểu ActivityLoginBinding để sử dụng View Binding
    private ActivityLoginBinding binding;

    // Khai báo biến firebaseAuth kiểu FirebaseAuth để xác thực người dùng
    private FirebaseAuth firebaseAuth;

    // Khai báo biến progressDialog để hiển thị hộp thoại tiến trình
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng View Binding để thiết lập giao diện
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy instance của FirebaseAuth
        firebaseAuth = firebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        // Không cho phép hủy hộp thoại khi chạm ra ngoài
        progressDialog.setCanceledOnTouchOutside(false);

        // // Xử lý sự kiện khi bấm vào TextView noAccountTv, mở màn hình đăng ký
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Mở màn hình đăng ký
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Xử lý sự kiện khi bấm vào nút loginBtn, bắt đầu quá trình đăng nhập
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gọi phương thức validateData để kiểm tra dữ liệu
                validateData();
            }
        });

        // Xử lý sự kiện khi bấm vào TextView forgotTv, mở màn hình quên mật khẩu
        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)); //Mở màn hình quên mật khẩu
            }
        });
    }

    //Khai báo các biến để lưu trữ thông tin đăng nhập
    private String email = "", password = "";

    private void validateData() {
        /*Kiểm tra dữ liệu trước khi đăng nhập */

        //Lấy dữ liệu từ các EditText
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (!Patterns.EMAIL_ADDRESS.matcher (email).matches()) {
            Toast.makeText( this,"Invalid email pattern...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText( this,"Enter password...!", Toast.LENGTH_SHORT).show();
        }
        else{
            loginUser(); // Nếu dữ liệu hợp lệ, gọi phương thức loginUser để đăng nhập
        }
    }

    private void loginUser() {
        //Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        //Đăng nhập người dùng
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Đăng nhập thành công, kiểm tra loại người dùng
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Đăng nhập thất bại
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUser(){
        //Cập nhật tông điệp của hộp thoại tiến trình
        progressDialog.setMessage("Checking User...");

        //Lấy người dùng hiện tại
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //Lấy loại người dùng
                        String userType = "" + snapshot.child("userType").getValue();
                        //Kiểm tra loại người dùng
                        if (userType.equals("user")) {
                            //Nếu là  user, mở DashboardUserActivity
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            //Nếu là  admin, mở DashboardAdminActivity
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }

                });


    }
}