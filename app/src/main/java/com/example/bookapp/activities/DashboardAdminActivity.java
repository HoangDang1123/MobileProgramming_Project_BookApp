package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    //Khai báo biến dùng View Binding
    private ActivityDashboardAdminBinding binding;

    //Khai báo biến Firebase Auth
    private FirebaseAuth firebaseAuth;

    //Khai báo danh sách chứa các đối tượng ModelCategory
    private ArrayList<ModelCategory> categoryArrayList;

    //Khai báo Adapter cho RecyclerView
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Khởi tạo Firebase Auth
        firebaseAuth = firebaseAuth.getInstance();

        //Kiểm tra người dùng hiện tại
        checkUser();

        //Tải danh mục từ Firebase
        loadCategories();

        // Thiết lập sự kiện cho ô tìm kiếm
        binding.searchEt.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //called as and when user type each letter
                try{
                    adapterCategory.getFilter().filter(s);
                }
                catch (Exception e){

                }
            }
            @Override
            public void afterTextChanged(Editable s){

            }

        });
        //Xử lý sự kiện click cho nút đăng xuất
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        //Xử lý sự kiện click cho nút
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        //Xử lý sự kiện click cho nút thêm PDF
        binding.addPdfFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });

        //Xử lý sự kiện click cho nút xem hồ sơ
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, ProfileActivity.class));
            }
        });
    }

    //Phương thức tải danh mục từ Firebase
    private void loadCategories() {
        //Khởi tạo danh sách danh mục
        categoryArrayList = new ArrayList<>();

        // Tham chiếu đến nhánh "Categories" trong Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ trước khi thêm dữ liệu mới
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //Lấy dữ liệu
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    //Thêm vào danh sách
                    categoryArrayList.add(model);
                }
                //Thiết lập adapter cho RecyclerView
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                //set adapter to recyclerview
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Phương thức kiểm tra người dùng hiện tại
    private void checkUser() {
        //Lấy người dùng hiện tại
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Nếu chưa đăng nhập, chuyển hướng đến màn hình chính
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Nếu đã đăng nhập, lấy email người dùng và hiển thị
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }
}