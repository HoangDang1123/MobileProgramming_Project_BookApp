package com.example.bookapp.models;

// Phan Thị Ngọc Mai - 21110238
public class ModelCategory {
    String id; // Mã định danh duy nhất của danh mục
    String category; // Tên của danh mục
    String uid; // Mã định danh của người dùng tạo ra danh mục
    long timestamp; // Thời gian tạo danh mục (theo số mili giây kể từ 1/1/1970)

    // Hàm khởi tạo không tham số
    public ModelCategory(){

    }

    // Hàm khởi tạo với tất cả các thuộc tính
    public ModelCategory(String id,String category, String uid, long timestamp){
        this.id = id;
        this.category= category;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    // Các phương thức getter và setter cho các thuộc tính
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}