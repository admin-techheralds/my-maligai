package com.techheralds.annam.prod.supplier;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDeepLinkBuilder;

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
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateSaleActivity extends AppCompatActivity {
    Button saveBtn, cancelBtn, viewBundlesBtn;
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
        bottomSheetDialog = new BottomSheetDialog(CreateSaleActivity.this);
        bottomSheetDialog.setContentView(R.layout.add_bundle_sheet);
        viewBottomSheet = new BottomSheetDialog(CreateSaleActivity.this);
        viewBottomSheet.setContentView(R.layout.view_bundle_sheet);

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

                                final Map<String, Object> data = new HashMap<>();
                                data.put("name", saleName);
                                data.put("desc", saleDesc);
                                data.put("startDate", startDate);
                                data.put("endDate", endDate);
                                data.put("status", "not published");
                                data.put("supplier", getSupplierId());

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


}