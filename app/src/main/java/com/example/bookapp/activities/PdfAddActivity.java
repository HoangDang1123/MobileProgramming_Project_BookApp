package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding; // Binding đến layout của activity
    private FirebaseAuth firebaseAuth; // Đối tượng Firebase Authentication
    private ProgressDialog progressDialog; // Hiển thị dialog tiến trình

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList; // Danh sách các danh mục

    private Uri pdfUri = null; // URI của tệp PDF được chọn
    private static final int PDF_PICK_CODE = 1000; // Mã định danh để xác định kết quả từ Intent chọn tệp PDF
    private static final String TAG = "ADD_PDF_TAG"; // Tên tag để ghi log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater()); // Khởi tạo binding với layout activity_pdf_add.xml
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance(); // Lấy instance của FirebaseAuth
        loadPdfCategories(); // Tải danh sách các danh mục

        progressDialog = new ProgressDialog(this); // Khởi tạo ProgressDialog
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Xử lý sự kiện nhấn nút "Back"
        binding.backBtn.setOnClickListener(v -> onBackPressed());

        // Xử lý sự kiện nhấn nút "Attach"
        binding.attachBtn.setOnClickListener(v -> pdfPickIntent());

        // Xử lý sự kiện nhấn vào textview danh mục
        binding.categoryTv.setOnClickListener(v -> categoryPickDialog());

        // Xử lý sự kiện nhấn nút "Submit"
        binding.submitBtn.setOnClickListener(v -> validateData());
    }

    private String title="", description=""; // Lưu trữ tiêu đề và mô tả của tệp PDF

    private void validateData() {
        // Kiểm tra dữ liệu người dùng nhập vào
        Log.d(TAG, "validateData: validating data...");

        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        // Kiểm tra tiêu đề, mô tả, danh mục và PDF được chọn
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this,"Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri  == null) {
            Toast.makeText(this, "Pick Pdf...", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        // Tải lên tệp PDF lên Firebase Storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis(); // Lấy thời gian hiện tại (timestamp)

        String filePathAndName = "Books/" + timestamp; // Đường dẫn lưu trữ tệp PDF

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri) // Tải lên tệp PDF lên Firebase Storage
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                    Log.d(TAG, "onSuccess: getting pdf url");

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl(); // Lấy URL tải về của tệp PDF
                    while (!uriTask.isSuccessful()); // Chờ đến khi lấy được URL
                    String uploadedPdfUrl = uriTask.getResult().toString();

                    uploadedPdfInfoToDb(uploadedPdfUrl, timestamp); // Lưu thông tin tệp PDF vào Firebase Database
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG,"onFailure: PDF upload failed due to "+e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "PDF upload failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadedPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        // Lưu thông tin tệp PDF vào Firebase Database
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase db...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid(); // Lấy ID người dùng hiện tại

        // Tạo một HashMap chứa thông tin tệp PDF
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("categoryId", selectedCategoryId);
        hashMap.put("url", uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp) // Tạo một node mới với key là timestamp
                .setValue(hashMap) // Lưu thông tin tệp PDF vào node đó
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: Successfully uploaded...");
                    Toast.makeText(PdfAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG,"onFailure: Failed to upload to db due to "+e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPdfCategories() {
        // Tải danh sách các danh mục từ Firebase Database
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    // Lưu trữ ID và tên danh mục vào các ArrayList tương ứng
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        // Hiển thị hộp thoại cho người dùng chọn danh mục
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        // Lấy danh sách các danh mục và chuyển thành mảng
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        // Tạo và hiển thị hộp thoại chọn danh mục
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Lưu lại danh mục đã chọn
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        // Cập nhật giao diện
                        binding.categoryTv.setText(selectedCategoryTitle);
                        Log.d(TAG, "onClick: Selected Category: "+selectedCategoryId+" "+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        // Khởi chạy intent để người dùng chọn tệp PDF
        Log.d(TAG, "pdfPickIntent: starting pdf intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả chọn tệp PDF
        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG,  "onActivityResult: PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG,  "onActivityResult: URI: "+pdfUri);
            }
        }
        else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this,"cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}