package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    //Khai báo biến binding kiểu ActivityRegisterBinding để sử dụng View Binding
    private ActivityRegisterBinding binding;

    // Khai báo biến firebaseAuth kiểu FirebaseAuth để xác thực người dùng
    private FirebaseAuth firebaseAuth;

    // Khai báo biến progressDialog để hiển thị hộp thoại tiến trình
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sử dụng View Binding để thiết lập giao diện
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Lấy instance của FirebaseAuth
        firebaseAuth = firebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        // Không cho phép hủy hộp thoại khi chạm ra ngoài
        progressDialog.setCanceledOnTouchOutside(false);

        //Xử lý sự kiện khi bấm vào nút backBtn, quay lại màn hình trước
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Xử lý sự kiện khi bấm vào nút registerBtn, bắt đầu quá trình đăng ký
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name="", email ="", password="";
    private void validateData() {
        /* Trước khi tạo tài khoản, kiểm tra dữ liệu */

        // Lấy dữ liệu từ các EditText
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (TextUtils.isEmpty (name)) {
            Toast.makeText(this,"Enter you name...", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches()) {
            Toast.makeText( this,"Invalid email pattern...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText( this,"Enter password...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "Confirm Password...!", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword)){
            Toast.makeText(this, "Password doesn't match...!", Toast.LENGTH_SHORT).show();
        }
        else{
            // Nếu dữ liệu hợp lệ, gọi phương thức createUserAccount để tạo tài khoản
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        // Tạo người dùng trong Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword (email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess (AuthResult authResult) {
                        // Khi tạo tài khoản thành công, thêm thông tin người dùng vào Firebase Realtime Database
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Khi tạo tài khoản thất bại
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void updateUserInfo() {
        // Cập nhật thông điệp của hộp thoại tiến trình
        progressDialog.setMessage("Saving user info...");
        // Lấy thời gian hiện tại
        long timestamp = System.currentTimeMillis();
        // Lấy UID của người dùng hiện tại
        String uid = firebaseAuth.getUid();

        // Tạo dữ liệu để thêm vào cơ sở dữ liệu
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); // Để trống, sẽ thêm sau
        hashMap.put("userType", "user"); // Giá trị có thể là user hoặc admin; sẽ thêm admin thủ công trong Firebase Realtime Database
        hashMap.put("timestamp", timestamp);

        // Thêm dữ liệu vào cơ sở dữ liệu
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Khi dữ liệu được thêm thành công vào cơ sở dữ liệu
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account create...", Toast.LENGTH_SHORT).show();
                        // Bắt đầu DashboardUserActivity
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Khi thêm dữ liệu vào cơ sở dữ liệu thất bại
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}