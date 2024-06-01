package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterPdfFavorite;
import com.example.bookapp.databinding.ActivityProfileBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding; // Binding đối tượng giao diện người dùng

    private FirebaseAuth firebaseAuth; // Đối tượng xác thực Firebase
    private FirebaseUser firebaseUser; // Người dùng Firebase hiện tại

    private ArrayList<ModelPdf> pdfArrayList; // Danh sách sách yêu thích
    private AdapterPdfFavorite adapterPdfFavorite; // Bộ điều hợp danh sách sách yêu thích
    //progress dialog
    private ProgressDialog progressDialog; // Hộp thoại hiển thị tiến trình

    private static final String TAG = "PROFILE_TAG"; // Tag đăng ký

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập các giá trị mặc định cho các view
        binding.accountTypeTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.favoriteBookCountTv.setText("N/A");
        binding.accountStatusTv.setText("N/A");

        firebaseAuth = FirebaseAuth.getInstance(); // Lấy đối tượng xác thực Firebase
        firebaseUser = firebaseAuth.getCurrentUser(); // Lấy người dùng Firebase hiện tại

        // Thiết lập hộp thoại tiến trình
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo(); // Tải thông tin người dùng
        loadFavoriteBooks(); // Tải danh sách sách yêu thích

        // Xử lý sự kiện click nút quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Xử lý sự kiện click nút chỉnh sửa hồ sơ
        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        // Xử lý sự kiện click trạng thái tài khoản
        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this, "Already verified...", Toast.LENGTH_SHORT).show();
                }
                else {
                    emailVerificationDialog(); // Hiển thị hộp thoại xác minh email
                }
            }
        });
    }

    // Hiển thị hộp thoại xác minh email
    private void emailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Email")
                .setMessage("Are you sure you want to send email verification instructions to your email "+firebaseUser.getEmail())
                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification(); // Gửi email xác minh
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Gửi email xác minh
    private void sendEmailVerification() {
        // Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Sending email verification instruction to your email"+firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void unused){
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Instructions sent, check your email"+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Faileddue to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Tải thông tin người dùng
    private void loadUserInfo(){
        Log.d(TAG, "LoadUserInfo: Loading user info of user " + firebaseAuth.getUid());

        // Hiển thị trạng thái tài khoản
        if (firebaseUser.isEmailVerified()) {
            binding.accountStatusTv.setText("Verified");
        } else {
            binding.accountStatusTv.setText("Not Verified");
        }

        // Lấy thông tin người dùng từ Firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Cập nhật thông tin người dùng lên giao diện
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        // Tải ảnh đại diện người dùng
                        if (!profileImage.equals("")) {
                            Glide.with(ProfileActivity.this)
                                    .load(profileImage)
                                    .override(120, 120)
                                    .placeholder(R.drawable.ic_personal_gray)
                                    .into(binding.profileIv);
                        } else {
                            Glide.with(ProfileActivity.this)
                                    .load(R.drawable.ic_personal_gray)
                                    .override(120, 120)
                                    .into(binding.profileIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // Tải danh sách sách yêu thích
    private void loadFavoriteBooks() {
        // Khởi tạo danh sách sách yêu thích
        pdfArrayList = new ArrayList<>();

        // Lấy danh sách sách yêu thích từ Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Xóa danh sách sách yêu thích cũ
                        pdfArrayList.clear();

                        // Lặp qua các sách yêu thích trong Firebase
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Lấy ID của sách yêu thích
                            String bookId = "" + ds.child("bookId").getValue();

                            // Tạo một đối tượng ModelPdf mới
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            // Thêm đối tượng modelPdf vào danh sách pdfArrayList
                            pdfArrayList.add(modelPdf);
                        }

                        // Cập nhật số lượng sách yêu thích
                        binding.favoriteBookCountTv.setText("" + pdfArrayList.size());

                        // Tạo một AdapterPdfFavorite mới và gán vào booksRv
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);
                        binding.booksRv.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
                        binding.booksRv.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi khi lấy dữ liệu từ Firebase
                    }
                });
    }
}