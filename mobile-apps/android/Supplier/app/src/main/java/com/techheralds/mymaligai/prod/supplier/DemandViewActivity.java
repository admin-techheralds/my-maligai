package com.techheralds.mymaligai.prod.supplier;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class DemandViewActivity extends AppCompatActivity {
    String supplier, orderedItems, status, deliveryTime, consumer, key, name, phoneNumber, userDp, createdOn, address, rejectionReason;
    String isPaid, payment_mode;
    double price;
    ArrayList<Map<String, Object>> demandList, timeline;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    TextView nameTxt, deliveryTimeText, phoneNumberText, createdOnText, priceText, orderIdText, deliveryTextHeader, addressText, totalItemstext, timelineHint, rejectionHeader, rejectionText, paymentMode, paid, paidHint;
    CircleImageView dp;
    Spinner statusSpinner;
    ArrayList<String> statusArr = new ArrayList<String>();
    Boolean isLoaded = false;
    ListView listView;
    itemsAdapterList adapterList;
    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    String currTime;
    WebView mWebView;
    Button viewOrdersBtn;
    timelineAdapterList timelineAdapterList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demand_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order Details");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        rejectionHeader = findViewById(R.id.rjectionHeader);
        rejectionText = findViewById(R.id.rejectionReason);

        if (!rejectionReason.equals("")) {
            rejectionHeader.setVisibility(View.VISIBLE);
            rejectionText.setVisibility(View.VISIBLE);

            rejectionText.setText(rejectionReason);
        }

        deliveryTextHeader = findViewById(R.id.deliveryTimeTextHeader);
        statusSpinner = findViewById(R.id.statusSpinner);
        timelineHint = findViewById(R.id.timelineHint);

        if (status.equalsIgnoreCase("placed")) {
            statusArr.add("Placed");
            statusArr.add("Accepted");
            statusArr.add("Rejected");
            deliveryTextHeader.setText("Delivery Expected On");
        } else if (status.equalsIgnoreCase("accepted")) {
            statusArr.add("Accepted");
            statusArr.add("Out for Delivery");
            deliveryTextHeader.setText("Delivery Expected On");
        } else if (status.equalsIgnoreCase("rejected")) {
            statusArr.add("Rejected");
            //  statusSpinner.setEnabled(false);
            deliveryTextHeader.setText("Delivery Rejected On");
        } else if (status.equalsIgnoreCase("delivered")) {
            deliveryTextHeader.setText("Delivered On");
            statusArr.add("Delivered");
            //statusSpinner.setEnabled(false);
        } else if (status.equalsIgnoreCase("out for delivery")) {
            deliveryTextHeader.setText("Delivery Expected On");
            statusArr.add("Out for Delivery");
            statusArr.add("Delivered");
            //statusSpinner.setEnabled(false);
        } else if (status.equalsIgnoreCase("cancelled")) {
            deliveryTextHeader.setText("Cancelled On");
            statusArr.add("Cancelled");
            //statusSpinner.setEnabled(false);
        }


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DemandViewActivity.this, R.layout.support_simple_spinner_dropdown_item, statusArr);
        statusSpinner.setAdapter(adapter);
        statusSpinner.setSelection(statusArr.indexOf((capitalize(status))));

        dp = findViewById(R.id.dp);
        nameTxt = findViewById(R.id.nameText);
        deliveryTimeText = findViewById(R.id.deliveryTimeText);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        createdOnText = findViewById(R.id.createdOnText);
        priceText = findViewById(R.id.priceText);
        orderIdText = findViewById(R.id.orderIdText);
        addressText = findViewById(R.id.deliveryAddress);
        totalItemstext = findViewById(R.id.totalItems);
        viewOrdersBtn = findViewById(R.id.viewOrdersBtn);
        paymentMode = findViewById(R.id.paymentMode);
        paid = findViewById(R.id.isPaid);
        paidHint = findViewById(R.id.paidHint);

        orderIdText.setText(key);
        nameTxt.setText(name);
        phoneNumberText.setText(phoneNumber);
        createdOnText.setText(createdOn);
        addressText.setText(address);
        totalItemstext.setText(demandList.size() == 1 ? demandList.size() + " Item" : demandList.size() + " Items");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        if (payment_mode != null) {
            paymentMode.setText(!payment_mode.equals("") ? payment_mode.toUpperCase() : "None");
        } else {
            paymentMode.setText("None");
        }

        if (isPaid != null) {
            if (!isPaid.equals("")) {
                if (isPaid.equalsIgnoreCase("paid")) {
                    paid.setText("Paid");
                } else {
                    paid.setText("Not Paid");
                    paidHint.setVisibility(View.VISIBLE);
                }
            } else {
                paid.setText("Not Paid");
                paidHint.setVisibility(View.VISIBLE);
            }
        } else {
            paid.setText("Not Paid");
            paidHint.setVisibility(View.VISIBLE);
        }

        paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaid != null) {
                    if (!isPaid.equalsIgnoreCase("paid")) {
                        changeToPaid();
                    }
                } else {
                    changeToPaid();
                }
            }
        });

        String moneyString = formatter.format(price);
        priceText.setText(moneyString);

        if (userDp.equals("")) {
            dp.setImageResource(R.drawable.nouser);
        } else {
            Picasso.with(DemandViewActivity.this).load(userDp).into(dp);
        }


        statusSpinner.setVisibility(View.VISIBLE);
        TextView header = findViewById(R.id.header);
        header.setText("Consumer Details");
        deliveryTimeText.setText(deliveryTime);

        viewOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DemandViewActivity.this);
                bottomSheetDialog.setContentView(R.layout.demand_item_sheet);
                TextView header, priceText;
                ListView listView;

                header = bottomSheetDialog.findViewById(R.id.header);
                header.setText("Demanded Items");

                priceText = bottomSheetDialog.findViewById(R.id.totalPrice);
                priceText.setVisibility(View.GONE);

                listView = bottomSheetDialog.findViewById(R.id.itemsListView);
                adapterList = new itemsAdapterList(DemandViewActivity.this, demandList);
                listView.setAdapter(adapterList);

                bottomSheetDialog.show();
            }
        });

        timelineHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog timeLineSheet = new BottomSheetDialog(DemandViewActivity.this);

                timeLineSheet.setContentView(R.layout.timeline_sheet);

                ListView listView = timeLineSheet.findViewById(R.id.listView);

                timelineAdapterList = new timelineAdapterList(DemandViewActivity.this, timeline);
                listView.setAdapter(timelineAdapterList);

                timeLineSheet.show();
            }
        });


        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLoaded) {
                    if (statusArr.get(position).equalsIgnoreCase("accepted")) {
                        show_Datepicker();
                    } else if (statusArr.get(position).equalsIgnoreCase("delivered")) {
                        if (isPaid.equalsIgnoreCase("paid")) {
                            orderDelivered(false); //Already Paid
                        } else {
                            new AlertDialog.Builder(DemandViewActivity.this)
                                    .setTitle("Amount Paid")
                                    .setMessage("Total amount for this order is paid?")

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton("Paid", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            orderDelivered(true); //Paid
                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton("Not Paid", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            orderDelivered(false); //Not Paid
                                        }
                                    })
                                    .show();
                        }
                    } else if (statusArr.get(position).equalsIgnoreCase("placed")) {

                        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Please Wait...");
                        firebaseDatabase.getReference().child("demands/" + key + "/status").setValue("Placed").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(DemandViewActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (statusArr.get(position).equalsIgnoreCase("out for delivery")) {
                        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Please wait...");
                        firebaseDatabase.getReference().child("demands/" + key + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.getValue().toString().equalsIgnoreCase("cancelled")) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("status", "Out for Delivery");
                                    data.put("deliveryTime", currTime);

                                    Map<String, Object> timeLineData = new HashMap<>();

                                    timeLineData.put("status", "Out for Delivery");
                                    timeLineData.put("date", currTime);
                                    timeline.add(timeLineData);
                                    data.put("timeLine", timeline);

                                    firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    statusArr.clear();
                                    statusArr.add("Cancelled");
                                    deliveryTextHeader.setText("Cancelled On");
                                    Toast.makeText(DemandViewActivity.this, "Order cancelled by the customer", Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    } else if (statusArr.get(position).equalsIgnoreCase("rejected")) {
                        final BottomSheetDialog rejectionSheet = new BottomSheetDialog(DemandViewActivity.this);
                        rejectionSheet.setContentView(R.layout.rejection_reason_sheet);
                        final EditText reasonIp = rejectionSheet.findViewById(R.id.ip);
                        Button btn = rejectionSheet.findViewById(R.id.btn);

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String reason = reasonIp.getText().toString().trim();

                                if (!reason.equals("")) {
                                    final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Rejecting Order...");
                                    firebaseDatabase.getReference().child("demands/" + key + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.getValue().toString().equalsIgnoreCase("cancelled")) {
                                                rejectionSheet.dismiss();
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("status", "Rejected");
                                                data.put("deliveryTime", currTime);
                                                data.put("rejectionReason", reason);

                                                Map<String, Object> timeLineData = new HashMap<>();

                                                timeLineData.put("status", "Rejected");
                                                timeLineData.put("date", currTime);
                                                timeline.add(timeLineData);
                                                data.put("timeLine", timeline);

                                                firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                progressDialog.dismiss();
                                                statusArr.clear();
                                                statusArr.add("Cancelled");
                                                deliveryTextHeader.setText("Cancelled On");
                                                Toast.makeText(DemandViewActivity.this, "Order cancelled by the customer.Can't reject the order", Toast.LENGTH_LONG).show();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    Toast.makeText(DemandViewActivity.this, "Enter reason for rejection", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        rejectionSheet.show();
                    }
                }
                isLoaded = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void changeToPaid() {
        new AlertDialog.Builder(DemandViewActivity.this)
                .setTitle("Amount Paid")
                .setMessage("Total amount for this order is paid?")
                .setPositiveButton("Paid", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Please Wait...");

                        firebaseDatabase.getReference().child("demands/" + key + "/paid").setValue("paid").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Not Paid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        orderDelivered(false); //Not Paid
                    }
                })
                .show();
    }

    private void orderDelivered(final Boolean isPaid) {
        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Please wait...");
        firebaseDatabase.getReference().child("demands/" + key + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue().toString().equalsIgnoreCase("cancelled")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("status", "Delivered");
                    data.put("deliveryTime", currTime);

                    if (isPaid) {
                        data.put("paid", "paid");
                    }

                    Map<String, Object> timeLineData = new HashMap<>();

                    timeLineData.put("status", "Delivered");
                    timeLineData.put("date", currTime);
                    timeline.add(timeLineData);
                    data.put("timeLine", timeline);

                    firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    statusArr.clear();
                    statusArr.add("Cancelled");
                    deliveryTextHeader.setText("Cancelled On");
                    Toast.makeText(DemandViewActivity.this, "Order cancelled by the customer", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void show_Datepicker() {
        Calendar c = Calendar.getInstance();
        int mYearParam = mYear;
        int mMonthParam = mMonth - 1;
        int mDayParam = mDay;

        DatePickerDialog datePickerDialog = new DatePickerDialog(DemandViewActivity.this,
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
        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                statusSpinner.setSelection(statusArr.indexOf((capitalize(status))));
            }
        });
    }

    private void show_Timepicker() {

        TimePickerDialog timePickerDialog = new TimePickerDialog(DemandViewActivity.this,
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
                        final String dateTime;
                        if (mMinute < 10) {
                            dateTime = mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":0" + mMinute + " " + AM_PM;
                        } else {
                            dateTime = mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + mMinute + " " + AM_PM;
                        }
                        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Accepting Order...");
                        firebaseDatabase.getReference().child("demands/" + key + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.getValue().toString().equalsIgnoreCase("cancelled")) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("status", "Accepted");
                                    data.put("deliveryTime", dateTime);

                                    Map<String, Object> timeLineData = new HashMap<>();

                                    timeLineData.put("status", "Accepted");
                                    timeLineData.put("date", currTime);
                                    timeline.add(timeLineData);
                                    data.put("timeLine", timeline);

                                    firebaseDatabase.getReference().child("demands/" + key).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    statusArr.clear();
                                    statusArr.add("Cancelled");
                                    deliveryTextHeader.setText("Cancelled On");
                                    Toast.makeText(DemandViewActivity.this, "Order cancelled by the customer.Can't accept the order", Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();

        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                statusSpinner.setSelection(statusArr.indexOf((capitalize(status))));
            }
        });
    }


    public void deleteForSupplier() {

        AlertDialog alertDialog = new AlertDialog.Builder(DemandViewActivity.this).setTitle("Reject Demand")
                .setMessage("Are you sure?").setPositiveButton("Reject Demand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = ProgressDialog.show(DemandViewActivity.this, null, "Rejecting Demand...");
                        firebaseDatabase.getReference().child("demands/" + key + "/status").setValue("Rejected").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                progressDialog.dismiss();
                                Intent intent = new Intent(DemandViewActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(DemandViewActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        statusSpinner.setSelection(statusArr.indexOf((capitalize(status))));
                    }
                }).show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                statusSpinner.setSelection(statusArr.indexOf((capitalize(status))));
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
        getMenuInflater().inflate(R.menu.demand_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.printBtn:
                printReceipt();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(DemandViewActivity.this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html>" +
                "<body>" +
                "<h2>" + firebaseUser.getDisplayName() +
                "</h2>\n" +
                "<p>Ph No:<b>" + phoneNumber +
                "</b></p>\n" +
                "<p>Delivery Address: <b>" + address +
                "</b></p>\n" +
                "<p>Order Id: <b>" + key +
                "</b></p>\n" +
                "<p>Ordered on: <b>" + createdOn +
                "</b></p>\n" +
                "<p>Printed On: <b>" + currTime +
                "</b></p>\n" +
                "<table style='border-collapse: collapse;width: 100%;'><thead>" +
                "<td style='border: 1px solid black;padding:5px' colspan='2'>Item</td>" +
                "<td style='border: 1px solid black;padding:5px'>Quantity</td>" +
                "<td style='border: 1px solid black;padding:5px'>Price</td>" +
                "</thead>";

        for (Map<String, Object> demand : demandList) {
            htmlDocument += "<tr>" +
                    " <td style='border: 1px solid black;padding:5px' colspan='2'>" + demand.get("name") + "</td>" +
                    "<td style='border: 1px solid black;padding:5px'>" + demand.get("quantity") + "</td>" +
                    "<td style='border: 1px solid black;padding:5px'>₹" + demand.get("price") + "</td>" +
                    "</tr>";
        }

        htmlDocument += "<tr>" +
                "<td style='border: 1px solid black;padding:5px' colspan='3'>" +
                "Total Price" +
                "</td>" +
                "<td  style='border: 1px solid black;padding:5px'>₹" + price + "</td>" +
                "</tr>" +
                "</table></body></html>";

        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) DemandViewActivity.this
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    public void printReceipt() {
        new AlertDialog.Builder(DemandViewActivity.this).setTitle("Print Receipt")
                .setMessage("Are you sure to print receipt for this order?")
                .setNegativeButton("Cancel", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doWebViewPrint();
            }
        }).show();
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
        public View getView(int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.demand_item_list, parent, false);
            TextView itemName = view.findViewById(R.id.itemName);
            TextView itemQuantity = view.findViewById(R.id.itemQuantity);
            ImageView itemImg = view.findViewById(R.id.itemImg);
            TextView countText = view.findViewById(R.id.itemSelectedCount);

            if (items.get(position).get("img") != null) {
                if (!items.get(position).get("img").equals("")) {
                    Picasso.with(DemandViewActivity.this).load(items.get(position).get("img").toString()).into(itemImg);
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
            countText.setText(items.get(position).get("count").toString());
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
