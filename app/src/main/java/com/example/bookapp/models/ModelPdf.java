package com.example.bookapp.models;

// Phan Thị Ngọc Mai - 21110238
public class ModelPdf {
    String uid; // Mã định danh của người dùng tải lên tài liệu PDF
    String id; // Mã định danh duy nhất của tài liệu PDF
    String title; // Tiêu đề của tài liệu PDF
    String description; // Mô tả tài liệu PDF
    String categoryId; // Mã định danh của thể loại của tài liệu PDF
    String url; // Đường dẫn URL của tài liệu PDF
    long timestamp; // Thời gian tải lên tài liệu PDF (ở dạng timestamp)
    long viewsCount; // Số lượt xem của tài liệu PDF
    long downloadsCount; // Số lượt tải xuống của tài liệu PDF
    boolean favorite; // Trạng thái yêu thích của tài liệu PDF
    // Hàm khởi tạo không tham số
    public ModelPdf() {
    }

    // Hàm khởi tạo với tất cả các thuộc tính
    public ModelPdf(String uid, String id, String title, String description, String categoryId, String url, long timestamp, long viewsCount, long downloadsCount, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
        this.favorite = favorite;
    }

    // Các phương thức getter và setter cho các thuộc tính
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
