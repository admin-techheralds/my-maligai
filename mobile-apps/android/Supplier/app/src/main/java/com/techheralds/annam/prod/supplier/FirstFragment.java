package com.techheralds.annam.prod.supplier;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstFragment extends Fragment {
    Button addItemsBtn;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    ArrayList<inventory> items;
    ArrayList<Map<String, Object>> selectedItems;
    inventoryAdapterList inventoryAdapterList;
    inventoryAdapterList1 inventoryAdapterList1;
    ListView listView;
    String key, status;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        addItemsBtn = view.findViewById(R.id.addItemsBtn);
        items = new ArrayList<>();
        selectedItems = new ArrayList<>();
        listView = view.findViewById(R.id.listView);

        key = getActivity().getIntent().getExtras().getString("key");
        status = getActivity().getIntent().getExtras().getString("status");

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");

        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    selectedItems.addAll((Collection<? extends Map<String, Object>>) dataSnapshot.getValue());
                    inventoryAdapterList1 = new inventoryAdapterList1(getContext(), selectedItems);
                    listView.setAdapter(inventoryAdapterList1);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                bottomSheetDialog.setContentView(R.layout.select_items_sheet);

                bottomSheetDialog.setCancelable(false);

                ImageButton backBtn = bottomSheetDialog.findViewById(R.id.backBtn);

                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

                final ListView listView1 = bottomSheetDialog.findViewById(R.id.listView);
                final ArrayList<Map<String, Object>> tags;
                final ArrayList<String> tempTags; //for spinner
                final Spinner spinner = bottomSheetDialog.findViewById(R.id.selectTagBtn);
                ImageButton doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                tags = new ArrayList<>();
                tempTags = new ArrayList<>();
                final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please Wait...");

                //Load preferred tags
                firebaseDatabase.getReference().child("suppliers/" + getSupplierId() + "/tags").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("category", ds.getValue());
                                data.put("id", ds.getKey());
                                tags.add(data);
                                tempTags.add(capitalize(ds.getValue().toString()));

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, tempTags);
                                spinner.setAdapter(adapter);

                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading items with the category '" + capitalize(tags.get(position).get("category").toString()) + "'.Please wait...");

                                        firebaseDatabase.getReference().child("inventory/" + getSupplierId() + "/" + tags.get(position).get("id")).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                items.clear();
                                                inventoryAdapterList = new inventoryAdapterList(getContext(), items);
                                                listView1.setAdapter(inventoryAdapterList);
                                                if (dataSnapshot.getChildrenCount() > 0) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        progressDialog.dismiss();
                                                        inventory data = ds.getValue(inventory.class);
                                                        if (status.equalsIgnoreCase("published")) {
                                                            if (findIndexOf(data.getSku()) > -1) {
                                                                items.add(data);
                                                            }
                                                        } else {
                                                            items.add(data);
                                                        }
                                                        inventoryAdapterList = new inventoryAdapterList(getContext(), items);
                                                        listView1.setAdapter(inventoryAdapterList);
                                                    }
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getContext(), "No inventory items in this category", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "You haven't added any categories", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Can't Load Data", Toast.LENGTH_SHORT).show();
                    }
                });

                doneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final ProgressDialog dialog = ProgressDialog.show(getContext(), null, "Please wait...");

                        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").setValue(selectedItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                inventoryAdapterList1 = new inventoryAdapterList1(getContext(), selectedItems);
                                listView.setAdapter(inventoryAdapterList1);
                                dialog.dismiss();
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Try agian", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });

                bottomSheetDialog.show();
            }
        });
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

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.select_item_list, parent, false);

            if (inventories.size() > 0) {
                ImageView image = view.findViewById(R.id.itemImg);
                final CheckBox checkBox = view.findViewById(R.id.checkBox);
                final EditText quantityIp, priceIp;

                quantityIp = view.findViewById(R.id.quantity);
                priceIp = view.findViewById(R.id.price);

                if (status.equalsIgnoreCase("published")) {
                    priceIp.setFocusable(false);
                    checkBox.setEnabled(false);
                }

                int qType = Math.toIntExact(inventories.get(position).getQuantity_type());

                if (qType == 0) {
                    priceIp.setHint("Price for 1kg");
                }
                if (qType == 1) {
                    priceIp.setHint("Price for 100g");
                }
                if (qType == 2) {
                    priceIp.setHint("Price for 1L");
                }
                if (qType == 3) {
                    priceIp.setHint("Price for 100ml");
                }
                if (qType == 4) {
                    priceIp.setHint("Price for 1pc");
                }

                checkBox.setText(capitalize(inventories.get(position).getName()));

                if (inventories.get(position).getImg() != null) {
                    if (!inventories.get(position).getImg().equals("")) {
                        Picasso.with(getContext()).load(inventories.get(position).getImg()).into(image);
                    }
                }

                if (findIndexOf(inventories.get(position).getSku()) > -1) {
                    checkBox.setChecked(true);
                    quantityIp.setText(selectedItems.get(findIndexOf(inventories.get(position).getSku())).get("quantity").toString());
                    priceIp.setText(selectedItems.get(findIndexOf(inventories.get(position).getSku())).get("price").toString());
                }

                quantityIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().equals("")) {
                            if (!status.equalsIgnoreCase("published")) {
                                checkBox.setChecked(false);

                                if (findIndexOf(inventories.get(position).getSku()) > -1) {
                                    selectedItems.remove(findIndexOf(inventories.get(position).getSku()));
                                }
                            }
                        } else {
                            if (findIndexOf(inventories.get(position).getSku()) > -1) {
                                selectedItems.get(findIndexOf(inventories.get(position).getSku())).put("quantity", charSequence.toString().trim());
                                // inventoryAdapterList1.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                priceIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().equals("")) {
                            if (!status.equalsIgnoreCase("published")) {
                                checkBox.setChecked(false);
                                if (findIndexOf(inventories.get(position).getSku()) > -1) {
                                    selectedItems.remove(findIndexOf(inventories.get(position).getSku()));
                                }
                            }
                        } else {
                            if (findIndexOf(inventories.get(position).getSku()) > -1) {
                                selectedItems.get(findIndexOf(inventories.get(position).getSku())).put("price", charSequence.toString().trim());
                                //  inventoryAdapterList1.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                        if (!quantityIp.getText().toString().trim().equalsIgnoreCase("")) {
                            if (!priceIp.getText().toString().trim().equalsIgnoreCase("")) {
                                if (findIndexOf(inventories.get(position).getSku()) == -1) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", inventories.get(position).getName());
                                    data.put("sku", inventories.get(position).getSku());
                                    data.put("img", inventories.get(position).getImg());
                                    data.put("quantity", quantityIp.getText().toString().trim());
                                    data.put("price", priceIp.getText().toString().trim());
                                    data.put("qType", inventories.get(position).getQuantity_type());
                                    selectedItems.add(data);
                                } else {
                                    selectedItems.remove(findIndexOf(inventories.get(position).getSku()));
                                }
                            } else {
                                checkBox.setChecked(false);
                                Toast.makeText(context, "Enter Price", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            checkBox.setChecked(false);
                            Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show();
                        }

                        //  inventoryAdapterList1 = new inventoryAdapterList1(getContext(), selectedItems);
                        //   listView.setAdapter(inventoryAdapterList1);
                    }
                });
            }

            return view;
        }
    }

    public class inventoryAdapterList1 extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> inventories;

        public inventoryAdapterList1(Context context, ArrayList<Map<String, Object>> inventories) {
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
                ImageView image = view.findViewById(R.id.itemImg);
                CheckBox checkBox = view.findViewById(R.id.checkBox);
                final EditText quantityIp, priceIp;
                TextView name = view.findViewById(R.id.name);
                TextView quantity, price;

                quantity = view.findViewById(R.id.quantityTxt);
                price = view.findViewById(R.id.priceTxt);

                quantity.setVisibility(View.VISIBLE);
                price.setVisibility(View.VISIBLE);

                quantityIp = view.findViewById(R.id.quantity);
                priceIp = view.findViewById(R.id.price);

                quantityIp.setVisibility(View.GONE);
                priceIp.setVisibility(View.GONE);
                name.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.GONE);


                Long qType = ((Long) inventories.get(position).get("qType"));

                if (qType == 0) {
                    price.setText("Price for 1kg: " + inventories.get(position).get("price").toString());
                    quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString() + " Kg");
                }
                if (qType == 1) {
                    price.setText("Price for 100g: " + inventories.get(position).get("price").toString());
                    quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString() + " Kg");
                }
                if (qType == 2) {
                    price.setText("Price for 1L: " + inventories.get(position).get("price").toString());
                    quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString() + " L");
                }
                if (qType == 3) {
                    price.setText("Price for 100ml: " + inventories.get(position).get("price").toString());
                    quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString() + " L");
                }
                if (qType == 4) {
                    price.setText("Price for 1pc: " + inventories.get(position).get("price").toString());
                    quantity.setText("Available Quantity: " + inventories.get(position).get("quantity").toString() + " pc");
                }

                quantityIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        inventories.get(position).put("quantity", quantityIp.getText().toString().trim());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                priceIp.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        inventories.get(position).put("price", priceIp.getText().toString().trim());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                name.setText(capitalize(inventories.get(position).get("name").toString()));
                checkBox.setText(capitalize(inventories.get(position).get("name").toString()));
                priceIp.setText(inventories.get(position).get("price").toString());
                quantityIp.setText(inventories.get(position).get("quantity").toString());

                if (inventories.get(position).get("img") != null) {
                    if (!inventories.get(position).get("img").toString().equals("")) {
                        Picasso.with(getContext()).load(inventories.get(position).get("img").toString()).into(image);
                    }
                }

                if (findIndexOf(inventories.get(position).get("sku").toString()) > -1) {
                    checkBox.setChecked(true);
                    priceIp.setText(selectedItems.get(findIndexOf(inventories.get(position).get("sku").toString())).get("price").toString());
                    quantityIp.setText(selectedItems.get(findIndexOf(inventories.get(position).get("sku").toString())).get("quantity").toString());
                }

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        inventories.remove(inventories.get(position));
                        inventoryAdapterList1.notifyDataSetChanged();
                    }
                });

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

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
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
}