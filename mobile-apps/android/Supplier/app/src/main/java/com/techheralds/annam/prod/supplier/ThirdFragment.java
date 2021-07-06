package com.techheralds.annam.prod.supplier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Picasso;
import com.techheralds.annam.prod.supplier.ui.demands_list.DemandsListFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThirdFragment extends Fragment {
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
    String key;
    ImageButton refreshBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_third, container, false);
        noDemandsText = root.findViewById(R.id.emptyText);
        demandsArr = new ArrayList<>();

        listView = root.findViewById(R.id.listView);

        key = getActivity().getIntent().getExtras().getString("key");
        userNames = new ArrayList<>();
        userPhoneNumbers = new ArrayList<>();
        userDps = new ArrayList<>();


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Orders.Please wait...");

        firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        firebaseDatabase.getReference().child("demands/" + ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                progressDialog.dismiss();
                                checkArrSize();

                                listView.setVisibility(View.VISIBLE);
                                demand demandData = dataSnapshot1.getValue(demand.class);
                                //  if (!demandData.getStatus().equals("Rejected")) {
                                demandsArr.add(demandData);
                                userNames.add(null);
                                userPhoneNumbers.add(null);
                                userDps.add(null);
                                adapterList = new demandsAdapterList(getContext(), demandsArr);
                                listView.setAdapter(adapterList);
                                //   }

                                Collections.reverse(demandsArr);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    progressDialog.dismiss();
                    noDemandsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return root;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            // Refresh tab data:

       /*     if(getFragmentManager() != null) {

                getFragmentManager()
                        .beginTransaction()
                        .detach(this)
                        .attach(this)
                        .commit();
            }*/
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    public void checkArrSize() {
        final int minutes = 1000;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (demandsArr.size() == 0) {
                        noDemandsText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    handler.postDelayed(this, minutes);
                }
            }
        }, minutes);
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
                TextView userTypeText = view.findViewById(R.id.userTypeText);
                TextView itemCount = view.findViewById(R.id.itemCount);
                TextView demandStatus = view.findViewById(R.id.demandStatus);
                TextView demandPlacedTime = view.findViewById(R.id.demandPlacedTime);
                final TextView name = view.findViewById(R.id.userName);
                final TextView phoneNumber = view.findViewById(R.id.userPhoneNumber);
                final CircleImageView dp = view.findViewById(R.id.userDp);
                final String[] userDp = {""};

                userTypeText.setText("Consumer");
                demandStatus.setText(capitalize(demands.get(position).getStatus()));
                demandPlacedTime.setText(demands.get(position).getTimeCreated());
                if (demands.get(position).getDemandList() != null) {
                    itemCount.setText(demands.get(position).getDemandList().size() > 1 ? demands.get(position).getDemandList().size() + " items" : demands.get(position).getDemandList().size() + " item");
                } else {
                    itemCount.setText("0 items");
                }
                if (userNames.get(position) == null) {
                    firebaseDatabase.getReference().child("customers/" + demands.get(position).getConsumer() + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userNames.set(position, dataSnapshot.getValue().toString());
                            name.setText(capitalize(dataSnapshot.getValue().toString()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    name.setText(userNames.get(position));
                }

                if (userPhoneNumbers.get(position) == null) {
                    firebaseDatabase.getReference().child("customers/" + demands.get(position).getConsumer() + "/phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            phoneNumber.setText(dataSnapshot.getValue().toString());
                            userPhoneNumbers.set(position, dataSnapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    phoneNumber.setText(userPhoneNumbers.get(position));
                }

                if (userDps.get(position) == null) {

                    firebaseDatabase.getReference().child("customers/" + demands.get(position).getConsumer() + "/dp").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userDp[0] = dataSnapshot.getValue().toString();
                            if (dataSnapshot.getValue().toString().equals("")) {
                                dp.setImageResource(R.drawable.nouser);
                                userDps.set(position, "");
                            } else {
                                userDps.set(position, dataSnapshot.getValue().toString());
                                Picasso.with(getContext()).load(dataSnapshot.getValue().toString()).into(dp);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    if (userDps.get(position).equals("")) {
                        dp.setImageResource(R.drawable.nouser);
                    } else {
                        Picasso.with(getContext()).load(userDps.get(position)).into(dp);
                    }
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
                        if (!name.getText().toString().equals("") && !phoneNumber.getText().toString().equals("")) {
                            Intent intent = new Intent(getActivity(), DemandViewActivity.class);
                            intent.putExtra("name", name.getText().toString());
                            intent.putExtra("phoneNumber", phoneNumber.getText().toString());
                            intent.putExtra("demandList", demands.get(position).getDemandList());
                            intent.putExtra("timeline", demands.get(position).getTimeLine());
                            intent.putExtra("dp", userDp[0]);
                            intent.putExtra("supplier", demands.get(position).getSupplier());
                            intent.putExtra("consumer", demands.get(position).getConsumer());
                            intent.putExtra("status", demands.get(position).getStatus());
                            intent.putExtra("deliveryTime", demands.get(position).getDeliveryTime());
                            intent.putExtra("address", demands.get(position).getAddress());
                            intent.putExtra("createdOn", demands.get(position).getTimeCreated());
                            intent.putExtra("items", stringBuilder.toString());
                            intent.putExtra("key", demands.get(position).getKey());
                            intent.putExtra("paid", demands.get(position).getPaid());
                            intent.putExtra("payment_mode", demands.get(position).getPayment_mode());
                            intent.putExtra("saleId", demands.get(position).getSaleId());

                            if (demands.get(position).getRejectionReason() != null) {
                                intent.putExtra("rejectionReason", demands.get(position).getRejectionReason());
                            } else {
                                intent.putExtra("rejectionReason", "");
                            }
                            Bundle b = new Bundle();
                            b.putDouble("price", demands.get(position).getPrice());
                            intent.putExtras(b);
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