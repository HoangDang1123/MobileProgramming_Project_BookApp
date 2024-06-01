package com.example.bookapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookapp.adapters.AdapterPdfUser;
import com.example.bookapp.databinding.FragmentBooksUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

    // Các biến để lưu thông tin danh mục, loại danh mục và uid người dùng
    private String categoryId;
    private String category;
    private String uid;

    // Danh sách chứa các đối tượng sách PDF
    private ArrayList<ModelPdf> pdfArrayList;
    // Adapter để hiển thị danh sách sách PDF
    private AdapterPdfUser adapterPdfUser;

    // Binding để liên kết với giao diện người dùng
    private FragmentBooksUserBinding binding;

    // Tag để ghi log
    private static final String TAG = "BOOKS_USER_TAG";

    // Constructor mặc định
    public BooksUserFragment() {
    }

    // Phương thức để tạo một thể hiện của fragment với các tham số truyền vào
    public static BooksUserFragment newInstance(String categoryId, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy các tham số từ bundle
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Liên kết giao diện người dùng với lớp FragmentBooksUserBinding
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: Category: " + category);

        // Kiểm tra loại danh mục để tải sách phù hợp
        if (category.equals("All")) {
            loadAllBooks();
        } else if (category.equals("Most Viewed")) {
            loadMostViewedDownloadedBooks("viewCount");
        } else if (category.equals("Most Downloaded")) {
            loadMostViewedDownloadedBooks("downloadsCount");
        } else {
            // Tải sách theo danh mục đã chọn
            loadCategorizedBooks();
        }

        // Tìm kiếm sách
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không làm gì trước khi văn bản thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // Lọc danh sách sách dựa trên từ khóa tìm kiếm
                    adapterPdfUser.getFilter().filter(s);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không làm gì sau khi văn bản thay đổi
            }
        });

        return binding.getRoot();
    }

    // Phương thức để tải sách theo danh mục đã chọn
    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();

        // Tham chiếu đến Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Xóa danh sách cũ
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Lấy dữ liệu sách và thêm vào danh sách
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                        }
                        // Thiết lập adapter và gán cho RecyclerView
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi nếu có
                    }
                });
    }

    // Phương thức để tải sách được xem nhiều nhất hoặc tải xuống nhiều nhất
    private void loadMostViewedDownloadedBooks(String orderBy) {
        pdfArrayList = new ArrayList<>();

        // Tham chiếu đến Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Xóa danh sách cũ
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Lấy dữ liệu sách và thêm vào danh sách
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                        }
                        // Thiết lập adapter và gán cho RecyclerView
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi nếu có
                    }
                });
    }

    // Phương thức để tải tất cả sách
    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();

        // Tham chiếu đến Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ
                pdfArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Lấy dữ liệu sách và thêm vào danh sách
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                // Thiết lập adapter và gán cho RecyclerView
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.bookRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }
}