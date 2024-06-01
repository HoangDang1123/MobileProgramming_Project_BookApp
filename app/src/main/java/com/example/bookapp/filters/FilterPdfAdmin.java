package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;

// Phan Thị Ngọc Mai - 21110238
public class FilterPdfAdmin extends Filter {
    ArrayList<ModelPdf> filterList; // Danh sách các sách PDF cần lọc
    AdapterPdfAdmin adapterPdfAdmin; // Adapter hiển thị danh sách các sách PDF

    // Hàm khởi tạo, nhận vào danh sách các sách PDF và adapter tương ứng
    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    // Phương thức thực hiện việc lọc danh sách các sách PDF
    @Override
    protected FilterResults performFiltering (CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            // Chuyển từ khóa tìm kiếm thành chữ in hoa để so sánh
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++){
                // Kiểm tra xem tiêu đề sách có chứa từ khóa tìm kiếm hay không
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count =  filteredModels.size(); // Số lượng sách PDF sau khi lọc
            results.values = filteredModels; // Danh sách các sách PDF sau khi lọc
        }
        else {
            results.count = filterList.size(); // Nếu không có từ khóa tìm kiếm, trả về danh sách gốc
            results.values = filterList;
        }
        return results;
    }

    // Phương thức cập nhật giao diện sau khi lọc danh sách các sách PDF
    @Override
    protected void publishResults (CharSequence constraint, FilterResults results) {
        // Cập nhật danh sách sách PDF trong adapter
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        // Thông báo cho adapter về việc dữ liệu thay đổi
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
