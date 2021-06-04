package com.techheralds.annam.prod.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SalesActivity extends AppCompatActivity {
    String title, desc, startDate, endDate, key;
    TextView nameTxt, descTxt, sDateTxt, eDateTxt;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        Intent i = getIntent();
        title = i.getExtras().getString("title");
        desc = i.getExtras().getString("desc");
        startDate = i.getExtras().getString("startDate");
        endDate = i.getExtras().getString("endDate");
        key = i.getExtras().getString("key");

        nameTxt = findViewById(R.id.name);
        descTxt = findViewById(R.id.desc);
        sDateTxt = findViewById(R.id.sDate);
        eDateTxt = findViewById(R.id.eDate);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameTxt.setText(title);
        if (!desc.equals("")) {
            descTxt.setVisibility(View.VISIBLE);
            descTxt.setText(desc);
        }
        sDateTxt.setText("Start at: " + startDate);
        eDateTxt.setText("End at: " + endDate);
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
}