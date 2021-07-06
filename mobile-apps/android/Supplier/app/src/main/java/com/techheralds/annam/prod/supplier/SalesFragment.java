package com.techheralds.annam.prod.supplier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SalesFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    TextView emptyText;
    ListView listView;
    ArrayList<Map<String, Object>> sales;
    salesAdapterList salesAdapterList;
    int mDay, mMonth, mYear, mHour, mMinute;
    Calendar c;
    BottomSheetDialog editBottomSheetDialog;
    EditText nameIp, descIp, startDateIp, endDateIp;
    ArrayList<Map<String, Object>> customers, tempArray;
    ArrayList<String> tempSelected;
    adapterList adapterList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_sales, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        editBottomSheetDialog = new BottomSheetDialog(getContext());

        editBottomSheetDialog.setContentView(R.layout.edit_sale_sheet);
        nameIp = editBottomSheetDialog.findViewById(R.id.saleNameIp);
        descIp = editBottomSheetDialog.findViewById(R.id.saleDescIp);
        startDateIp = editBottomSheetDialog.findViewById(R.id.startDateIp);
        endDateIp = editBottomSheetDialog.findViewById(R.id.endDateIp);

        customers = new ArrayList<>();
        tempSelected = new ArrayList<>();
        tempArray = new ArrayList<>();

        emptyText = root.findViewById(R.id.emptyText);
        listView = root.findViewById(R.id.listView);
        sales = new ArrayList<>();

        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Loading Sales...");

        firebaseDatabase.getReference().child("sales/" + getSupplierId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    listView.setVisibility(View.VISIBLE);

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Map<String, Object> sale = new HashMap<>();
                        sale.put("key", ds.getKey());
                        sale.putAll((Map<? extends String, ?>) ds.getValue());

                        sales.add(sale);

                        salesAdapterList = new salesAdapterList(getContext(), sales);
                        listView.setAdapter(salesAdapterList);

                        progressDialog.dismiss();
                    }
                } else {
                    progressDialog.dismiss();
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                final Button inventroyBtn, salesBtn;
                name = view.findViewById(R.id.name);
                desc = view.findViewById(R.id.desc);
                sDate = view.findViewById(R.id.sDate);
                eDate = view.findViewById(R.id.eDate);
                status = view.findViewById(R.id.status);

                inventroyBtn = view.findViewById(R.id.inventoryBtn);
                salesBtn = view.findViewById(R.id.salesBtn);

                name.setText(sales.get(position).get("name").toString());
                status.setText(capitalize(sales.get(position).get("status").toString()));

                if (sales.get(position).get("status").toString().equals("published")) {
                    status.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    status.setTextColor(Color.parseColor("#f04141"));
                }

                if (!sales.get(position).get("desc").toString().equals("")) {
                    //desc.setVisibility(View.VISIBLE);
                    //desc.setText(sales.get(position).get("desc").toString());
                }
                sDate.setText("Start at: " + sales.get(position).get("startDate").toString());
                eDate.setText("End at: " + sales.get(position).get("endDate").toString());

                //Get orders count

                firebaseDatabase.getReference().child("sales_orders/" + getSupplierId() + "/" + sales.get(position).get("key").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            salesBtn.setText("Orders (" + dataSnapshot.getChildrenCount() + ")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                salesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), OrderMngActivity.class);
                        intent.putExtra("key", sales.get(position).get("key").toString());
                        intent.putExtra("title", sales.get(position).get("name").toString());
                        intent.putExtra("desc", sales.get(position).get("desc").toString());
                        intent.putExtra("startDate", sales.get(position).get("startDate").toString());
                        intent.putExtra("endDate", sales.get(position).get("endDate").toString());
                        intent.putExtra("status", sales.get(position).get("status").toString());
                        startActivity(intent);
                    }
                });

                inventroyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), SalesInventory.class);
                        intent.putExtra("key", sales.get(position).get("key").toString());
                        intent.putExtra("title", sales.get(position).get("name").toString());
                        intent.putExtra("desc", sales.get(position).get("desc").toString());
                        intent.putExtra("startDate", sales.get(position).get("startDate").toString());
                        intent.putExtra("endDate", sales.get(position).get("endDate").toString());
                        intent.putExtra("status", sales.get(position).get("status").toString());
                        startActivity(intent);
                    }
                });
            }

            view.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    nameIp.setText(sales.get(position).get("name").toString());
                    descIp.setText(sales.get(position).get("desc").toString());
                    startDateIp.setText(sales.get(position).get("startDate").toString());
                    endDateIp.setText(sales.get(position).get("endDate").toString());


                    final Button saveBtn, cancelBtn, publishBtn;
                    final CheckBox hideSaleCb;
                    RadioGroup radioGroup;
                    final Button manageCustomersBtn = editBottomSheetDialog.findViewById(R.id.manageCustomers);

                    radioGroup = editBottomSheetDialog.findViewById(R.id.radioGroup);
                    saveBtn = editBottomSheetDialog.findViewById(R.id.saveBtn);
                    cancelBtn = editBottomSheetDialog.findViewById(R.id.cancelBtn);
                    publishBtn = editBottomSheetDialog.findViewById(R.id.publishBtn);
                    hideSaleCb = editBottomSheetDialog.findViewById(R.id.checkBox);
                    int saleType = 0;

                    if (sales.get(position).get("hide") != null) {
                        hideSaleCb.setChecked((Boolean) sales.get(position).get("hide"));
                    }

                    if (sales.get(position).get("saleType") != null) {
                        saleType = Math.toIntExact((Long) sales.get(position).get("saleType"));
                        ((RadioButton) radioGroup.getChildAt(Math.toIntExact((Long) sales.get(position).get("saleType")))).setChecked(true);

                        if ((Long) sales.get(position).get("saleType") == 1) {
                            manageCustomersBtn.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
                    }


                    firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + sales.get(position).get("key").toString() + "/selectedCustomers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                tempSelected = (ArrayList<String>) dataSnapshot.getValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    final Long[] finalSaleType = {Long.valueOf(saleType)};
                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            View radioButton = radioGroup.findViewById(i);
                            finalSaleType[0] = Long.valueOf(radioGroup.indexOfChild(radioButton));

                            if (finalSaleType[0] == 0) {
                                manageCustomersBtn.setVisibility(View.GONE);
                            } else {
                                manageCustomersBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    manageCustomersBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");

                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                            bottomSheetDialog.setContentView(R.layout.manage_customers_sheet);
                            bottomSheetDialog.setCancelable(false);

                            ImageButton backBtn = bottomSheetDialog.findViewById(R.id.backBtn);
                            ImageButton doneBtn = bottomSheetDialog.findViewById(R.id.doneBtn);
                            final ListView listView = bottomSheetDialog.findViewById(R.id.listView);

                            doneBtn.setVisibility(View.GONE);
                            backBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    bottomSheetDialog.dismiss();
                                }
                            });


                            SearchView searchView = bottomSheetDialog.findViewById(R.id.searchBar);

                            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                @Override
                                public boolean onQueryTextSubmit(String query) {
                                    return false;
                                }

                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public boolean onQueryTextChange(final String newText) {
                                    adapterList = new adapterList(getContext(), tempArray);
                                    listView.setAdapter(adapterList);
                                    adapterList.getFilter().filter(newText);

                                    return false;
                                }
                            });

                          /*  doneBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (tempSelected.size() > 0) {
                                        finalSelectedCustomers[0] = tempSelected;
                                        bottomSheetDialog.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Select atleast one customer", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });*/

                            firebaseDatabase.getReference().child("customers/").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        customers.clear();
                                        tempArray.clear();

                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                            Map<String, Object> customer = (Map<String, Object>) ds.getValue();
                                            customers.add(customer);
                                            tempArray.add(customer);
                                            adapterList = new adapterList(getContext(), customers);
                                            listView.setAdapter(adapterList);
                                            bottomSheetDialog.show();
                                            progressDialog.dismiss();
                                        }
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "No Customers", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                    int hWidth = width / 2;
                    int tWidth = width / 3;

                    if (sales.get(position).get("status").toString().equalsIgnoreCase("published")) {
                        publishBtn.setVisibility(View.GONE);
                        saveBtn.setWidth(hWidth);
                        cancelBtn.setWidth(hWidth);
                    } else {
                        publishBtn.setText("Publish");
                        saveBtn.setWidth(tWidth);
                        cancelBtn.setWidth(tWidth);
                        publishBtn.setWidth(tWidth);
                    }

                    startDateIp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (b) {
                                try {
                                    datePicker("start");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    endDateIp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (b) {
                                try {
                                    datePicker("end");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    saveBtn.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(View view) {
                            final String saleName = nameIp.getText().toString().trim();
                            final String saleDesc = descIp.getText().toString().trim();
                            final String startDate = startDateIp.getText().toString().trim();
                            final String endDate = endDateIp.getText().toString().trim();
                            Date date1 = null;
                            Date date2 = null;

                            try {
                                date1 = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(startDate);
                                date2 = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(endDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Long t1 = date1.getTime();
                            Long t2 = date2.getTime();
                            if (!saleName.equals("")) {
                                if (!startDate.equals("")) {
                                    if (!endDate.equals("")) {
                                        if (t1 < t2) {
                                            if (finalSaleType[0] == 1) {
                                                if (tempSelected.size() == 0) {
                                                    Toast.makeText(getContext(), "Select atleast one customer to show this sale", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }
                                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Updating Sale...");
                                            if (saleName.equals(sales.get(position).get("name").toString())) {
                                                final Map<String, Object> data = new HashMap<>();
                                                data.put("name", saleName);
                                                data.put("desc", saleDesc);
                                                data.put("startDate", startDate);
                                                data.put("endDate", endDate);
                                                data.put("hide", hideSaleCb.isChecked());
                                                data.put("status", sales.get(position).get("status").toString());
                                                data.put("saleType", finalSaleType[0]);
                                                data.put("selectedCustomers", finalSaleType[0] == 1 ? tempSelected : null);

                                                firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + sales.get(position).get("key").toString()).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        sales.get(position).putAll(data);
                                                        salesAdapterList.notifyDataSetChanged();
                                                        editBottomSheetDialog.dismiss();
                                                        progressDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {


                                                firebaseDatabase.getReference().child("sales/" + getSupplierId()).orderByChild("name").equalTo(saleName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.getValue() != null) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getContext(), "Sale Name already present", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            final Map<String, Object> data = new HashMap<>();
                                                            data.put("name", saleName);
                                                            data.put("desc", saleDesc);
                                                            data.put("startDate", startDate);
                                                            data.put("endDate", endDate);
                                                            data.put("hide", hideSaleCb.isChecked());
                                                            data.put("status", sales.get(position).get("status").toString());
                                                            data.put("saleType", finalSaleType[0]);
                                                            data.put("selectedCustomers", finalSaleType[0] == 1 ? tempSelected : null);

                                                            firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + sales.get(position).get("key").toString()).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    sales.get(position).putAll(data);
                                                                    salesAdapterList.notifyDataSetChanged();
                                                                    editBottomSheetDialog.dismiss();
                                                                    progressDialog.dismiss();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Check your Date & Time", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Select End Date", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Select Start Date", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Enter Sale Name", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    publishBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(getContext()).setTitle("Publish Sale").setMessage("Are you sure to publish this sale?")
                                    .setNegativeButton("No", null).setPositiveButton("Yes, Publish", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Date date = null;
                                    try {
                                        date = new SimpleDateFormat("dd/MM/yyyy, HH:mm a").parse(sales.get(position).get("startDate").toString());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (DateUtils.isToday(date.getTime())) {
                                        firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + sales.get(position).get("key").toString() + "/items").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() != null) {
                                                    final Map<String, Object> data = new HashMap<>();
                                                    if (sales.get(position).get("status").toString().equalsIgnoreCase("published")) {
                                                        data.put("status", "not published");
                                                    } else {
                                                        data.put("status", "published");
                                                    }
                                                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, "Please wait...");
                                                    firebaseDatabase.getReference().child("sales/" + getSupplierId() + "/" + sales.get(position).get("key").toString()).updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sales.get(position).put("status", data.get("status").toString());
                                                            progressDialog.dismiss();
                                                            editBottomSheetDialog.dismiss();
                                                            salesAdapterList.notifyDataSetChanged();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(context, "Add some items to the sale", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(context, "Sale Start Date is not Today's Date", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editBottomSheetDialog.dismiss();
                        }
                    });

                    editBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            publishBtn.setVisibility(View.VISIBLE);
                            tempSelected.clear();
                        }
                    });

                    editBottomSheetDialog.show();
                }
            });

            return view;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void datePicker(final String date) throws ParseException {
        c = Calendar.getInstance();
        int mYearParam = mYear;
        int mMonthParam = mMonth - 1;
        int mDayParam = mDay;

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mMonth = monthOfYear + 1;
                        mYear = year;
                        mDay = dayOfMonth;
                        show_Timepicker(date);

                    }
                }, mYearParam, mMonthParam, mDayParam);

        if (date.equalsIgnoreCase("start")) {
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        } else {
            String myDate = startDateIp.getText().toString().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm a");
            Date d = sdf.parse(myDate);
            long millis = d.getTime();

            datePickerDialog.getDatePicker().setMinDate(millis);
        }

        datePickerDialog.show();
    }

    private void show_Timepicker(final String date) {

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker view, int pHour,
                                          int pMinute) {

                        mHour = pHour;
                        if (pMinute < 10) {
                            mMinute = Integer.parseInt("0" + pMinute);
                        } else {
                            mMinute = pMinute;
                        }
                        String AM_PM;
                        if (mHour < 12) {
                            if (mHour == 0) {
                                mHour = 12;
                            }
                            AM_PM = "AM";
                        } else {
                            if ((mHour - 12) > 0) {
                                mHour = mHour - 12;
                            }
                            AM_PM = "PM";
                        }

                        if (date.equals("start")) {
                            startDateIp.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + (mMinute < 10 ? "0" : "") + mMinute + AM_PM);
                        } else {
                            endDateIp.setText(mDay + "/" + mMonth + "/" + mYear + ", " + mHour + ":" + (mMinute < 10 ? "0" : "") + mMinute + AM_PM);
                        }
                    }
                }, mHour, mMinute, false);

        timePickerDialog.show();
    }

    public class adapterList extends BaseAdapter implements Filterable {
        ArrayList<Map<String, Object>> customers;
        Context context;

        public adapterList(Context context, ArrayList<Map<String, Object>> customers) {
            this.customers = customers;
            this.context = context;
        }

        @Override
        public int getCount() {
            return customers.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view = LayoutInflater.from(context).inflate(R.layout.manage_customers_list, parent, false);
            CheckBox name = view.findViewById(R.id.name);
            CircleImageView avatar = view.findViewById(R.id.avatar);
            TextView number = view.findViewById(R.id.phoneNumber);

            if (customers.get(position).get("dp") != null) {
                if (!customers.get(position).get("dp").toString().equalsIgnoreCase("")) {
                    Picasso.with(getContext()).load(customers.get(position).get("dp").toString()).into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.nouser);
                }
            }

            name.setText(customers.get(position).get("name").toString());
            number.setText(customers.get(position).get("phoneNumber").toString());

            if (tempSelected.contains(customers.get(position).get("uid").toString())) {
                name.setChecked(true);
            } else {
                name.setChecked(false);
            }

            name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        tempSelected.add(customers.get(position).get("uid").toString());
                    } else {
                        tempSelected.remove(customers.get(position).get("uid").toString());
                    }
                }
            });

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
                        for (Map<String, Object> item : customers) {
                            String firstName = item.get("name").toString().toLowerCase(); // if we ignore case
                            String phoneNumber = item.get("phoneNumber").toString().toLowerCase();
                            //String lastName = item.get("pho").toLowerCase(); // if we ignore case
                            if (firstName.contains(constraint.toString()) || phoneNumber.contains(constraint.toString())) {
                                filteredList.add(item); // added item witch contains our text in EditText
                            }
                        }

                        results.count = filteredList.size(); // set count of filtered list
                        results.values = filteredList; // set filtered list
                    }
                    return results; // return our filtered list
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    customers = (ArrayList<Map<String, Object>>) results.values; // replace list to filtered list
                    notifyDataSetChanged(); // refresh adapter
                }
            };
            return filter;
        }
    }

}