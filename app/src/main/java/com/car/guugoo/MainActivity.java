package com.car.guugoo;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int CHOOSE_PHOTO = 2;
    private ImageView picture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView button = (TextView) findViewById(R.id.btn_choice);
        this.picture = (ImageView) findViewById(R.id.iv_picture);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View param1View) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    MainActivity.this.openAlbum();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE"}, 1);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    // android.support.v4.app.FragmentActivity, android.app.Activity, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != 0) {
                        Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                openAlbum();
                return;
            }
            Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && resultCode == -1) {
            handleImageOnKitKat(data);
        }
    }


    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = "_id=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Log.d("TEST", imagePath);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId).longValue());
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        Log.d("aaaaaaaaaa", "ima " + imagePath);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            writeImage(imagePath, uri);
        } else {
            writeImage(imagePath);
        }

    }


    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("_data"));
            }
            cursor.close();
        }
        return path;
    }


    private void writeImage(String imagePath) {
        byte[] encode;
        if (imagePath == null) {
            Toast.makeText(this, "获取图片失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap2 = toConformBitmap(bitmap);
        this.picture.setImageBitmap(bitmap2);
        int i = 100;
        do {
            bitmap2.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            encode = Base64.encode(bytes, 0);
            i -= 10;
        } while (encode.length > 100000);
        try {
            if (!Environment.getExternalStorageState().equals("mounted")) {
                Toast.makeText(this, "SDCard不存在或不可写", Toast.LENGTH_SHORT).show();
            } else {
                File file = new File(Environment.getExternalStorageDirectory(), "sansanmm");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(encode);
                outputStream.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void writeImage(String imagePath, Uri imageUri) {
        byte[] encode;
        if (imagePath == null) {
            Toast.makeText(this, "获取图片失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap2 = toConformBitmap3(bitmap, imageUri);
        this.picture.setImageBitmap(bitmap2);
        int i = 100;
        do {
            bitmap2.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            encode = Base64.encode(bytes, 0);
            i -= 10;
        } while (encode.length > 100000);
        try {
            if (!Environment.getExternalStorageState().equals("mounted")) {
                Toast.makeText(this, "SDCard不存在或不可写", Toast.LENGTH_SHORT).show();
            } else {
                File file = new File(Environment.getExternalStorageDirectory(), "sansanmm");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(encode);
                outputStream.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private Bitmap toConformBitmap(Bitmap paramBitmap) {
        paramBitmap.getWidth();
        paramBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.parseColor("#000000"));
        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.setRotate((int) (Math.random() * 20.0D), 100.0F, 100.0F);
        matrix.postScale(280.0F / paramBitmap.getWidth(), 340.0F / paramBitmap.getHeight());
        matrix.postTranslate(100.0F, 50.0F);
        canvas.drawBitmap(paramBitmap, matrix, null);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Bitmap toConformBitmap3(Bitmap paramBitmap, Uri imageUri) {
        Bitmap cropBitmap = paramBitmap;
        // 创建一个空的400x400 ARGB_8888格式的Bitmap对象
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        // 用黑色填充整个Bitmap
        bitmap.eraseColor(Color.parseColor("#000000"));

        // 获取原始图像的宽度和高度
        int srcWidth = cropBitmap.getWidth();
        int srcHeight = cropBitmap.getHeight();

        // 通过 ExifInterface 获取图像的旋转信息
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(imageUri));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                default:
                    rotation = 0;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果原始图像是竖直方向的，则进行横向变换以适应目标尺寸
        Matrix matrix = new Matrix();
        if (rotation == 90 || rotation == 270) {
            matrix.setRotate(rotation, srcWidth / 2f, srcHeight / 2f);
            int temp = srcWidth;
            srcWidth = srcHeight;
            srcHeight = temp;
        }

        // 计算缩放比例和平移距离
        float scaleWidth = 280f / srcWidth;
        float scaleHeight = 340f / srcHeight;
        float translateX = (400 - srcWidth * scaleWidth) / 2f;
        float translateY = (400 - srcHeight * scaleHeight) / 2f;

        // 将原始图像进行旋转、缩放和平移，绘制到目标Bitmap上
        matrix.postScale(scaleWidth, scaleHeight);
        matrix.postTranslate(translateX, translateY);
        matrix.postTranslate(-50, 50);
//        matrix.setRotate((int) (Math.random() * 20.0D), 100.0F, 100.0F);
//        matrix.postScale(280.0F / paramBitmap.getWidth(), 340.0F / paramBitmap.getHeight());
//        matrix.postTranslate(100.0F, 50.0F);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(cropBitmap, matrix, null);

        // 保存并返回目标Bitmap
        canvas.save();
        canvas.restore();
        return bitmap;
    }


    private Bitmap cropBitmap(Bitmap paramBitmap) {
        int srcWidth = paramBitmap.getWidth();
        int srcHeight = paramBitmap.getHeight();
        float scale = 1.0f;
        int startX = 0;
        int startY = 0;

        // 计算缩放比例
        if (srcWidth < 3456 || srcHeight < 4608) {
            scale = Math.max(3456.0f / srcWidth, 4608.0f / srcHeight);
        } else {
            scale = Math.min(3456.0f / srcWidth, 4608.0f / srcHeight);
        }

        // 计算裁剪的起点坐标
        int scaledWidth = (int) (srcWidth * scale);
        int scaledHeight = (int) (srcHeight * scale);
        if (scaledWidth > 3456) {
            startX = (scaledWidth - 3456) / 2;
        }
        if (scaledHeight > 4608) {
            startY = (scaledHeight - 4608) / 2;
        }

        // 创建目标Bitmap
        Bitmap bitmap = Bitmap.createBitmap(3456, 4608, Bitmap.Config.ARGB_8888);

        // 将原始图像进行缩放和平移，并绘制到目标Bitmap上
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(-startX, -startY);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(paramBitmap, matrix, null);

        return bitmap;
    }



}