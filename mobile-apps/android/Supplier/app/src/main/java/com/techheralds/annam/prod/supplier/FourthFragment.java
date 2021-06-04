package com.techheralds.annam.prod.supplier;

import androidx.annotation.RequiresApi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FourthFragment extends Fragment {
    String key;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    ArrayList<Map<String, Object>> reportArr, aQArr;
    reportAdapterList adapterList;
    TableLayout tableLayout;
    ListView listView;
    TextView emptyTxt, tableTxt1, tableTxt2, tableTxt3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fourth_fragment, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        aQArr = new ArrayList<>();
        reportArr = new ArrayList<>();
        key = getActivity().getIntent().getExtras().getString("key");
        listView = root.findViewById(R.id.listView);
        tableLayout = root.findViewById(R.id.table);
        emptyTxt = root.findViewById(R.id.noDemandsText);
        tableTxt1 = root.findViewById(R.id.tableTxt1);
        tableTxt2 = root.findViewById(R.id.tableTxt2);
        tableTxt3 = root.findViewById(R.id.tableTxt3);

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        int tWidth = width / 3;

        tableTxt1.setWidth(tWidth);
        tableTxt2.setWidth(tWidth);
        tableTxt3.setWidth(tWidth);

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Orders.Please wait...");

        //Get Ordered Quantity
        firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    tableLayout.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        firebaseDatabase.getReference().child("demands/" + ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                progressDialog.dismiss();

                                demand demandData = dataSnapshot1.getValue(demand.class);

                                for (Map<String, Object> item : demandData.getDemandList()) {
                                    String sku = item.get("sku").toString();
                                    String name = item.get("name").toString();
                                    String quantity = item.get("quantity").toString();
                                    Long count = (Long) item.get("count");

                                    //Individual Items
                                    if (!sku.startsWith("bundle")) {
                                        int index = findIndexOf(name);
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
                                            }
                                            else if (reportQuantity.endsWith("L")) {
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
                                            }
                                            else {
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

                                    }
                                    adapterList = new reportAdapterList(getContext(), reportArr);
                                    listView.setAdapter(adapterList);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    progressDialog.dismiss();
                    emptyTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Get Available Quantity
        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();

                    ArrayList<Map<String, Object>> items = (ArrayList<Map<String, Object>>) data.get("items");

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
        Toast.makeText(getContext(), " d "+getTotalWithCount("0.5kg", (long) 1), Toast.LENGTH_SHORT).show();
        return root;
    }

    public float getTotalWithCount(String quantity, Long count) {
        return Integer.valueOf(quantity.replaceAll("[^0-9]", "")) * count;
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

                itemName.setText(reportsArr.get(position).get("name").toString());
                itemQuantity.setText(reportsArr.get(position).get("quantity").toString());

                int i = findIndexOf1(reportArr.get(position).get("name").toString());
                if (i > -1) {
                    aQuantity.setText(aQArr.get(i).get("aQuantity").toString());
                }
            }
            return view;
        }
    }

}