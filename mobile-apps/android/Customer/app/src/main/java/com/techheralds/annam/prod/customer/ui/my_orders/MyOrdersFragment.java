package com.techheralds.annam.prod.customer.ui.my_orders;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.techheralds.annam.prod.customer.OrderViewActivity;
import com.techheralds.annam.prod.customer.R;
import com.techheralds.annam.prod.customer.demand;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyOrdersFragment extends Fragment {
    TextView noDemandsText;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    ArrayList<demand> demandsArr;
    demandsAdapterList adapterList;
    ListView listView;
    ArrayList<String> userNames;
    ArrayList<String> userPhoneNumbers;
    ArrayList<String> userDps;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_orders, container, false);
        noDemandsText = root.findViewById(R.id.noDemandsText);
        demandsArr = new ArrayList<>();

        userNames = new ArrayList<>();
        userPhoneNumbers = new ArrayList<>();
        userDps = new ArrayList<>();

        listView = root.findViewById(R.id.demandsList);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Orders.Please wait...");

        firebaseDatabase.getReference().child("demands").orderByChild("consumer").equalTo(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                progressDialog.dismiss();
                if (dataSnapshot.getChildrenCount() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        demand demandData = ds.getValue(demand.class);
                        userNames.add(null);
                        userPhoneNumbers.add(null);
                        userDps.add(null);
                        demandsArr.add(demandData);

                        adapterList = new demandsAdapterList(getContext(), demandsArr);
                        listView.setAdapter(adapterList);
                    }
                    Collections.reverse(demandsArr);
                } else {
                    noDemandsText.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return root;
    }

    public class demandsAdapterList extends BaseAdapter {
        Context context;
        ArrayList<demand> demands;

        public demandsAdapterList(Context context, ArrayList<demand> demands) {
            this.context = context;
            this.demands = demands;
        }

        @Override
        public int getCount() {
            return demands.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.demand_list, parent, false);

            if (demands.size() > 0) {
                TextView itemCount = view.findViewById(R.id.itemCount);
                TextView demandStatus = view.findViewById(R.id.demandStatus);
                TextView demandPlacedTime = view.findViewById(R.id.demandPlacedTime);
                final TextView name = view.findViewById(R.id.saleName);
                final CircleImageView dp = view.findViewById(R.id.userDp);
                final String[] userDp = {""};

                demandStatus.setText(capitalize(demands.get(position).getStatus()));
                demandPlacedTime.setText(demands.get(position).getTimeCreated());
                if (demands.get(position).getDemandList() != null) {
                    itemCount.setText(demands.get(position).getDemandList().size() > 1 ? demands.get(position).getDemandList().size() + " items" : demands.get(position).getDemandList().size() + " item");
                } else {
                    itemCount.setText("0 items");
                }
                if (userNames.get(position) == null) {
                    firebaseDatabase.getReference().child("sales/" + demands.get(position).getSupplier() + "/" + demands.get(position).getSaleId() + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userNames.set(position, dataSnapshot.getValue().toString());
                            name.setText(dataSnapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    name.setText(userNames.get(position));
                }


                final StringBuilder stringBuilder = new StringBuilder();


                if (demands.get(position).getDemandList() != null) {
                    for (Map<String, Object> item : demands.get(position).getDemandList()) {
                        String iname = item.get("name").toString();
                        String quantity = item.get("quantity").toString();
                        stringBuilder.append(iname + "(" + quantity + ") ");
                    }
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!name.getText().toString().equals("")) {
                            Intent intent = new Intent(getActivity(), OrderViewActivity.class);
                            intent.putExtra("name", name.getText().toString());
                            intent.putExtra("demandList", demands.get(position).getDemandList());
                            intent.putExtra("timeline", demands.get(position).getTimeLine());
                            intent.putExtra("dp", userDp[0]);
                            intent.putExtra("supplier", demands.get(position).getSupplier());
                            intent.putExtra("consumer", demands.get(position).getConsumer());
                            Bundle b = new Bundle();
                            b.putDouble("price", demands.get(position).getPrice());
                            intent.putExtras(b);
                            intent.putExtra("status", demands.get(position).getStatus());
                            intent.putExtra("address", demands.get(position).getAddress());
                            intent.putExtra("deliveryTime", demands.get(position).getDeliveryTime());
                            intent.putExtra("createdOn", demands.get(position).getTimeCreated());
                            intent.putExtra("items", stringBuilder.toString());
                            intent.putExtra("key", demands.get(position).getKey());
                            intent.putExtra("paid", demands.get(position).getPaid());
                            intent.putExtra("payment_mode", demands.get(position).getPayment_mode());
                            intent.putExtra("saleId",demands.get(position).getSaleId());

                            if (demands.get(position).getRejectionReason() != null) {
                                intent.putExtra("rejectionReason", demands.get(position).getRejectionReason());
                            } else {
                                intent.putExtra("rejectionReason", "");
                            }
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            return view;
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

}