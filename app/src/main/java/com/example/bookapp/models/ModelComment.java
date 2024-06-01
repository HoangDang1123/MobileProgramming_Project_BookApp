package com.example.bookapp.models;

// Đào Hoàng Đăng - 21110163
public class ModelComment {
    String id; // Mã định danh duy nhất của bình luận
    String bookId; // Mã định danh của quyển sách được bình luận
    String timestamp; // Thời gian tạo bình luận (ở dạng chuỗi)
    String comment; // Nội dung bình luận
    String uid; // Mã định danh của người dùng tạo bình luận

    // Hàm khởi tạo không tham số
    public ModelComment() {
    }

    // Hàm khởi tạo với tất cả các thuộc tính
    public ModelComment(String id, String bookId, String timestamp, String comment, String uid) {
        this.id = id;
        this.bookId = bookId;
        this.timestamp = timestamp;
        this.comment = comment;
        this.uid = uid;
    }

    // Các phương thức getter và setter cho các thuộc tính
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}