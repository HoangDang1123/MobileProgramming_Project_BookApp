package com.example.bookapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private Uri imageUri = null;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ProgressDialog để hiển thị trong quá trình cập nhật thông tin
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false); // Không cho phép người dùng thoát khỏi ProgressDialog bằng cách nhấn bên ngoài

        // Lấy instance của FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Tải thông tin người dùng hiện tại từ Firebase
        loadUserInfo();

        // Thiết lập sự kiện bấm nút quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Thiết lập sự kiện bấm nút ảnh đại diện
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageAttachMenu();
            }
        });

        // Thiết lập sự kiện bấm nút cập nhật thông tin
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private void loadUserInfo() {
        Log.d(TAG, "LoadUserInfo: Loading user info of user " + firebaseAuth.getUid());
        // Truy cập vào nút "Users" trong Firebase Realtime Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lấy thông tin tên và ảnh đại diện của người dùng
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        // Hiển thị tên người dùng trong EditText
                        binding.nameEt.setText(name);

                        // Nếu có ảnh đại diện, hiển thị ảnh. Nếu không, hiển thị ảnh mặc định
                        if (!profileImage.equals("")) {
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImage)
                                    .override(110, 110)
                                    .placeholder(R.drawable.ic_personal_gray)
                                    .into(binding.profileIv);
                        } else {
                            Glide.with(ProfileEditActivity.this)
                                    .load(R.drawable.ic_personal_gray)
                                    .override(110, 110)
                                    .into(binding.profileIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void validateData() {
        // Lấy dữ liệu từ EditText
        name = binding.nameEt.getText().toString().trim();

        // Kiểm tra xem tên có trống không
        if (TextUtils.isEmpty(name)) {
            // Nếu tên trống, hiển thị thông báo
            Toast.makeText(this, "Enter name...", Toast.LENGTH_SHORT).show();
        } else {
            // Nếu tên không trống
            if (imageUri == null) {
                // Nếu không có ảnh, cập nhật hồ sơ với ảnh trống
                updateProfile("");
            } else {
                // Nếu có ảnh, tải ảnh lên
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        // Hiển thị ProgressDialog
        Log.d(TAG, "uploadImage: Uploading profile image...");
        progressDialog.setMessage("Updating profile image");
        progressDialog.show();

        // Tạo đường dẫn lưu trữ ảnh
        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        // Tham chiếu đến vị trí lưu trữ ảnh trong Firebase Storage
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);

        // Tải ảnh lên Firebase Storage
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Nếu tải ảnh thành công, lấy URL của ảnh đã tải
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();
                        Log.d(TAG, "onSucc String imageUrli Image URL: " + uploadedImageUrl);

                        // Sau khi có URL, cập nhật hồ sơ
                        updateProfile(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Nếu tải ảnh thất bại, hiển thị thông báo lỗi
                        Log.d(TAG, "onFailure: Failed to upload image due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to upload image due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String imageUrl) {
        // Hiển thị ProgressDialog
        Log.d(TAG, "updateProfile: Updating user profile");
        progressDialog.setMessage("Updating user profile...");
        progressDialog.show();

        // Tạo HashMap chứa các thông tin cần cập nhật
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        if (imageUri != null) {
            hashMap.put("profileImage", "" + imageUrl);
        }

        // Cập nhật thông tin người dùng trong Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Nếu cập nhật thành công, hiển thị thông báo
                        Log.d(TAG, "onSuccess: Profile updated...");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Profile updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Nếu cập nhật thất bại, hiển thị thông báo lỗi
                        Log.d(TAG, "onFailure: Failed to update db due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to update db due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Phương thức hiển thị menu đính kèm hình ảnh
    private void showImageAttachMenu() {
        // Tạo một đối tượng PopupMenu mới, được neo vào view binding.profileIv
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);

        // Thêm hai mục menu vào PopupMenu: "Camera" và "Gallery"
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");

        // Hiển thị PopupMenu
        popupMenu.show();

        // Thiết lập một listener click cho các mục menu của PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Lấy ID của mục menu được click
                int which = item.getItemId();

                // Xử lý click dựa trên ID của mục menu
                if (which == 0) {
                    // Người dùng click vào "Camera" - gọi phương thức pickImageCamera()
                    pickImageCamera();
                } else if (which == 1) {
                    // Người dùng click vào "Gallery" - gọi phương thức pickImageGallery()
                    pickImageGallery();
                }

                // Trả về false để cho biết mục menu đã được xử lý
                return false;
            }
        });
    }

    // Phương thức xử lý chụp ảnh từ camera
    private void pickImageCamera() {
        // Tạo một đối tượng ContentValues mới để lưu trữ siêu dữ liệu ảnh
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        // Chèn siêu dữ liệu ảnh vào MediaStore và lấy URI được tạo
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Tạo một Intent để khởi chạy ứng dụng Camera và chụp ảnh
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        // Khởi chạy hoạt động Camera và xử lý kết quả bằng cách sử dụng cameraActivityResultLauncher
        cameraActivityResultLauncher.launch(intent);
    }

    // Phương thức xử lý chọn ảnh từ thư viện
    private void pickImageGallery() {
        // Tạo một Intent để mở thư viện ảnh
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // Khởi chạy hoạt động Thư viện ảnh và xử lý kết quả bằng cách sử dụng galleryActivityResultLauncher
        galleryActivityResultLauncher.launch(intent);
    }

    // Launcher để xử lý kết quả của hoạt động Camera
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imageUri);

                        // Hiển thị ảnh đã chụp lên ImageView
                        binding.profileIv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Launcher để xử lý kết quả của hoạt động Thư viện ảnh
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imageUri);
                        Intent data = result.getData();

                        // Lấy URI của ảnh được chọn từ Thư viện
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked from Gallery" + imageUri);

                        // Hiển thị ảnh đã chọn lên ImageView
                        binding.profileIv.setImageURI(imageUri);
                    }
                }
            });
}