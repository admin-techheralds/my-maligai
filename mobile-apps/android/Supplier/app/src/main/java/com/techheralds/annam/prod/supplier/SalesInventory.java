package com.techheralds.annam.prod.supplier;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.techheralds.annam.prod.supplier.ui.main.SectionsPagerAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SalesInventory extends AppCompatActivity {
    String key, name, desc, startDate, endDate, status;
    TextView titleTxt;
    ImageButton backBtn;
    int mDay, mMonth, mYear, mHour, mMinute;
    Calendar c;
    BottomSheetDialog editBottomSheetDialog;
    EditText nameIp, descIp, startDateIp, endDateIp;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_inventory);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        titleTxt = findViewById(R.id.title);
        backBtn = findViewById(R.id.backBtn);

        Intent i = getIntent();
        key = i.getExtras().getString("key");
        name = i.getExtras().getString("title");
        desc = i.getExtras().getString("desc");
        startDate = i.getExtras().getString("startDate");
        endDate = i.getExtras().getString("endDate");
        status = i.getExtras().getString("status");

        titleTxt.setText(name);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public String getSupplierId() {
        SharedPreferences sharedPreferences = SalesInventory.this.getSharedPreferences("local", Context.MODE_PRIVATE);
        final String mainSupplier = sharedPreferences.getString("mainSupplier", "");

        if (mainSupplier.equalsIgnoreCase("")) {
            return firebaseAuth.getCurrentUser().getUid();
        } else {
            return mainSupplier;
        }
    }

}