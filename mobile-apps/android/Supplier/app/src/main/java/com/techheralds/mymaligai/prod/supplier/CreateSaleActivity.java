package com.techheralds.mymaligai.prod.supplier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateSaleActivity extends AppCompatActivity {
    Button addItemsBtn;
    ArrayList<inventory> items;
    ArrayList<Map<String, Object>> selectedItems;
    inventoryAdapterList inventoryAdapterList;
    inventoryAdapterList1 inventoryAdapterList1;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sale);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create New Sale");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        addItemsBtn = findViewById(R.id.addItemsBtn);
        items = new ArrayList<>();
        selectedItems = new ArrayList<>();
        listView = findViewById(R.id.listView);

        addItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CreateSaleActivity.this);

                bottomSheetDialog.setContentView(R.layout.select_items_sheet);

                final ListView listView = bottomSheetDialog.findViewById(R.id.listView);
                final ArrayList<Map<String, Object>> tags;
                final ArrayList<String> tempTags; //for spinner
                final Spinner spinner = bottomSheetDialog.findViewById(R.id.selectTagBtn);
                tags = new ArrayList<>();
                tempTags = new ArrayList<>();
                final ProgressDialog progressDialog = ProgressDialog.show(CreateSaleActivity.this, null, "Please Wait...");

                //Load preferred tags
                firebaseDatabase.getReference().child("suppliers/" + firebaseUser.getUid() + "/tags").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("category", ds.getValue());
                                data.put("id", ds.getKey());
                                tags.add(data);
                                tempTags.add(capitalize(ds.getValue().toString()));

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, tempTags);
                                spinner.setAdapter(adapter);

                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        final ProgressDialog progressDialog = ProgressDialog.show(CreateSaleActivity.this, null, "Loading items with the category '" + capitalize(tags.get(position).get("category").toString()) + "'.Please wait...");

                                        firebaseDatabase.getReference().child("inventory/" + firebaseUser.getUid() + "/" + tags.get(position).get("id")).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                items.clear();
                                                inventoryAdapterList = new inventoryAdapterList(CreateSaleActivity.this, items);
                                                listView.setAdapter(inventoryAdapterList);
                                                if (dataSnapshot.getChildrenCount() > 0) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        progressDialog.dismiss();
                                                        inventory data = ds.getValue(inventory.class);
                                                        items.add(data);
                                                        inventoryAdapterList = new inventoryAdapterList(CreateSaleActivity.this, items);
                                                        listView.setAdapter(inventoryAdapterList);
                                                    }
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreateSaleActivity.this, "No inventory items in this category", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                                Toast.makeText(CreateSaleActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        //  currTag = tags.get(position).get("category").toString();
                                        // currId = tags.get(position).get("id").toString();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                                progressDialog.dismiss();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateSaleActivity.this, "You haven't added any categories", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CreateSaleActivity.this, "Can't Load Data", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

       public class inventoryAdapterList extends BaseAdapter {
        Context context;
        ArrayList<inventory> inventories;

        public inventoryAdapterList(Context context, ArrayList<inventory> inventories) {
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

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.select_item_list, parent, false);

            if (inventories.size() > 0) {
                TextView title = view.findViewById(R.id.itemName);
                ImageView image = view.findViewById(R.id.itemImg);
                CheckBox checkBox = view.findViewById(R.id.checkBox);

                title.setText(capitalize(inventories.get(position).getName()));

                if (inventories.get(position).getImg() != null) {
                    if (!inventories.get(position).getImg().equals("")) {
                        Picasso.with(CreateSaleActivity.this).load(inventories.get(position).getImg()).into(image);
                    }
                }

                if (findIndexOf(inventories.get(position).getSku()) > -1) {
                    checkBox.setChecked(true);
                }

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (findIndexOf(inventories.get(position).getSku()) == -1) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", inventories.get(position).getName());
                            data.put("sku", inventories.get(position).getSku());
                            data.put("img", inventories.get(position).getImg());
                            selectedItems.add(data);
                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", inventories.get(position).getName());
                            data.put("sku", inventories.get(position).getSku());
                            data.put("img", inventories.get(position).getImg());
                            selectedItems.remove(data);return;
                        }
                        inventoryAdapterList1 = new inventoryAdapterList1(CreateSaleActivity.this, selectedItems);
                        listView.setAdapter(inventoryAdapterList1);
                    }
                });
            }

            return view;
        }

    }

    public class inventoryAdapterList1 extends BaseAdapter {
        Context context;
        ArrayList<Map<String,Object>> inventories;

        public inventoryAdapterList1(Context context, ArrayList<Map<String,Object>> inventories) {
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

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.select_item_list, parent, false);

            if (inventories.size() > 0) {
                TextView title = view.findViewById(R.id.itemName);
                ImageView image = view.findViewById(R.id.itemImg);
                CheckBox checkBox = view.findViewById(R.id.checkBox);

                title.setText(capitalize(inventories.get(position).get("name").toString()));

                if (inventories.get(position).get("img") != null) {
                    if (!inventories.get(position).get("img").toString().equals("")) {
                        Picasso.with(CreateSaleActivity.this).load(inventories.get(position).get("img").toString()).into(image);
                    }
                }

                if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                    checkBox.setChecked(true);
                }

                inventoryAdapterList1 = new inventoryAdapterList1(CreateSaleActivity.this, selectedItems);
                listView.setAdapter(inventoryAdapterList1);

            }

            return view;
        }

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