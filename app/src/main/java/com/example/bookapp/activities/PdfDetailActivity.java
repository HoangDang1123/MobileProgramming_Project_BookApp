package com.example.bookapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterComment;
import com.example.bookapp.adapters.AdapterPdfFavorite;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogCommentAddBinding;
import com.example.bookapp.models.ModelComment;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    // Khai báo các biến cần thiết
    private ActivityPdfDetailBinding binding;
    String bookId, bookTitle, bookUrl;
    boolean isInMyFavorite = false;
    private FirebaseAuth firebaseAuth;
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private ProgressDialog progressDialog;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đính kết view với layout
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy thông tin sách từ Intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        // Ẩn nút tải xuống
        binding.downloadBookBtn.setVisibility(View.GONE);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        // Lấy FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // Kiểm tra sách có trong danh sách yêu thích không
            checkIsFavorite();
        }

        // Tải thông tin chi tiết sách
        loadBookDetails();
        // Tải các bình luận của sách
        loadComments();

        // Tăng lượt xem sách
        MyApplication.incrementBookViewCount(bookId);

        // Xử lý sự kiện nhấn nút quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Xử lý sự kiện nhấn nút đọc sách
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở PdfViewActivity để đọc sách
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        // Xử lý sự kiện nhấn nút tải xuống sách
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra quyền ghi vào bộ nhớ ngoài
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    // Nếu có quyền, tải sách
                    MyApplication.downloadBook(PdfDetailActivity.this, ""+bookId, ""+bookTitle,""+bookUrl);
                }
                else{
                    // Nếu không có quyền, yêu cầu cấp quyền
                    requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        // Xử lý sự kiện nhấn nút thêm vào/xóa khỏi danh sách yêu thích
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // Nếu người dùng chưa đăng nhập, hiển thị thông báo
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu đã đăng nhập, thêm/xóa khỏi danh sách yêu thích
                    if (isInMyFavorite) {
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                    } else {
                        MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                    }
                }
            }
        });

        // Xử lý sự kiện nhấn nút thêm bình luận
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // Nếu người dùng chưa đăng nhập, hiển thị thông báo
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in...", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Nếu đã đăng nhập, hiển thị dialog để thêm bình luận
                    addCommentDialog();
                }
            }
        });
    }

    // Hàm tải các bình luận của sách
    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot snapshot) {
                        // Xóa danh sách bình luận cũ
                        commentArrayList.clear();
                        // Thêm các bình luận mới vào danh sách
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelComment model = ds.getValue(ModelComment.class);
                            commentArrayList.add(model);
                        }
                        // Cập nhật AdapterComment với danh sách bình luận mới
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // Hàm hiển thị dialog để thêm bình luận
    private String comment = "";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Xử lý sự kiện nhấn nút quay lại trên dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // Xử lý sự kiện nhấn nút gửi bình luận
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy nội dung bình luận
                comment = commentAddBinding.commentEt.getText().toString().trim();
                if (TextUtils.isEmpty(comment)){
                    // Nếu bình luận trống, hiển thị thông báo
                    Toast.makeText(PdfDetailActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Nếu có bình luận, đóng dialog và thêm bình luận
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {
        // Hiển thị hộp thoại tiến trình để thông báo cho người dùng biết đang thêm bình luận
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        // Lấy thời gian hiện tại dưới dạng timestamp
        String timestamp = ""+System.currentTimeMillis();

        // Tạo một HashMap để lưu trữ các thông tin liên quan đến bình luận
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp); // Sử dụng timestamp làm ID của bình luận
        hashMap.put("bookId", ""+bookId); // ID của cuốn sách mà bình luận liên quan đến
        hashMap.put("timestamp", ""+timestamp); // Timestamp của bình luận
        hashMap.put("uid", ""+firebaseAuth.getUid()); // ID của người dùng đang thêm bình luận
        hashMap.put("comment", ""+comment); // Nội dung bình luận

        // Lấy tham chiếu đến nút "Comments" trong Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap) // Thêm bình luận vào Firebase Database
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Nếu thêm bình luận thành công
                        Toast.makeText(PdfDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        // Nếu thêm bình luận thất bại
                        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Định nghĩa một ActivityResultLauncher để yêu cầu quyền từ người dùng
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if(isGranted){ // Nếu quyền được cấp
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    // Gọi hàm MyApplication.downloadBook() để tải xuống sách
                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else { // Nếu quyền bị từ chối
                    Log.d(TAG_DOWNLOAD, "Permission was denied...:");
                    // Hiển thị Toast thông báo quyền bị từ chối
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        // Lấy tham chiếu đến nút "Books" trong Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        // Lấy dữ liệu của cuốn sách có bookId tương ứng
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Đọc các thuộc tính của cuốn sách
                        bookTitle =""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryTitle =""+snapshot.child("categoryTitle").getValue();
                        String viewsCount =""+snapshot.child("viewsCount").getValue();
                        String downloadsCount =""+snapshot.child("downloadsCount").getValue();
                        bookUrl =""+snapshot.child("url").getValue();
                        String timestamp =""+snapshot.child("timestamp").getValue();

                        // Hiển thị nút "Download Book"
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        // Định dạng thời gian
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        // Hiển thị các thông tin khác của cuốn sách
                        MyApplication.loadCategory(""+categoryTitle, binding.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, binding.pdfView, binding.progressBar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, binding.sizeTv);
                        MyApplication.loadPdfPageCount(PdfDetailActivity.this, ""+bookUrl, binding.pagesTv);

                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite (){
        // Lấy tham chiếu đến nút "Users" trong Firebase Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        // Lấy dữ liệu của danh sách yêu thích của người dùng
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot snapshot) {
                        // Kiểm tra xem cuốn sách có trong danh sách yêu thích hay không
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite){ // Nếu có trong danh sách yêu thích
                            // Hiển thị nút "Remove Favorite"
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else { // Nếu không có trong danh sách yêu thích
                            // Hiển thị nút "Add Favorite"
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}