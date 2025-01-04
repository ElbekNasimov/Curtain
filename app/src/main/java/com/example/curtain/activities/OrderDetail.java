package com.example.curtain.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.MainActivity;
import com.example.curtain.R;
import com.example.curtain.adapter.AdapterObjectProducts;
import com.example.curtain.adapter.AdapterOrderObject;
import com.example.curtain.adapter.AdapterOrderPay;
import com.example.curtain.adapter.AdapterProductOrder;
import com.example.curtain.constants.Constants;
import com.example.curtain.crud.EditOrder;
import com.example.curtain.model.ModelOrderObject;
import com.example.curtain.model.ModelOrderPays;
import com.example.curtain.model.ModelProduct;
import com.example.curtain.model.ModelProductOrder;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class OrderDetail extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private AdapterOrderPay adapterOrderPay;
    private AdapterOrderObject adapterOrderObject;
    private AdapterProductOrder adapterProductOrder;
    private ArrayList<ModelOrderPays> paysArrayList;
    private ArrayList<ModelProductOrder> productOrderArrayList;
    private ArrayList<ModelOrderObject> objectsArrayList;
    private ImageButton backBtn, delBtn, editBtn, orderPayBtn, editOrderPoshivIB, editOrderUstanovkaIB;
    private TextView orderNumberTV,orderTypeTV ,orderNameTV, orderPhoneTV, orderLocTV, orderSumTV, orderZakladTV, orderLoanTV,
            designerPercentTV, designerSumTV, designerPayStatusTV, payHistoryTV,orderObjectsTV,productOrdersTV,
            orderDescTV, orderCreateTV, orderPoshivPriceTV, orderUstanovkaPriceTV, orderStatusTV;
    private Button addObjToOrderBtn, addPrToOrderBtn, addExtraBtn, orderStatusBtn;
    private RecyclerView payHistoryRV, orderObjectsRV, productOrdersRV;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private String orderId, sharedUserType, sharedUsername, orderRoom, objRoom, addExtraTxt;
    private LinearLayout priceOrderDetailLL, buttonsLL, priceDesignerLL, addExtraPoshivLL, addExtraUstanovkaLL;
