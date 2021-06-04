package com.techheralds.annam.prod.supplier;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class SecondFragment extends Fragment {
    Button addBundlesBtn;
    ArrayList<bundle> bundles;
    BottomSheetDialog bottomSheetDialog;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    String key, status;
    ArrayList<Map<String, Object>> items;
    bundleAdapterList bundleAdapterList;
    viewBundleAdapter viewBundleAdapter;
    ArrayList<Map<String, Object>> selectedItems;
    ListView listView, itemsListView;
    Uri imageUri;
    ImageView bundleImg;
    viewBundleItemsAdapter viewBundleItemsAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.add_bundle_sheet);

        bottomSheetDialog.setCancelable(false);
        bundleImg = bottomSheetDialog.findViewById(R.id.bundleImg);
        ImageButton backBtn = bottomSheetDialog.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        key = getActivity().getIntent().getExtras().getString("key");
        status = getActivity().getIntent().getExtras().getString("status");
        selectedItems = new ArrayList<>();
        bundles = new ArrayList<>();
        items = new ArrayList<>();
        addBundlesBtn = view.findViewById(R.id.addBundlesBtn);


        listView = view.findViewById(R.id.listView);
        if (status.equalsIgnoreCase("published")) {
            addBundlesBtn.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) listView
                    .getLayoutParams();

            mlp.topMargin = 10;

        }
        itemsListView = bottomSheetDialog.findViewById(R.id.listView);
        final ProgressDialog dialog = ProgressDialog.show(getContext(), null, "Please wait...");

        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/bundles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        bundle data = ds.getValue(bundle.class);

                        bundles.add(data);
                    }

                    viewBundleAdapter = new viewBundleAdapter(getContext(), bundles);
                    listView.setAdapter(viewBundleAdapter);
                }

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addBundlesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageButton doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                final EditText bundleName = bottomSheetDialog.findViewById(R.id.bundleName);

                items.clear();
                selectedItems = new ArrayList<>();

                bundleImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = CropImage.activity()
                                .setAspectRatio(4, 3).setRequestedSize(400, 200)
                                .getIntent(getContext());

                        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                });

                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");

                firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            items.addAll((Collection<? extends Map<String, Object>>) dataSnapshot.getValue());
                            progressDialog.dismiss();
                            bundleAdapterList = new bundleAdapterList(getContext(), items);
                            itemsListView.setAdapter(bundleAdapterList);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Add Items to Inventory first", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                doneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String name = bundleName.getText().toString().trim();

                        if (imageUri != null) {
                            if (!name.equals("")) {
                                if (selectedItems.size() > 0) {
                                    final ProgressDialog progressDialog1 = ProgressDialog.show(getContext(), null, "Please wait...");
                                    firebaseStorage.getReference().child("bundles/" + key + "_" + name.toLowerCase() + "_" + bundles.size()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            firebaseStorage.getReference().child("bundles/" + key + "_" + name.toLowerCase() + "_" + bundles.size()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    bundle data = new bundle();

                                                    data.setName(name.trim());
                                                    data.setItems(selectedItems);
                                                    data.setImg(uri.toString());

                                                    bundles.add(data);

                                                    firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/bundles").setValue(bundles).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            viewBundleAdapter = new viewBundleAdapter(getContext(), bundles);
                                                            listView.setAdapter(viewBundleAdapter);
                                                            progressDialog1.dismiss();
                                                            bottomSheetDialog.dismiss();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog1.dismiss();
                                                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog1.dismiss();
                                            Toast.makeText(getContext(), "Failed to upload image.Try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getContext(), "Add atleast one item to this bundle", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Enter Bundle Name", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Select Bundle Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                bottomSheetDialog.show();

                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        bundleName.setText("");
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                bundleImg.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
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

    public class bundleAdapterList extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> inventories;

        public bundleAdapterList(Context context, ArrayList<Map<String, Object>> inventories) {
            this.context = context;
            this.inventories = inventories;
        }

        @Override
        public int getCount() {
            return inventories.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.select_item_list, parent, false);

            if (inventories.size() > 0) {
                ImageView image = view.findViewById(R.id.itemImg);
                final CheckBox checkBox = view.findViewById(R.id.checkBox);
                final EditText quantityIp, priceIp, minQuantityIp, actualQuantityIp;
                TextView name = view.findViewById(R.id.name);
                TextView quantity, price;
                minQuantityIp = view.findViewById(R.id.minQuantity);
                quantity = view.findViewById(R.id.quantityTxt);
                price = view.findViewById(R.id.priceTxt);
                actualQuantityIp = view.findViewById(R.id.actualQuantity);

                actualQuantityIp.setVisibility(View.VISIBLE);
                price.setVisibility(View.VISIBLE);
                //  quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString());

                quantityIp = view.findViewById(R.id.quantity);
                priceIp = view.findViewById(R.id.price);

                //  quantity.setVisibility(View.VISIBLE);
                quantityIp.setVisibility(View.GONE);
                priceIp.setVisibility(View.GONE);
                minQuantityIp.setVisibility(View.VISIBLE);

                if (inventories.get(position).get("img") != null) {
                    if (!inventories.get(position).get("img").toString().equals("")) {
                        Picasso.with(getContext()).load(inventories.get(position).get("img").toString()).into(image);
                    }
                }

                if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                    checkBox.setChecked(true);
                    minQuantityIp.setText(selectedItems.get(findIndexOf(inventories.get(position).get("sku").toString())).get("minQuantity").toString());
                    actualQuantityIp.setText(selectedItems.get(findIndexOf(inventories.get(position).get("sku").toString())).get("actualQuantity").toString());
                }

                Long qType = ((Long) inventories.get(position).get("qType"));

                if (qType == 0) {
                    price.setText("Price for 1kg: " + inventories.get(position).get("price").toString());
                }
                if (qType == 1) {
                    price.setText("Price for 100g: " + inventories.get(position).get("price").toString());
                }
                if (qType == 2) {
                    price.setText("Price for 1L: " + inventories.get(position).get("price").toString());
                }
                if (qType == 3) {
                    price.setText("Price for 100ml: " + inventories.get(position).get("price").toString());
                }
                if (qType == 4) {
                    price.setText("Price for 1pc: " + inventories.get(position).get("price").toString());
                }

                actualQuantityIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (actualQuantityIp.getText().toString().trim().equals("")) {
                            checkBox.setChecked(false);

                            if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                selectedItems.remove(findIndexOf(inventories.get(position).get("sku").toString()));
                            }
                        } else {
                           if(!actualQuantityIp.getText().toString().startsWith(".")){
                               if (!actualQuantityIp.getText().toString().trim().equals("")) {
                                   if (!minQuantityIp.getText().toString().trim().equals("")) {
                                       if (Float.parseFloat(actualQuantityIp.getText().toString()) < Float.parseFloat(minQuantityIp.getText().toString())) {
                                           if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                               selectedItems.remove(findIndexOf(inventories.get(position).get("sku").toString()));
                                           }
                                           checkBox.setChecked(false);
                                           Toast.makeText(context, "Actual Quantity must be greater than Min. Quantity", Toast.LENGTH_SHORT).show();
                                       } else {
                                           Map<String, Object> data = new HashMap<>();
                                           data.put("name", inventories.get(position).get("name"));
                                           data.put("sku", inventories.get(position).get("sku"));
                                           data.put("img", inventories.get(position).get("img"));
                                           data.put("quantity", inventories.get(position).get("quantity"));
                                           data.put("minQuantity", minQuantityIp.getText().toString().trim());
                                           data.put("actualQuantity", actualQuantityIp.getText().toString().trim());
                                           data.put("price", inventories.get(position).get("price"));
                                           data.put("qType", inventories.get(position).get("qType"));

                                           if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                               selectedItems.set(findIndexOf(inventories.get(position).get("sku").toString()), data);
                                           }
                                       }
                                   }
                               }
                           }
                           else {
                               actualQuantityIp.setText("0.");
                               int pos = actualQuantityIp.getText().length();
                               actualQuantityIp.setSelection(pos);
                           }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                minQuantityIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (minQuantityIp.getText().toString().trim().equals("")) {
                            checkBox.setChecked(false);

                            if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                selectedItems.remove(findIndexOf(inventories.get(position).get("sku").toString()));
                            }
                        } else {
                            if(!minQuantityIp.getText().toString().startsWith(".")){

                                if (!actualQuantityIp.getText().toString().trim().equals("")) {
                                    if (!minQuantityIp.getText().toString().trim().equals("")) {
                                        if (Float.parseFloat(actualQuantityIp.getText().toString()) < Float.parseFloat(minQuantityIp.getText().toString())) {
                                            if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                                selectedItems.remove(findIndexOf(inventories.get(position).get("sku").toString()));
                                            }
                                            checkBox.setChecked(false);
                                            Toast.makeText(context, "Actual Quantity must be greater than Min. Quantity", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("name", inventories.get(position).get("name"));
                                            data.put("sku", inventories.get(position).get("sku"));
                                            data.put("img", inventories.get(position).get("img"));
                                            data.put("quantity", inventories.get(position).get("quantity"));
                                            data.put("minQuantity", minQuantityIp.getText().toString().trim());
                                            data.put("actualQuantity", actualQuantityIp.getText().toString().trim());
                                            data.put("price", inventories.get(position).get("price"));
                                            data.put("qType", inventories.get(position).get("qType"));

                                            if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                                                selectedItems.set(findIndexOf(inventories.get(position).get("sku").toString()), data);
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                minQuantityIp.setText("0.");
                                int pos = minQuantityIp.getText().length();
                                minQuantityIp.setSelection(pos);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                checkBox.setText(capitalize(inventories.get(position).get("name").toString()));

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (findIndexOf(inventories.get(position).get("sku").toString()) == -1) {
                            //Add Item
                            if (!actualQuantityIp.getText().toString().trim().equalsIgnoreCase("")) {
                                if (!minQuantityIp.getText().toString().trim().equals("")) {
                                    if (Float.valueOf(actualQuantityIp.getText().toString()) > Float.valueOf(minQuantityIp.getText().toString())) {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("name", inventories.get(position).get("name"));
                                        data.put("sku", inventories.get(position).get("sku"));
                                        data.put("img", inventories.get(position).get("img"));
                                        data.put("quantity", inventories.get(position).get("quantity"));
                                        data.put("minQuantity", minQuantityIp.getText().toString().trim());
                                        data.put("actualQuantity", actualQuantityIp.getText().toString().trim());
                                        data.put("price", inventories.get(position).get("price"));
                                        data.put("qType", inventories.get(position).get("qType"));
                                        selectedItems.add(data);
                                    } else {
                                        checkBox.setChecked(false);
                                        Toast.makeText(context, "Actual Quantity must be greater than Min. Quantity", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    checkBox.setChecked(false);
                                    Toast.makeText(context, "Enter Minimum Quantity", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                checkBox.setChecked(false);
                                Toast.makeText(context, "Enter Actual Quantity", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //Remove Item
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", inventories.get(position).get("name"));
                            data.put("sku", inventories.get(position).get("sku"));
                            data.put("img", inventories.get(position).get("img"));
                            data.put("quantity", inventories.get(position).get("quantity"));
                            data.put("minQuantity", minQuantityIp.getText().toString().trim());
                            data.put("actualQuantity", actualQuantityIp.getText().toString().trim());
                            data.put("price", inventories.get(position).get("price"));
                            data.put("qType", inventories.get(position).get("qType"));
                            selectedItems.remove(data);
                        }
                    }
                });
            }

            return view;
        }

    }

    public class viewBundleAdapter extends BaseAdapter {
        Context context;
        ArrayList<bundle> bundles;

        public viewBundleAdapter(Context context, ArrayList<bundle> bundles) {
            this.context = context;
            this.bundles = bundles;
        }

        @Override
        public int getCount() {
            return bundles.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.bundle_list, parent, false);

            if (bundles.size() > 0) {
                TextView name = view.findViewById(R.id.name);
                TextView count = view.findViewById(R.id.itemsCount);
                ImageView img = view.findViewById(R.id.bundleImg);

                name.setText(bundles.get(position).getName());
                int size = bundles.get(position).getItems().size();
                count.setText(size == 1 ? size + " Item" : size + " Items");

                if (bundles.get(position).getImg() != null) {
                    Picasso.with(getContext()).load(bundles.get(position).getImg()).into(img);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!status.equalsIgnoreCase("published")) {
                        new AlertDialog.Builder(getContext()).setTitle("Manage Bundle").setMessage(bundles.get(position).getName())
                                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        final TextView header = bottomSheetDialog.findViewById(R.id.header);
                                        final EditText bundleName = bottomSheetDialog.findViewById(R.id.bundleName);
                                        ImageButton doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                                        ImageView img = bottomSheetDialog.findViewById(R.id.bundleImg);

                                        if (bundles.get(position).getImg() != null) {
                                            Picasso.with(getContext()).load(bundles.get(position).getImg()).into(img);
                                        }

                                        img.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = CropImage.activity()
                                                        .setAspectRatio(4, 3).setRequestedSize(400, 200)
                                                        .getIntent(getContext());

                                                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                                            }
                                        });

                                        bundleName.setText(bundles.get(position).getName());
                                        header.setText("Edit Bundle");
                                        selectedItems = bundles.get(position).getItems();
                                        items.clear();

                                        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() != null) {
                                                    items.addAll((Collection<? extends Map<String, Object>>) dataSnapshot.getValue());
                                                    //  progressDialog.dismiss();
                                                    bundleAdapterList = new bundleAdapterList(getContext(), items);
                                                    itemsListView.setAdapter(bundleAdapterList);
                                                } else {
                                                    //   progressDialog.dismiss();
                                                    Toast.makeText(getContext(), "Add Items to Inventory first", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                header.setText("Add New Bundle");
                                                bundleName.setText("");
                                                // selectedItems.clear();
                                            }
                                        });

                                        doneBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                final String name = bundleName.getText().toString().trim();

                                                if (imageUri != null) {
                                                    if (!name.equals("")) {
                                                        if (selectedItems.size() > 0) {
                                                            final ProgressDialog progressDialog1 = ProgressDialog.show(getContext(), null, "Please wait...");
                                                            firebaseStorage.getReference().child("bundles/" + key + "_" + name.toLowerCase() + "_" + bundles.size()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    firebaseStorage.getReference().child("bundles/" + key + "_" + name.toLowerCase() + "_" + bundles.size()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            bundle data = new bundle();

                                                                            data.setName(name.trim());
                                                                            data.setItems(selectedItems);
                                                                            data.setImg(uri.toString());

                                                                            bundles.set(position, data);

                                                                            firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/bundles").setValue(bundles).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    viewBundleAdapter = new viewBundleAdapter(getContext(), bundles);
                                                                                    listView.setAdapter(viewBundleAdapter);
                                                                                    progressDialog1.dismiss();
                                                                                    bottomSheetDialog.dismiss();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressDialog1.dismiss();
                                                                                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog1.dismiss();
                                                                    Toast.makeText(getContext(), "Failed to upload image.Try again", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(getContext(), "Add atleast one item to this bundle", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(getContext(), "Enter Bundle Name", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    if (!name.equals("")) {
                                                        if (selectedItems.size() > 0) {
                                                            final ProgressDialog progressDialog1 = ProgressDialog.show(getContext(), null, "Please wait...");

                                                            bundle data = new bundle();

                                                            data.setName(name.trim());
                                                            data.setItems(selectedItems);
                                                            data.setImg(bundles.get(position).getImg());

                                                            bundles.set(position, data);

                                                            firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/bundles").setValue(bundles).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    viewBundleAdapter = new viewBundleAdapter(getContext(), bundles);
                                                                    listView.setAdapter(viewBundleAdapter);
                                                                    progressDialog1.dismiss();
                                                                    bottomSheetDialog.dismiss();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog1.dismiss();
                                                                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(getContext(), "Add atleast one item to this bundle", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(getContext(), "Enter Bundle Name", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });

                                        bottomSheetDialog.show();
                                    }
                                }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bundles.remove(position);
                                viewBundleAdapter.notifyDataSetChanged();

                                firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/bundles").setValue(bundles).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Bundle Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).show();
                    } else {
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                        bottomSheetDialog.setContentView(R.layout.view_bundle_sheet);

                        TextView bundleName = bottomSheetDialog.findViewById(R.id.bundleName);
                        ListView listView = bottomSheetDialog.findViewById(R.id.listView);
                        bundleName.setText(bundles.get(position).getName());

                        viewBundleItemsAdapter = new viewBundleItemsAdapter(getContext(), bundles.get(position).getItems());
                        listView.setAdapter(viewBundleItemsAdapter);

                        bottomSheetDialog.show();
                    }
                }

            });

            return view;
        }
    }

    public class viewBundleItemsAdapter extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> items;

        public viewBundleItemsAdapter(Context context, ArrayList<Map<String, Object>> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.view_bundle_list, parent, false);
            TextView name, actualQnt, minQun, price;
            name = view.findViewById(R.id.nameTxt);
            actualQnt = view.findViewById(R.id.aQuantityTxt);
            minQun = view.findViewById(R.id.mQuantityTxt);
            price = view.findViewById(R.id.priceTxt);
            ImageView img = view.findViewById(R.id.itemImg);

            if (items.size() > 0) {
                name.setText(items.get(position).get("name").toString());


                if (items.get(position).get("img") != null) {
                    if (!items.get(position).get("img").toString().equalsIgnoreCase("")) {
                        Picasso.with(getContext()).load(items.get(position).get("img").toString()).into(img);
                    }
                }

                int qType = Math.toIntExact((Long) items.get(position).get("qType"));

                if (qType == 0) {
                    price.setText("Price for 1kg: " + items.get(position).get("price").toString());
                    actualQnt.setText("Actual Quantity: " + items.get(position).get("actualQuantity").toString() + " Kg");
                    minQun.setText("Minimum Quantity: " + items.get(position).get("minQuantity").toString() + " Kg");
                }
                if (qType == 1) {
                    price.setText("Price for 100g: " + items.get(position).get("price").toString());
                    actualQnt.setText("Actual Quantity: " + items.get(position).get("actualQuantity").toString() + " g");
                    minQun.setText("Minimum Quantity: " + items.get(position).get("minQuantity").toString() + " g");
                }
                if (qType == 2) {
                    price.setText("Price for 1L: " + items.get(position).get("price").toString());
                    actualQnt.setText("Actual Quantity: " + items.get(position).get("actualQuantity").toString() + " L");
                    minQun.setText("Minimum Quantity: " + items.get(position).get("minQuantity").toString() + " L");
                }
                if (qType == 3) {
                    price.setText("Price for 100ml: " + items.get(position).get("price").toString());
                    actualQnt.setText("Actual Quantity: " + items.get(position).get("actualQuantity").toString() + " ml");
                    minQun.setText("Minimum Quantity: " + items.get(position).get("minQuantity").toString() + " ml");
                }
                if (qType == 4) {
                    price.setText("Price for 1pc: " + items.get(position).get("price").toString());
                    actualQnt.setText("Actual Quantity: " + items.get(position).get("actualQuantity").toString() + " pc");
                    minQun.setText("Minimum Quantity: " + items.get(position).get("minQuantity").toString() + " pc");
                }
            }

            return view;
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

    public int findIndexOf(String itemName) {
        int index = -1;
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i).get("sku").toString().trim().toLowerCase().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }
}