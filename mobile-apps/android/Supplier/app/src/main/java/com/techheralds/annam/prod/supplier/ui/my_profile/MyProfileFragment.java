package com.techheralds.mymaligai.prod.supplier.ui.my_profile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.techheralds.annam.prod.supplier.R;
import com.techheralds.annam.prod.supplier.Supplier;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class MyProfileFragment extends Fragment {
    tagsAdapterList adapterList;
    ListView listView;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    ArrayList<Map<String, Object>> tags;
    ArrayList<String> allTags;
    CircleImageView dp;
    TextView name, phoneNumber, headerText;
    Button editBtn, addTagsBtn, addUserBtn, listUserBtn;
    TextView smsTemplteText, upiIdText;
    String smsTemplte, userUpiId = "";
    ArrayList<Map<String, Object>> users = new ArrayList<>();
    usersAdapterList usersAdapterList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_my_profile, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        tags = new ArrayList<>();
        allTags = new ArrayList<>();

        listView = root.findViewById(R.id.profileUserTagsList);
        smsTemplteText = root.findViewById(R.id.profileSmsTemplate);
        upiIdText = root.findViewById(R.id.upiId);
        headerText = root.findViewById(R.id.profileTagsheader);
        addUserBtn = root.findViewById(R.id.addUser);
        listUserBtn = root.findViewById(R.id.listUser);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);
        smsTemplte = sharedPreferences.getString("smsTemplate", "");
        smsTemplteText.setText(smsTemplte);

        if (smsTemplte != null) {
            if (smsTemplte.equalsIgnoreCase("")) {
                firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/smsTemplate").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            smsTemplteText.setText(dataSnapshot.getValue().toString());

                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("local", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("smsTemplate", dataSnapshot.getValue().toString());

                            editor.apply();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        editBtn = root.findViewById(R.id.profileEditBtn);
        addTagsBtn = root.findViewById(R.id.profileAddTagsBtn);

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        int halfWidth = width / 2;

        editBtn.setWidth(halfWidth - 16);
        addTagsBtn.setWidth(halfWidth - 16);
        addUserBtn.setWidth(halfWidth - 16);
        listUserBtn.setWidth(halfWidth - 16);

        dp = root.findViewById(R.id.profileUserDp);
        name = root.findViewById(R.id.profileUserName);
        phoneNumber = root.findViewById(R.id.profileUserPhoneNumber);

        name.setText(firebaseAuth.getCurrentUser().getDisplayName());

        phoneNumber.setText(firebaseAuth.getCurrentUser().getPhoneNumber());

        Uri photoUrl = firebaseAuth.getCurrentUser().getPhotoUrl();

        if (photoUrl != null) {
            Picasso.with(getContext()).load(photoUrl).into(dp);
        } else {
            dp.setImageResource(R.drawable.nouser);
        }

        //UPI ID

        firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/upiId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    upiIdText.setText(dataSnapshot.getValue().toString());
                    userUpiId = dataSnapshot.getValue().toString();
                } else {
                    upiIdText.setText("UPI ID not set.Tap on edit profile to set UPI Id");
                    upiIdText.setTextSize(15);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Add tags Btn

        addTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheerDialog = new BottomSheetDialog(getContext());
                bottomSheerDialog.setContentView(R.layout.edit_tag_sheet);
                bottomSheerDialog.show();

                Button updateBtn = bottomSheerDialog.findViewById(R.id.updateTagsBtn);
                final EditText updateInput = bottomSheerDialog.findViewById(R.id.updateTagsInput);

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!updateInput.getText().toString().equals("")) {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Updating Categories...");
                            String[] tempTags = updateInput.getText().toString().split(",");

                            for (String tag : tempTags) {
                                if (allTags.indexOf(tag.toLowerCase().trim()) == -1) {
                                    Map<String, Object> data = new HashMap<>();
                                    String randomId = UUID.randomUUID().toString();
                                    data.put("category", tag.trim().toLowerCase());
                                    data.put("id", randomId);
                                    tags.add(data);

                                    firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/tags/" + randomId).setValue(tag.trim().toLowerCase());
                                }
                                headerText.setText("List of Categories");
                                progressDialog.dismiss();
                                adapterList = new tagsAdapterList(getContext(), tags);
                                listView.setAdapter(adapterList);
                                bottomSheerDialog.dismiss();
                            }
                        } else {
                            Toast.makeText(getContext(), "Enter Some Category", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //Edit Profile Dialog
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Edit Profile");
                builder.setItems(new CharSequence[]
                                {"Edit Profile Picture", "Edit SMS Template", "Edit UPI Id"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        editProfile(0);
                                        break;

                                    case 1:
                                        editProfile(1);
                                        break;
                                    case 2:
                                        editUpiId();
                                        break;
                                }
                            }
                        });

                builder.create().show();
            }
        });

        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                bottomSheetDialog.setContentView(R.layout.add_user_sheet);

                bottomSheetDialog.show();

                final EditText phNoIp = bottomSheetDialog.findViewById(R.id.phoneNumberIp);
                Button btn = bottomSheetDialog.findViewById(R.id.btn);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String phNo = phNoIp.getText().toString();

                        if (!phNo.trim().equalsIgnoreCase("")) {
                            if (phNo.trim().length() == 10) {
                                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Adding User...");

                                firebaseDatabase.getReference().child("suppliers/").orderByChild("phoneNumber").equalTo("+91" + phNo.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                        if (dataSnapshot1.getChildrenCount() > 0) {
                                            for (DataSnapshot ds : dataSnapshot1.getChildren()) {
                                                final Supplier data = ds.getValue(Supplier.class);
                                                firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/users/+91" + phNo.trim()).setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        firebaseDatabase.getReference().child("main/+91" + phNo.trim()).setValue(getSupplierId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                firebaseDatabase.getReference().child("suppliers/" + data.getUid() + "/mainSupplier").setValue(getSupplierId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        progressDialog.dismiss();
                                                                        bottomSheetDialog.dismiss();
                                                                        Toast.makeText(getContext(), "User Added", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/users/+91" + phNo.trim()).setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    firebaseDatabase.getReference().child("main/+91" + phNo.trim()).setValue(getSupplierId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            bottomSheetDialog.dismiss();
                                                            Toast.makeText(getContext(), "User Added", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Enter 10 Digit Phone Number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Enter Phone Number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        listUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                users.clear();

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                bottomSheetDialog.setContentView(R.layout.list_users_sheet);
                final ListView listView = bottomSheetDialog.findViewById(R.id.listView);

                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");

                firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                firebaseDatabase.getReference().child("suppliers/").orderByChild("phoneNumber").equalTo(ds.getKey())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                                                   Object data = ds1.getValue();

                                                    users.add((Map<String, Object>) data);

                                                    usersAdapterList = new usersAdapterList(getContext(),users);
                                                    listView.setAdapter(usersAdapterList);

                                                    progressDialog.dismiss();
                                                    bottomSheetDialog.show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "No Users Added", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("category", ds.getValue());
                        data.put("id", ds.getKey());
                        tags.add(data);
                        allTags.add(ds.getValue().toString()); //to check index of elements
                        adapterList = new tagsAdapterList(getContext(), tags);
                        listView.setAdapter(adapterList);
                    }
                } else {
                    headerText.setText("List of Categories (No Categories Added)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Can't Load Data", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    public String getSupplierId() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("local", Context.MODE_PRIVATE);
        final String mainSupplier = sharedPreferences.getString("mainSupplier", "");

        if (mainSupplier.equalsIgnoreCase("")) {
            return firebaseAuth.getCurrentUser().getUid();
        } else {
            return mainSupplier;
        }
    }

    public void editUpiId() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.edit_upi_id_sheet);

        bottomSheetDialog.show();
        Button upiBtn = bottomSheetDialog.findViewById(R.id.updateUpiBtn);
        final EditText upiInput = bottomSheetDialog.findViewById(R.id.upiIdInput);
        if (!userUpiId.equals("")) {
            upiInput.setText(userUpiId);
        }

        upiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!upiInput.getText().toString().trim().equals("")) {
                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");
                    firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/upiId").setValue(upiInput.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            bottomSheetDialog.dismiss();
                            userUpiId = upiInput.getText().toString().trim();
                            upiIdText.setText(upiInput.getText().toString().trim());
                            upiIdText.setTextSize(17);
                            Toast.makeText(getContext(), "UPI Id updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Enter UPI Id", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void editProfile(int type) {
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        switch (type) {
            case 0:
                CropImage.activity().setAspectRatio(1, 1).setOutputCompressQuality(50).setRequestedSize(400, 400)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getContext(), MyProfileFragment.this);
                break;
                                 /*   case 1:
                                        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                                        bottomSheetDialog.setContentView(R.layout.edit_name_sheet);

                                        bottomSheetDialog.show();

                                        Button updateBtn = bottomSheetDialog.findViewById(R.id.updateNameBtn);
                                        final EditText updateInput = bottomSheetDialog.findViewById(R.id.updateNameInput);
                                        updateInput.setText(user.getDisplayName());
                                        updateBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!updateInput.getText().toString().equals("")) {
                                                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Updating Name...");
                                                    firebaseDatabase.getReference().child("suppliers/").orderByChild("name").equalTo(updateInput.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Supplier name already exists.Try another name", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                firebaseDatabase.getReference().child("suppliers/" + firebaseAuth.getCurrentUser().getUid() + "/name").setValue(updateInput.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        //Update firebase user profile

                                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                .setDisplayName(updateInput.getText().toString().trim())
                                                                                .build();

                                                                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                                                                        progressDialog.dismiss();
                                                                        name.setText(updateInput.getText().toString().trim());
                                                                        bottomSheetDialog.dismiss();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getContext(), "Error occurred.Try again!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getContext(), "Enter Name", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        break;*/
            case 1:
                final BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(getContext());
                bottomSheetDialog1.setContentView(R.layout.edit_sms_template_sheet);
                final EditText smsIp;
                Button button;

                smsIp = bottomSheetDialog1.findViewById(R.id.smsIp);
                button = bottomSheetDialog1.findViewById(R.id.btn);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);
                smsIp.setText(sharedPreferences.getString("smsTemplate", ""));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String smsValue = smsIp.getText().toString();
                        if (!smsValue.equals("")) {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");
                            firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/smsTemplate").setValue(smsValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Save sms template locally
                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("smsTemplate", smsValue);
                                    editor.apply();

                                    smsTemplteText.setText(smsValue);
                                    progressDialog.dismiss();
                                    bottomSheetDialog1.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Can't update.Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Enter SMS Template", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                bottomSheetDialog1.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Uploading Profile Picture...");

                firebaseStorage.getReference().child("Dp/" + user.getUid()).putFile(result.getUri()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        firebaseStorage.getReference().child("Dp/" + user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri url) {
                                //Update firebase user profile

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        //        .setDisplayName(name)
                                        .setPhotoUri(Uri.parse(url.toString()))
                                        .build();

                                user.updateProfile(profileUpdates);
                                Picasso.with(getContext()).load(url).into(dp);

                                writeJsonData("supplierdetails.json", url.toString());
                                SharedPreferences sharedPreferences = getContext().getSharedPreferences("local", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isChange", true);
                                editor.putString("url", url.toString());
                                editor.apply();

                                firebaseDatabase.getReference().child("suppliers/").child(user.getUid()).child("photo").setValue(url.toString()).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        //Update firebase user profile


                                    }
                                });

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error occurred.Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public class tagsAdapterList extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> tags;

        public tagsAdapterList(Context context, ArrayList<Map<String, Object>> tags) {
            this.context = context;
            this.tags = tags;
        }

        @Override
        public int getCount() {
            return tags.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.tags_list, parent, false);
            TextView title = view.findViewById(R.id.listTagName);
            final String captializeTag = capitalize(tags.get(position).get("category").toString());

            title.setText(captializeTag);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext()).setTitle("Manage Category")
                            .setMessage(captializeTag)
                            .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                                    bottomSheetDialog.setContentView(R.layout.edit_category_sheet);

                                    final EditText updateIp = bottomSheetDialog.findViewById(R.id.updateInput);
                                    updateIp.setText(tags.get(position).get("category").toString());

                                    Button updateBtn = bottomSheetDialog.findViewById(R.id.updateBtn);

                                    updateBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!updateIp.getText().toString().trim().equals("")) {
                                                final String category = updateIp.getText().toString().toLowerCase().trim();
                                                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");
                                                firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/tags/" + tags.get(position).get("id")).setValue(category).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        bottomSheetDialog.dismiss();
                                                        tags.get(position).put("category", category);
                                                        adapterList = new tagsAdapterList(getContext(), tags);
                                                        listView.setAdapter(adapterList);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(getContext(), "Enter Category", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    bottomSheetDialog.show();
                                }
                            })
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(getContext()).setTitle("Delete Category")
                                            .setMessage("Are you sure to delete " + captializeTag + "?")
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");
                                                    firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/tags/" + tags.get(position).get("id")).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseDatabase.getReference().child("inventory/" + getSupplierId() + "/" + tags.get(position).get("id")).removeValue();
                                                            progressDialog.dismiss();
                                                            tags.remove(tags.get(position));
                                                            if (tags.size() == 0) {
                                                                headerText.setText("List of Categories");
                                                            }
                                                            adapterList = new tagsAdapterList(getContext(), tags);
                                                            listView.setAdapter(adapterList);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }).setNegativeButton("Cancel", null)
                                            .show();
                                }
                            }).show();
                }
            });
            return view;
        }
    }

    public void writeJsonData(String params, String url) {
        try {
            InputStream is = getContext().getAssets().open(params);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String mResponse = new String(buffer);
            JSONObject jsonObject = new JSONObject(mResponse);

            if (!jsonObject.get("SUPPLIERLOGO").toString().equals("")) {
                jsonObject.put("SUPPLIERLOGO", url);
            }

        } catch (IOException e) {
            // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class usersAdapterList extends BaseAdapter {
        ArrayList<Map<String, Object>> users;
        Context context;

        public usersAdapterList(Context context, ArrayList<Map<String, Object>> users) {
            this.users = users;
            this.context = context;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.users_list, parent, false);
            TextView name = view.findViewById(R.id.name);
            CircleImageView avatar = view.findViewById(R.id.avatar);
            TextView number = view.findViewById(R.id.phoneNumber);

            name.setText(users.get(position).get("name").toString());
            number.setText(users.get(position).get("phoneNumber").toString());

            if(users.get(position).get("photo") != null){
                if(!users.get(position).get("photo").equals("")){
                    Picasso.with(getContext()).load(users.get(position).get("photo").toString()).into(avatar);
                }
            }

            return view;
        }
    }

}