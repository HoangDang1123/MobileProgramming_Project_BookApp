package com.example.bookapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.bookapp.BooksUserFragment;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    // Khai báo các biến cần thiết
    public ArrayList<ModelCategory> categoryArrayList; // Danh sách các danh mục
    public ViewPagerAdapter viewPagerAdapter; // Adapter cho ViewPager
    private ActivityDashboardUserBinding binding; // Binding layout

    private FirebaseAuth firebaseAuth; // Xác thực Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy instance của FirebaseAuth
        firebaseAuth = firebaseAuth.getInstance();
        // Kiểm tra người dùng hiện tại
        checkUser();

        // Thiết lập ViewPagerAdapter
        setupViewPagerAdapter(binding.viewPager);
        // Liên kết TabLayout và ViewPager
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        // Xử lý sự kiện click logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đăng xuất khỏi Firebase Auth
                firebaseAuth.signOut();
                // Chuyển sang màn hình chính
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                // Kết thúc activity
                finish();
            }
        });

        // Xử lý sự kiện click profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang màn hình profile
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        // Khởi tạo ViewPagerAdapter
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        // Khởi tạo danh sách các danh mục
        categoryArrayList = new ArrayList<>();

        // Lấy dữ liệu danh mục từ Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Xóa danh sách trước khi thêm mới
                categoryArrayList.clear();

                // Thêm các danh mục mặc định
                ModelCategory modelAll = new ModelCategory("01", "All", " ", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", " ", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", " ", 1);
                categoryArrayList.add(modelAll);
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelAll.getId(),
                        "" + modelAll.getCategory(),
                        "" + modelAll.getUid()
                ), modelAll.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelMostViewed.getId(),
                        "" + modelMostViewed.getCategory(),
                        "" + modelMostViewed.getUid()
                ), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelMostDownloaded.getId(),
                        "" + modelMostDownloaded.getCategory(),
                        "" + modelMostDownloaded.getUid()
                ), modelMostDownloaded.getCategory());

                // Cập nhật ViewPagerAdapter
                viewPagerAdapter.notifyDataSetChanged();

                // Lặp qua các danh mục trong Firebase Database
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Lấy thông tin danh mục
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);

                    // Thêm danh mục vào ViewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            "" + model.getId(),
                            "" + model.getCategory(),
                            "" + model.getUid()), model.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        // Liên kết ViewPager với ViewPagerAdapter
        viewPager.setAdapter(viewPagerAdapter);
    }

    // Lớp ViewPagerAdapter để quản lý các Fragment
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BooksUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        // Lấy người dùng hiện tại từ Firebase Auth
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Nếu người dùng chưa đăng nhập, hiển thị "Not Logged in"
            binding.subTitleTv.setText("Not Logged in");
        } else {
            // Nếu người dùng đã đăng nhập, hiển thị email
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }
}