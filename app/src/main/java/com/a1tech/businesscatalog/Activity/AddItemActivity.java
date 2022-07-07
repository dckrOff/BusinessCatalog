package com.a1tech.businesscatalog.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.MotionButton;

import com.a1tech.businesscatalog.Model.Item;
import com.a1tech.businesscatalog.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 22;
    private final String TAG = "AddItemActivity";
    private ImageView ivItemPhoto;
    private EditText etItemName, etItemAmount, etItemPrice;
    private TextView tvItemPhoto;
    private MotionButton btnItemSave;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String urlImage;
    private ArrayList<Item> mList = new ArrayList();
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        init();
        getList();
        setOnClicks();
    }

    private void init() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("items_list");
        ivItemPhoto = findViewById(R.id.iv_item_photo);
        tvItemPhoto = findViewById(R.id.tv_item_photo);
        etItemName = findViewById(R.id.et_item_name);
        etItemAmount = findViewById(R.id.et_item_amount);
        btnItemSave = findViewById(R.id.btn_item_save);
        etItemPrice = findViewById(R.id.et_item_price);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void setOnClicks() {
        tvItemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set intent to take photo from gallery
                selectImage();
            }
        });
        btnItemSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onBackPressed();
                if (!etItemName.getText().toString().isEmpty() && !etItemAmount.getText().toString().isEmpty() && !etItemPrice.getText().toString().isEmpty()) {
                    uploadImage();
                } else {
                    Toast.makeText(AddItemActivity.this, "Iltimos maydonlarni to'ldiring", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getList() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mList.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    Log.e(TAG, "1) " + childDataSnapshot.getKey()); //displays the key for the node
//                    Log.e(TAG, "2) " + childDataSnapshot.child("img").getValue());   //gives the value for given keyname
                    mList.add(new Item(childDataSnapshot.child("itemName").getValue().toString(), childDataSnapshot.child("itemPrice").getValue().toString(), childDataSnapshot.child("itemImg").getValue().toString(), childDataSnapshot.child("itemAmount").getValue().toString()));
                }
                Log.e(TAG, "list size-> " + mList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
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

    private void loadDataToServer() {
        myRef.setValue(mList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddItemActivity.this, "Data is added to server successfully", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });
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
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    urlImage = uri.toString();

                                    Glide.with(AddItemActivity.this)
                                            .load(urlImage)
                                            .centerCrop()
//                                            .placeholder(R.drawable.i) // if fail to load image
                                            .into(ivItemPhoto);

                                    Log.e("URL image-> ", uri.toString());

                                    mList.add(new Item(etItemName.getText().toString(), etItemPrice.getText().toString(), uri.toString(), etItemAmount.getText().toString()));
                                    loadDataToServer();
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
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivItemPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}