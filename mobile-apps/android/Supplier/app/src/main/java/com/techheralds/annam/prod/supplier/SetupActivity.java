package com.techheralds.annam.prod.supplier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {
    CircleImageView userDp;
    Button doneBtn;
    EditText userName;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    Uri profileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Setup Profile");
        }

        userName = findViewById(R.id.userName);
        userDp = findViewById(R.id.userDp);
        doneBtn = findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userName.getText().toString().equals("")) {
                    uploadData();
                } else {
                    Toast.makeText(SetupActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        userDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1, 1).setOutputCompressQuality(50).setRequestedSize(400, 400)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SetupActivity.this);
            }
        });
    }

    private void uploadData() {
        final ProgressDialog progressDialog = ProgressDialog.show(SetupActivity.this, "", "Setting up Profile.Please wait...");
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        final String name = userName.getText().toString().trim();
        SharedPreferences sharedPreferences = SetupActivity.this.getSharedPreferences("local", Context.MODE_PRIVATE);
        final String mainSupplier = sharedPreferences.getString("mainSupplier", "");

        if (profileUri != null) {
            firebaseStorage.getReference().child("Dp/" + user.getUid()).putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    firebaseStorage.getReference().child("Dp/" + user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {


                            //Update firebase user profile

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(Uri.parse(uri.toString()))
                                    .build();

                            user.updateProfile(profileUpdates);


                            Supplier newUser = new Supplier(name,firebaseAuth.getCurrentUser().getUid(),firebaseAuth.getCurrentUser().getPhoneNumber(),"","",true,firebaseAuth.getCurrentUser().getMetadata().getCreationTimestamp(),uri.toString(),"",0,"",mainSupplier);
                            firebaseDatabase.getReference().child("suppliers/" + user.getUid()).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SetupActivity.this, "Error occurred.Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).

                    addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SetupActivity.this, "Error occurred.Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //Update firebase user profile
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(null)
                    .build();

            user.updateProfile(profileUpdates);

            Supplier newUser = new Supplier(name,firebaseAuth.getCurrentUser().getUid(),firebaseAuth.getCurrentUser().getPhoneNumber(),"","",true,firebaseAuth.getCurrentUser().getMetadata().getCreationTimestamp(),"","",0,"",mainSupplier);
            firebaseDatabase.getReference().child("suppliers/" + user.getUid()).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SetupActivity.this, "Error occurred.Please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileUri = result.getUri();

                userDp.setImageURI(profileUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
