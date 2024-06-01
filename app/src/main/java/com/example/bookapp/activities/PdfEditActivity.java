package com.example.bookapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    // Lưu trữ ID của cuốn sách
    private String bookId;

    // Khởi tạo ProgressDialog để hiển thị khi cập nhật thông tin sách
    private ProgressDialog progressDialog;

    // Lưu trữ danh sách ID và tiêu đề của các danh mục
    private ArrayList<String> categoryTitleArraylist, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy ID của cuốn sách từ Intent
        bookId = getIntent().getStringExtra("bookId");

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        // Tải danh sách các danh mục
        loadCategories();
        // Tải thông tin của cuốn sách
        loadBookInfo();

        // Xử lý sự kiện khi bấm nút "Back"
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Xử lý sự kiện khi bấm nút "Submit"
        binding.submitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                validateData();
            }
        });
    }

    // Lưu trữ tiêu đề và mô tả của cuốn sách
    private String title="", description="";
    private void validateData() {
        // Lấy giá trị của các trường nhập liệu
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        // Kiểm tra các trường nhập liệu
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description......", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category......", Toast.LENGTH_SHORT).show();
        }
        else {
            // Cập nhật thông tin của cuốn sách
            updatePdf();
        }

    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db...");

        // Hiển thị ProgressDialog
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        // Tạo một HashMap chứa thông tin mới của cuốn sách
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("decription",""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        // Cập nhật thông tin cuốn sách trên Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Book info updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");

        // Lấy thông tin của cuốn sách từ Firebase Database
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lấy các thông tin của cuốn sách
                        selectedCategoryId =""+snapshot.child("categoryId").getValue();
                        String description =""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        // Hiển thị các thông tin trên giao diện
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        // Lấy thông tin của danh mục sách
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // Lấy tên danh mục
                                        String category = ""+snapshot.child("category").getValue();
                                        // Hiển thị tên danh mục trên giao diện
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private String selectedCategoryId="", selectedCategoryTitle="";
    private void categoryDialog(){
        // Tạo một mảng chứa các tiêu đề của các danh mục
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        for(int i=0; i<categoryTitleArraylist.size(); i++){
            categoriesArray[i] = categoryTitleArraylist.get(i);
        }

        // Hiển thị một dialog để người dùng chọn danh mục
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArraylist.get(which);

                        //set to textview
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                });
    }
    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArraylist = new ArrayList<>();
    }
}