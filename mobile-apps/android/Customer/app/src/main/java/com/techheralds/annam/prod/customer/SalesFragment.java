package com.techheralds.annam.prod.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SalesFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    TextView emptyText;
    ListView listView;
    ArrayList<Map<String, Object>> sales;
    salesAdapterList salesAdapterList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_sales, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        emptyText = root.findViewById(R.id.emptyText);
        listView = root.findViewById(R.id.listView);
        sales = new ArrayList<>();

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Sales...");

        firebaseDatabase.getReference().child("sales/").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        for (DataSnapshot ds1 : ds.getChildren()) {

                            listView.setVisibility(View.VISIBLE);
                            Map<String, Object> sale = new HashMap<>();
                            sale.put("key", ds1.getKey());
                            sale.put("supplierUid", ds.getKey());
                            sale.putAll((Map<? extends String, ?>) ds1.getValue());

                            Date date1 = null;
                            Date date2 = null;

                            try {
                                //   date1 = new Date("dd/MM/yyyy, HH:mm a");
                                date2 = new SimpleDateFormat("dd/MM/yyyy, hh:mm a").parse(sale.get("endDate").toString());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Long t1 = Calendar.getInstance().getTimeInMillis();
                            Long t2 = date2.getTime();

                            // Log.d("myTag", "SALE NAME "+sale.get("name").toString());
                            //    Log.d("myTag", "CURR TIME & DATE " + Calendar.getInstance().getTime());
                            //   Log.d("myTag", "CURR MILLIS " + t1);
                            //   Log.d("myTag", "END TIME & DATE " + date2);
                            // Log.d("myTag", "END MILLIS " + t2);
                            // Log.d("myTag", "-------------------------------------------------------------------------------------");

                            if (sale.get("status").toString().equalsIgnoreCase("published")) {
                                if (!(boolean) sale.get("hide")) {
                                    if ((Long) sale.get("saleType") == 0) {
                                        if (t1 < t2) {
                                            sales.add(sale);
                                        }
                                    } else {
                                        if (((ArrayList<String>) sale.get("selectedCustomers")).contains(firebaseUser.getUid())) {
                                            Log.d("myTag",firebaseUser.getUid());
                                            if (t1 < t2) {
                                                sales.add(sale);
                                            }
                                        }
                                    }
                                }
                            }

                            salesAdapterList = new salesAdapterList(getContext(), sales);
                            listView.setAdapter(salesAdapterList);

                            progressDialog.dismiss();
                        }
                    }
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (sales.size() == 0) {
                    listView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                }
            }
        });

        return root;
    }

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public class salesAdapterList extends BaseAdapter {
        Context context;
        ArrayList<Map<String, Object>> sales;

        public salesAdapterList(Context context, ArrayList<Map<String, Object>> sales) {
            this.context = context;
            this.sales = sales;
        }

        @Override
        public int getCount() {
            return sales.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.sales_list, parent, false);

            if (sales.size() > 0) {
                TextView name, desc, sDate, eDate, status;
                Button inventroyBtn, salesBtn;
                name = view.findViewById(R.id.name);
                desc = view.findViewById(R.id.desc);
                sDate = view.findViewById(R.id.sDate);
                eDate = view.findViewById(R.id.eDate);


                name.setText(sales.get(position).get("name").toString());

                sDate.setText("Start at: " + sales.get(position).get("startDate").toString());
                eDate.setText("End at: " + sales.get(position).get("endDate").toString());

                if (sales.get(position).get("desc") != null) {
                    if (!sales.get(position).get("desc").toString().equals("")) {
                        desc.setVisibility(View.VISIBLE);
                        desc.setText(sales.get(position).get("desc").toString());
                    }
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), PlaceOrderActivity.class);
                        intent.putExtra("key", sales.get(position).get("key").toString());
                        intent.putExtra("title", sales.get(position).get("name").toString());
                        intent.putExtra("uid", sales.get(position).get("supplierUid").toString());
                        intent.putExtra("desc", sales.get(position).get("desc").toString());
                        intent.putExtra("startDate", sales.get(position).get("startDate").toString());
                        intent.putExtra("endDate", sales.get(position).get("endDate").toString());
                        intent.putExtra("status", sales.get(position).get("status").toString());
                        intent.putExtra("items", ((ArrayList<Map<String, Object>>) sales.get(position).get("items")));
                        intent.putExtra("bundles", ((ArrayList<Map<String, Object>>) sales.get(position).get("bundles")));
                        startActivity(intent);
                    }
                });
            }

            return view;
        }
    }
}