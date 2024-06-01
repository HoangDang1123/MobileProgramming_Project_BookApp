package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filters.FilterPdfUser;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfUser filter;

    private RowPdfAdminBinding binding;
    private static final String TAG ="ADAPTER_PDF_USER_TAG";

    // Hàm khởi tạo để khởi tạo adapter với dữ liệu cần thiết
    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    // Tạo một view holder mới cho recycler view
    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfUser(binding.getRoot());
    }

    // Gắn dữ liệu vào view holder
    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();

        // Định dạng timestamp thành ngày tháng năm dễ đọc
        String date = MyApplication.formatTimestamp(timestamp);

        // Đặt dữ liệu vào view holder
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        // Tải PDF và thông tin liên quan
        MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        // Đặt một listener click cho view của item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });

    }

    // Lấy tổng số lượng item trong adapter
    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    // Lấy bộ lọc cho adapter
    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    // Lớp view holder cho recycler view
    class HolderPdfUser extends RecyclerView.ViewHolder{

        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            // Khởi tạo các tham chiếu đến view
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
