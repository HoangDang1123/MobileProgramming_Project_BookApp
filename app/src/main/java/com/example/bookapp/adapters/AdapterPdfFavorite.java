package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

// Đào Hoàng Đăng - 21110163
public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{
    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    // Hàm khởi tạo, nhận vào context và danh sách các PDF yêu thích
    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    // Hàm này sẽ được gọi khi cần tạo một ViewHolder mới
    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate giao diện row_pdf_favorite.xml và trả về một HolderPdfFavorite
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    // Hàm này sẽ được gọi để bind dữ liệu vào một ViewHolder
    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull HolderPdfFavorite holder, int position) {
        // Lấy đối tượng ModelPdf tương ứng với vị trí position
        ModelPdf model = pdfArrayList.get(position);

        // Gọi hàm loadBookDetails để tải thông tin của cuốn sách
        loadBookDetails(model, holder);

        // Xử lý sự kiện khi người dùng click vào một cuốn sách
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở màn hình chi tiết của cuốn sách
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);
            }
        });

        // Xử lý sự kiện khi người dùng click vào nút xóa khỏi danh sách yêu thích
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm xóa sách khỏi danh sách yêu thích
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });
    }

    // Hàm này sẽ tải thông tin của cuốn sách từ Firebase
    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: Book Details of Book ID: "+bookId);

        // Truy cập vào nút "Books" trong Firebase Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lấy các thông tin của cuốn sách từ Firebase
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                        // Cập nhật thông tin vào đối tượng ModelPdf
                        model.setFavorite (true);
                        model.setTitle(bookTitle);
                        model.setDescription (description);
                        model.setTimestamp (Long.parseLong(timestamp));
                        model.setCategoryId (categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        // Định dạng thời gian
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        // Tải các thông tin khác như danh mục, PDF, kích thước
                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

                        // Cập nhật các thông tin lên giao diện
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                        // Xử lý khi có lỗi xảy ra
                    }
                });
    }

    // Trả về số lượng các cuốn sách yêu thích
    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    // ViewHolder để giữ các view của một item trong RecyclerView
    class HolderPdfFavorite extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;
        public HolderPdfFavorite(@NonNull View itemView){
            super(itemView);

            // Ánh xạ các view trong giao diện row_pdf_favorite.xml
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv  = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}