/* test */
    private AdapterObjectProducts adapterObjectProduct;
    private ArrayList<ModelProduct> productList;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        init();

        loadOrderDetail(orderId);

        loadOrderObjects(orderId);

        if (!sharedUserType.equals("sklad")) {
            loadOrderPays(orderId);
        }

        if (!sharedUserType.equals(Constants.userTypes[4])){
            orderPayBtn.setVisibility(View.GONE);
        }

        orderNumberTV.setOnClickListener(view -> Toast.makeText(OrderDetail.this, "clicked", Toast.LENGTH_SHORT).show());

        loadProductOrders(orderId);

        payHistoryRV.setVisibility(View.GONE);
        payHistoryTV.setOnClickListener(view -> {
            String open = getResources().getString(R.string.payHistoryOpen);
            String close = getResources().getString(R.string.payHistoryClose);
            if (payHistoryTV.getText().toString().trim().equals(open)){
                payHistoryRV.setVisibility(View.VISIBLE);
                payHistoryTV.setText(close);
            } else {
                payHistoryRV.setVisibility(View.GONE);
                payHistoryTV.setText(open);
            }
        });

        designerPayStatusTV.setOnClickListener(view -> {
            if (sharedUserType.equals(Constants.userTypes[4])){
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetail.this);
                builder.setTitle("Designer's salary").setMessage("Are you agree with it?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("orderDesignerSalary", "oldi");
                            DocumentReference docRef = firestore.collection("Orders").document(orderId);
                            docRef.update(hashMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Updated...", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, OrderDetail.class);
                                    intent.putExtra("orderId", orderId);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Updated Users False... " +
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
            }
        });

        addObjToOrderBtn.setVisibility(View.GONE);

        SharedPreferences sharedFromOrders = getSharedPreferences("FROM_ORDERS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedFromOrders.edit();
        editor.putString("from_orders", "from_orders");
        editor.apply();

        if (sharedUserType.equals("sklad")){
            priceOrderDetailLL.setVisibility(View.GONE);
            buttonsLL.setVisibility(View.GONE);
            priceDesignerLL.setVisibility(View.GONE);
            delBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            payHistoryTV.setVisibility(View.GONE);
        } else if (sharedUserType.equals("bichuvchi")){
            priceDesignerLL.setVisibility(View.GONE);
            payHistoryTV.setVisibility(View.GONE);
            delBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            buttonsLL.setVisibility(View.GONE);
        }

        backBtn.setOnClickListener(view -> startActivity(new Intent(OrderDetail.this, MainActivity.class)));

        editOrderPoshivIB.setOnClickListener(view -> {

        });

        editOrderUstanovkaIB.setOnClickListener(view -> {

        });

        addExtraBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_extra_to_order, null);
            alertDialog.setTitle("Poshiv qo'shish");

            Spinner addExtraSpinner = dialogView.findViewById(R.id.addExtraSpinner);
            EditText addExtraPriceET = dialogView.findViewById(R.id.addExtraPriceET);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, Constants.extraOrder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            addExtraSpinner.setAdapter(adapter);

            addExtraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {

                    if (!addExtraSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")){
                        addExtraTxt = addExtraSpinner.getSelectedItem().toString().trim();
                    } else {
                        TextView errTxt = (TextView) addExtraSpinner.getSelectedView();
                        errTxt.setError("");
                        errTxt.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Toast.makeText(OrderDetail.this, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        if (!TextUtils.isEmpty(addExtraPriceET.getText()) &&
                                !addExtraSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")
                        ) {
                            String addExtraPrice = addExtraPriceET.getText().toString().trim();
                            HashMap<String, Object> hashMap = new HashMap<>();

                            if (addExtraTxt.equals("Poshiv")){
                                hashMap.put("orderPoshiv", "" + addExtraPrice);
                            }
                            if (addExtraTxt.equals("Ustanovka")){
                                hashMap.put("orderUstanovka", "" + addExtraPrice);
                            }
                            firestore.collection("Orders").document(orderId).update(hashMap)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()){
                                            Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, OrderDetail.class);
                                            intent.putExtra("orderId", orderId);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Qo'shishda muammo " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();




        });


        delBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetail.this);
            builder.setTitle("Delete").setMessage(" Are you agree with it?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // delete
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
                        orderRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().exists()){
                                String deletedOrderId;
                                if (task.getResult().contains("orderId")){
                                    deletedOrderId = task.getResult().getString("orderId");
                                } else {
                                    deletedOrderId = null;
                                }

                                orderRef.delete().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        Toast.makeText(OrderDetail.this, "Smeta o'chirildi", Toast.LENGTH_SHORT).show();
                                        if (deletedOrderId!=null){
                                                deletePaysByOrder(deletedOrderId);
                                                deleteObjectsByOrder(deletedOrderId);
                                                deleteProductOrdersByOrder(deletedOrderId);
                                                deleteProductObjectByOrder(deletedOrderId);
                                            startActivity(new Intent(OrderDetail.this, MainActivity.class));
                                        }
                                    } else {
                                        Toast.makeText(OrderDetail.this, "Mahsulot o'chirishda xato", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(OrderDetail.this, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        editBtn.setOnClickListener(view -> {
            Intent intent = new Intent(OrderDetail.this, EditOrder.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        });

        orderPayBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderDetail.this);
            LayoutInflater inflater = LayoutInflater.from(OrderDetail.this.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_pay, null);
            alertDialog.setTitle("Pay");

            EditText dialPayET = dialogView.findViewById(R.id.dialPayET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        String timestamps = "" + System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("orderPayId", "" + timestamps);
                        hashMap.put("orderNumber", "" + orderId);
//                            hashMap.put("prId", "" + prId);
                        hashMap.put("orderPay", "" + dialPayET.getText().toString().trim());

                        Date prDate = new Date(Long.parseLong(timestamps));
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String created_at = sdfFormat.format(prDate);
                        hashMap.put("created_at", "" + created_at);
                        hashMap.put("created_by", sharedUsername);

                        if (!TextUtils.isEmpty(dialPayET.getText())) {
                            firestore.collection("OrderPays").document(timestamps).set(hashMap)
                                    .addOnCompleteListener(task -> {
//                                    progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    String amountPay = dialPayET.getText().toString().trim();
                                        changeOrderZaklad(orderId, amountPay);
                                    Toast.makeText(OrderDetail.this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(OrderDetail.this, "Qo'shishda muammo "
                                            + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(OrderDetail.this, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();

        });

        addPrToOrderBtn.setOnClickListener(view -> {
            bottomSheetDialog(orderId);
        });

        addObjToOrderBtn.setOnClickListener(view -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_obj_to_order, null);
            alertDialog.setTitle("Obyekt qo'shish");

            Spinner orderRoomSpinner = dialogView.findViewById(R.id.orderRoomSpinner);
            Spinner objRoomSpinner = dialogView.findViewById(R.id.objRoomSpinner);
            EditText objWidthET = dialogView.findViewById(R.id.objWidthET);
            EditText objHeightET = dialogView.findViewById(R.id.objHeightET);
            EditText objDescET = dialogView.findViewById(R.id.objDescET);

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, Constants.orderRooms);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            orderRoomSpinner.setAdapter(adapter1);

            orderRoomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {

                    if (!orderRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")){
                        orderRoom = orderRoomSpinner.getSelectedItem().toString().trim();
                    } else {
                        TextView errTxt = (TextView) orderRoomSpinner.getSelectedView();
                        errTxt.setError("");
                        errTxt.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Toast.makeText(OrderDetail.this, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
                }
            });

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, Constants.objRooms);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            objRoomSpinner.setAdapter(adapter);

            objRoomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {

                    if (!objRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")){
                        objRoom = objRoomSpinner.getSelectedItem().toString().trim();
                    } else {
                        TextView errTxt = (TextView) objRoomSpinner.getSelectedView();
                        errTxt.setError("");
                        errTxt.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Toast.makeText(OrderDetail.this, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
                }
            });


            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        String timestamps = "" + System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("orderObjectId", "" + timestamps);
                        hashMap.put("orderId", "" + orderId);
                        hashMap.put("orderRoom", "" + orderRoom);
                        hashMap.put("objWidthET", "" +objWidthET.getText().toString().trim());
                        hashMap.put("objRoom", "" + objRoom);
                        hashMap.put("objHeightET", "" +objHeightET.getText().toString().trim());
                        if (!objDescET.getText().toString().trim().isEmpty()) {
                            hashMap.put("objDescET", "" + objDescET.getText().toString().trim());
                        }
                        hashMap.put("created_by", firebaseAuth.getCurrentUser().getDisplayName());

                        if (!TextUtils.isEmpty(objWidthET.getText()) &&
                                !TextUtils.isEmpty(objHeightET.getText()) &&
                                !orderRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:") &&
                                !objRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")
                        ) {
                            firestore.collection("OrderObjects").document(timestamps).set(hashMap)
                                    .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                    loadOrderObjects(orderId);
                                } else {
                                    Toast.makeText(this, "Qo'shishda muammo " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

    }
    private void bottomSheetDialog(String orderId){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bs_pr_to_object, null);
        bottomSheetDialog.setContentView(view);

        TextInputEditText searchPrObjET = view.findViewById(R.id.searchPrObjET);
        TextView searchPrIdObjET = view.findViewById(R.id.searchPrIdObjET);
        TextInputEditText prObjLenET = view.findViewById(R.id.prObjLenET);
        RecyclerView prObjRV = view.findViewById(R.id.prObjRV);
        Button savePrObjBtn = view.findViewById(R.id.savePrObjBtn);

        CollectionReference collectionReference  = firestore.collection("Products");
        collectionReference.addSnapshotListener((value, error) -> {
            if (error!=null){
                Toast.makeText(this, "Mahsulotlar yuklashda xato", Toast.LENGTH_SHORT).show();
            }
            if (value != null && value.isEmpty()) {
                progressDialog.dismiss();
                prObjRV.setVisibility(View.GONE);
            }
            productList.clear();
            for (QueryDocumentSnapshot documentSnapshot:value){
                ModelProduct modelProduct = documentSnapshot.toObject(ModelProduct.class);
                productList.add(modelProduct);
            }
            adapterObjectProduct = new AdapterObjectProducts(this, productList, searchPrObjET,searchPrIdObjET);
            prObjRV.setLayoutManager(new LinearLayoutManager(this));
            prObjRV.setAdapter(adapterObjectProduct);
        });

        searchPrObjET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterObjectProduct.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        bottomSheetDialog.show();

        savePrObjBtn.setOnClickListener(view1 -> {
            String productObjectOrder =  searchPrObjET.getText().toString().trim();
            String lenProductObjectOrder = prObjLenET.getText().toString().trim();
            String productId = searchPrIdObjET.getText().toString().trim();

            Toast.makeText(this, "productId " + productId, Toast.LENGTH_SHORT).show();
            if (TextUtils.isEmpty(productObjectOrder)){
                Toast.makeText(this, "Pardani tanlang...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(lenProductObjectOrder)){
                Toast.makeText(this, "Uzunligini kiriting...", Toast.LENGTH_SHORT).show();
                return;
            }
            String timestamps = "" + System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
//            Document - ProductOrder, ProductObjectOrder
            hashMap.put("productObjectOrder", productObjectOrder);
            hashMap.put("lenProductObjectOrder", lenProductObjectOrder);
            hashMap.put("productObjectOrderId", timestamps);
            hashMap.put("orderId", orderId);
            hashMap.put("productId", productId);
            hashMap.put("partStatusProductOrder", "holat");
            hashMap.put("created_by", firebaseAuth.getCurrentUser().getDisplayName());
            firestore.collection("ProductsOrder").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OrderDetail.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Qo'shishda muammo " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void init(){
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);
        sharedUserType = sharedPreferences.getString("user_type", "");
        sharedUsername = sharedPreferences.getString("username", "");
        orderId = getIntent().getStringExtra("orderId");

        backBtn = findViewById(R.id.backBtn);
        delBtn = findViewById(R.id.delBtn);
        addExtraBtn = findViewById(R.id.addExtraBtn);
        orderStatusBtn = findViewById(R.id.orderStatusBtn);
        editBtn = findViewById(R.id.editBtn);
        orderPayBtn = findViewById(R.id.orderPayBtn);
        editOrderUstanovkaIB = findViewById(R.id.editOrderUstanovkaIB);
        editOrderPoshivIB = findViewById(R.id.editOrderPoshivIB);

        addObjToOrderBtn = findViewById(R.id.addObjToOrderBtn);
        addPrToOrderBtn = findViewById(R.id.addPrToOrderBtn);

        orderNumberTV = findViewById(R.id.orderNumberTV);
        orderTypeTV = findViewById(R.id.orderTypeTV);
        orderNameTV = findViewById(R.id.orderNameTV);
        orderPhoneTV = findViewById(R.id.orderPhoneTV);
        orderLocTV = findViewById(R.id.orderLocTV);
        orderSumTV = findViewById(R.id.orderSumTV);
        orderZakladTV = findViewById(R.id.orderZakladTV);
        orderLoanTV = findViewById(R.id.orderLoanTV);
        designerPercentTV = findViewById(R.id.designerPercentTV);
        designerSumTV = findViewById(R.id.designerSumTV);
        payHistoryTV = findViewById(R.id.payHistoryTV);
        orderObjectsTV = findViewById(R.id.orderObjectsTV);
        productOrdersTV = findViewById(R.id.productOrdersTV);
        designerPayStatusTV = findViewById(R.id.designerPayStatusTV);
        orderUstanovkaPriceTV = findViewById(R.id.orderUstanovkaPriceTV);
        orderPoshivPriceTV = findViewById(R.id.orderPoshivPriceTV);
        orderDescTV = findViewById(R.id.orderDescTV);
        orderCreateTV = findViewById(R.id.orderCreateTV);
        orderStatusTV = findViewById(R.id.orderStatusTV);

        priceOrderDetailLL = findViewById(R.id.priceOrderDetailLL);
        buttonsLL = findViewById(R.id.buttonsLL);
        priceDesignerLL = findViewById(R.id.priceDesignerLL);
        addExtraUstanovkaLL = findViewById(R.id.addExtraUstanovkaLL);
        addExtraPoshivLL = findViewById(R.id.addExtraPoshivLL);

        payHistoryRV = findViewById(R.id.payHistoryRV);
        orderObjectsRV = findViewById(R.id.orderObjectsRV);
        productOrdersRV = findViewById(R.id.productOrdersRV);
/* test */
        productList = new ArrayList<>();

        /* test */
        paysArrayList = new ArrayList<>();
        objectsArrayList = new ArrayList<>();
        productOrderArrayList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void loadOrderDetail(String orderId) {
        progressDialog.setMessage("Loading order Detail");
        progressDialog.show();
        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()){
                    orderNumberTV.setText(doc.getString("orderNumber"));
                    String orderCat = doc.getString("orderCat");
                    orderTypeTV.setText(orderCat);
                    String orderOwn = doc.getString("created_by");
                    String orderCreateDate = doc.getString("created_at");
                    orderCreateTV.setText(String.format("%s da %s qo'shdi", orderCreateDate, orderOwn));

                    if (orderCat != null && orderCat.equals("Parda")) {
                        addObjToOrderBtn.setVisibility(View.VISIBLE);
                        addPrToOrderBtn.setVisibility(View.GONE);
                        productOrdersTV.setVisibility(View.GONE);
                        addExtraBtn.setVisibility(View.GONE);
                    }
                    if (orderCat != null && !orderCat.equals("Parda")) {
                        orderObjectsTV.setVisibility(View.GONE);
                        orderObjectsRV.setVisibility(View.GONE);
                    }

                    orderNameTV.setText(doc.getString("orderName"));
                    if (doc.contains("orderPhone")) {
                        orderPhoneTV.setText(doc.getString("orderPhone"));
                        orderPhoneTV.setOnClickListener(view -> {
                            String number = orderPhoneTV.getText().toString();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:"+number));
                            startActivity(intent);
                        });
                    }

                    if (doc.contains("orderLoc")) {
                        orderLocTV.setText(doc.getString("orderLoc"));
                    }
                    if (doc.contains("orderStatus")) {
                        orderStatusTV.setText(doc.getString("orderStatus"));
                    }

                    if (doc.contains("orderDesignerSalary")) {
                        designerPayStatusTV.setText(String.format("Berildi"));
                        designerPayStatusTV.setTextColor(Color.BLUE);
                    }

                    if (doc.contains("orderStatus")) {
                        String status = doc.getString("orderStatus");
                        if (status.equals("Yangi")) {

                            orderStatusBtn.setOnClickListener(view -> {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("orderStatus", "Skladga");  // kesish holati
                                DocumentReference orderReference1 = firestore.collection("Orders").document(orderId);
                                orderReference1.update(hashMap).addOnSuccessListener(unused ->{
                                    Toast.makeText(this, "Skladga yuborildi", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, OrderDetail.class);
                                    intent.putExtra("orderId", orderId);
                                    startActivity(intent);
                                    finish();

                                }).addOnFailureListener(e -> Toast.makeText(this, "part not updated "
                                        + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });

                            if (!sharedUserType.equals(Constants.userTypes[4])
                                    && !firebaseAuth.getCurrentUser().getDisplayName().equals(orderOwn)) {
                                orderStatusBtn.setVisibility(View.GONE);
                            }
                            orderStatusBtn.setText("Skladga");
                        } else if (status.equals("Skladga")) {
                            orderStatusBtn.setText("Yuborilgan");
                            orderStatusBtn.setClickable(false);
                        } else {
                            if (!sharedUserType.equals(Constants.userTypes[4])) {
//                                delBtn.setEnabled(false);
                                delBtn.setClickable(false);
                            }
                            orderStatusBtn.setText(status);
                        }
                        if (status.equals("bichildi") && sharedUserType.equals(Constants.userTypes[4])){
                            orderStatusBtn.setClickable(false);
                            delBtn.setClickable(false);

                            orderStatusBtn.setOnClickListener(view -> {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("orderStatus", "Yopildi");  // kesish holati
                                DocumentReference orderReference1 = firestore.collection("Orders").document(orderId);
                                orderReference1.update(hashMap).addOnSuccessListener(unused ->{
                                    Toast.makeText(this, "Smeta yopildi", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, OrderDetail.class);
                                    intent.putExtra("orderId", orderId);
                                    startActivity(intent);
                                    finish();

                                }).addOnFailureListener(e -> Toast.makeText(this, "part not updated "
                                        + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                        }

                    } else {
                        orderStatusBtn.setText("Skladga");
                    }

                    if (doc.contains("orderUstanovka")) {
                        orderUstanovkaPriceTV.setText(doc.getString("orderUstanovka"));
                    } else {
                        addExtraUstanovkaLL.setVisibility(View.GONE);
                    }

                    if (doc.contains("orderPoshiv")) {
                        orderPoshivPriceTV.setText(doc.getString("orderPoshiv"));
                    } else {
                        addExtraPoshivLL.setVisibility(View.GONE);
                    }

                    String ordSum=null, ordZaklad=null, ordLoan;
                    String designerPer = doc.getString("orderPercent");

                    if (doc.contains("orderSum")) {  // Check if field exists
                        ordSum = doc.getString("orderSum");
                        if (ordSum.length()>0){
                            orderSumTV.setText(String.format("Zakaz - %s", ordSum));
                            float designerSum = (Float.parseFloat(ordSum) * Float.parseFloat(designerPer))/100;
                            designerSumTV.setText(String.format("%s $ bo'ladi", designerSum));
                        }
                    }

                    if (doc.contains("orderZaklad")) { // Check if field exists
                        ordZaklad = doc.getString("orderZaklad");
                        orderZakladTV.setText(String.format("Zaklad - %s", ordZaklad));
                    }

                    if (doc.contains("orderDesc")) { // Check if field exists
                        orderDescTV.setText(doc.getString("orderDesc"));
                    } else {
                        orderDescTV.setVisibility(View.GONE);
                    }
                    if (orderCat != null && !orderCat.equals("Parda")) {
                        orderObjectsTV.setVisibility(View.GONE);
                        orderObjectsRV.setVisibility(View.GONE);
                    }
                    String desPercent = doc.getString("orderPercent");
                    designerPercentTV.setText(desPercent + " % dan");

                    if (sharedUserType.equals(Constants.userTypes[4])){
                        designerPercentTV.setOnClickListener(view ->{
                                Toast.makeText(OrderDetail.this, "foiz o'zgartirish", Toast.LENGTH_SHORT).show();
                        });
                    }

                    if (ordSum!=null){
                        if (ordZaklad!=null) {
                            ordLoan = String.valueOf(Float.parseFloat(ordSum) - Float.parseFloat(ordZaklad));
                            orderLoanTV.setText(String.format("Qarz - %s", ordLoan));
                        } else{
                            orderLoanTV.setText(String.format("Qarz - %s", ordSum));
                        }
                    }
                } else {
                    Toast.makeText(OrderDetail.this, "Smeta topilmadimi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderDetail.this, "Error fetching Smeta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderPays(String orderId) {
        progressDialog.setMessage("load order pays");
        progressDialog.show();
        CollectionReference partsRef = firestore.collection("OrderPays");
        partsRef.whereEqualTo("orderNumber", orderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                paysArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelOrderPays modelOrderPays = snapshot.toObject(ModelOrderPays.class);
                    paysArrayList.add(modelOrderPays);
                }
                if (paysArrayList.isEmpty()){
                    payHistoryRV.setVisibility(View.GONE);
                }
                adapterOrderPay = new AdapterOrderPay(OrderDetail.this, paysArrayList);
                payHistoryRV.setAdapter(adapterOrderPay);
            } else {
                progressDialog.dismiss();
                Toast.makeText(OrderDetail.this, "qismlar yuklashda xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductOrders(String orderId) {
        progressDialog.setMessage("load product orders");
        progressDialog.show();
        CollectionReference productsOrderRef = firestore.collection("ProductsOrder");
        productsOrderRef.whereEqualTo("orderId", orderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                productOrderArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelProductOrder modelProductOrder = snapshot.toObject(ModelProductOrder.class);
                    productOrderArrayList.add(modelProductOrder);
                }
                if (productOrderArrayList.isEmpty()){
                    productOrdersRV.setVisibility(View.GONE);
                }
                adapterProductOrder = new AdapterProductOrder(OrderDetail.this, productOrderArrayList,
                        sharedPreferences);
                productOrdersRV.setAdapter(adapterProductOrder);
            } else {
                progressDialog.dismiss();
                Toast.makeText(OrderDetail.this, "mahsulotlar yuklashada xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderObjects(String orderId) {
        progressDialog.setMessage("Loading");
        progressDialog.show();
        CollectionReference objectsRef = firestore.collection("OrderObjects");
        objectsRef.whereEqualTo("orderId", orderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                objectsArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelOrderObject modelOrderObject = snapshot.toObject(ModelOrderObject.class);
                    objectsArrayList.add(modelOrderObject);
                }
                if (objectsArrayList.isEmpty()){
                    orderObjectsRV.setVisibility(View.GONE);
                }
                adapterOrderObject = new AdapterOrderObject(OrderDetail.this, objectsArrayList, sharedPreferences);
                orderObjectsRV.setAdapter(adapterOrderObject);
            } else {
                progressDialog.dismiss();
                Toast.makeText(OrderDetail.this, "qismlar yuklashda xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void changeOrderZaklad(String orderId, String amountPay) {
        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String ordZaklad = doc.getString("orderZaklad");
                    HashMap<String, Object> hashMap = new HashMap<>();
                    float newZaklad;
                    if (ordZaklad != null) {
                        newZaklad = Float.parseFloat(ordZaklad) + Float.parseFloat(amountPay);
                    } else {
                        newZaklad = Float.parseFloat(amountPay);
                    }
                    hashMap.put("orderZaklad", "" + newZaklad);

                    orderRef.update(hashMap).addOnSuccessListener(unused -> {
                        loadOrderDetail(orderId);
                        loadOrderPays(orderId);
                    }).addOnFailureListener(e -> Toast.makeText(OrderDetail.this,
                            "Not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                } else {
                    Toast.makeText(OrderDetail.this, "Smeta topilmadi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderDetail.this, "Error fetching Smeta: "
                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deletePaysByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("OrderPays");
        orderPaysRef.whereEqualTo("orderNumber", deletedOrderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                Toast.makeText(OrderDetail.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteObjectsByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("OrderObjects");
        orderPaysRef.whereEqualTo("orderId", deletedOrderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                Toast.makeText(OrderDetail.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteProductOrdersByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("ProductsOrder");
        orderPaysRef.whereEqualTo("orderNumber", deletedOrderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                Toast.makeText(OrderDetail.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteProductObjectByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("ProductObjectOrder");
        orderPaysRef.whereEqualTo("orderId", deletedOrderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                Toast.makeText(OrderDetail.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search){
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.settings){
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.about){
            Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}