package com.techheralds.annam.prod.customer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.chainsaw.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    String userType;
    FloatingActionButton newDemandFab, addItemsFab;
    FirebaseRemoteConfig firebaseRemoteConfig;
    TextView versionName;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String msgType = extras.getString("typeOfMsg");

            if (msgType.equalsIgnoreCase("order")) {
                String orderId = extras.getString("orderId");
                //  Toast.makeText(this, "ORDER " + orderId, Toast.LENGTH_SHORT).show();
                final ProgressDialog progressDialog12 = ProgressDialog.show(this, null, "Please Wait...");

                firebaseDatabase.getReference().child("demands/" + orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            final demand data = dataSnapshot.getValue(demand.class);

                            firebaseDatabase.getReference().child("sales/" + data.getSupplier() + "/" + data.getSaleId() + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                    if (dataSnapshot1.getValue() != null) {
                                        final StringBuilder stringBuilder = new StringBuilder();

                                        for (Map<String, Object> item : data.getDemandList()) {
                                            String iname = item.get("name").toString();
                                            String quantity = item.get("quantity").toString();
                                            stringBuilder.append(iname + "(" + quantity + ") ");
                                        }

                                        Intent intent = new Intent(MainActivity.this, OrderViewActivity.class);
                                        intent.putExtra("name", dataSnapshot1.getValue().toString());
                                        intent.putExtra("demandList", data.getDemandList());
                                        intent.putExtra("timeline", data.getTimeLine());
                                        //intent.putExtra("dp", userDp[0]);
                                        intent.putExtra("supplier", data.getSupplier());
                                        intent.putExtra("consumer", data.getConsumer());
                                        Bundle b = new Bundle();
                                        b.putDouble("price", data.getPrice());
                                        intent.putExtras(b);
                                        intent.putExtra("status", data.getStatus());
                                        intent.putExtra("address", data.getAddress());
                                        intent.putExtra("deliveryTime", data.getDeliveryTime());
                                        intent.putExtra("createdOn", data.getTimeCreated());
                                        intent.putExtra("items", stringBuilder.toString());
                                        intent.putExtra("key", data.getKey());
                                        intent.putExtra("paid", data.getPaid());
                                        intent.putExtra("payment_mode", data.getPayment_mode());

                                        if (data.getRejectionReason() != null) {
                                            intent.putExtra("rejectionReason", data.getRejectionReason());
                                        } else {
                                            intent.putExtra("rejectionReason", "");
                                        }
                                        progressDialog12.dismiss();
                                        startActivity(intent);
                                    } else {

                                        progressDialog12.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            progressDialog12.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else if (msgType.equalsIgnoreCase("sale")) {
                final String saleId = extras.getString("saleId");
                final String supplierId = extras.getString("supplierId");

                final ProgressDialog progressDialog12 = ProgressDialog.show(this, null, "Please Wait...");

                firebaseDatabase.getReference().child("sales/" + supplierId + "/" + saleId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();

                            Intent intent = new Intent(MainActivity.this, PlaceOrderActivity.class);
                            intent.putExtra("key", saleId);
                            intent.putExtra("title", data.get("name").toString());
                            intent.putExtra("uid", supplierId);
                            intent.putExtra("desc", data.get("desc").toString());
                            intent.putExtra("startDate", data.get("startDate").toString());
                            intent.putExtra("endDate", data.get("endDate").toString());
                            intent.putExtra("status", data.get("status").toString());
                            intent.putExtra("items", ((ArrayList<Map<String, Object>>) data.get("items")));
                            intent.putExtra("bundles", ((ArrayList<Map<String, Object>>) data.get("bundles")));
                            startActivity(intent);
                        }

                        progressDialog12.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }


        //Get FCM token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        firebaseDatabase.getReference().child("tokens/" + firebaseAuth.getCurrentUser().getUid()).setValue(token);
                    }
                });

        //Remote Config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1000)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("recent_orders_count", "");
        firebaseRemoteConfig.setDefaultsAsync(defaults);

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    String count = firebaseRemoteConfig.getString("recent_orders_count");
                    //  Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();
                    //Save sms template locally
                    SharedPreferences sharedPreferences = getSharedPreferences("local", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("count", count);
                    editor.apply();
                } else {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = findViewById(R.id.newOrderFAB);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SelectSupplierActivity.class);
                startActivity(intent);
            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_sales, R.id.nav_recent_orders,
                R.id.nav_my_orders, R.id.nav_my_profile)
                .setDrawerLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Nav Header
        final View header = navigationView.getHeaderView(0);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.nav_my_profile);
                drawer.closeDrawers();
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            firebaseDatabase.getReference().child("customers/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Customer data = dataSnapshot.getValue(Customer.class);
                        TextView userName = (TextView) header.findViewById(R.id.nav_header_userName);
                        userName.setText(data.getName());
                        TextView userPhone = (TextView) header.findViewById(R.id.nav_header_userPhone);
                        userPhone.setText(data.getPhoneNumber());
                        CircleImageView userDp = (CircleImageView) header.findViewById(R.id.nav_header_userDp);

                        if (!data.getDp().equals("")) {
                            Picasso.with(getApplicationContext()).load(data.getDp()).into(userDp);
                        } else {
                            userDp.setImageResource(R.drawable.nouser);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        final int minutes = 1000;
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
              /*  if (navController.getCurrentDestination().getLabel().toString().equalsIgnoreCase("recent orders")) {
                    fab.setVisibility(View.VISIBLE);
                } else if (navController.getCurrentDestination().getLabel().toString().equalsIgnoreCase("my orders")) {
                    fab.setVisibility(View.VISIBLE);
                } else if (navController.getCurrentDestination().getLabel().toString().equalsIgnoreCase("invites")) {
                    fab.setVisibility(View.GONE);
                } else if (navController.getCurrentDestination().getLabel().toString().equalsIgnoreCase("my profile")) {
                    fab.setVisibility(View.GONE);
                }*/
                handler.postDelayed(this, minutes);

            }
        }, minutes);

        versionName = findViewById(R.id.versionName);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionName.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout from this account?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseDatabase.getReference().child("tokens/" + firebaseAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        firebaseAuth.signOut();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
