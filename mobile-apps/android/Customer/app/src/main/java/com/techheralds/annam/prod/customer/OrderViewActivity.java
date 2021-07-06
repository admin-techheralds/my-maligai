package com.techheralds.annam.prod.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment;
import com.shreyaspatil.EasyUpiPayment.listener.PaymentStatusListener;
import com.shreyaspatil.EasyUpiPayment.model.TransactionDetails;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrderViewActivity extends AppCompatActivity {
    String supplier, orderedItems, status, deliveryTime, consumer, key, name, phoneNumber, userDp, createdOn, address, rejectionReason, saleId;
    String isPaid, payment_mode, supplierUpiId;
    double price;
    ArrayList<Map<String, Object>> demandList, timeline;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    TextView nameTxt, statusDesc, statusTextHelper, demandStatusTxt, deliveryTimeText, createdOnText, priceText, orderIdText, deliveryTextHeader, addressText, totalItemstext, rejectionHeader, rejectionText, paymentMode, paid;
    ListView listView;
    ArrayList<String> statusArr = new ArrayList<>();
    itemsAdapterList adapterList;
    timelineAdapterList timelineAdapterList;
    Button viewOrdersBtn, payWithUpiBtn;
    LinearLayout itemsLL;
    String currTime;
    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    viewBundleItemsAdapter viewBundleItemsAdapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        demandList = new ArrayList<>();
        timeline = new ArrayList<>();

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

        Intent i = getIntent();
        demandList = (ArrayList<Map<String, Object>>) getIntent().getSerializableExtra("demandList");
        timeline = (ArrayList<Map<String, Object>>) getIntent().getSerializableExtra("timeline");
        key = i.getExtras().getString("key");
        name = i.getExtras().getString("name");
        createdOn = i.getExtras().getString("createdOn");
        phoneNumber = i.getExtras().getString("phoneNumber");
        userDp = i.getExtras().getString("dp");
        supplier = i.getExtras().getString("supplier");
        orderedItems = i.getExtras().getString("items");
        status = i.getExtras().getString("status");
        deliveryTime = i.getExtras().getString("deliveryTime");
        consumer = i.getExtras().getString("consumer");
        Bundle b = getIntent().getExtras();
        price = b.getDouble("price");
        address = i.getExtras().getString("address");
        rejectionReason = i.getExtras().getString("rejectionReason");
        isPaid = i.getExtras().getString("paid");
        payment_mode = i.getExtras().getString("payment_mode");
        saleId = i.getExtras().getString("saleId");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        listView = findViewById(R.id.listView);
        nameTxt = findViewById(R.id.nameText);
        demandStatusTxt = findViewById(R.id.statusText);
        itemsLL = findViewById(R.id.itemsLL);
        //  deliveryTimeText = findViewById(R.id.deliveryTimeText);
        createdOnText = findViewById(R.id.createdOnText);
           priceText = findViewById(R.id.priceText);
        orderIdText = findViewById(R.id.orderIdText);
        addressText = findViewById(R.id.deliveryAddress);
        statusDesc = findViewById(R.id.statusDesc);
        // totalItemstext = findViewById(R.id.totalItems);
        //  viewOrdersBtn = findViewById(R.id.viewOrdersBtn);
        paymentMode = findViewById(R.id.paymentMode);
        //  paid = findViewById(R.id.isPaid);
        // payWithUpiBtn = findViewById(R.id.payWithUpi);
        statusTextHelper = findViewById(R.id.statusTextHelper);
        orderIdText.setText(key);
        nameTxt.setText(capitalize(firebaseUser.getDisplayName()));
        createdOnText.setText(createdOn);
        demandStatusTxt.setText(capitalize(status));
//        deliveryTimeText.setText(deliveryTime);
        addressText.setText(address);
        //   if (demandList != null) {
        //       totalItemstext.setText(demandList.size() == 1 ? demandList.size() + " Item" : demandList.size() + " Items");
        //   } else {
        //      totalItemstext.setText("0 Items");
        //  }
        // deliveryTextHeader = findViewById(R.id.deliveryTimeTextHeader);
    /*    if (deliveryTime != null) {
            deliveryTextHeader.setVisibility(View.VISIBLE);
            deliveryTimeText.setVisibility(View.VISIBLE);
            if (status.equalsIgnoreCase("placed")) {
                deliveryTextHeader.setText("Delivery Expected On");
            } else if (status.equalsIgnoreCase("accepted")) {
                deliveryTextHeader.setText("Delivery Expected On");
            } else if (status.equalsIgnoreCase("rejected")) {
                deliveryTextHeader.setText("Delivery Rejected On");
            } else if (status.equalsIgnoreCase("delivered")) {
                deliveryTextHeader.setText("Delivered On");
            } else if (status.equalsIgnoreCase("cancelled")) {
                deliveryTextHeader.setText("Cancelled On");
            } else if (status.equalsIgnoreCase("out for delivery")) {
                deliveryTextHeader.setText("Out for Delivery On");
            }
        }
*/
        if (payment_mode.equalsIgnoreCase("pod")) {
            if (isPaid.equalsIgnoreCase("not paid")) {
                paymentMode.setText("Pay on Delivery (Not Paid)");
            } else {
                paymentMode.setText("Pay on Delivery (Paid)");
            }
        } else {
            if (isPaid.equalsIgnoreCase("not paid")) {
                paymentMode.setText("UPI (Not Paid)");
            } else {
                paymentMode.setText("UPI (Paid)");
            }
        }


        statusDesc.setText("The order from sale: " + name +  " was " + status);

        firebaseDatabase.getReference().child("suppliers/" + supplier + "/upiId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    supplierUpiId = dataSnapshot.getValue().toString();

                    if (isPaid.equalsIgnoreCase("not paid")) {
                        paid.setText("Not Paid.You can pay with UPI");
                        payWithUpiBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
/*
        payWithUpiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random random = new Random();
                long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);
                String transactionId = String.valueOf(n);

                final EasyUpiPayment easyUpiPayment = new EasyUpiPayment.Builder()
                        .with(OrderViewActivity.this)
                        .setPayeeVpa(supplierUpiId)
                        .setPayeeName(name)
                        .setTransactionId(transactionId)
                        .setTransactionRefId(key)
                        .setDescription("Annam Farm Veggies")
                        .setAmount(String.valueOf(price))
                        .build();
                easyUpiPayment.startPayment();
                easyUpiPayment.setPaymentStatusListener(new PaymentStatusListener() {
                    @Override
                    public void onTransactionCompleted(TransactionDetails transactionDetails) {
                        //  Toast.makeText(PlaceOrderActivity.this, "Payment Completed", Toast.LENGTH_SHORT).show();
                        Map<String, Object> data = new HashMap<>();
                        data.put("details", transactionDetails);
                        data.put("timestamp", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
                        ;
                        String key1 = firebaseDatabase.getReference().child("demands/" + key + "/payment").push().getKey();
                        firebaseDatabase.getReference().child("demands/" + key + "/payment/" + key1).setValue(data);
                    }

                    @Override
                    public void onTransactionSuccess() {
                        Toast.makeText(OrderViewActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();

                        final ProgressDialog progressDialog = ProgressDialog.show(OrderViewActivity.this, null, "Placing Order.Please Wait...");

                        Map<String, Object> data = new HashMap<>();
                        data.put("paid", "paid");
                        data.put("payment_mode", "upi");

                        firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(OrderViewActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(OrderViewActivity.this, "Error occurred.Try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onTransactionSubmitted() {
                        // Toast.makeText(PlaceOrderActivity.this, "Pending | Subbmited", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTransactionFailed() {
                        Toast.makeText(OrderViewActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTransactionCancelled() {
                        Toast.makeText(OrderViewActivity.this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAppNotFound() {
                        Toast.makeText(OrderViewActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        String moneyString = formatter.format(price);
           priceText.setText(moneyString);

        rejectionHeader = findViewById(R.id.rejectionHeader);
         rejectionText = findViewById(R.id.rejectionReason);

        if (!rejectionReason.equals("")) {
            rejectionHeader.setVisibility(View.VISIBLE);
            rejectionText.setVisibility(View.VISIBLE);

            rejectionText.setText(rejectionReason);
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order Summary");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Append Ordered Items to Linear Layout
        for (int j = 0; j < demandList.size(); j++) {
            View view = LayoutInflater.from(OrderViewActivity.this).inflate(R.layout.orders_view_list,
                    itemsLL, false);

            TextView itemName = view.findViewById(R.id.itemName);
            TextView subHeader = view.findViewById(R.id.subHeader);
            TextView totalPrice = view.findViewById(R.id.totalPrice);
            ImageView itemImg = view.findViewById(R.id.itemImg);

            if (demandList.get(j).get("img") != null) {
                if (!demandList.get(j).get("img").equals("")) {
                    Picasso.with(OrderViewActivity.this).load(demandList.get(j).get("img").toString()).into(itemImg);
                }
            }

            itemName.setText(capitalize(demandList.get(j).get("name").toString()) + " (" + demandList.get(j).get("quantity").toString() + ")");
            NumberFormat formatter1 = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

            double price;
            if (demandList.get(j).get("price").getClass().getSimpleName().equalsIgnoreCase("long")) {
                Long l = new Long((Long) demandList.get(j).get("price"));
                price = l.doubleValue();
            } else {
                price = (double) demandList.get(j).get("price");
            }

            String moneyString1 = formatter1.format(price / Long.valueOf(demandList.get(j).get("count").toString()));
            String moneyString2 = formatter1.format(price);

            subHeader.setText(demandList.get(j).get("count").toString() + " X " + moneyString1);
            totalPrice.setText(moneyString2);

            final int finalJ = j;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (demandList.get(finalJ).get("sku").toString().startsWith("bundle")) {

                        ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) demandList.get(finalJ).get("items");

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OrderViewActivity.this);
                        bottomSheetDialog.setContentView(R.layout.view_bundle_sheet);

                        TextView bundleName = bottomSheetDialog.findViewById(R.id.bundleName);
                        ListView listView = bottomSheetDialog.findViewById(R.id.listView);
                        bundleName.setText(demandList.get(finalJ).get("name").toString());

                        viewBundleItemsAdapter = new viewBundleItemsAdapter(OrderViewActivity.this, data);
                        listView.setAdapter(viewBundleItemsAdapter);
                        bottomSheetDialog.show();
                    }
                }
            });

            itemsLL.addView(view);
        }

/*
        viewOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (demandList != null) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OrderViewActivity.this);
                    bottomSheetDialog.setContentView(R.layout.demand_item_sheet);
                    TextView header, priceText;
                    ListView listView;

                    header = bottomSheetDialog.findViewById(R.id.header);
                    header.setText("Ordered Items");

                    priceText = bottomSheetDialog.findViewById(R.id.totalPrice);
                    priceText.setVisibility(View.GONE);

                    listView = bottomSheetDialog.findViewById(R.id.itemsListView);
                    adapterList = new itemsAdapterList(OrderViewActivity.this, demandList);
                    listView.setAdapter(adapterList);

                    bottomSheetDialog.show();
                } else {
                    Toast.makeText(OrderViewActivity.this, "No Items to Show", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        demandStatusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog timeLineSheet = new BottomSheetDialog(OrderViewActivity.this);

                timeLineSheet.setContentView(R.layout.timeline_sheet);

                ListView listView = timeLineSheet.findViewById(R.id.listView);

                timelineAdapterList = new timelineAdapterList(OrderViewActivity.this, timeline);
                listView.setAdapter(timelineAdapterList);

                timeLineSheet.show();
            }
        });

        statusTextHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog timeLineSheet = new BottomSheetDialog(OrderViewActivity.this);

                timeLineSheet.setContentView(R.layout.timeline_sheet);

                ListView listView = timeLineSheet.findViewById(R.id.listView);

                timelineAdapterList = new timelineAdapterList(OrderViewActivity.this, timeline);
                listView.setAdapter(timelineAdapterList);

                timeLineSheet.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_view, menu);
        if (!status.equalsIgnoreCase("placed")) {
            menu.findItem(R.id.deleteDemandBtn).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    public void deleteDemandForConsumber() {
        new AlertDialog.Builder(OrderViewActivity.this).setTitle("Cancel Order")
                .setMessage("Are you sure?").setPositiveButton("Cancel Order", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog progressDialog = ProgressDialog.show(OrderViewActivity.this, null, "Cancelling Order...");

                firebaseDatabase.getReference().child("demands/" + key + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != "") {
                            if (dataSnapshot.getValue().toString().equalsIgnoreCase("placed")) {
                                firebaseDatabase.getReference().child("sales/" + supplier + "/" + saleId + "/endDate").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Date date1 = null;
                                            Date date2 = null;

                                            try {
                                                date2 = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(dataSnapshot.getValue().toString());
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            Long t1 = Calendar.getInstance().getTimeInMillis();
                                            Long t2 = date2.getTime();

                                            if (t1 < t2) {

                                                Map<String, Object> data = new HashMap<>();
                                                Map<String, Object> timeLineData = new HashMap<>();

                                                timeLineData.put("status", "Cancelled");
                                                timeLineData.put("date", currTime);
                                                timeline.add(timeLineData);

                                                data.put("deliveryTime", currTime);
                                                data.put("status", "cancelled");
                                                data.put("timeLine", timeline);

                                                firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        Intent intent = new Intent(OrderViewActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(OrderViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(OrderViewActivity.this, "Sale is over.You can't cancel this order", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                demandStatusTxt.setText(capitalize(dataSnapshot.getValue().toString()));
                                Toast.makeText(OrderViewActivity.this, "Order status changed by the supplier.Can't cancel the order", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).show();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.deleteDemandBtn:
                deleteDemandForConsumber();
                break;

        }

        return super.onOptionsItemSelected(item);
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
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.orders_view_list, parent, false);
            TextView itemName = view.findViewById(R.id.itemName);
            TextView itemQuantity = view.findViewById(R.id.itemQuantity);
            TextView count = view.findViewById(R.id.itemSelectedCount);
            ImageView itemImg = view.findViewById(R.id.itemImg);

            if (items.get(position).get("img") != null) {
                if (!items.get(position).get("img").equals("")) {
                    Picasso.with(OrderViewActivity.this).load(items.get(position).get("img").toString()).into(itemImg);
                }
            }

            itemName.setText(capitalize(items.get(position).get("name").toString()));
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            double price;
            if (items.get(position).get("price").getClass().getSimpleName().equalsIgnoreCase("long")) {
                Long l = new Long((Long) items.get(position).get("price"));
                price = l.doubleValue();
            } else {
                price = (double) items.get(position).get("price");
            }
            String moneyString = formatter.format(price);
            itemQuantity.setText(items.get(position).get("quantity").toString() + " - MRP: " + moneyString);
            count.setText(items.get(position).get("count").toString());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (items.get(position).get("sku").toString().startsWith("bundle")) {

                        ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) items.get(position).get("items");

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OrderViewActivity.this);
                        bottomSheetDialog.setContentView(R.layout.view_bundle_sheet);

                        TextView bundleName = bottomSheetDialog.findViewById(R.id.bundleName);
                        ListView listView = bottomSheetDialog.findViewById(R.id.listView);
                        bundleName.setText(items.get(position).get("name").toString());

                        viewBundleItemsAdapter = new viewBundleItemsAdapter(OrderViewActivity.this, data);
                        listView.setAdapter(viewBundleItemsAdapter);
                        bottomSheetDialog.show();
                    }
                }
            });

            return view;
        }
    }

    public float calculatePrice(String quantity, String price) {
        float q = Float.valueOf(quantity.replaceAll("[^\\d.]+|\\.(?!\\d)", ""));
        float p = Float.valueOf(price.replaceAll("[^\\d.]+|\\.(?!\\d)", ""));
        return q * p;
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
            price = view.findViewById(R.id.priceTxt);
            ImageView img = view.findViewById(R.id.itemImg);

            if (items.size() > 0) {
                name.setText(items.get(position).get("name").toString());


                if (items.get(position).get("img") != null) {
                    if (!items.get(position).get("img").toString().equalsIgnoreCase("")) {
                        Picasso.with(OrderViewActivity.this).load(items.get(position).get("img").toString()).into(img);
                    }
                }

                int qType = Math.toIntExact((Long) items.get(position).get("qType"));
                String q = items.get(position).get("actualQuantity").toString();
                String p = items.get(position).get("price").toString();

                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                double qp = calculatePrice(q, p);

                String moneyString = formatter.format(qp);

                if (qType == 0) {
                    price.setText("Price: " + moneyString);
                    actualQnt.setText("Quantity: " + items.get(position).get("actualQuantity").toString() + " Kg");
                }
                if (qType == 1) {
                    price.setText("Price: " + moneyString);
                    actualQnt.setText("Quantity: " + items.get(position).get("actualQuantity").toString() + " g");
                }
                if (qType == 2) {
                    price.setText("Price: " + moneyString);
                    actualQnt.setText("Quantity: " + items.get(position).get("actualQuantity").toString() + " L");
                }
                if (qType == 3) {
                    price.setText("Price: " + moneyString);
                    actualQnt.setText("Quantity: " + items.get(position).get("actualQuantity").toString() + " ml");
                }
                if (qType == 4) {
                    price.setText("Price: " + moneyString);
                    actualQnt.setText("Quantity: " + items.get(position).get("actualQuantity").toString() + " pc");
                }
            }

            return view;
        }
    }

    public class timelineAdapterList extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> items;

        public timelineAdapterList(Context context, ArrayList<Map<String, Object>> items) {
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
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.timeline_list, parent, false);
            TextView status = view.findViewById(R.id.status);
            TextView date = view.findViewById(R.id.date);

            status.setText("Order " + capitalize(items.get(position).get("status").toString()) + " On");
            date.setText(items.get(position).get("date").toString());

            return view;
        }
    }

}
