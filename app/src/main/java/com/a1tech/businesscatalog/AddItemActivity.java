package com.a1tech.businesscatalog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 22;
    private final String TAG = "AddItemActivity";
    private ImageView ivBack, ivDebterPhoto;
    private EditText etDebterName, etDebterPhone;
    private TextView actionBarText, tvItemPhoto;
    private Button btnDebtorSave;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String urlImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        init();
        setOnClicks();
    }

    private void init() {
        ivBack = findViewById(R.id.iv_back);
        ivDebterPhoto = findViewById(R.id.iv_debtor_photo);
        tvItemPhoto = findViewById(R.id.tv_debter_photo);
        etDebterName = findViewById(R.id.et_debter_name);
        etDebterPhone = findViewById(R.id.et_debter_phone);
        btnDebtorSave = findViewById(R.id.btn_debtor_save);
        actionBarText = findViewById(R.id.action_bar_txt);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void setOnClicks() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        tvItemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set intent to take photo from gallery
                selectImage();
            }
        });
        btnDebtorSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    // Select Image method
    private void selectImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    private void setDataToServer(){

    }

    // UploadImage method
    private void uploadImage() {
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageRef.child("images/" + UUID.randomUUID().toString());

            // adding listeners on upload or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            // Dismiss dialog
                            progressDialog.dismiss();
                            Toast.makeText(AddItemActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImage = uri.toString();

                                    Glide.with(AddItemActivity.this)
                                            .load(urlImage)
                                            .centerCrop()
//                                            .placeholder(R.drawable.i) // if fail to load image
                                            .into(ivDebterPhoto);

                                    Log.e("URL image-> ", uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(AddItemActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "error-> " + e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // Progress Listener for loading
                        // percentage on the dialog box
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of data
            filePath = data.getData();

            uploadImage();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivDebterPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}