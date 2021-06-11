package com.rono.barcode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;

import java.io.File;

import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class MainActivity extends AppCompatActivity
    implements View.OnClickListener,
        BarcodeScanner.BarcodeListeners,
        PermissionCallback,
        ErrorCallback {
  public static TextView tvresult;
  public static final int CAMERA_PERM_CODE = 101;
  public static final int CAMERA_REQUEST_CODE = 102;
  public static final int GALLERY_REQUEST_CODE = 105;
  static final int REQUEST_IMAGE_CAPTURE = 1;
  ImageView selectedImage;
  String currentPhotoPath;

  private static  final String resultDefault="Result will be here";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    checkPermissions();

    init();
  }

  void init() {
    tvresult = (TextView) findViewById(R.id.tvresult);
    Button btnQrScanner = (Button) findViewById(R.id.btn_qr_scanner);
    Button btnMLKit = (Button) findViewById(R.id.btn_ml_scanner);
    btnQrScanner.setOnClickListener(this);
    btnMLKit.setOnClickListener(this);
  }

  private void checkPermissions() {
    new AskPermission.Builder(this)
        .setPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        .setCallback(this)
        .setErrorCallback(this)
        .request(100);
  }

  private void callBarCodeScanner(Uri bitmap) {
    BarcodeScanner barcodeScanner = new BarcodeScanner(getApplicationContext(),bitmap);
    barcodeScanner.listeners(this);
  }

  @Override
  public void onSuccessListener(String data) {
    tvresult.setText(data);
  }

  @Override
  public void onFailureListener() {}

  @Override
  public void onPermissionsGranted(int requestCode) {}

  @Override
  public void onPermissionsDenied(int requestCode) {}

  @Override
  public void onShowRationalDialog(PermissionInterface permissionInterface, int requestCode) {}

  @Override
  public void onShowSettings(PermissionInterface permissionInterface, int requestCode) {}

  @Override
  public void onClick(View v) {
    tvresult.setText(resultDefault);
    switch (v.getId()) {
      case R.id.btn_qr_scanner:
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
        break;
      case R.id.btn_ml_scanner:
        dispatchTakePictureIntent();
        break;
    }
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CAMERA_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        File f = new File(currentPhotoPath);
      //  selectedImage.setImageURI(Uri.fromFile(f));
        Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        callBarCodeScanner(contentUri);
      }

    }

    if (requestCode == GALLERY_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Uri contentUri = data.getData();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
        Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageFileName);
        callBarCodeScanner(contentUri);
      }

    }


  }

  private String getFileExt(Uri contentUri) {
    ContentResolver c = getContentResolver();
    MimeTypeMap mime = MimeTypeMap.getSingleton();
    return mime.getExtensionFromMimeType(c.getType(contentUri));
  }


  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
       File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
   // File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
    );

    // Save a file: path for use with ACTION_VIEW intents
    currentPhotoPath = image.getAbsolutePath();
    return image;
  }


  private void dispatchTakePictureIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      // Create the File where the photo should go
      File photoFile = null;
      try {
        photoFile = createImageFile();
      } catch (IOException ex) {

      }
      // Continue only if the File was successfully created
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.rono.android.fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
      }
    }
  }


}
