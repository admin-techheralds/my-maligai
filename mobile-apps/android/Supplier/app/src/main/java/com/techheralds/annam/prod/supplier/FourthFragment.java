package com.techheralds.annam.prod.supplier;

import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FourthFragment extends Fragment {
    String key, title;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    ArrayList<Map<String, Object>> reportArr, aQArr, bArr, customersOrders;
    ArrayList<String> outOfStockArr, orderedKeys, orderedItems;
    reportAdapterList adapterList;
    TableLayout tableLayout;
    ListView listView;
    TextView emptyTxt, tableTxt1, tableTxt2, tableTxt3;
    LinearLayout inventorySaleSummaryLayout, btnsLayout, orderDetailViewLayout;
    CardView inventorySaleSummaryButton, orderDetailViewButton;
    ImageButton backBtn1, backBtn2;
    FloatingActionButton nFab;
    int nCount = 0;
    Set<String> s;
    ArrayList<Map<String, Object>> saleItems;
    orderAdapterList orderAdapterList;
    orderAdapterList1 orderAdapterList1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fourth_fragment, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        saleItems = new ArrayList<>();
        outOfStockArr = new ArrayList<>();
        customersOrders = new ArrayList<>();
        orderedItems = new ArrayList<>();
        s = new LinkedHashSet<>();
        aQArr = new ArrayList<>();
        reportArr = new ArrayList<>();
        orderedKeys = new ArrayList<>();
        bArr = new ArrayList<>();
        key = getActivity().getIntent().getExtras().getString("key");
        title = getActivity().getIntent().getExtras().getString("title");
        listView = root.findViewById(R.id.listView1);
        tableLayout = root.findViewById(R.id.table);
        emptyTxt = root.findViewById(R.id.noDemandsText);
        tableTxt1 = root.findViewById(R.id.tableTxt1);
        tableTxt2 = root.findViewById(R.id.tableTxt2);
        tableTxt3 = root.findViewById(R.id.tableTxt3);
        inventorySaleSummaryLayout = root.findViewById(R.id.inventorySaleSummaryLayout);
        orderDetailViewLayout = root.findViewById(R.id.orderDetailViewLayout);
        inventorySaleSummaryButton = root.findViewById(R.id.inventorySaleSummaryBtn);
        orderDetailViewButton = root.findViewById(R.id.orderDetailViewBtn);
        backBtn1 = root.findViewById(R.id.backBtn1);
        backBtn2 = root.findViewById(R.id.backBtn2);
        btnsLayout = root.findViewById(R.id.btnsLayout);
        nFab = root.findViewById(R.id.noramlizeBtn);

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        int tWidth = width / 3;

        tableTxt1.setWidth(tWidth);
        tableTxt2.setWidth(tWidth);
        tableTxt3.setWidth(tWidth);

        inventorySaleSummaryButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                btnsLayout.setVisibility(View.GONE);
                inventorySaleSummaryLayout.setVisibility(View.VISIBLE);

            }
        });
        orderDetailViewButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                btnsLayout.setVisibility(View.GONE);
                orderDetailViewLayout.setVisibility(View.VISIBLE);

            }
        });
        backBtn1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                btnsLayout.setVisibility(View.VISIBLE);
                inventorySaleSummaryLayout.setVisibility(View.GONE);
            }
        });

        backBtn2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                btnsLayout.setVisibility(View.VISIBLE);
                orderDetailViewLayout.setVisibility(View.GONE);
            }
        });

        nFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext()).setTitle("Normalization")
                        .setMessage("Are you sure to apply normalization on " + outOfStockArr.toString().replace("[", "").replace("]", "")).
                        setNegativeButton("Cancel", null).setPositiveButton("Yes, normmalize", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nCount = orderedKeys.size() - 1;
                        final ProgressDialog p = ProgressDialog.show(getContext(), null, "Please wait...");

                        firebaseDatabase.getReference().child("demands/" + orderedKeys.get(nCount)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    demand demand = dataSnapshot.getValue(com.techheralds.annam.prod.supplier.demand.class);
                                    double price = demand.getPrice();
                                    ArrayList<Map<String, Object>> items = demand.getDemandList();

                                    for (Map<String, Object> item : items) {
                                        String sku = item.get("sku").toString().toLowerCase();

                                        if (sku.startsWith("bundle")) {
                                            ArrayList<Map<String, Object>> bundleItems = (ArrayList<Map<String, Object>>) item.get("items");

                                            for (Map<String, Object> bundleItem : bundleItems) {
                                                String name = bundleItem.get("name").toString();
                                                String actualQuantity = bundleItem.get("actualQuantity").toString();
                                                String minQuantity = bundleItem.get("minQuantity").toString();
                                                String bPrice = bundleItem.get("price").toString();

                                                if (outOfStockArr.contains(name)) {
                                                    if (getNumberFromString(actualQuantity) > getNumberFromString(minQuantity)) {
                                                        bundleItem.put("actualQuantity", minQuantity);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    firebaseDatabase.getReference().child("demands/" + orderedKeys.get(nCount) + "/demandList").setValue(items).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            p.dismiss();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }).show();
            }
        });

        firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        orderedKeys.add(ds.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadAll(false);

        loadEachCutomersOrders();

        return root;
    }

    public void loadEachCutomersOrders() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Orders.Please wait...");

        firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String orderKey = ds.getKey();
                        firebaseDatabase.getReference().child("demands/" + orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    final demand demand = dataSnapshot.getValue(demand.class);

                                    firebaseDatabase.getReference().child("customers/" + demand.getConsumer()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                            if (dataSnapshot1.getValue() != null) {
                                                Map<String, Object> customer = (Map<String, Object>) dataSnapshot1.getValue();
                                                String customerName = customer.get("name").toString();
                                                String uid = customer.get("uid").toString();

                                                ArrayList<Map<String, Object>> items = demand.getDemandList();

                                                for (Map<String, Object> item : items) {
                                                    int i = findIndexOf3(orderKey);
                                                    String sku = item.get("sku").toString();
                                                    String name = item.get("name").toString();
                                                    String quantity = item.get("quantity").toString().toLowerCase();
                                                    Long count = (Long) item.get("count");

                                                    if (!sku.toLowerCase().startsWith("bundle")) {

                                                        if (i > -1) {
                                                            if (customersOrders.get(i).get(name) != null) {
                                                                String qq = customersOrders.get(i).get(name).toString().toLowerCase();
                                                                if (qq.endsWith("kg")) {
                                                                    if (quantity.endsWith("kg")) {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + getTotalWithCount(qq, (long) 1) + "Kg");
                                                                    } else {
                                                                        customersOrders.get(i).put(name, (getTotalWithCount(quantity, count) / 1000) + getTotalWithCount(qq, (long) 1) + "Kg");
                                                                    }
                                                                } else if (qq.endsWith("l")) {
                                                                    if (quantity.endsWith("ml")) {
                                                                        customersOrders.get(i).put(name, (getTotalWithCount(quantity, count) / 1000) + getTotalWithCount(qq, (long) 1) + "L");
                                                                    } else {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + getTotalWithCount(qq, (long) 1) + "L");
                                                                    }
                                                                } else if (qq.endsWith("pcs")) {
                                                                    customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + getTotalWithCount(qq, (long) 1) + "Pcs");
                                                                }
                                                            } else {

                                                                if (quantity.endsWith("g")) {
                                                                    if (quantity.endsWith("kg")) {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + "Kg");
                                                                    } else {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) / 1000 + "Kg");
                                                                    }
                                                                } else if (quantity.endsWith("l")) {
                                                                    if (quantity.endsWith("ml")) {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) / 1000 + "L");
                                                                    } else {
                                                                        customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + "L");
                                                                    }
                                                                } else if (quantity.endsWith("pc") || quantity.endsWith("pcs")) {
                                                                    customersOrders.get(i).put(name, getTotalWithCount(quantity, count) + "Pcs");
                                                                }
                                                            }
                                                        } else {
                                                            Map<String, Object> data = new HashMap<>();

                                                            data.put("uid", uid);
                                                            data.put("name", customerName);
                                                            data.put("orderKey", orderKey);

                                                            if (quantity.endsWith("g")) {
                                                                if (quantity.endsWith("kg")) {
                                                                    data.put(name, getTotalWithCount(quantity, count) + "Kg");
                                                                } else {
                                                                    data.put(name, getTotalWithCount(quantity, count) / 1000 + "Kg");
                                                                }
                                                            } else if (quantity.endsWith("l")) {
                                                                if (quantity.endsWith("ml")) {
                                                                    data.put(name, getTotalWithCount(quantity, count) / 1000 + "L");
                                                                } else {
                                                                    data.put(name, getTotalWithCount(quantity, count) + "L");
                                                                }
                                                            } else if (quantity.endsWith("pc") || quantity.endsWith("pcs")) {
                                                                data.put(name, getTotalWithCount(quantity, count) + "Pcs");
                                                            }

                                                            customersOrders.add(data);
                                                        }
                                                    } else {
                                                        ArrayList<Map<String, Object>> bItems = (ArrayList<Map<String, Object>>) item.get("items");

                                                        for (Map<String, Object> bItem : bItems) {
                                                            int bI = findIndexOf3(orderKey);
                                                            String bSku = bItem.get("sku").toString();
                                                            String bName = bItem.get("name").toString();
                                                            String bq = bItem.get("actualQuantity").toString();
                                                            String bQuantity = null;
                                                            Long bCount = count;
                                                            Long bqType = ((Long) bItem.get("qType"));

                                                            if (bqType == 0) {
                                                                bQuantity = bq + "kg";
                                                            }
                                                            if (bqType == 1) {
                                                                bQuantity = bq + "g";
                                                            }
                                                            if (bqType == 2) {
                                                                bQuantity = bq + "L";
                                                            }
                                                            if (bqType == 3) {
                                                                bQuantity = bq + "ml";
                                                            }
                                                            if (bqType == 4) {
                                                                bQuantity = bq + "pc";
                                                            }

                                                            if (bI > -1) {
                                                                if (customersOrders.get(bI).get(bName) != null) {
                                                                    String qq = customersOrders.get(bI).get(bName).toString().toLowerCase();
                                                                    if (qq.endsWith("kg")) {
                                                                        if (bQuantity.endsWith("kg")) {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + getTotalWithCount(qq, (long) 1) + "Kg");
                                                                        } else {
                                                                            customersOrders.get(bI).put(bName, (getTotalWithCount(bQuantity, bCount) / 1000) + getTotalWithCount(qq, (long) 1) + "Kg");
                                                                        }
                                                                    } else if (qq.endsWith("l")) {
                                                                        if (bQuantity.endsWith("ml")) {
                                                                            customersOrders.get(bI).put(bName, (getTotalWithCount(bQuantity, bCount) / 1000) + getTotalWithCount(qq, (long) 1) + "L");
                                                                        } else {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + getTotalWithCount(qq, (long) 1) + "L");
                                                                        }
                                                                    } else if (qq.endsWith("pcs")) {
                                                                        customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + getTotalWithCount(qq, (long) 1) + "Pcs");
                                                                    }
                                                                } else {

                                                                    if (bQuantity.endsWith("g")) {
                                                                        if (bQuantity.endsWith("kg")) {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + "Kg");
                                                                        } else {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) / 1000 + "Kg");
                                                                        }
                                                                    } else if (bQuantity.endsWith("l")) {
                                                                        if (bQuantity.endsWith("ml")) {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) / 1000 + "L");
                                                                        } else {
                                                                            customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + "L");
                                                                        }
                                                                    } else if (bQuantity.endsWith("pc") || bQuantity.endsWith("pcs")) {
                                                                        customersOrders.get(bI).put(bName, getTotalWithCount(bQuantity, bCount) + "Pcs");
                                                                    }
                                                                }
                                                            } else {
                                                                Map<String, Object> data = new HashMap<>();

                                                                data.put("uid", uid);
                                                                data.put("name", customerName);
                                                                data.put("orderKey", orderKey);

                                                                if (bQuantity.endsWith("g")) {
                                                                    if (bQuantity.endsWith("kg")) {
                                                                        data.put(bName, getTotalWithCount(bQuantity, bCount) + "Kg");
                                                                    } else {
                                                                        data.put(bName, getTotalWithCount(bQuantity, bCount) / 1000 + "Kg");
                                                                    }
                                                                } else if (bQuantity.endsWith("l")) {
                                                                    if (bQuantity.endsWith("ml")) {
                                                                        data.put(bName, getTotalWithCount(bQuantity, bCount) / 1000 + "L");
                                                                    } else {
                                                                        data.put(bName, getTotalWithCount(bQuantity, bCount) + "L");
                                                                    }
                                                                } else if (bQuantity.endsWith("pc") || bQuantity.endsWith("pcs")) {
                                                                    data.put(bName, getTotalWithCount(bQuantity, bCount) + "Pcs");
                                                                }

                                                                customersOrders.add(data);
                                                            }
                                                        }
                                                    }
                                                }

                                                progressDialog.dismiss();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        /*
                         */
                    }

                    new android.os.Handler(Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                public void run() {
                                    orderedItems.add(0, "Cus. Name");
                                    s = new LinkedHashSet<>(orderedItems);
                                    formTableRow(s);
                                }
                            }, 3000);
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadAll(final Boolean fromNZTN) {

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Orders.Please wait...");

        //Get Ordered Quantity
        firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        firebaseDatabase.getReference().child("demands/" + ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                progressDialog.dismiss();

                                demand demandData = dataSnapshot1.getValue(demand.class);

                                if (demandData.getDemandList() != null) {

                                    for (Map<String, Object> item : demandData.getDemandList()) {
                                        String sku = item.get("sku").toString();
                                        String name = item.get("name").toString();
                                        String quantity = item.get("quantity").toString();
                                        Long count = (Long) item.get("count");

                                        //Individual Items
                                        if (!sku.startsWith("bundle")) {
                                            int index = findIndexOf(name);

                                            if (orderedItems.indexOf(name) == -1) {
                                                orderedItems.add(name);
                                            }
                                            //Item exists
                                            if (index > -1) {
                                                //item aready exits
                                                String reportQuantity = reportArr.get(index).get("quantity").toString();
                                                if (reportQuantity.endsWith("kg")) {
                                                    //its a kg value
                                                    if (quantity.endsWith("kg")) {
                                                        //kilogram

                                                        float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(quantity, count);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", total + "kg");

                                                        reportArr.set(index, data);
                                                    } else {
                                                        //gram

                                                        float total = getTotalWithCount(reportQuantity, (long) 1) + (getTotalWithCount(quantity, count) / 1000);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", total + "kg");

                                                        reportArr.set(index, data);
                                                    }
                                                } else if (reportQuantity.endsWith("L")) {
                                                    //its a Litre value
                                                    if (quantity.endsWith("ml")) {
                                                        //milli litre
                                                        float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(quantity, count) / 1000;
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", total + "L");
                                                        reportArr.set(index, data);
                                                    } else {
                                                        //Litre
                                                        float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(quantity, count);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", total + "L");
                                                        reportArr.set(index, data);
                                                    }
                                                } else {
                                                    float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(quantity, count);
                                                    //unit in count
                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("name", name);
                                                    data.put("quantity", total + "Pcs");
                                                    reportArr.set(index, data);
                                                }
                                            }
                                            //Item not exists
                                            else {
                                                //new item
                                                if (quantity.endsWith("g")) {
                                                    //gram/kilogram values
                                                    if (quantity.endsWith("kg")) {
                                                        //kilogram

                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", getTotalWithCount(quantity, count) + "kg");

                                                        reportArr.add(data);
                                                    } else {
                                                        //gram
                                                        String subsStringed = quantity.substring(0, quantity.length() - 1);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", getTotalWithCount(quantity, count) / 1000 + "kg");


                                                        reportArr.add(data);
                                                    }
                                                } else if (quantity.toLowerCase().endsWith("l")) {
                                                    //litre/milli litre values
                                                    if (quantity.endsWith("ml")) {
                                                        //milli litre
                                                        String subsStringed = quantity.substring(0, quantity.length() - 2);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", getTotalWithCount(quantity, count) + "L");
                                                        reportArr.add(data);
                                                    } else {
                                                        //Litre
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", name);
                                                        data.put("quantity", getTotalWithCount(quantity, count) + "L");
                                                        reportArr.add(data);
                                                    }
                                                } else {
                                                    //unit in count
                                                    Map<String, Object> data = new HashMap<>();
                                                    data.put("name", name);
                                                    data.put("quantity", getTotalWithCount(quantity, count) + "pcs");
                                                    reportArr.add(data);
                                                }
                                            }
                                        }
                                        //Bundles
                                        else {
                                            //Get Bundles
                                            ArrayList<Map<String, Object>> bundleItems = (ArrayList<Map<String, Object>>) item.get("items");
                                            for (Map<String, Object> bundleItem : bundleItems) {
                                                String bname = bundleItem.get("name").toString();
                                                String bq = bundleItem.get("actualQuantity").toString();
                                                String bquantity = null;
                                                Long bcount = count;
                                                Long bqType = ((Long) bundleItem.get("qType"));

                                                if (bqType == 0) {
                                                    bquantity = bq + "kg";
                                                }
                                                if (bqType == 1) {
                                                    bquantity = bq + "g";
                                                }
                                                if (bqType == 2) {
                                                    bquantity = bq + "L";
                                                }
                                                if (bqType == 3) {
                                                    bquantity = bq + "ml";
                                                }
                                                if (bqType == 4) {
                                                    bquantity = bq + "pc";
                                                }

                                                int index = findIndexOf(bname);

                                                if (orderedItems.indexOf(name) == -1) {
                                                    orderedItems.add(bname);
                                                }
                                                //Item exists
                                                if (index > -1) {
                                                    //item aready exits
                                                    String reportQuantity = reportArr.get(index).get("quantity").toString();
                                                    if (reportQuantity.endsWith("kg")) {
                                                        //its a kg value
                                                        if (bquantity.endsWith("kg")) {
                                                            //kilogram

                                                            float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(bquantity, bcount);
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", total + "kg");

                                                            reportArr.set(index, data);
                                                        } else {
                                                            //gram

                                                            float total = getTotalWithCount(reportQuantity, (long) 1) + (getTotalWithCount(bquantity, bcount) / 1000);
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", total + "kg");

                                                            reportArr.set(index, data);
                                                        }
                                                    } else if (reportQuantity.endsWith("L")) {
                                                        //its a Litre value
                                                        if (bquantity.endsWith("ml")) {
                                                            //milli litre
                                                            float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(bquantity, bcount) / 1000;
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", total + "L");
                                                            reportArr.set(index, data);
                                                        } else {
                                                            //Litre
                                                            float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(bquantity, bcount);
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", total + "L");
                                                            reportArr.set(index, data);
                                                        }
                                                    } else {
                                                        float total = getTotalWithCount(reportQuantity, (long) 1) + getTotalWithCount(bquantity, bcount);
                                                        //unit in count
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", bname);
                                                        data.put("quantity", total + "Pcs");
                                                        reportArr.set(index, data);
                                                    }
                                                }
                                                //Item not exists
                                                else {
                                                    //new item
                                                    if (bquantity.endsWith("g")) {
                                                        //gram/kilogram values
                                                        if (bquantity.endsWith("kg")) {
                                                            //kilogram

                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", getTotalWithCount(bquantity, bcount) + "kg");

                                                            reportArr.add(data);
                                                        } else {
                                                            //gram
                                                            String subsStringed = bquantity.substring(0, bquantity.length() - 1);
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", getTotalWithCount(bquantity, bcount) / 1000 + "kg");


                                                            reportArr.add(data);
                                                        }
                                                    } else if (bquantity.toLowerCase().endsWith("l")) {
                                                        //litre/milli litre values
                                                        if (bquantity.endsWith("ml")) {
                                                            //milli litre
                                                            String subsStringed = bquantity.substring(0, bquantity.length() - 2);
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", getTotalWithCount(bquantity, bcount) + "L");
                                                            reportArr.add(data);
                                                        } else {
                                                            //Litre
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", bname);
                                                            data.put("quantity", getTotalWithCount(bquantity, bcount) + "L");
                                                            reportArr.add(data);
                                                        }
                                                    } else {
                                                        //unit in count
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", bname);
                                                        data.put("quantity", getTotalWithCount(bquantity, bcount) + "pcs");
                                                        reportArr.add(data);
                                                    }
                                                }
                                            }

                                            adapterList = new reportAdapterList(getContext(), reportArr);
                                            listView.setAdapter(adapterList);


                                        }
                                        adapterList = new reportAdapterList(getContext(), reportArr);
                                        listView.setAdapter(adapterList);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    if (fromNZTN) {
                        new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                new Runnable() {
                                    public void run() {
                                        if (outOfStockArr.size() > 0) {
                                            if (nCount > -1) {

                                                final ProgressDialog p = ProgressDialog.show(getContext(), null, "Please wait...");
                                                firebaseDatabase.getReference().child("demands/" + orderedKeys.get(nCount)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.getKey() != null) {
                                                            Map<String, Object> order = (Map<String, Object>) dataSnapshot.getValue();

                                                            final ArrayList<Map<String, Object>> orderedItems = (ArrayList<Map<String, Object>>) order.get("demandList");
                                                            final int l = orderedItems.size();

                                                            for (int i = 0; i < orderedItems.size(); i++) {
                                                                String name = orderedItems.get(i).get("name").toString();
                                                                String quantity = orderedItems.get(i).get("quantity").toString().toLowerCase();

                                                                if (outOfStockArr.contains(name)) {
                                                                    if (quantity.endsWith("g") || quantity.endsWith("l")) {
                                                                        Float nQuantity = getTotalWithCount(quantity, (long) 1);
                                                                        if (nQuantity > 1) {
                                                                            //Like 250g, 500g, 100l etc.,
                                                                            if (nQuantity > 250) {
                                                                                orderedItems.get(i).put("quantity", "250" + (quantity.endsWith("g") ? "g" : "l"));
                                                                            } else {
                                                                                orderedItems.remove(i);
                                                                            }
                                                                        } else {
                                                                            orderedItems.get(i).put("quantity", "250" + (quantity.endsWith("g") ? "g" : "l"));
                                                                        }
                                                                    } else {
                                                                        //Pcs
                                                                    }
                                                                }
                                                            }

                                                            //   Toast.makeText(getContext(), "D " + orderedItems, Toast.LENGTH_SHORT).show();
                                                            firebaseDatabase.getReference().child("demands/" + orderedKeys.get(nCount) + "/demandList").setValue(orderedItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    if (l != orderedItems.size()) {
                                                                        //    Toast.makeText(getContext(), "D", Toast.LENGTH_SHORT).show();
                                                                        nCount--;
                                                                    }
                                                                    p.dismiss();
                                                                    outOfStockArr.clear();
                                                                    reportArr.clear();
                                                                    aQArr.clear();
                                                                    loadAll(true);
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                },
                                1000);
                    }
                } else {
                    progressDialog.dismiss();
                    btnsLayout.setVisibility(View.GONE);
                    emptyTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Get Available Quantity
        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ArrayList<Map<String, Object>> items = (ArrayList<Map<String, Object>>) dataSnapshot.getValue();

                    for (Map<String, Object> item : items) {
                        String name = item.get("name").toString();
                        String q = item.get("quantity").toString();
                        Long qType = ((Long) item.get("qType"));

                        Map<String, Object> d = new HashMap<>();
                        d.put("name", name);
                        if (qType == 0) {
                            d.put("aQuantity", q + " Kg");
                        }
                        if (qType == 1) {
                            d.put("aQuantity", q + " Kg");
                        }
                        if (qType == 2) {
                            d.put("aQuantity", q + " L");
                        }
                        if (qType == 3) {
                            d.put("aQuantity", q + " L");
                        }
                        if (qType == 4) {
                            d.put("aQuantity", q + " pc");
                        }

                        aQArr.add(d);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void formTableRow(Set<String> array) {

        TableLayout tl = (TableLayout) getActivity().findViewById(R.id.table2nd);

        TableRow tr = new TableRow(getContext());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));


        for (String s1 : array) {
            TextView textView = new TextView(getContext());
            //  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            //  params.setMargins(10,10,10,10);
            //    textView.setLayoutParams(params);
            textView.setPadding(10, 10, 10, 10);
            Typeface face = Typeface.createFromAsset(getActivity().getAssets(),
                    "NotoSans-Regular.ttf");
            textView.setTypeface(face);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setText(s1);
            tr.addView(textView);
        }

        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));


        for (final Map<String, Object> customersOrder : customersOrders) {
            TableRow tr1 = new TableRow(getContext());
            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            for (String s1 : array) {
                TextView textView1 = new TextView(getContext());
                //  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                //  params.setMargins(10,10,10,10);
                //    textView.setLayoutParams(params);
                textView1.setPadding(20, 20, 20, 20);
                Typeface face = Typeface.createFromAsset(getActivity().getAssets(),
                        "NotoSans-Regular.ttf");
                textView1.setTypeface(face);
                textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView1.setTextColor(Color.parseColor("#000000"));
                textView1.setTypeface(textView1.getTypeface(), Typeface.BOLD);

                if (customersOrder.get(s1) != null) {
                    textView1.setText(customersOrder.get(s1).toString());
                } else {
                    if (s1.toLowerCase().equalsIgnoreCase("cus. name")) {
                        textView1.setText(Html.fromHtml("<span>" + capitalize(customersOrder.get("name").toString()) + "<span><br><small><font color=#aaaaaa>" + customersOrder.get("orderKey") + "</font></small>"));
                    } else {
                        textView1.setText("---");
                    }
                }
                tr1.addView(textView1);
            }

            tr1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext()).setTitle("Order Options").setMessage("View or Edit Order \nCustomer Name: " + customersOrder.get("name") + "\nOrder Id: " + customersOrder.get("orderKey")).setNegativeButton("View Order", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            viewOrder(customersOrder.get("orderKey").toString());
                        }
                    }).setPositiveButton("Edit Order", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editOrder(customersOrder.get("orderKey").toString());
                        }
                    }).show();
                }
            });

            tl.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }
    }

    private void editOrder(final String orderKey) {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");

        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    saleItems = (ArrayList<Map<String, Object>>) dataSnapshot.getValue();

                    firebaseDatabase.getReference().child("demands/" + orderKey + "/demandList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            if (dataSnapshot1.getValue() != null) {
                                final ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) dataSnapshot1.getValue();

                                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                                bottomSheetDialog.setContentView(R.layout.edit_order_sheet);
                                bottomSheetDialog.setCancelable(false);
                                ImageButton backBtn, doneBtn;
                                LinearLayout linearLayout;
                                ListView listView = bottomSheetDialog.findViewById(R.id.listView);

                                backBtn = bottomSheetDialog.findViewById(R.id.backBtn);
                                doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);

                                backBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        bottomSheetDialog.dismiss();
                                    }
                                });

                                doneBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final ProgressDialog p = ProgressDialog.show(getContext(), null, "Please wait...");
                                        Float totalPrice = Float.valueOf(0);
                                        for (Map<String, Object> orderItem : orderItems) {
                                            totalPrice = totalPrice + Float.valueOf(orderItem.get("price").toString());
                                        }

                                        Map<String, Object> data = new HashMap<>();

                                        data.put("price", totalPrice);
                                        data.put("demandList", orderItems);

                                        firebaseDatabase.getReference().child("demands/" + orderKey).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                getActivity().finish();
                                                Intent intent = new Intent(getContext(), OrderMngActivity.class);
                                                intent.putExtra("key", key);
                                                intent.putExtra("title", title);
                                                startActivity(intent);
                                                bottomSheetDialog.dismiss();
                                                Toast.makeText(getContext(), "Order Items & Price Updated", Toast.LENGTH_SHORT).show();
                                                p.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                p.dismiss();
                                                Toast.makeText(getContext(), "Failed.Try again", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });

                                orderAdapterList = new orderAdapterList(getContext(), orderItems);
                                listView.setAdapter(orderAdapterList);

                                progressDialog.dismiss();
                                bottomSheetDialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void viewOrder(String orderKey) {
        final ProgressDialog progressDialog12 = ProgressDialog.show(getContext(), null, "Please Wait...");

        firebaseDatabase.getReference().child("demands/" + orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    final demand data = dataSnapshot.getValue(demand.class);

                    firebaseDatabase.getReference().child("customers/" + data.getConsumer()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            if (dataSnapshot1.getValue() != null) {
                                Map<String, Object> customerData = (Map<String, Object>) dataSnapshot1.getValue();
                                final StringBuilder stringBuilder = new StringBuilder();

                                for (Map<String, Object> item : data.getDemandList()) {
                                    String iname = item.get("name").toString();
                                    String quantity = item.get("quantity").toString();
                                    stringBuilder.append(iname + "(" + quantity + ") ");
                                }

                                Intent intent = new Intent(getActivity(), DemandViewActivity.class);
                                intent.putExtra("name", customerData.get("name").toString());
                                intent.putExtra("phoneNumber", customerData.get("phoneNumber").toString());
                                intent.putExtra("demandList", data.getDemandList());
                                intent.putExtra("timeline", data.getTimeLine());
                                intent.putExtra("dp", customerData.get("dp").toString());
                                intent.putExtra("supplier", data.getSupplier());
                                intent.putExtra("consumer", data.getConsumer());
                                intent.putExtra("status", data.getStatus());
                                intent.putExtra("deliveryTime", data.getDeliveryTime());
                                intent.putExtra("address", data.getAddress());
                                intent.putExtra("createdOn", data.getTimeCreated());
                                intent.putExtra("items", stringBuilder.toString());
                                intent.putExtra("key", data.getKey());
                                intent.putExtra("paid", data.getPaid());
                                intent.putExtra("payment_mode", data.getPayment_mode());
                                intent.putExtra("saleId", data.getSaleId());

                                if (data.getRejectionReason() != null) {
                                    intent.putExtra("rejectionReason", data.getRejectionReason());
                                } else {
                                    intent.putExtra("rejectionReason", "");
                                }
                                Bundle b = new Bundle();
                                b.putDouble("price", data.getPrice());
                                intent.putExtras(b);
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
    }

    public float getTotalWithCount(String quantity, Long count) {
        float f = Float.valueOf(quantity.replaceAll("[^\\d.]+|\\.(?!\\d)", ""));
        return f * count;
    }

    public float getNumberFromString(String quantity) {
        float f = Float.valueOf(quantity.replaceAll("[^\\d.]+|\\.(?!\\d)", ""));
        return f;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public int findIndexOf(String itemName) {
        int index = -1;
        for (int i = 0; i < reportArr.size(); i++) {
            if (reportArr.get(i).get("name").toString().toLowerCase().trim().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    public int findIndexOf1(String itemName) {
        int index = -1;
        for (int i = 0; i < aQArr.size(); i++) {
            if (aQArr.get(i).get("name").toString().toLowerCase().trim().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    public int findIndexOf2(String itemName) {
        int index = -1;
        for (int i = 0; i < outOfStockArr.size(); i++) {
            if (outOfStockArr.get(i).toLowerCase().trim().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    public int findIndexOf3(String orderKey) {
        int index = -1;
        for (int i = 0; i < customersOrders.size(); i++) {
            if (customersOrders.get(i).get("orderKey").toString().toLowerCase().trim().equals(orderKey.toLowerCase())) {
                index = i;
            }
        }

        return index;
    }

    public int findIndexOf4(String itemName) {
        int index = -1;
        for (int i = 0; i < saleItems.size(); i++) {
            if (saleItems.get(i).get("name").toString().toLowerCase().trim().equals(itemName.toLowerCase())) {
                index = i;
            }
        }

        return index;
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

    private String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public class reportAdapterList extends BaseAdapter {

        Context context;
        ArrayList<Map<String, Object>> reportsArr;

        public reportAdapterList(Context context, ArrayList<Map<String, Object>> reportsArr) {
            this.context = context;
            this.reportsArr = reportsArr;
        }

        @Override
        public int getCount() {
            return reportsArr.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.report_list, parent, false);
            if (reportsArr.size() > 0) {
                TextView itemName = view.findViewById(R.id.itemName);
                TextView itemQuantity = view.findViewById(R.id.itemQuantity);
                TextView aQuantity = view.findViewById(R.id.itemAvailQuantity);

                int width = Resources.getSystem().getDisplayMetrics().widthPixels;

                int tWidth = width / 3;

                itemName.setWidth(tWidth);
                itemQuantity.setWidth(tWidth);
                aQuantity.setWidth(tWidth);

                String name = reportsArr.get(position).get("name").toString();
                String quantity = reportsArr.get(position).get("quantity").toString();
                String aQ = null;

                itemName.setText(name);
                itemQuantity.setText(quantity);

                int i = findIndexOf1(name);
                if (i > -1) {
                    aQ = aQArr.get(i).get("aQuantity").toString();
                    aQuantity.setText(aQ);
                }

                if (getNumberFromString(aQ) < getNumberFromString(quantity)) {
                    itemName.setTextColor(Color.parseColor("#f04141"));
                    itemQuantity.setTextColor(Color.parseColor("#f04141"));
                    aQuantity.setTextColor(Color.parseColor("#f04141"));

                    if (findIndexOf2(name) == -1) {
                        outOfStockArr.add(name);
                    }
                }

            }
            return view;
        }
    }

    public class orderAdapterList extends BaseAdapter {

        Context context;
        ArrayList<Map<String, Object>> orderItems;

        public orderAdapterList(Context context, ArrayList<Map<String, Object>> orderItems) {
            this.context = context;
            this.orderItems = orderItems;
        }

        @Override
        public int getCount() {
            return orderItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.edit_order_item_input, parent, false);
            if (orderItems.size() > 0) {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;

                int width1 = (width / 100) * 50;
                int width2 = (width / 100) * 25;

                final EditText editText = view.findViewById(R.id.editText);
                TextInputLayout textInputLayout = view.findViewById(R.id.editTextLayout);
                EditText editText1 = view.findViewById(R.id.editText1);
                TextInputLayout textInputLayout1 = view.findViewById(R.id.editTextLayout1);
                EditText editText2 = view.findViewById(R.id.editText2);
                TextInputLayout textInputLayout2 = view.findViewById(R.id.editTextLayout2);

                editText.setWidth(width1);
                editText1.setWidth(width2);
                editText2.setWidth(width2);

                editText1.setFocusable(false);

                final String name = orderItems.get(position).get("name").toString();
                String quantity = orderItems.get(position).get("quantity").toString().toLowerCase();
                final String count = orderItems.get(position).get("count").toString();


                textInputLayout.setHint(name);

                String qq = null;

                if (quantity.endsWith("g")) {
                    if (quantity.endsWith("kg")) {
                        editText.setText(String.valueOf(getTotalWithCount(quantity, (long) 1)));
                        qq = "kg";
                        editText1.setText("kg");
                    } else {
                        editText.setText(String.valueOf(getTotalWithCount(quantity, (long) 1)));
                        qq = "g";
                        editText1.setText("g");
                    }
                } else if (quantity.endsWith("l")) {
                    if (quantity.endsWith("ml")) {
                        editText.setText(String.valueOf(getTotalWithCount(quantity, (long) 1)));
                        qq = "ml";
                        editText1.setText("ml");
                    } else {
                        editText.setText(String.valueOf(getTotalWithCount(quantity, (long) 1)));
                        qq = "L";
                        editText1.setText("L");
                    }
                } else if (quantity.endsWith("pc") || quantity.endsWith("pcs")) {

                    editText.setText(String.valueOf(getTotalWithCount(quantity, (long) 1)));

                    if (getTotalWithCount(quantity, (long) 1) > 1) {
                        qq = "pcs";
                        editText1.setText("pcs");
                    } else {
                        qq = "pc";
                        editText1.setText("pc");
                    }
                } else {
                    editText1.setText("Bundle");
                    editText.setText(quantity);
                }

                if (orderItems.get(position).get("count") != null) {
                    editText2.setText(count);
                } else {
                    textInputLayout2.setVisibility(View.GONE);
                }

                final String finalQq = qq;
                final String finalQq1 = qq;

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String q = charSequence.toString().trim();
                        if (!q.equalsIgnoreCase("")) {
                            if (!q.startsWith(".")) {
                                orderItems.get(position).put("quantity", q + finalQq);
                            } else {
                                q = "0.";
                                orderItems.get(position).put("quantity", "0.");
                            }

                            int sIndex = findIndexOf4(name);

                            if (sIndex > -1) {
                                Long qType = (Long) saleItems.get(sIndex).get("qType");
                                Float price = Float.valueOf(saleItems.get(sIndex).get("price").toString());
                                Float f = Float.valueOf(0);
                                if (qType == 0) {
                                    if (finalQq1.equalsIgnoreCase("g")) {
                                        f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) / 1000 * price;
                                    } else {
                                        f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) * price;
                                    }
                                } else if (qType == 1) {
                                    f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) / 100 * price;
                                } else if (qType == 2) {
                                    if (finalQq1.equalsIgnoreCase("ml")) {
                                        f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) / 1000 * price;
                                    } else {
                                        f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) * price;
                                    }
                                } else if (qType == 3) {
                                    f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) / 100 * price;
                                } else if (qType == 4) {
                                    f = getTotalWithCount(q, Long.valueOf(orderItems.get(position).get("count").toString())) * price;
                                }

                                orderItems.get(position).put("price", f);
                            }
                        } else {
                            orderItems.get(position).put("quantity", "0" + finalQq);
                            orderItems.get(position).put("price", 0.0);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                editText2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String q = charSequence.toString().trim();
                        if (!q.equalsIgnoreCase("")) {
                            orderItems.get(position).put("count", Integer.valueOf(q));

                            int sIndex = findIndexOf4(name);

                            if (sIndex > -1) {
                                Long qType = (Long) saleItems.get(sIndex).get("qType");
                                Float price = Float.valueOf(saleItems.get(sIndex).get("price").toString());
                                Float f = Float.valueOf(0);

                                if (qType == 0) {
                                    if (finalQq1.equalsIgnoreCase("g")) {
                                        f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) / 1000 * price;
                                    } else {
                                        f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) * price;
                                    }
                                } else if (qType == 1) {
                                    f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) / 100 * price;
                                } else if (qType == 2) {
                                    if (finalQq1.equalsIgnoreCase("ml")) {
                                        f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) / 1000 * price;
                                    } else {
                                        f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) * price;
                                    }
                                } else if (qType == 3) {
                                    f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) / 100 * price;
                                } else if (qType == 4) {
                                    f = getTotalWithCount(orderItems.get(position).get("quantity").toString(), Long.valueOf(q)) * price;
                                }

                                orderItems.get(position).put("price", f);
                            }

                            if (orderItems.get(position).get("sku").toString().startsWith("bundle")) {
                                Float total = Float.valueOf(0);
                                for (Map<String, Object> orderItem : (ArrayList<Map<String, Object>>) orderItems.get(position).get("items")) {

                                    Long qType1 = (Long) orderItem.get("qType");
                                    Float price1 = Float.valueOf(orderItem.get("price").toString());
                                    Float f1 = Float.valueOf(0);

                                    if (qType1 == 0) {
                                        f1 = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(q)) * price1);
                                    } else if (qType1 == 1) {
                                        f1 = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(q)) / 100 * price1);
                                    } else if (qType1 == 2) {
                                        f1 = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(q)) * price1);
                                    } else if (qType1 == 3) {
                                        f1 = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(q)) / 100 * price1);
                                    } else if (qType1 == 4) {
                                        f1 = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(q)) * price1);
                                    }

                                    total = total + f1;
                                    orderItems.get(position).put("price", total);

                                }

                            }

                        } else {
                            orderItems.get(position).put("count", 0);
                            orderItems.get(position).put("price", 0.0);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                if (orderItems.get(position).get("sku").toString().startsWith("bundle")) {
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (b) {
                                editText.clearFocus();

                                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                                bottomSheetDialog.setContentView(R.layout.edit_order_bundle_sheet);
                                bottomSheetDialog.setCancelable(false);
                                ImageButton backBtn, doneBtn;
                                LinearLayout linearLayout;
                                ListView listView = bottomSheetDialog.findViewById(R.id.listView);
                                TextView header = bottomSheetDialog.findViewById(R.id.header);

                                backBtn = bottomSheetDialog.findViewById(R.id.backBtn);
                                doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                                doneBtn.setVisibility(View.GONE);

                                header.setText(orderItems.get(position).get("name").toString());

                                backBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        bottomSheetDialog.dismiss();
                                    }
                                });

                                orderAdapterList1 = new orderAdapterList1(getContext(), (ArrayList<Map<String, Object>>) orderItems.get(position).get("items"));
                                listView.setAdapter(orderAdapterList1);

                                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        Float total = Float.valueOf(0);

                                        for (Map<String, Object> orderItem : (ArrayList<Map<String, Object>>) orderItems.get(position).get("items")) {

                                            Long qType = (Long) orderItem.get("qType");
                                            Float price = Float.valueOf(orderItem.get("price").toString());
                                            Float f = Float.valueOf(0);

                                            if (qType == 0) {
                                                f = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(count)) * price);
                                            } else if (qType == 1) {
                                                f = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(count)) / 100 * price);
                                            } else if (qType == 2) {
                                                f = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(count)) * price);
                                            } else if (qType == 3) {
                                                f = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(count)) / 100 * price);
                                            } else if (qType == 4) {
                                                f = (getTotalWithCount(orderItem.get("actualQuantity").toString(), Long.valueOf(count)) * price);
                                            }

                                            total = total + f;
                                            orderItems.get(position).put("price", total);

                                        }
                                    }
                                });

                                bottomSheetDialog.show();
                            }
                        }
                    });
                }
            }
            return view;
        }
    }

    public class orderAdapterList1 extends BaseAdapter {

        Context context;
        ArrayList<Map<String, Object>> orderItems;

        public orderAdapterList1(Context context, ArrayList<Map<String, Object>> orderItems) {
            this.context = context;
            this.orderItems = orderItems;
        }

        @Override
        public int getCount() {
            return orderItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.edit_order_item_input, parent, false);
            if (orderItems.size() > 0) {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;

                int width1 = (width / 100) * 75;
                int width2 = (width / 100) * 25;

                final EditText editText = view.findViewById(R.id.editText);
                TextInputLayout textInputLayout = view.findViewById(R.id.editTextLayout);

                EditText editText1 = view.findViewById(R.id.editText1);
                TextInputLayout textInputLayout1 = view.findViewById(R.id.editTextLayout1);
                EditText editText2 = view.findViewById(R.id.editText2);
                TextInputLayout textInputLayout2 = view.findViewById(R.id.editTextLayout2);

                editText.setWidth(width1);
                editText1.setWidth(width2);

                editText1.setFocusable(false);

                Long qType = Long.valueOf(orderItems.get(position).get("qType").toString());
                String qq = null;
                if (qType == 0) {
                    qq = "kg";
                } else if (qType == 1) {
                    qq = "g";
                } else if (qType == 2) {
                    qq = "L";
                } else if (qType == 3) {
                    qq = "ml";
                } else if (qType == 4) {
                    qq = "pc";
                }

                editText1.setText(qq);

                textInputLayout.setHint(orderItems.get(position).get("name").toString());
                editText.setText(orderItems.get(position).get("actualQuantity").toString());

                textInputLayout2.setVisibility(View.GONE);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String q = charSequence.toString().trim();
                        if (!q.equalsIgnoreCase("")) {
                            orderItems.get(position).put("actualQuantity", q);
                        } else {
                            orderItems.get(position).put("actualQuantity", "0");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
            return view;
        }
    }

}