package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.databinding.ActivityPdfListAdminBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;

    private ArrayList<ModelPdf> pdfArrayList;

    private AdapterPdfAdmin adapterPdfAdmin;

    private String categoryId, categoryTitle;

    private static  final String TAG = "PDF_LIST_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        binding.subTitleTv.setText(categoryTitle);

        loadPdfList();

        //tìm kiếm
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            //Sử dụng bộ lọc của adapterPdfAdmin để lọc danh sách các tệp PDF
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterPdfAdmin.getFilter().filter(s);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //xử lý khi nhấn nút back , chuyển đến activity trước đó
        binding.backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        //danh sách trước khi thêm dữ liệu
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ( "Books");
        //Lấy các tệp PDF thuộc về danh mục hiện tại
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //lấy dữ liệu
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //Thêm vào danh sách
                            pdfArrayList.add(model);

                            Log.d(TAG, "onChange: "+model.getId()+""+model.getTitle());
                        }
                        //setup adapter
                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}