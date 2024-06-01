package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.models.ModelCategory;

import java.util.ArrayList;

// Phan Thị Ngọc Mai - 21110238
public class FilterCategory extends Filter {
    ArrayList<ModelCategory> filterList; // Danh sách các danh mục sách cần lọc
    AdapterCategory adapterCategory; // Adapter hiển thị danh sách các danh mục sách

    // Hàm khởi tạo, nhận vào danh sách các danh mục sách và adapter tương ứng
    public FilterCategory (ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    // Phương thức thực hiện việc lọc danh sách các danh mục sách
    @Override
    protected FilterResults performFiltering (CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            // Chuyển từ khóa tìm kiếm thành chữ in hoa để so sánh
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++){
                // Kiểm tra xem tên danh mục có chứa từ khóa tìm kiếm hay không
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count =  filteredModels.size(); // Số lượng danh mục sách sau khi lọc
            results.values = filteredModels; // Danh sách các danh mục sách sau khi lọc
        }
        else {
            results.count = filterList.size(); // Nếu không có từ khóa tìm kiếm, trả về danh sách gốc
            results.values = filterList;
        }
        return results;
    }

    // Phương thức cập nhật giao diện sau khi lọc danh sách các danh mục sách
    @Override
    protected void publishResults (CharSequence constraint, FilterResults results) {
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();
    }
}
