package com.example.bookapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
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
import java.util.Objects;

public class PdfEditActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfEditBinding binding;

    //book id get from intent started from AdapterPdfAdmin
    private  String bookId;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArraylist, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //book id get from intent started from AdapterPdf Admin
        bookId = getIntent().getStringExtra("bookId");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);
        
        loadCategories();
        loadBookInfo();

        //handle click, go to previous screen
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                validateData();
            }
        });
    }

    private String title="", description="";
    private void validateData() {
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
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
            updatePdf();
        }

    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db...");

        //show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        //set data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("decription",""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        //start updating
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

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId =""+snapshot.child("categoryId").getValue();
                        String description =""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to category text view
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


    private  String selectedCategoryId="", selectedCategoryTitle="";
    private  void categoryDialog(){
        //make string array from arraylist of string
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        for(int i=0; i<categoryTitleArraylist.size(); i++){
            categoriesArray[i] = categoryTitleArraylist.get(i);
        }

        //Alert Dialog
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