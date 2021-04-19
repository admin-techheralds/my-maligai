package com.techheralds.mymaligai.prod.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment;
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener;
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails;
import com.squareup.picasso.Picasso;
import com.techheralds.mymaligai.prod.customer.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import jxl.Image;

public class PlaceOrderActivity extends AppCompatActivity {
    final int UPI_PAYMENT = 101;
    String supplierUid, supplierName, supplierPhoneNumber, supplierDp, supplierUpiId = "";
    TextView nameText;
    CircleImageView dpImageView;
    EditText deliveryTImeBtn;
    Spinner spinner;
    ArrayList<Map<String, Object>> tags;
    ArrayList<String> tempTags;//for spinner
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    String currTag = "";
    ArrayList<inventory> inventoryItems, tempArray, searchArr;
    inventoryAdapterList inventoryAdapterList;
    ListView inventoryItemListView;
    String currTime;
    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    String startDate, endDate;
    Calendar c;
    Context ctx = this;
    ArrayList<String> unitsArr[];
    ArrayList<Double> multiplyFactorArr[];
    ArrayList<Map<String, Object>> demandsArr, tempDemandsArr, arrtoFindIndex;
    ArrayList<Integer> spinnerPoitions;
    TextView textCartItemCount;
    int mCartItemCount = 0;
    long hoursDiff = 0;
    double totalAmount = 0.0;
    itemsAdapterList itemsAdapterList;
    SearchView searchView;
    BottomSheetDialog bottomSheetDialog;

    //Search Sheet
    BottomSheetDialog searchSheet;
    int searchIndex = 0;
    LinearLayout searchLinearLayout;
    ListView searchListView;
    ProgressDialog searchProgressDialog;
    TextView searchTextView;
    //   demand tempDemand;
    String generatedKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        //Search View
        searchSheet = new BottomSheetDialog(PlaceOrderActivity.this);
        searchSheet.setContentView(R.layout.search_sheet);
        searchTextView = searchSheet.findViewById(R.id.textView);

        searchProgressDialog = new ProgressDialog(PlaceOrderActivity.this);

        searchView = searchSheet.findViewById(R.id.searchView);
        searchLinearLayout = searchSheet.findViewById(R.id.linearLayout);
        searchListView = searchSheet.findViewById(R.id.listView);

        searchSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                inventoryAdapterList = new inventoryAdapterList(PlaceOrderActivity.this, inventoryItems);
                inventoryItemListView.setAdapter(inventoryAdapterList);
            }
        });
        searchView.setIconified(false);
        searchView.setQueryHint("Search...");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Place Order");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        spinnerPoitions = new ArrayList<>();
        unitsArr = new ArrayList[5];
        multiplyFactorArr = new ArrayList[5];
        // initializing
        for (int i = 0; i < 5; i++) {
            unitsArr[i] = new ArrayList<>();
        }
        for (int i = 0; i < 5; i++) {
            multiplyFactorArr[i] = new ArrayList<>();
        }

        bottomSheetDialog = new BottomSheetDialog(PlaceOrderActivity.this);
        bottomSheetDialog.setContentView(R.layout.demand_item_sheet);

        // Big in grams
        unitsArr[0].add("250g");
        unitsArr[0].add("500g");
        unitsArr[0].add("1kg");
        unitsArr[0].add("2kg");
        unitsArr[0].add("5kg");
        unitsArr[0].add("10kg");
        // Low in grams
        unitsArr[1].add("5g");
        unitsArr[1].add("10g");
        unitsArr[1].add("25g");
        unitsArr[1].add("50g");
        unitsArr[1].add("100g");
        unitsArr[1].add("150g");
        // Big in liters
        unitsArr[2].add("200ml");
        unitsArr[2].add("250ml");
        unitsArr[2].add("500ml");
        unitsArr[2].add("1L");
        unitsArr[2].add("2L");
        // Low in liters
        unitsArr[3].add("10ml");
        unitsArr[3].add("25ml");
        unitsArr[3].add("50ml");
        unitsArr[3].add("100ml");
        unitsArr[3].add("150ml");
        //Counts
        unitsArr[4].add("1pc");
        unitsArr[4].add("2pcs");
        unitsArr[4].add("5pcs");
        unitsArr[4].add("10pcs");
        unitsArr[4].add("20pcs");

        // multuply factor grams
        multiplyFactorArr[0].add(0.25);
        multiplyFactorArr[0].add(0.5);
        multiplyFactorArr[0].add(1.0);
        multiplyFactorArr[0].add(2.0);
        multiplyFactorArr[0].add(5.0);
        multiplyFactorArr[0].add(10.0);
        //multiply factor small grams
        multiplyFactorArr[1].add(0.05);
        multiplyFactorArr[1].add(0.1);
        multiplyFactorArr[1].add(0.25);
        multiplyFactorArr[1].add(0.5);
        multiplyFactorArr[1].add(1.0);
        multiplyFactorArr[1].add(1.5);
        // multiply factor liters
        multiplyFactorArr[2].add(0.2);
        multiplyFactorArr[2].add(0.25);
        multiplyFactorArr[2].add(0.5);
        multiplyFactorArr[2].add(1.0);
        multiplyFactorArr[2].add(2.0);
        // multiply factor low liters
        multiplyFactorArr[3].add(0.1);
        multiplyFactorArr[3].add(0.25);
        multiplyFactorArr[3].add(0.5);
        multiplyFactorArr[3].add(1.0);
        multiplyFactorArr[3].add(1.5);
        //multiplyfactor counts
        multiplyFactorArr[4].add(1.0);
        multiplyFactorArr[4].add(2.0);
        multiplyFactorArr[4].add(5.0);
        multiplyFactorArr[4].add(10.0);
        multiplyFactorArr[4].add(20.0);

        tags = new ArrayList<>();
        inventoryItems = new ArrayList<>();
        tempArray = new ArrayList<>();

        inventoryItemListView = findViewById(R.id.inventoryItemsList);

       /* searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(final String newText) {
                inventoryAdapterList = new inventoryAdapterList(PlaceOrderActivity.this, tempArray);
                inventoryItemListView.setAdapter(inventoryAdapterList);
                inventoryAdapterList.getFilter().filter(newText);

                return false;
            }
        });*/


        tempTags = new ArrayList<>();

        demandsArr = new ArrayList<>();
        tempDemandsArr = new ArrayList<>();
        arrtoFindIndex = new ArrayList<>();
        searchArr = new ArrayList<>();

        mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        mMinute = Calendar.getInstance().get(Calendar.MINUTE);

        String AM_PM;
        if (mHour < 12) {
            AM_PM = "AM";
        } else {
            if ((mHour - 12) > 0) {
                mHour = mHour - 12;
            }
            AM_PM = "PM";
        }

        if (mMinute < 10) {
            currTime = mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":0" + mMinute + " " + AM_PM;
        } else {
            currTime = mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + mMinute + " " + AM_PM;
        }


        startDate = mYear + "-" + mMonth + "-" + mDay + " @ " + mHour + ":" + mMinute + " " + AM_PM;

        supplierUid = getIntent().getExtras().getString("uid");
        supplierName = getIntent().getExtras().getString("name");
        supplierPhoneNumber = getIntent().getExtras().getString("phoneNumber");
        supplierDp = getIntent().getExtras().getString("dp");

        dpImageView = findViewById(R.id.supplierDp);
        spinner = findViewById(R.id.spinner);
        nameText = findViewById(R.id.supplierName);

        deliveryTImeBtn = findViewById(R.id.deleveryTImeBtn);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 6);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy, hh:mm a");
        //   deliveryTImeBtn.setText(simpleDateFormat.format(calendar.getTime()));


        if (supplierDp.equals("")) {
            dpImageView.setImageResource(R.drawable.nouser);
        } else {
            Picasso.with(getApplicationContext()).load(supplierDp).into(dpImageView);
        }
        //get deleviry time
        deliveryTImeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_Datepicker();
            }
        });
        nameText.setText(supplierName);

        final ProgressDialog progressDialog = ProgressDialog.show(PlaceOrderActivity.this, null, "Please Wait...");
        tags.clear();

        //Load preferred category
        firebaseDatabase.getReference().child("suppliers/" + supplierUid + "/tags").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                loadItemsWithTag(tags.get(position).get("id").toString(), tags.get(position).get("category").toString());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(PlaceOrderActivity.this, "This supplier haven't added any categories", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlaceOrderActivity.this, "Can't Load Data", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseDatabase.getReference().child("suppliers/" + supplierUid + "/upiId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    supplierUpiId = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_order, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_cart);

        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    public void loadItemsWithTag(String id, String category) {
        final ProgressDialog progressDialog = ProgressDialog.show(PlaceOrderActivity.this, null, "Loading items with the category '" + capitalize(category) + "'.Please wait...");

        firebaseDatabase.getReference().child("inventory/" + supplierUid + "/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inventoryItems.clear();
                inventoryAdapterList = new inventoryAdapterList(PlaceOrderActivity.this, inventoryItems);
                inventoryItemListView.setAdapter(inventoryAdapterList);
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                        spinnerPoitions.add(0);
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        progressDialog.dismiss();
                        inventory data = ds.getValue(inventory.class);
                        tempArray.add(data);
                        inventoryItems.add(data);
                        inventoryAdapterList = new inventoryAdapterList(PlaceOrderActivity.this, inventoryItems);
                        inventoryItemListView.setAdapter(inventoryAdapterList);
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(PlaceOrderActivity.this, "No inventory items in this category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(PlaceOrderActivity.this, "Try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.placeDemandBtn:
                final String dTime = deliveryTImeBtn.getText().toString();

                if (demandsArr.size() > 0) {
                    if (dTime.length() > 0) {
                        if (hoursDiff >= 6) {
                            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                            double price = totalAmount;
                            String moneyString = formatter.format(price);

                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PlaceOrderActivity.this);
                            bottomSheetDialog.setContentView(R.layout.place_order_sheet);
                            RadioGroup radioGroup;
                            RadioButton podBtn, upiBtn;
                            radioGroup = bottomSheetDialog.findViewById(R.id.radioGroup);
                            podBtn = bottomSheetDialog.findViewById(R.id.payment_pod);
                            upiBtn = bottomSheetDialog.findViewById(R.id.payment_upi);

                            if (supplierUpiId.equals("")) {
                                podBtn.setChecked(true);
                                upiBtn.setVisibility(View.GONE);
                            }


                            final Button btn, addBtn;
                            btn = bottomSheetDialog.findViewById(R.id.placeOrderBtn);
                            addBtn = bottomSheetDialog.findViewById(R.id.addBtn);

                            final TextView totalItems, totalMrp, address, date;
                            totalItems = bottomSheetDialog.findViewById(R.id.totalItems);
                            totalMrp = bottomSheetDialog.findViewById(R.id.totalMrp);
                            address = bottomSheetDialog.findViewById(R.id.address);
                            date = bottomSheetDialog.findViewById(R.id.date);
                            final String[] selectedPayment = {"pod"};
                            radioGroup.check(R.id.payment_pod);

                            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup rg, int i) {
                                    switch (i) {
                                        case R.id.payment_pod:
                                            selectedPayment[0] = "pod";
                                            btn.setText("Place Order");
                                            break;
                                        case R.id.payment_upi:
                                            selectedPayment[0] = "upi";
                                            btn.setText("Continue");
                                            break;
                                    }
                                }
                            });

                            addBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Add New Delivery Address

                                    final BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(PlaceOrderActivity.this);
                                    bottomSheetDialog1.setContentView(R.layout.edit_address_sheet);

                                    TextView header = bottomSheetDialog1.findViewById(R.id.header);
                                    header.setText("Add Address");

                                    Button btn1 = bottomSheetDialog1.findViewById(R.id.addressBtn);
                                    btn1.setText("Add Address");

                                    final EditText addressIp = bottomSheetDialog1.findViewById(R.id.addressIp);

                                    btn1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (!addressIp.getText().toString().trim().equals("")) {
                                                final ProgressDialog progressDialog = ProgressDialog.show(PlaceOrderActivity.this, null, "Please wait...");
                                                firebaseDatabase.getReference().child("customers/" + firebaseAuth.getCurrentUser().getUid() + "/address").setValue(addressIp.getText().toString().trim())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                //Save delivery address locally
                                                                SharedPreferences sharedPreferences = getSharedPreferences("local", Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.putString("address", addressIp.getText().toString().trim());
                                                                editor.apply();
                                                                address.setText(addressIp.getText().toString().trim());
                                                                progressDialog.dismiss();
                                                                bottomSheetDialog1.dismiss();
                                                                addBtn.setVisibility(View.GONE);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(PlaceOrderActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(PlaceOrderActivity.this, "Enter Delivery Address", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    bottomSheetDialog1.show();
                                }
                            });

                            totalItems.setText(demandsArr.size() == 1 ? demandsArr.size() + " Item" : demandsArr.size() + " Items");
                            totalMrp.setText(moneyString);
                            date.setText(deliveryTImeBtn.getText().toString());

                            //Get delivery address
                            SharedPreferences sharedPreferences = getSharedPreferences("local", Context.MODE_PRIVATE);
                            final String deliveryAddress = sharedPreferences.getString("address", "");

                            if (deliveryAddress.equals("")) {
                                address.setText("No delivery address added");
                                addBtn.setVisibility(View.VISIBLE);
                            } else {
                                address.setText(deliveryAddress);
                            }

                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Get delivery address
                                    SharedPreferences sharedPreferences = getSharedPreferences("local", Context.MODE_PRIVATE);
                                    final String deliveryAddress = sharedPreferences.getString("address", "");

                                    if (!deliveryAddress.equals("")) {
                                        final ArrayList<Map<String, Object>> timeLine = new ArrayList<>();
                                        Map<String, Object> timeLineData = new HashMap<>();

                                        timeLineData.put("status", "Placed");
                                        timeLineData.put("date", currTime);
                                        timeLine.add(timeLineData);
                                        generatedKey = firebaseDatabase.getReference().child("demands").push().getKey();

                                        if (selectedPayment[0].equals("pod")) {
                                            bottomSheetDialog.dismiss();
                                            final ProgressDialog progressDialog = ProgressDialog.show(PlaceOrderActivity.this, null, "Placing Order.Please Wait...");

                                            demand newDemand = new demand(firebaseUser.getUid(), supplierUid, dTime, "placed", currTime, demandsArr, timeLine, generatedKey, deliveryAddress, totalAmount, selectedPayment[0], null, "not paid");
                                            firebaseDatabase.getReference().child("demands/" + generatedKey).setValue(newDemand).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ctx, "Error occurred.Try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            //  tempDemand = new demand(firebaseUser.getUid(), supplierUid, dTime, "placed", currTime, demandsArr, timeLine, generatedKey, deliveryAddress, totalAmount, selectedPayment[0], null,true);
                                            Random random = new Random();
                                            long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);
                                            String transactionId = String.valueOf(n);
                                            String price = String.valueOf(Double.valueOf(String.valueOf(Math.round(totalAmount))));

                                            final EasyUpiPayment easyUpiPayment = new EasyUpiPayment.Builder()
                                                    .with(PlaceOrderActivity.this)
                                                    .setPayeeVpa(supplierUpiId)
                                                    .setPayeeName(supplierName)
                                                    .setTransactionId(transactionId)
                                                    .setTransactionRefId(generatedKey)
                                                    .setDescription("Annam Farm Veggies")
                                                    .setAmount(price)
                                                    .build();

                                            easyUpiPayment.startPayment();

                                            final TransactionDetails[] tempTransactionDetails = new TransactionDetails[1];

                                            easyUpiPayment.setPaymentStatusListener(new PaymentStatusListener() {
                                                @Override
                                                public void onTransactionCompleted(TransactionDetails transactionDetails) {
                                                    //  Toast.makeText(PlaceOrderActivity.this, "Payment Completed", Toast.LENGTH_SHORT).show();
                                                    tempTransactionDetails[0] = transactionDetails;
                                                }

                                                @Override
                                                public void onTransactionSuccess() {
                                                    Toast.makeText(PlaceOrderActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                                                    bottomSheetDialog.dismiss();
                                                    final ProgressDialog progressDialog = ProgressDialog.show(PlaceOrderActivity.this, null, "Placing Order.Please Wait...");

                                                    demand newDemand = new demand(firebaseUser.getUid(), supplierUid, dTime, "placed", currTime, demandsArr, timeLine, generatedKey, deliveryAddress, totalAmount, selectedPayment[0], null, "paid");
                                                    firebaseDatabase.getReference().child("demands/" + generatedKey).setValue(newDemand).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @SuppressLint("SimpleDateFormat")
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("details", tempTransactionDetails[0]);
                                                            data.put("timestamp", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));                       ;
                                                            String key1 = firebaseDatabase.getReference().child("demands/" + generatedKey + "/payment").push().getKey();
                                                            firebaseDatabase.getReference().child("demands/" + generatedKey + "/payment/" + key1).setValue(data);
                                                            progressDialog.dismiss();
                                                            Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(ctx, "Error occurred.Try again", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onTransactionSubmitted() {
                                                    // Toast.makeText(PlaceOrderActivity.this, "Pending | Subbmited", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onTransactionFailed() {
                                                    Toast.makeText(PlaceOrderActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onTransactionCancelled() {
                                                    Toast.makeText(PlaceOrderActivity.this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onAppNotFound() {
                                                    Toast.makeText(PlaceOrderActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(PlaceOrderActivity.this, "Add Delivery Address", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            bottomSheetDialog.show();

                        } else {
                            Toast.makeText(ctx, "Delivery Time should be 6 hours from now", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ctx, "Select delivery time", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PlaceOrderActivity.this, "Add atleast one item you need", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_cart:
                if (mCartItemCount > 0) {

                    TextView priceText = bottomSheetDialog.findViewById(R.id.totalPrice);
                    TextView header = bottomSheetDialog.findViewById(R.id.header);
                    header.setText("Items in Cart");

                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    double price = totalAmount;
                    String moneyString = formatter.format(price);
                    priceText.setText("Total Price: " + moneyString);
                    ListView listView1 = bottomSheetDialog.findViewById(R.id.itemsListView);
                    itemsAdapterList = new itemsAdapterList(PlaceOrderActivity.this, demandsArr);
                    listView1.setAdapter(itemsAdapterList);
                    bottomSheetDialog.show();

                } else {
                    Toast.makeText(ctx, "No items added to cart", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.searchBtn:
                searchSheet.show();

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        if (!s.trim().equals("")) {
                            searchIndex = 0;
                            searchArr.clear();
                            searchProgressDialog.setMessage("Searching...");
                            searchProgressDialog.show();
                            search(s);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void search(final String s) {
        firebaseDatabase.getReference().child("inventory/" + supplierUid + "/" + tags.get(searchIndex).get("id")).orderByChild("searchname")
                .startAt(s.toLowerCase().trim())
                .endAt(s.toLowerCase().trim() + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        inventory data = ds.getValue(inventory.class);
                        // Toast.makeText(ctx, "S "+data.getSku(), Toast.LENGTH_SHORT).show();
                        if (findIndexOf(data.getSku()) == -1) {
                            searchArr.add(data);
                        }
                    }
                }

                searchIndex++;
                if (searchIndex >= 1) {
                    if (searchIndex < tags.size()) {
                        search(s);

                    } else {
                        if (searchArr.size() > 0) {
                            searchLinearLayout.setVisibility(View.GONE);
                        } else {
                            searchTextView.setText("No Results for '" + s + "'");
                            searchLinearLayout.setVisibility(View.VISIBLE);
                        }
                        searchProgressDialog.dismiss();
                        inventoryAdapterList = new inventoryAdapterList(PlaceOrderActivity.this, searchArr);
                        searchListView.setAdapter(inventoryAdapterList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public int findIndexOf(String itemName) {
        int index = -1;
        for (int i = 0; i < searchArr.size(); i++) {
            if (searchArr.get(i).getSku().trim().toLowerCase().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    private void setupBadge() {

        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static Date parseDate(String strDate) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    private void show_Datepicker() {
        c = Calendar.getInstance();
        int mYearParam = mYear;
        int mMonthParam = mMonth - 1;
        int mDayParam = mDay;

        DatePickerDialog datePickerDialog = new DatePickerDialog(ctx,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mMonth = monthOfYear + 1;
                        mYear = year;
                        mDay = dayOfMonth;
                        show_Timepicker();

                    }
                }, mYearParam, mMonthParam, mDayParam);
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    private void show_Timepicker() {

        TimePickerDialog timePickerDialog = new TimePickerDialog(ctx,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int pHour,
                                          int pMinute) {

                        mHour = pHour;
                        mMinute = pMinute;
                        String AM_PM;
                        if (mHour < 12) {
                            AM_PM = "AM";
                        } else {
                            if ((mHour - 12) > 0) {
                                mHour = mHour - 12;
                            }
                            AM_PM = "PM";
                        }
                        if (mMinute < 10) {
                            deliveryTImeBtn.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":0" + mMinute + " " + AM_PM);
                        } else {
                            deliveryTImeBtn.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + mMinute + " " + AM_PM);
                        }
                        String dateFormat = "yyyy-MM-dd @ hh:mm a";
                        endDate = mYear + "-" + mMonth + "-" + mDay + " @ " + mHour + ":" + mMinute + " " + AM_PM;
                        try {
                            hoursDiff = hoursDifference(new SimpleDateFormat(dateFormat).parse(startDate), new SimpleDateFormat(dateFormat).parse(endDate));
                        } catch (ParseException e) {
                            Toast.makeText(ctx, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();
    }

    private long hoursDifference(Date date1, Date date2) {
        long secs = (date2.getTime() - date1.getTime()) / 1000;
        long hours = secs / 3600;
        return hours;
    }


    public class inventoryAdapterList extends BaseAdapter implements Filterable {
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
            view = LayoutInflater.from(context).inflate(R.layout.place_demand_list, parent, false);

            if (inventories.size() > 0) {
                ImageView itemImg = view.findViewById(R.id.itemImg);
                TextView itemName = view.findViewById(R.id.itemName);
                final TextView itemPrice = view.findViewById(R.id.itemPrice);
                final ImageButton addItemBtn = view.findViewById(R.id.addItemBtn);
                final ImageButton minusItemBtn = view.findViewById(R.id.minusItemBtn);
                final TextView countText = view.findViewById(R.id.itemSelectedCount);
                final Spinner quantitySpinner = view.findViewById(R.id.itemquantitySpinner);
                final String[] currSpinnerValue = {unitsArr[inventories.get(position).getQuantity_type()].get(0)};
                final double[] currItemPrice = {0};
                final LinearLayout counterLayout = view.findViewById(R.id.counterLayout);

                if (inventories.get(position).getImg() != null) {
                    if (!inventories.get(position).getImg().equals("")) {
                        Picasso.with(PlaceOrderActivity.this).load(inventories.get(position).getImg()).into(itemImg);
                    }
                }

                itemName.setText(capitalize(inventories.get(position).getName()));

                ArrayAdapter<String> adapter = new ArrayAdapter<>(PlaceOrderActivity.this, R.layout.support_simple_spinner_dropdown_item, unitsArr[inventories.get(position).getQuantity_type()]);
                quantitySpinner.setAdapter(adapter);

                quantitySpinner.setSelection(spinnerPoitions.get(position));


                Map<String, Object> data = new HashMap<>();
                data.put("name", inventories.get(position).getName());
                data.put("quantity", quantitySpinner.getSelectedItem().toString());
                data.put("img", inventories.get(position).getImg());
                data.put("sku", inventories.get(position).getSku());
                int i = arrtoFindIndex.indexOf(data);
                if (i > -1) {
                    countText.setText(demandsArr.get(i).get("count").toString());
                }

                final int count = Integer.parseInt(countText.getText().toString());

                if (count > 0) {
                    counterLayout.setBackgroundResource(R.drawable.green_border);
                } else {
                    counterLayout.setBackgroundResource(R.drawable.border);
                }

                quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int sPosition, long id) {
                        spinnerPoitions.set(position, quantitySpinner.getSelectedItemPosition());
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                        double price = multiplyFactorArr[inventories.get(position).getQuantity_type()].get(sPosition) * inventories.get(position).getPrice();
                        String moneyString = formatter.format(price);
                        itemPrice.setText("Price: " + moneyString);

                        //   countText.setText();

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", inventories.get(position).getName());
                        data.put("quantity", quantitySpinner.getSelectedItem());
                        data.put("sku", inventories.get(position).getSku());
                        data.put("img", inventories.get(position).getImg());

                        int i = arrtoFindIndex.indexOf(data);

                        if (!currSpinnerValue[0].equals(quantitySpinner.getSelectedItem())) {
                            if (i == -1) {
                                countText.setText("0");
                                counterLayout.setBackgroundResource(R.drawable.border);
                            } else {
                                countText.setText(demandsArr.get(i).get("count").toString());
                                counterLayout.setBackgroundResource(R.drawable.green_border);
                            }
                        }
                        currSpinnerValue[0] = (String) quantitySpinner.getSelectedItem();
                        currItemPrice[0] = price;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                addItemBtn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(View v) {

                        Map<String, Object> temp = new HashMap<>();
                        temp.put("name", inventories.get(position).getName());
                        temp.put("quantity", quantitySpinner.getSelectedItem().toString());
                        temp.put("img", inventories.get(position).getImg());
                        temp.put("sku", inventories.get(position).getSku());

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", inventories.get(position).getName());
                        data.put("quantity", quantitySpinner.getSelectedItem().toString());
                        data.put("img", inventories.get(position).getImg());
                        data.put("sku", inventories.get(position).getSku());

                        int count = Integer.parseInt(countText.getText().toString()) + 1;

                        countText.setText(String.valueOf(count));

                        int i = arrtoFindIndex.indexOf(temp);

                        data.put("count", count);
                        data.put("price", currItemPrice[0] * count);
                        totalAmount = totalAmount + currItemPrice[0];
                        if (i == -1) {
                            demandsArr.add(data);
                            arrtoFindIndex.add(temp);
                        } else {
                            demandsArr.set(i, data);
                            arrtoFindIndex.set(i, temp);
                        }

                        counterLayout.setBackgroundResource(R.drawable.green_border);

                        mCartItemCount = demandsArr.size();
                        setupBadge();

                    }
                });

                minusItemBtn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(View v) {

                        Map<String, Object> temp = new HashMap<>();
                        temp.put("name", inventories.get(position).getName());
                        temp.put("quantity", quantitySpinner.getSelectedItem().toString());
                        temp.put("img", inventories.get(position).getImg());
                        temp.put("sku", inventories.get(position).getSku());

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", inventories.get(position).getName());
                        data.put("quantity", quantitySpinner.getSelectedItem().toString());
                        data.put("img", inventories.get(position).getImg());
                        data.put("sku", inventories.get(position).getSku());

                        if (Integer.parseInt(countText.getText().toString()) > 0) {
                            int count = Integer.parseInt(countText.getText().toString()) - 1;

                            countText.setText(String.valueOf(count));

                            int i = arrtoFindIndex.indexOf(temp);

                            data.put("price", currItemPrice[0] * count);
                            if (count == 0) {
                                data.put("count", count);
                                totalAmount = totalAmount - currItemPrice[0];
                                demandsArr.remove(i);
                                arrtoFindIndex.remove(i);
                            } else {
                                data.put("count", count);
                                totalAmount = totalAmount - currItemPrice[0];
                                demandsArr.set(i, data);
                                arrtoFindIndex.set(i, temp);
                            }

                            mCartItemCount = demandsArr.size();
                            setupBadge();

                            if (count == 0) {
                                counterLayout.setBackgroundResource(R.drawable.border);
                            }
                        }

                    }
                });
            }

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
                        for (inventory item : tempArray) {
                            String firstName = item.getName().toLowerCase(); // if we ignore case
                            //String lastName = item.get("pho").toLowerCase(); // if we ignore case
                            if (firstName.contains(constraint.toString())) {
                                filteredList.add((Map<String, Object>) item); // added item witch contains our text in EditText
                            }
                        }

                        results.count = filteredList.size(); // set count of filtered list
                        results.values = filteredList; // set filtered list
                    }
                    return results; // return our filtered list
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    inventoryItems = (ArrayList<inventory>) results.values; // replace list to filtered list
                    notifyDataSetChanged(); // refresh adapter
                }
            };
            return filter;
        }
    }

    public class itemsAdapterList extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> items;

        public itemsAdapterList(Context context, ArrayList<Map<String, Object>> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @SuppressLint({"ViewHolder", "SetTextI18n"})
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.demand_item_list, parent, false);
            TextView itemName = view.findViewById(R.id.itemName);
            TextView itemQuantity = view.findViewById(R.id.itemQuantity);
            final TextView countText = view.findViewById(R.id.itemSelectedCount);
            ImageView itemImg = view.findViewById(R.id.itemImg);
            ImageButton addItemBtn = view.findViewById(R.id.addItemBtn);
            ImageButton minusItemBtn = view.findViewById(R.id.minusItemBtn);
            final LinearLayout counterLayout = view.findViewById(R.id.counterLayout);

            int count = ((int) items.get(position).get("count"));

            if (count > 0) {
                counterLayout.setBackgroundResource(R.drawable.green_border);
            } else {
                counterLayout.setBackgroundResource(R.drawable.border);
            }

            if (items.get(position).get("img") != null) {
                if (!items.get(position).get("img").equals("")) {
                    Picasso.with(PlaceOrderActivity.this).load(items.get(position).get("img").toString()).into(itemImg);
                }
            }
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            final double price = ((double) items.get(position).get("price"));
            String moneyString = formatter.format(price);
            itemName.setText(capitalize(items.get(position).get("name").toString()));
            itemQuantity.setText(items.get(position).get("quantity").toString() + " - Price: " + moneyString);
            countText.setText(items.get(position).get("count").toString());

            addItemBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {

                    Map<String, Object> temp = new HashMap<>();
                    temp.put("name", items.get(position).get("name"));
                    temp.put("quantity", items.get(position).get("quantity"));
                    temp.put("img", items.get(position).get("img"));
                    temp.put("sku", items.get(position).get("sku"));

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", items.get(position).get("name"));
                    data.put("quantity", items.get(position).get("quantity"));
                    data.put("img", items.get(position).get("img"));
                    data.put("sku", items.get(position).get("sku"));

                    int count = Integer.parseInt(countText.getText().toString()) + 1;
                    int divider = Integer.parseInt(countText.getText().toString());

                    countText.setText(String.valueOf(count));

                    int i = arrtoFindIndex.indexOf(temp);

                    double price = Double.parseDouble(items.get(position).get("price").toString()) / divider;

                    data.put("count", count);
                    data.put("price", price * count);
                    totalAmount = totalAmount + price;
                    if (i == -1) {
                        demandsArr.add(data);
                        items.add(data);
                        arrtoFindIndex.add(temp);
                    } else {
                        demandsArr.set(i, data);
                        items.set(i, data);
                        arrtoFindIndex.set(i, temp);
                    }

                    mCartItemCount = demandsArr.size();
                    setupBadge();

                    updatePriceInCartView();

                    counterLayout.setBackgroundResource(R.drawable.green_border);

                    itemsAdapterList.notifyDataSetChanged();

                    inventoryAdapterList.notifyDataSetChanged();
                }
            });

            minusItemBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {

                    Map<String, Object> temp = new HashMap<>();
                    temp.put("name", items.get(position).get("name"));
                    temp.put("quantity", items.get(position).get("quantity"));
                    temp.put("img", items.get(position).get("img"));
                    temp.put("sku", items.get(position).get("sku"));

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", items.get(position).get("name"));
                    data.put("quantity", items.get(position).get("quantity"));
                    data.put("img", items.get(position).get("img"));
                    data.put("sku", items.get(position).get("sku"));

                    if (Integer.parseInt(countText.getText().toString()) > 0) {
                        int count = Integer.parseInt(countText.getText().toString()) - 1;
                        int divider = Integer.parseInt(countText.getText().toString());
                        countText.setText(String.valueOf(count));

                        int i = arrtoFindIndex.indexOf(temp);
                        double price = Double.parseDouble(items.get(position).get("price").toString()) / divider;

                        data.put("price", price * count);
                        if (count == 0) {
                            data.put("count", count);
                            totalAmount = totalAmount - price;
                            demandsArr.remove(i);
//                            items.remove(i);
                            arrtoFindIndex.remove(i);
                        } else {
                            data.put("count", count);
                            totalAmount = totalAmount - price;
                            demandsArr.set(i, data);
                            items.set(i, data);
                            arrtoFindIndex.set(i, temp);
                        }

                        mCartItemCount = demandsArr.size();
                        setupBadge();

                        updatePriceInCartView();

                        if (demandsArr.size() == 0) {
                            bottomSheetDialog.dismiss();
                        }
                    }

                    itemsAdapterList.notifyDataSetChanged();

                    inventoryAdapterList.notifyDataSetChanged();
                }
            });

            return view;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updatePriceInCartView() {
        TextView priceText = bottomSheetDialog.findViewById(R.id.totalPrice);
        NumberFormat formatter1 = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        double price1 = totalAmount;
        String moneyString1 = formatter1.format(price1);
        priceText.setText("Total Price: " + moneyString1);
    }

    @Override
    public void onBackPressed() {
        if (demandsArr.size() > 0) {
            new AlertDialog.Builder(PlaceOrderActivity.this).setTitle("Hold On!").setMessage("This order will be cancelled.Are you sure to go back?").setNegativeButton("Stay here", null)
                    .setPositiveButton("Cancel Order", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        } else {
            finish();
        }
    }
}
