package com.example.bookapp;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Phương thức định dạng timestamp thành chuỗi ngày tháng năm
    public static final String formatTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;
    }

    // Phương thức xóa sách từ Firebase Storage và Firebase Database
    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting " + bookTitle + "....");
        progressDialog.show();

        // Xóa sách từ Firebase Storage
        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");

                        // Xóa thông tin sách từ Firebase Database
                        Log.d(TAG, "onSuccess: Now deleting info from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSucess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Phương thức tải kích thước của file PDF từ Firebase Storage
    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess:" + pdfTitle + " " + bytes);

                        // Chuyển đổi kích thước từ bytes sang KB hoặc MB
                        double kb = bytes / 1024;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            sizeTv.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            sizeTv.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            sizeTv.setText(String.format("%.2f", bytes) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    // Phương thức tải một trang đơn từ file PDF từ Firebase Storage
    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + pdfTitle + " successfully got the file");

                        // Hiển thị trang đầu tiên của file PDF
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");

                                        // Hiển thị số trang nếu có
                                        if (pagesTv != null) {
                                            pagesTv.setText("" + nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed getting file from url due to " + e.getMessage());
                    }
                });
    }

    // Phương thức tải tên danh mục từ Firebase Database
    public static void loadCategory(String categoryId, TextView categoryTv) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lấy tên danh mục và hiển thị
                        String category = "" + snapshot.child("category").getValue();
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi nếu có
                    }
                });
    }

    // Phương thức tăng số lượng lượt xem của sách
    public static void incrementBookViewCount(String bookId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = "" + snapshot.child("viewCount").getValue();
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }
                        long newViewsCount = Long.parseLong(viewsCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        // Cập nhật số lượng lượt xem mới trong Firebase Database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi nếu có
                    }
                });
    }

    // Phương thức tải sách từ Firebase Storage và lưu vào thiết bị
    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book... ");

        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading " + nameWithExtension + "..."); // Ví dụ: Downloding ABC_Book.pdf
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm này dùng để lưu trữ cuốn sách đã tải xuống
    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try{
            // Tạo thư mục Downloads nếu chưa tồn tại
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            // Đường dẫn tới file sách được lưu trữ
            String filePath = downloadsFolder.getPath() + "/" +nameWithExtension;

            // Lưu nội dung sách vào file
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            // Hiển thị thông báo và ghi log
            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            // Tăng số lần tải sách lên
            incrementBookDownloadCount(bookId);
        }
        catch (Exception e){
            // Hiển thị thông báo và ghi log nếu lưu trữ thất bại
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to"+e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    // Hàm này dùng để tăng số lần tải sách lên
    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing Book Download Count");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: "+downloadsCount);

                        // Nếu chưa có dữ liệu, đặt mặc định là 0
                        if (downloadsCount.equals("") || downloadsCount.equals("null")){
                            downloadsCount = "0";
                        }

                        // Tăng số lần tải lên 1
                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG_DOWNLOAD,"onDataChange: New Download Count: "+newDownloadsCount);

                        // Thiết lập dữ liệu để cập nhật
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        // Cập nhật số lần tải mới vào Firebase
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference ("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count updated..." );
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to"+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // Hàm này dùng để tải số trang của một cuốn sách từ Firebase Storage
    public static void loadPdfPageCount (Context context, String pdfUrl, TextView pagesTv) {
        // Lấy tệp PDF từ Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference
                .getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //Đã nhận được file
                        // Đọc số trang của file PDF bằng thư viện PdfView
                        PDFView pdfView = new PDFView(context, null);
                        pdfView.fromBytes(bytes)
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        // PDF đã được tải từ bytes từ Firebase Storage, có thể hiển thị số trang
                                        pagesTv.setText("" + nbPages);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Không nhận được file
                    }
                });
    }

    // Hàm này dùng để thêm sách vào danh sách yêu thích
    public static void addToFavorite(Context context, String bookId) {
        // Chỉ có thể thêm nếu người dùng đã đăng nhập
        // 1) Kiểm tra nếu người dùng đã đăng nhập
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            // Chưa đăng nhập, không thể thêm vào yêu thích
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            // Thiết lập dữ liệu để thêm vào cơ sở dữ liệu Firebase của người dùng hiện tại cho sách yêu thích
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timestamp", "" + timestamp);

            //Lưu vào cơ sở dữ liệu
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to your favourites list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add to favorite due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Hàm này dùng để xóa sách khỏi danh sách yêu thích
    public static void removeFromFavorite (Context context, String bookId) {
        // Chỉ có thể xóa nếu người dùng đã đăng nhập
        // 1) Kiểm tra nếu người dùng đã đăng nhập
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            // Chưa đăng nhập, không thể xóa khỏi yêu thích
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {
            // Xóa khỏi cơ sở dữ liệu
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from your favorites list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to remove from favorite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
