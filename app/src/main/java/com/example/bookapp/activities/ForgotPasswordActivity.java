package com.example.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ForgotPasswordActivity extends AppCompatActivity {
    // Khai báo biến dùng View Binding
    private ActivityForgotPasswordBinding binding;

    // Khai báo biến Firebase Auth
    private FirebaseAuth firebaseAuth;

    // Khai báo biến Progress Dialog
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Khởi tạo và thiết lập Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Xử lý sự kiện click, quay lại màn hình trước
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Xử lý sự kiện click, bắt đầu quá trình khôi phục mật khẩu
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email = "";
    private void validateData() {
        //Lấy dữ liệu email
        email = binding.emailEt.getText().toString().trim();

        //Kiểm rea tính hợp lệ của dữ liệu email
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter Email...", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format...", Toast.LENGTH_SHORT).show();
        }
        else {
            //Nếu dữ liệu hợp lệ, gọi phương thức recoverPassword để khôi phục mật khẩu
            recoverPassword();
        }
    }

    private void recoverPassword() {
        //Hiển thị Progress Dialog
        progressDialog.setMessage("Sending password recovery instructions to " + email);
        progressDialog.show();

        //Bắt đầu gửi email khôi phục mật khẩu
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Gửi email thành công
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Instructions to reset password sent to " + email, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Gửi email thất bại
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}