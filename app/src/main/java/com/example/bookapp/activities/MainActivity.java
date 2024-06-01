package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //Khai báo biến binding kiểu ActivityMainBinding để sử dụng View Binding
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng View Binding để thiết lập giao diện
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý sự kiện khi bấm vào nút loginBtn, mở màn hình login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        //Xử lý sự kiện khi bấm vào nút skipBtn, chuyen sang giao dien DashboardUser
        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DashboardUserActivity.class));
            }
        });
    }
}