package com.techheralds.annam.prod.supplier;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavDeepLinkBuilder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateSaleActivity extends AppCompatActivity {
    Button saveBtn, cancelBtn, viewBundlesBtn, manageCustomersBtn;
    ArrayList<inventory> items;
    ArrayList<Map<String, Object>> selectedItems;
    ArrayList<bundle> bundles;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    ListView listView;
    BottomSheetDialog bottomSheetDialog, viewBottomSheet;
    int currBundle = 0;
    EditText startDateIp, endDateIp, saleNameIp, saleDescIp;
    Boolean canRemoveBundle = false;
    int mDay, mMonth, mYear, mHour, mMinute;
    Calendar c;
    RadioGroup radioGroup;
    ArrayList<Map<String, Object>> customers, tempArray;
    adapterList adapterList;
    ArrayList<String> selectedCustomers, tempSelected;
    int saleType = 0;

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
        customers = new ArrayList<>();
        tempArray = new ArrayList<>();
        bottomSheetDialog = new BottomSheetDialog(CreateSaleActivity.this);
        bottomSheetDialog.setContentView(R.layout.add_bundle_sheet);
        viewBottomSheet = new BottomSheetDialog(CreateSaleActivity.this);
        viewBottomSheet.setContentView(R.layout.view_bundle_sheet);
        radioGroup = findViewById(R.id.radioGroup);
        manageCustomersBtn = findViewById(R.id.manageCustomers);

        tempSelected = new ArrayList<>();
        selectedCustomers = new ArrayList<>();
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        viewBundlesBtn = findViewById(R.id.viewBundlesBtn);
        startDateIp = findViewById(R.id.startDateIp);
        endDateIp = findViewById(R.id.endDateIp);
        items = new ArrayList<>();
        selectedItems = new ArrayList<>();
        bundles = new ArrayList<>();
        listView = findViewById(R.id.listView);
        saleNameIp = findViewById(R.id.saleNameIp);
        saleDescIp = findViewById(R.id.saleDescIp);

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        int halfWidth = width / 2;

        saveBtn.setWidth(halfWidth - 16);
        cancelBtn.setWidth(halfWidth - 16);
        startDateIp.setWidth(halfWidth - 16);
        endDateIp.setWidth(halfWidth - 16);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                View radioButton = radioGroup.findViewById(i);
                saleType = radioGroup.indexOfChild(radioButton);

                if (saleType == 0) {
                    manageCustomersBtn.setVisibility(View.GONE);
                } else {
                    manageCustomersBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        manageCustomersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = ProgressDialog.show(CreateSaleActivity.this, null, "Please wait...");

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CreateSaleActivity.this);

                bottomSheetDialog.setContentView(R.layout.manage_customers_sheet);
                bottomSheetDialog.setCancelable(false);

                ImageButton backBtn = bottomSheetDialog.findViewById(R.id.backBtn);
                ImageButton doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                final ListView listView = bottomSheetDialog.findViewById(R.id.listView);

                SearchView searchView = bottomSheetDialog.findViewById(R.id.searchBar);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public boolean onQueryTextChange(final String newText) {
                        adapterList = new adapterList(CreateSaleActivity.this, tempArray);
                        listView.setAdapter(adapterList);
                        adapterList.getFilter().filter(newText);

                        return false;
                    }
                });

                doneBtn.setVisibility(View.GONE);

                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

                doneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //    if (tempSelected.size() > 0) {
                        selectedCustomers = tempSelected;
                        bottomSheetDialog.dismiss();
                        //     } else {
                        //        Toast.makeText(CreateSaleActivity.this, "Select atleast one customer", Toast.LENGTH_SHORT).show();
                        //    }
                    }
                });

                firebaseDatabase.getReference().child("customers/").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            customers.clear();
                            tempArray.clear();

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                Map<String, Object> customer = (Map<String, Object>) ds.getValue();
                                customers.add(customer);
                                tempArray.add(customer);
                                adapterList = new adapterList(CreateSaleActivity.this, customers);
                                listView.setAdapter(adapterList);
                                bottomSheetDialog.show();
                                progressDialog.dismiss();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateSaleActivity.this, "No Customers", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        startDateIp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        datePicker("start");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        endDateIp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        datePicker("end");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String saleName = saleNameIp.getText().toString().trim();
                String saleDesc = saleDescIp.getText().toString().trim();
                String startDate = startDateIp.getText().toString().trim();
                String endDate = endDateIp.getText().toString().trim();

                if (!saleName.equals("")) {
                    if (!startDate.equals("")) {
                        if (!endDate.equals("")) {
                            Date date1 = null;
                            Date date2 = null;

                            try {
                                date1 = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(startDate);
                                date2 = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(endDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Long t1 = date1.getTime();
                            Long t2 = date2.getTime();

                            if (t1 < t2) {
                                if (saleType == 1) {
                                    if (selectedCustomers.size() == 0) {
                                        Toast.makeText(CreateSaleActivity.this, "Select atleast one customer to show this sale", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                final Map<String, Object> data = new HashMap<>();
                                data.put("name", saleName);
                                data.put("desc", saleDesc);
                                data.put("startDate", startDate);
                                data.put("endDate", endDate);
                                data.put("status", "not published");
                                data.put("supplier", getSupplierId());
                                data.put("hide", false);
                                data.put("saleType", saleType);
                                data.put("selectedCustomers", saleType == 1 ? selectedCustomers : null);

                                final ProgressDialog progressDialog = ProgressDialog.show(CreateSaleActivity.this, null, "Creating Sale...");

                                firebaseDatabase.getReference().child("sales/" + getSupplierId()).orderByChild("name").equalTo(saleName).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreateSaleActivity.this, "Sale Name already present", Toast.LENGTH_SHORT).show();
                                        } else {
                                            firebaseDatabase.getReference().child("sales/" + getSupplierId()).push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(CreateSaleActivity.this, MainActivity.class);
                                                    intent.putExtra("fragment", "sales");
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreateSaleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(CreateSaleActivity.this, "Check your Date & Time", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreateSaleActivity.this, "Select End Date", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreateSaleActivity.this, "Select Start Date", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateSaleActivity.this, "Enter Sale Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public String getSupplierId() {
        SharedPreferences sharedPreferences = CreateSaleActivity.this.getSharedPreferences("local", Context.MODE_PRIVATE);
        final String mainSupplier = sharedPreferences.getString("mainSupplier", "");

        if (mainSupplier.equalsIgnoreCase("")) {
            return firebaseAuth.getCurrentUser().getUid();
        } else {
            return mainSupplier;
        }
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

    public int findIndexOf(String itemName) {
        int index = -1;
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i).get("sku").toString().trim().toLowerCase().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    public int findIndexOf1(String itemName) {
        int index = -1;
        for (int i = 0; i < bundles.get(currBundle).getItems().size(); i++) {
            if (bundles.get(currBundle).getItems().get(i).get("sku").toString().trim().toLowerCase().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void datePicker(final String date) throws ParseException {
        c = Calendar.getInstance();
        int mYearParam = mYear;
        int mMonthParam = mMonth - 1;
        int mDayParam = mDay;

        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateSaleActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mMonth = monthOfYear + 1;
                        mYear = year;
                        mDay = dayOfMonth;
                        show_Timepicker(date);

                    }
                }, mYearParam, mMonthParam, mDayParam);
        if (date.equalsIgnoreCase("start")) {
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        } else {
            String myDate = startDateIp.getText().toString().trim();
            android.icu.text.SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm a");
            Date d = sdf.parse(myDate);
            long millis = d.getTime();

            datePickerDialog.getDatePicker().setMinDate(millis);
        }
        datePickerDialog.show();
    }

    private void show_Timepicker(final String date) {

        TimePickerDialog timePickerDialog = new TimePickerDialog(CreateSaleActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker view, int pHour,
                                          int pMinute) {

                        mHour = pHour;
                        if (pMinute < 10) {
                            mMinute = Integer.parseInt("0" + pMinute);
                        } else {
                            mMinute = pMinute;
                        }
                        String AM_PM;
                        if (mHour < 12) {
                            if (mHour == 0) {
                                mHour = 12;
                            }
                            AM_PM = "AM";
                        } else {
                            if ((mHour - 12) > 0) {
                                mHour = mHour - 12;
                            }
                            AM_PM = "PM";
                        }

                        if (date.equals("start")) {
                            startDateIp.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + (mMinute < 10 ? "0" : "") + mMinute + AM_PM);
                        } else {
                            endDateIp.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + (mMinute < 10 ? "0" : "") + mMinute + AM_PM);
                        }
                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();
    }

    public class adapterList extends BaseAdapter implements Filterable {
        ArrayList<Map<String, Object>> customers;
        Context context;

        public adapterList(Context context, ArrayList<Map<String, Object>> customers) {
            this.customers = customers;
            this.context = context;
        }

        @Override
        public int getCount() {
            return customers.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.manage_customers_list, parent, false);
            CheckBox name = view.findViewById(R.id.name);
            CircleImageView avatar = view.findViewById(R.id.avatar);
            TextView number = view.findViewById(R.id.phoneNumber);

            if (customers.get(position).get("dp") != null) {
                if (!customers.get(position).get("dp").toString().equalsIgnoreCase("")) {
                    Picasso.with(CreateSaleActivity.this).load(customers.get(position).get("dp").toString()).into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.nouser);
                }
            }

            name.setText(customers.get(position).get("name").toString());
            number.setText(customers.get(position).get("phoneNumber").toString());

            if (selectedCustomers.contains(customers.get(position).get("uid").toString())) {
                name.setChecked(true);
            } else {
                name.setChecked(false);
            }

            name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectedCustomers.add(customers.get(position).get("uid").toString());
                    } else {
                        selectedCustomers.remove(customers.get(position).get("uid").toString());
                    }
                }
            });

            return view;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        // if your editText field is empty, return full list of FriendItem
                        results.count = tempArray.size();
                        results.values = tempArray;
                    } else {
                        ArrayList<Map<String, Object>> filteredList = new ArrayList<>();
                        constraint = constraint.toString().toLowerCase(); // if we ignore case
                        for (Map<String, Object> item : customers) {
                            String firstName = item.get("name").toString().toLowerCase(); // if we ignore case
                            String phoneNumber = item.get("phoneNumber").toString().toLowerCase();
                            //String lastName = item.get("pho").toLowerCase(); // if we ignore case
                            if (firstName.contains(constraint.toString()) || phoneNumber.contains(constraint.toString())) {
                                filteredList.add(item); // added item witch contains our text in EditText
                            }
                        }

                        results.count = filteredList.size(); // set count of filtered list
                        results.values = filteredList; // set filtered list
                    }
                    return results; // return our filtered list
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    customers = (ArrayList<Map<String, Object>>) results.values; // replace list to filtered list
                    notifyDataSetChanged(); // refresh adapter
                }
            };
            return filter;
        }
    }

}