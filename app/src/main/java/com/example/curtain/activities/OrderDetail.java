package com.example.curtain.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.curtain.databinding.ActivityOrderDetailBinding;
import com.example.curtain.model.ModelOrderObject;
import com.example.curtain.model.ModelOrderPays;
import com.example.curtain.model.ModelProduct;
import com.example.curtain.model.ModelProductObject;
import com.example.curtain.model.ModelProductOrder;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class OrderDetail extends AppCompatActivity {

    private ActivityOrderDetailBinding orderDetailBinding;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private AdapterOrderPay adapterOrderPay;
    private AdapterOrderObject adapterOrderObject;
    private AdapterProductOrder adapterProductOrder;
    private AdapterObjectProducts adapterObjectProduct;
    private ArrayList<ModelOrderPays> paysArrayList;
    private ArrayList<ModelProductOrder> productOrderArrayList;
    private ArrayList<ModelOrderObject> objectsArrayList;
    private ArrayList<ModelProductObject> productObjectsArrayList;
    private ArrayList<ModelProduct> productList;
    private ImageButton backBtn, delBtn, editBtn, orderPrintBtn, orderPayBtn, hidePayStatus,
            editOrderPoshivIB, editOrderUstanovkaIB;
    private TextView orderNumberTV,orderTypeTV ,orderNameTV, orderPhoneTV, orderLocTV, orderDeadlineTV, orderSumTV, orderZakladTV,
            orderLoanTV, designerPercentTV, designerSumTV, designerPayStatusTV, payHistoryTV, orderObjectsTV,
            productOrdersTV, orderDescTV, orderCreateTV, orderPoshivPriceTV, orderUstanovkaPriceTV, orderStatusTV,
            orderTotalTV, orderCostTV;
    private Button addObjToOrderBtn, addPrToOrderBtn, addExtraBtn, orderStatusBtn;
    private RecyclerView payHistoryRV, orderObjectsRV, productOrdersRV;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private String orderId, sharedUserType, sharedUsername, orderRoom, objRoom, addExtraTxt;
    private LinearLayout priceOrderDetailLL, buttonsLL, priceDesignerLL, addExtraPoshivLL, addExtraUstanovkaLL,
            idPoshAndUstLL;
    boolean click = false; //       To hide/open designer pay status
    Bitmap bitmap, scaledBitmap; // pdf rasm uchun
    private static final int OBJECTS_PER_PAGE = 2;     // Har bir sahifada nechta object joylashish
    private Context context;
    HashMap<String, Object> loadFromFirebaseMap;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

//        Button crashButton = new Button(this);
//        crashButton.setText("Test Crash");
//        crashButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                throw new RuntimeException("Test Crash"); // Force a crash
//            }
//        });
//
//        addContentView(crashButton, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));


        init();



        loadOrderDetail(orderId);

        loadOrderObjects(orderId);

        orderNumberTV.setOnClickListener(view -> Toast.makeText(OrderDetail.this,
                "clicked", Toast.LENGTH_SHORT).show());

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

        idPoshAndUstLL.setVisibility(View.GONE);
        orderCostTV.setVisibility(View.GONE);
        if (sharedUserType.equals("superAdmin")){
            idPoshAndUstLL.setVisibility(View.VISIBLE);

            // tannarx qo'shish: "Smeta tannarxi: 2800" kabi, chunki substring(16) qilingan
            orderCostTV.setVisibility(View.VISIBLE);
        }

        designerPercentTV.setVisibility(View.GONE);
        designerSumTV.setVisibility(View.GONE);
        hidePayStatus.setOnClickListener(view -> {
            if (!click) {
                designerPercentTV.setVisibility(View.VISIBLE);
                designerSumTV.setVisibility(View.VISIBLE);
                click = true;
            } else {
                designerPercentTV.setVisibility(View.GONE);
                designerSumTV.setVisibility(View.GONE);
                click = false;
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
            delBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            payHistoryTV.setVisibility(View.GONE);
            orderPrintBtn.setVisibility(View.GONE);
        } else if (sharedUserType.equals("bichuvchi")){
            payHistoryTV.setVisibility(View.GONE);
            delBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            buttonsLL.setVisibility(View.GONE);
            orderPrintBtn.setVisibility(View.GONE);
        }

        if (!sharedUserType.equals("sklad")) {
            loadOrderPays(orderId);
        }

        if (!sharedUserType.equals(Constants.userTypes[4])){
            orderPayBtn.setVisibility(View.GONE);
        }

        if (!sharedUserType.equals(Constants.userTypes[4])){
            priceDesignerLL.setVisibility(View.GONE);
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
                                !addExtraSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")) {
                            String addExtraPrice = addExtraPriceET.getText().toString().trim();
                            HashMap<String, Object> hashMap = new HashMap<>();

                            if (addExtraTxt.equals("Poshiv")){
                                hashMap.put("orderPoshiv", ""+addExtraPrice);
                            }
                            if (addExtraTxt.equals("Ustanovka")){
                                hashMap.put("orderUstanovka", ""+addExtraPrice);
                            }
                            firestore.collection("Orders").document(orderId).update(hashMap)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()){
                                            Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                            addPoshivToOrderTotal(orderId, addExtraPrice);
                                            Intent intent = new Intent(this, OrderDetail.class);
                                            intent.putExtra("orderId", orderId);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Qo'shishda muammo " +
                                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            builder.setTitle("O'chirish").setMessage("O'chirmoqchimisiz?")
                    .setPositiveButton("O'chirish", (dialog, which) -> {
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
                                        Toast.makeText(OrderDetail.this,
                                                "Smeta o'chirildi", Toast.LENGTH_SHORT).show();
                                        if (deletedOrderId!=null){
                                            deletePaysByOrder(deletedOrderId);  // to'lovlardan
                                            deleteProductOrdersByOrder(deletedOrderId);  // order objectlariproduct
                                            deleteObjectsByOrder(deletedOrderId);  // order objectlari
                                            deleteProductObjectByOrder(deletedOrderId); // product objectlari
                                            deleteCutPartProductObjectByOrder(deletedOrderId); // object kesilgan kusoklar
                                            deleteCutPartProductOrderByOrder(deletedOrderId);  // order kesilgan kusoklar

                                            Intent intent = new Intent(OrderDetail.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(OrderDetail.this,
                                                "Mahsulot o'chirishda xato", Toast.LENGTH_SHORT).show();
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
                        hashMap.put("orderPayId", ""+timestamps);
                        hashMap.put("orderNumber", ""+orderId);
                        hashMap.put("orderPay", ""+dialPayET.getText().toString().trim());

                        Date prDate = new Date(Long.parseLong(timestamps));
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String created_at = sdfFormat.format(prDate);
                        hashMap.put("created_at", ""+created_at);
                        hashMap.put("created_by", ""+sharedUsername);

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

        addPrToOrderBtn.setOnClickListener(view ->
                bottomSheetDialog(orderId)
        );

        addObjToOrderBtn.setOnClickListener(view -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_obj_to_order, null);
            alertDialog.setTitle("Xona qo'shish");

            Spinner orderRoomSpinner = dialogView.findViewById(R.id.orderRoomSpinner);
            Spinner objRoomSpinner = dialogView.findViewById(R.id.objRoomSpinner);
            EditText objDescET = dialogView.findViewById(R.id.objDescET);

            setupSpinner(orderRoomSpinner, Constants.orderRooms, "Xonani tanlang:", selected -> orderRoom = selected);
            setupSpinner(objRoomSpinner, Constants.objRooms, "Etajni tanlang:", selected -> objRoom = selected);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        String timestamps = "" + System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("orderObjectId", ""+timestamps);
                        hashMap.put("orderId", ""+orderId);
                        hashMap.put("orderRoom", ""+orderRoom);
                        hashMap.put("objRoom", ""+objRoom);

                        if (!objDescET.getText().toString().trim().isEmpty()) {
                            hashMap.put("objDescET", ""+objDescET.getText().toString().trim());
                        }
                        hashMap.put("created_by", ""+firebaseAuth.getCurrentUser().getDisplayName());

                        if (!orderRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase(Constants.orderRooms[0]) &&
                                !objRoomSpinner.getSelectedItem().toString().trim().equalsIgnoreCase(Constants.objRooms[0])) {
                            firestore.collection("OrderObjects").document(timestamps).set(hashMap)
                                    .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                    loadOrderObjects(orderId);
                                } else {
                                    Toast.makeText(this, "Qo'shishda muammo " +
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        orderPrintBtn.setOnClickListener(view -> {
            progressDialog.setMessage("Faylga saqlanmoqda");
            progressDialog.show();
            generatePdf();
        });
    }

    private void setupSpinner(Spinner spinner, String[] items, String defaultItem, Consumer<String> onItemSelected) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = spinner.getSelectedItem().toString().trim();
                if (!selectedItem.equalsIgnoreCase(defaultItem)) {
                    onItemSelected.accept(selectedItem);
                } else {
                    TextView errTxt = (TextView) spinner.getSelectedView();
                    errTxt.setError("");
                    errTxt.setTextColor(Color.RED);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(OrderDetail.this, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPoshivToOrderTotal(String orderId, String addExtraPrice) {
        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    float orderPoshivFloat;

                    if (!orderTotalTV.getText().toString().equals("0")) {
                        String[] parts = orderTotalTV.getText().toString().trim().split(" ");
                        orderPoshivFloat = Float.parseFloat(parts[1]);
                    } else {
                        orderPoshivFloat = Float.parseFloat(orderTotalTV.getText().toString());
                    }
                    float orderExtraPriceFloat = Float.parseFloat(addExtraPrice);
                    float orderTotal = orderPoshivFloat + orderExtraPriceFloat;
                    orderRef.update("orderTotal", ""+orderTotal);
                }
            } else {
                Toast.makeText(this, "Poshivni Orderga qo'shishda Xatolik", Toast.LENGTH_SHORT).show();
            }
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

        searchPrIdObjET.setVisibility(View.GONE);

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
            hashMap.put("productObjectOrder", ""+productObjectOrder);
            hashMap.put("lenProductObjectOrder", ""+lenProductObjectOrder);
            hashMap.put("productObjectOrderId", ""+timestamps);
            hashMap.put("orderId", ""+orderId);
            hashMap.put("productId", ""+productId);
            hashMap.put("partStatusProductOrder", "holat");
            hashMap.put("created_by", ""+firebaseAuth.getCurrentUser().getDisplayName());

            DocumentReference productRef = firestore.collection("Products").document(productId);
            productRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        hashMap.put("productPriceProductOrder", documentSnapshot.getString("prPrice"));
                        firestore.collection("ProductsOrder").document(timestamps).set(hashMap).
                                addOnCompleteListener(task1 -> {
                                    progressDialog.dismiss();
                                    if (task1.isSuccessful()){

                                        Toast.makeText(this, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, OrderDetail.class);
                                        intent.putExtra("orderId", orderId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Qo'shishda muammo " +
                                                task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(OrderDetail.this, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    private void init(){
        context = this;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);
        sharedUserType = sharedPreferences.getString("user_type", "");
        sharedUsername = sharedPreferences.getString("username", "");
        if (getIntent().hasExtra("orderId")){
            orderId = getIntent().getStringExtra("orderId");
        }
        backBtn = findViewById(R.id.backBtn);
        delBtn = findViewById(R.id.delBtn);
        addExtraBtn = findViewById(R.id.addExtraBtn);
        orderStatusBtn = findViewById(R.id.orderStatusBtn);
        editBtn = findViewById(R.id.editBtn);
        orderPrintBtn = findViewById(R.id.orderPrintBtn);
        orderPayBtn = findViewById(R.id.orderPayBtn);
        editOrderUstanovkaIB = findViewById(R.id.editOrderUstanovkaIB);
        editOrderPoshivIB = findViewById(R.id.editOrderPoshivIB);
        hidePayStatus = findViewById(R.id.hidePayStatus);

        addObjToOrderBtn = findViewById(R.id.addObjToOrderBtn);
        addPrToOrderBtn = findViewById(R.id.addPrToOrderBtn);

        orderNumberTV = findViewById(R.id.orderNumberTV);
        orderTypeTV = findViewById(R.id.orderTypeTV);
        orderNameTV = findViewById(R.id.orderNameTV);
        orderPhoneTV = findViewById(R.id.orderPhoneTV);
        orderLocTV = findViewById(R.id.orderLocTV);
        orderDeadlineTV = findViewById(R.id.orderDeadLineTV);
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
        orderTotalTV = findViewById(R.id.orderTotalTV);
        orderCostTV = findViewById(R.id.orderCostTV);

        priceOrderDetailLL = findViewById(R.id.priceOrderDetailLL);
        buttonsLL = findViewById(R.id.buttonsLL);
        priceDesignerLL = findViewById(R.id.priceDesignerLL);
        addExtraUstanovkaLL = findViewById(R.id.addExtraUstanovkaLL);
        addExtraPoshivLL = findViewById(R.id.addExtraPoshivLL);
        idPoshAndUstLL = findViewById(R.id.idPoshAndUstLL);

        payHistoryRV = findViewById(R.id.payHistoryRV);
        orderObjectsRV = findViewById(R.id.orderObjectsRV);
        productOrdersRV = findViewById(R.id.productOrdersRV);
/* test */
        productList = new ArrayList<>();

        /* test */
        paysArrayList = new ArrayList<>();
        objectsArrayList = new ArrayList<>();
        productOrderArrayList = new ArrayList<>();
        productList = new ArrayList<>();
        productObjectsArrayList = new ArrayList<>();

        loadFromFirebaseMap = new HashMap<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);
    }
    private void generatePdf() {

        if (orderTypeTV.getText().equals("Parda")) {
            fetchProductObjects(orderId);
        } else {
            goToPdf();
        }
    }
    private void fetchProductObjects(String orderId) {
        firestore.collection("ProductObjectOrder")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelProductObject productObject = document.toObject(ModelProductObject.class);
                            productObjectsArrayList.add(productObject);
                        }
                        fetchProducts();
                    }
                });
    }

    private void fetchProducts() {
        Set<String> productIds = new HashSet<>();
        for (ModelProductObject productObject : productObjectsArrayList) {
            productIds.add(productObject.getProductId());
        }

        // whereIn uchun productIds ro'yxatini 30 tadan iborat guruhlarga bo'lamiz
        List<List<String>> chunks = chunkList(new ArrayList<>(productIds), 30);
        if (!productIds.isEmpty()) {
            for (List<String> chunk:chunks){
                firestore.collection("Products").whereIn("prId", chunk).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ModelProduct product = document.toObject(ModelProduct.class);
                                    productList.add(product);
                                }
                                // Barcha so'rovlar tugagandan so'ng PDF yaratish
                                if (productList.size() == productIds.size()) {
                                    createPdf();
                                }
                            } else {
                                Toast.makeText(context, "Mahsulotlar yuklanmadi " +
                                        task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

    }

    // whereIn uchun Ro'yxatni berilgan o'lchamdagi guruhlarga bo'lish uchun yordamchi metod
    public static <T> List<List<T>> chunkList(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            int endIndex = Math.min(i + size, list.size());
            List<T> chunk = list.subList(i, endIndex);
            chunks.add(chunk);
        }
        return chunks;
    }

    private void createPdf() {
        progressDialog.dismiss();
        Toast.makeText(context, "create pdf", Toast.LENGTH_SHORT).show();
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint pageNumberPaint = new Paint();
        int pageNumber = 1;
        int yPosition;

        for (int i = 0; i < objectsArrayList.size(); i+=OBJECTS_PER_PAGE) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.
                    Builder(1240, 1754, pageNumber).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
            canvas.drawBitmap(scaledBitmap, 20, 20, paint);

            titlePaint.setTextSize(36f);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            float textSizeInPoints = 28f;

            paint.setTextSize(textSizeInPoints);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.BLACK);

            canvas.drawText(orderTypeTV.getText().toString() + " buyurtma # " +
                    orderNumberTV.getText().toString(), 450, 50, titlePaint);
            String orderOwn = orderCreateTV.getText().toString();
            String[] parts = orderOwn.split(" ");
            if (parts.length > 1) {
                canvas.drawText("Dizayner: " + parts[3], 170, 100, paint);
            }

            canvas.drawText("Mijoz: " + orderNameTV.getText().toString(), 500, 100, paint);
            if (orderPhoneTV.getText().toString().equals("Tel raqami")) {
                canvas.drawText("Tel raqami: ", 850, 100, paint);
                canvas.drawLine(1000, 100, 1200, 100, paint);
            } else {
                canvas.drawText("Tel raqami: " + orderPhoneTV.getText(), 850, 100, paint);
            }

            if (orderSumTV.getText().toString().equals("Smeta summasi")) {
                canvas.drawText("Zakaz: ", 170, 140, paint);
                canvas.drawLine(280, 140, 400, 140, paint);
            } else {
                canvas.drawText(orderSumTV.getText().toString(), 170, 140, paint);
            }

            if (orderZakladTV.getText().toString().equals("Zaklad")) {
                canvas.drawText("Zaklad: ", 500, 140, paint);
                canvas.drawLine(600, 140, 770, 140, paint);
            } else {
                canvas.drawText(orderZakladTV.getText().toString(), 500, 140, paint);
            }
            if (orderDeadlineTV.getText().toString().equals("Muddat")) {
                canvas.drawText("Topshiriladi: ", 850, 140, paint);
                canvas.drawLine(1000, 140, 1200, 140, paint);
            } else {
                canvas.drawText("Topshiriladi: " +
                        orderDeadlineTV.getText().toString(), 850, 140, paint);
            }
            if (orderDescTV.getText().toString().equals("Izoh")) {
                canvas.drawText("Izoh: ", 170, 180, paint);
                canvas.drawLine(280, 180, 800, 180, paint);
            } else {
                canvas.drawText("Izoh: " + orderDescTV.getText().toString(), 170, 180, paint);
            }
            canvas.drawText("Tasdiqlash: ", 850, 170, paint);
            canvas.drawLine(1000, 170, 1200, 170, paint);

            pageNumberPaint.setTextSize(20f);
            int pageNum = (int) Math.ceil(objectsArrayList.size()/2.0);
            canvas.drawText("Sahifa " + pageNum + " dan " + pageNumber, pageInfo.getPageWidth()-220,
                    pageInfo.getPageHeight()-20, pageNumberPaint);
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy");
            String formattedDate = dateFormat.format(today);
            canvas.drawText(formattedDate, 20,
                    pageInfo.getPageHeight()-20, pageNumberPaint);

            // objectlarni joylashtirish
            for (int j = 0; j < OBJECTS_PER_PAGE; j++){

                int index = i + j;

                if (index < objectsArrayList.size()){
                    ModelOrderObject orderObject = objectsArrayList.get(index);

                    yPosition = (j % 2 == 0) ? 290 : pageInfo.getPageHeight()/2+80;

                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawText("№", 30, yPosition-50, paint);
                    canvas.drawText("Mahsulot", 85, yPosition-50, paint);
                    canvas.drawText("Metr", 460, yPosition-50, paint);
                    canvas.drawText("Narx", 540, yPosition-50, paint);
                    canvas.drawText("Jami", 620, yPosition-50, paint);

                    titlePaint.setTextSize(28f);
                    if (orderObject.getObjDescET()==null) {
                        canvas.drawText(orderObject.getObjRoom() + " " + orderObject.getOrderRoom(), 720,
                                yPosition - 50, titlePaint);
                    } else {
                        canvas.drawText(orderObject.getObjRoom() + " " + orderObject.getOrderRoom()
                                + " " + orderObject.getObjDescET(), 720, yPosition - 50, paint);
                    }

                    canvas.drawLine(20, 200, 20, pageInfo.getPageHeight()-40, paint);
                    canvas.drawLine(75, 200, 75, pageInfo.getPageHeight()-40, paint);  // name
                    canvas.drawLine(450, 200, 450, pageInfo.getPageHeight()-40, paint);  // len
                    canvas.drawLine(530, 200, 530, pageInfo.getPageHeight()-40, paint);  // price
                    canvas.drawLine(610, 200, 610, pageInfo.getPageHeight()-40, paint);  // total
                    canvas.drawLine(710, 200, 710, pageInfo.getPageHeight()-40, paint);  // end total
                    canvas.drawLine(pageInfo.getPageWidth()-20, 200,
                            pageInfo.getPageWidth()-20, pageInfo.getPageHeight()-40, paint); // last

                    if (yPosition==290) {
                        canvas.drawLine(20, 200, pageInfo.getPageWidth()-20, 200, paint);
                        canvas.drawLine(20, 260, pageInfo.getPageWidth()-20, 260, paint);

                        for (int i1 = yPosition-30; i1 < pageInfo.getPageHeight()/2; i1+=38) {
                            canvas.drawLine(20, i1, 710, i1, paint);
                        }

                        paint.setTextSize(20f);
                        canvas.drawText("Eni: ", 720, yPosition, paint);
                        canvas.drawText("Bo'yi: ", 720, yPosition+28, paint);
                        canvas.drawText("Nisha: ", 720, yPosition+28*2, paint);

                        canvas.drawText("Eni: ", 950, yPosition, paint);
                        canvas.drawText("Bo'yi: ", 950, yPosition+28, paint);
                        canvas.drawText("Nisha: ", 950, yPosition+28*2, paint);

                        paint.setTextSize(25f);
                        int number = 1;
                        for (ModelProductObject productObject : productObjectsArrayList){
                            if (productObject.getObjectOrderId().equals(orderObject.getOrderObjectId())){
                                ModelProduct product = productList.stream().filter(p ->
                                        p.getPrId().equals(productObject.getProductId())).findFirst().orElse(null);
                                if (product!=null){
                                    if (productObject.getProductTypeProductOrder()!=null) {
                                        if (productObject.getProductTypeProductOrder().startsWith("T")) {
                                            canvas.drawText("Tyul:  " + productObject.getTitleProductObject(), 90, yPosition, paint);
                                        } else if (productObject.getProductTypeProductOrder().startsWith("P")) {
                                            canvas.drawText("Port:  " + productObject.getTitleProductObject(), 90, yPosition, paint);
                                        } else if (productObject.getProductTypeProductOrder().startsWith("O")) {
                                            canvas.drawText("Odno:  " + productObject.getTitleProductObject(), 90, yPosition, paint);
                                        } else {
                                            canvas.drawText(productObject.getTitleProductObject(), 90, yPosition, paint);
                                        }
                                    } else {
                                        canvas.drawText(productObject.getTitleProductObject(), 90, yPosition, paint);
                                    }
                                    canvas.drawText(productObject.getLenProductObject(), 465, yPosition, paint);
                                    if (productObject.getProductPriceProductOrder()!=null) {
                                        canvas.drawText(productObject.getProductPriceProductOrder(), 545, yPosition, paint);
                                    }
                                    float pr;
                                    pr = Float.parseFloat(productObject.getLenProductObject()) ;

                                    float len = Float.parseFloat(productObject.getProductPriceProductOrder()) ;
                                    canvas.drawText(String.valueOf(pr*len), 625, yPosition, paint);

                                    canvas.drawText(String.valueOf(number), 40, yPosition, paint);
                                    yPosition += 38;
                                    number++;
                                } else {
                                    Toast.makeText(context, "Berilgan Id product topilmadi", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        if (orderObject.getObjectUstanovka()!=null){
                            int spacing = 38;
                            int lastValue = (pageInfo.getPageHeight()/2 - spacing) / spacing * spacing+20-spacing;
                            canvas.drawText("Ustanovka", 90, lastValue, paint);
                            canvas.drawText(orderObject.getObjectUstanovka(), 625, lastValue, paint);
                        }

                        if (orderObject.getObjectPoshiv()!=null){
                            int spacing = 38;
                            int lastValue = (pageInfo.getPageHeight()/2-40 - spacing) / spacing * spacing+20-spacing;
                            canvas.drawText("Poshiv", 90, lastValue, paint);
                            canvas.drawText(orderObject.getObjectPoshiv(), 625, lastValue, paint);
                        }

                    } else if (yPosition==(pageInfo.getPageHeight() / 2+80)){
                        canvas.drawLine(20, yPosition-89, pageInfo.getPageWidth()-20,
                                yPosition-89, paint);
                        canvas.drawLine(20, yPosition-40, pageInfo.getPageWidth()-20,
                                yPosition-40, paint);

                        for (int i1 = yPosition-40; i1 < pageInfo.getPageHeight()-40; i1+=38) {
                            canvas.drawLine(20, i1, 710, i1, paint);
                        }

                        paint.setTextSize(20f);
                        canvas.drawText("Eni: ", 720, yPosition-10, paint);
                        canvas.drawText("Bo'yi: ", 720, yPosition-10+28, paint);
                        canvas.drawText("Nisha: ", 720, yPosition-10+28*2, paint);

                        canvas.drawText("Eni: ", 950, yPosition-10, paint);
                        canvas.drawText("Bo'yi: ", 950, yPosition-10+28, paint);
                        canvas.drawText("Nisha: ", 950, yPosition-10+28*2, paint);

                        paint.setTextSize(25f);
                        int number = 1;
                        for (ModelProductObject productObject : productObjectsArrayList){
                            if (productObject.getObjectOrderId().equals(orderObject.getOrderObjectId())){
                                ModelProduct product = productList.stream().filter(p ->
                                        p.getPrId().equals(productObject.getProductId())).findFirst().orElse(null);
                                if (product!=null){
                                    if (productObject.getProductTypeProductOrder().startsWith("T")) {
                                        canvas.drawText("Tyul:  " + productObject.getTitleProductObject(), 90, yPosition - 10, paint);
                                    } else if (productObject.getProductTypeProductOrder().startsWith("P")) {
                                        canvas.drawText("Port:  " + productObject.getTitleProductObject(), 90, yPosition - 10, paint);
                                    } else if (productObject.getProductTypeProductOrder().startsWith("O")) {
                                        canvas.drawText("Odno:  " + productObject.getTitleProductObject(), 90, yPosition - 10, paint);
                                    } else {
                                        canvas.drawText(productObject.getTitleProductObject(), 90, yPosition, paint);
                                    }

                                    canvas.drawText(productObject.getLenProductObject(), 465, yPosition - 10, paint);
                                    canvas.drawText(productObject.getProductPriceProductOrder(), 545, yPosition -10, paint);
                                    float len = Float.parseFloat(productObject.getLenProductObject()) ;
                                    float pr;
                                    if (productObject.getProductPriceProductOrder()!=null) {
                                        pr = Float.parseFloat(productObject.getProductPriceProductOrder());
                                    } else {
                                        pr = 0.0f;
                                    }
                                    pr = Float.parseFloat(productObject.getProductPriceProductOrder()) ;
                                    canvas.drawText(String.valueOf(pr*len), 625, yPosition - 10, paint);
                                    canvas.drawText(String.valueOf(number), 40, yPosition - 10, paint);
                                    yPosition += 38;
                                    number++;
                                } else {
                                    Toast.makeText(context, "Berilgan Id product topilmadi", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        if (orderObject.getObjectUstanovka()!=null){
                            int spacing = 38;
                            int lastValue = (pageInfo.getPageHeight()-40 - spacing) / spacing * spacing-5;
                            canvas.drawText("Ustanovka", 90, lastValue, paint);
                            canvas.drawText(orderObject.getObjectUstanovka(), 625, lastValue, paint);
                        }
                        if (orderObject.getObjectPoshiv()!=null){
                            int spacing = 38;
                            int lastValue = (pageInfo.getPageHeight()-80 - spacing) / spacing * spacing-4;
                            canvas.drawText("Poshiv", 90, lastValue, paint);
                            canvas.drawText(orderObject.getObjectPoshiv(), 625, lastValue, paint);
                        }
                    }
                    canvas.drawLine(20, pageInfo.getPageHeight()-40,
                            pageInfo.getPageWidth()-20, pageInfo.getPageHeight()-40, paint); // bottom line
                }
            }
            pdfDocument.finishPage(page);
            pageNumber++;
        }
        String fileName = orderNameTV.getText().toString()+"_"+orderTypeTV.getText().toString()+"_"
                +orderNumberTV.getText().toString();
        //        Pdf ga saqlash
        savePDF(pdfDocument, fileName);
    }
    private void goToPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint eniPaint = new Paint();

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo =new PdfDocument.PageInfo.
                Builder(1240, 1754,pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
        canvas.drawBitmap(scaledBitmap, 20, 20, paint);

        int startY = 290;
        int spacing = 40;
        int pageWidth = 1240;
        int pageHeight = 1754;

        titlePaint.setTextSize(36f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        float textSizeInPoints = 28f;

        paint.setTextSize(textSizeInPoints);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);

        canvas.drawText(orderTypeTV.getText().toString()+" buyurtma # " +
                orderNumberTV.getText().toString(), 450, 50, titlePaint);
        String orderOwn = orderCreateTV.getText().toString();
        String[] parts = orderOwn.split(" ");
        if (parts.length > 1){
            canvas.drawText("Dizayner: " + parts[3], 170,100, paint);
        }

        canvas.drawText("Mijoz: " + orderNameTV.getText().toString(), 500,100, paint);
        if (orderPhoneTV.getText().toString().equals("Tel raqami")) {
            canvas.drawText("Tel raqami: ", 850, 100, paint);
            canvas.drawLine(1000, 100, 1200, 100, paint);
        } else {
            canvas.drawText("Tel raqami: " + orderPhoneTV.getText(), 850, 100, paint);
        }

        if (orderSumTV.getText().toString().equals("Smeta summasi")){
            canvas.drawText("Zakaz: ", 170,140, paint);
            canvas.drawLine(280, 140, 400, 140, paint);
        } else {
            canvas.drawText( orderSumTV.getText().toString(), 170,140, paint);
        }

        if (orderZakladTV.getText().toString().equals("Zaklad")){
            canvas.drawText("Zaklad: ", 500, 140, paint);
            canvas.drawLine(600, 140, 770, 140, paint);
        } else {
            canvas.drawText(orderZakladTV.getText().toString(), 500, 140, paint);
        }
        if (orderDeadlineTV.getText().toString().equals("Muddat")){
            canvas.drawText("Topshiriladi: ", 850, 140, paint);
            canvas.drawLine(1000, 140, 1200, 140, paint);
        } else {
            canvas.drawText("Topshiriladi: " +
                    orderDeadlineTV.getText().toString(), 850, 140, paint);
        }

        if (orderDescTV.getText().toString().equals("Izoh")){
            canvas.drawText("Izoh: ", 170, 180, paint);
            canvas.drawLine(280, 180, 1200, 180, paint);
        } else {
            canvas.drawText("Izoh: " + orderDescTV.getText().toString(), 170, 180, paint);
        }
                    // ramka chizish
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(20, 200, pageWidth-20, 820, paint); // first
        canvas.drawLine(20, 260, pageWidth-20, 260, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("№", 30, 240, paint);
        canvas.drawText("Mahsulot", 85, 240, paint);
        canvas.drawText("Metr", 460, 240, paint);
        canvas.drawText("Narx", 540, 240, paint);
        canvas.drawText("Jami", 620, 240, paint);
        canvas.drawText("Ko'rinishi", 720, 240, paint);

        canvas.drawLine(75, 200, 75, 820, paint);  // name
        canvas.drawLine(450, 200, 450, 820, paint);  // len
        canvas.drawLine(530, 200, 530, 820, paint);  // price
        canvas.drawLine(610, 200, 610, 820, paint);  // total
        canvas.drawLine(705, 200, 705, 820, paint);  // end total
        canvas.drawLine(pageWidth-20, 200, pageWidth-20, 820, paint); // last

        paint.setTextSize(20f);
        canvas.drawText("Eni: ", 720, 290, paint);
        canvas.drawText("Bo'yi: ", 1000, 290, paint);

        paint.setTextSize(25f);

        for (int i1 = 260; i1 < pageHeight/2-40; i1+=spacing) {
            canvas.drawLine(20, i1, 705, i1, paint);
        }
        int number = 1;
        for (ModelProductOrder productOrder: productOrderArrayList){
            canvas.drawText( productOrder.getProductObjectOrder(), 90, startY, paint);
            canvas.drawText( productOrder.getLenProductObjectOrder(), 465, startY, paint);
            if (productOrder.getProductPriceProductOrder()!=null) {
                canvas.drawText(productOrder.getProductPriceProductOrder(), 545, startY, paint);
            }
            float price;
            if (productOrder.getProductPriceProductOrder()!=null) {
                price = Float.parseFloat(productOrder.getProductPriceProductOrder());
            } else {
                price = 0.0f;
            }
            float len = Float.parseFloat(productOrder.getLenProductObjectOrder());
            price = Float.parseFloat(productOrder.getProductPriceProductOrder());
            float sum = len * price;
            DecimalFormat df = new DecimalFormat("#.0");
            canvas.drawText(df.format(sum), 625, startY, paint);
            canvas.drawText(String.valueOf(number), 40, startY, paint);
            number ++;

            startY += spacing;
        }

        if (!orderPoshivPriceTV.getText().toString().isEmpty()){
            int lastValue = (pageHeight/2-40 - spacing) / spacing * spacing+10;
            canvas.drawText("Poshiv", 90, lastValue, paint);
            canvas.drawText(orderPoshivPriceTV.getText().toString(), 625, lastValue, paint);
        }


        pdfDocument.finishPage(page);
        String fileName = orderNameTV.getText().toString()+"_"+orderTypeTV.getText().toString()+"_"
                +orderNumberTV.getText().toString();
        //        Pdf ga saqlash
        savePDF(pdfDocument, fileName);
    }

    private void savePDF(PdfDocument pdfDocument, String fileName) {
        //        Sahifani tugatish
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName+".pdf");         // Fayl nomi
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,  "application/pdf");        // MIME turi
        // Fayl joyi
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Smetalar");

        // URI ni olish
        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

        // Faylga yozish
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)){
            if (outputStream != null){
                pdfDocument.writeTo(outputStream);                                    // PdfDocumentni faylga yozish
                progressDialog.dismiss();
                Toast.makeText(this, "PDF fayl saqlandi", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("PDF", "Error saving PDF: " + e.getMessage());
        }
        // sahifani yopish
        pdfDocument.close();
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
                        editOrderUstanovkaIB.setVisibility(View.GONE);
                        editOrderPoshivIB.setVisibility(View.GONE);
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
                    if (doc.contains("orderDeadline")) {
                        orderDeadlineTV.setText(doc.getString("orderDeadline"));
                    }
                    if (doc.contains("orderStatus")) {
                        orderStatusTV.setText(doc.getString("orderStatus"));
                    }
                    if (doc.contains("orderDesignerSalary")) {
                        designerPayStatusTV.setText("Berildi");
                        designerPayStatusTV.setTextColor(Color.BLUE);
                    }
                    if (doc.contains("orderTotal")) {
                        orderTotalTV.setText(String.format("Total: %s",doc.getString("orderTotal")));
                    }
                    else {
                        orderTotalTV.setText("0");
                        orderTotalTV.setVisibility(View.GONE);
                    }

                    if (doc.contains("orderStatus")) {
                        String status = doc.getString("orderStatus");
                        if (status.equals("Yangi")) {

                            orderStatusBtn.setOnClickListener(view -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetail.this);
                                builder.setTitle("Skladga").setMessage("Skladga yubormoqchimisiz?")
                                        .setPositiveButton("Yuborish", (dialog, which) -> {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("orderStatus", "Skladga");  // kesish holati
                                    DocumentReference orderReference1 = firestore.collection("Orders").document(orderId);
                                    orderReference1.update(hashMap).addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Skladga yuborildi", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, OrderDetail.class);
                                        intent.putExtra("orderId", orderId);
                                        startActivity(intent);
                                        finish();
                                    }).addOnFailureListener(e -> Toast.makeText(this, "part not updated "
                                            + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
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
                        // after "bichildi" change order status to "chiqdi"
                        if (status.equals("bichilmoqda") && sharedUserType.equals(Constants.userTypes[4])){
                            orderStatusBtn.setClickable(false);

                            orderStatusBtn.setOnClickListener(view -> {

                                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetail.this);
                                builder.setTitle("Sexdan chiqish").setMessage("Bichib bo'lindimi? Sexdan chiqdimi?")
                                        .setPositiveButton("Chiqarish", (dialog, which) -> {

                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("orderStatus", "Chiqdi");  // kesish holati

                                             DocumentReference orderReference1 = firestore.collection("Orders").document(orderId);
                                             orderReference1.update(hashMap).addOnSuccessListener(unused -> {

                                                 Toast.makeText(this, "Smeta sexdan chiqdi", Toast.LENGTH_SHORT).show();

                                                 addChiqqanOrderToOtchotlar();
                                                 collectSumChiqqanOrderToOtchotlar(orderSumTV.getText().toString(),
                                                         orderCostTV.getText().toString());

                                                 Intent intent = new Intent(this, OrderDetail.class);
                                                 intent.putExtra("orderId", orderId);
                                                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                 startActivity(intent);
                                                 finish();

                                             }).addOnFailureListener(e -> Toast.makeText(this, "part not updated "
                                                     + e.getMessage(), Toast.LENGTH_SHORT).show());

                                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
                            });
                        }
                        // Topshirilgan dan keyin Yopiladi
                        if (status.equals("Chiqdi") && sharedUserType.equals(Constants.userTypes[4])){
                            orderStatusBtn.setClickable(false);

                            orderStatusBtn.setOnClickListener(view -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetail.this);
                                builder.setTitle("Smetani yopish").setMessage("Smeta yopildimi?")
                                        .setPositiveButton("Yopish", (dialog, which) -> {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("orderStatus", "Yopildi");  // kesish holati
                                            DocumentReference orderReference1 = firestore.collection("Orders").document(orderId);
                                            orderReference1.update(hashMap).addOnSuccessListener(unused ->{
                                                Toast.makeText(this, "Smeta yopildi", Toast.LENGTH_SHORT).show();

                                                addYopilganOrderToOtchotlar();
                                                collectSumYopilganOrderToOtchotlar(orderSumTV.getText().toString(),
                                                        orderCostTV.getText().toString());

                                                Intent intent = new Intent(this, OrderDetail.class);
                                                intent.putExtra("orderId", orderId);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }).addOnFailureListener(e -> Toast.makeText(this, "part not updated "
                                                    + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
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
                            orderSumTV.setText(String.format("Zakaz: %s", ordSum));
                            float designerSum = (Float.parseFloat(ordSum) * Float.parseFloat(designerPer))/100;
                            designerSumTV.setText(String.format("%s $ bo'ladi", designerSum));
                        }
                    }

                    if (doc.contains("orderZaklad")) { // Check if field exists
                        ordZaklad = doc.getString("orderZaklad");
                        orderZakladTV.setText(String.format("Zaklad: %s", ordZaklad));
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
                            orderLoanTV.setText(String.format("Qarz: %s", ordLoan));
                        } else{
                            orderLoanTV.setText(String.format("Qarz: %s", ordSum));
                        }
                    }
                } else {
                    Toast.makeText(OrderDetail.this, "Smeta topilmadimi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderDetail.this, "Smeta yuklasjda xatolik: " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void collectSumYopilganOrderToOtchotlar(String ordSum, String orderCost) {
        // get today date
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(date);

        firestore.collection("Otchotlar").whereEqualTo("title", todayDate).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String otchotId = documentSnapshot.getId();



                        String sumYopilganOrder = documentSnapshot.getString("sumYopilganOrder") != null ?
                                documentSnapshot.getString("sumYopilganOrder") : "0";
                        String costYopilganOrder = documentSnapshot.getString("costYopilganOrder") != null ?
                                documentSnapshot.getString("costYopilganOrder") : "0";

                        HashMap<String, Object> changeOtchot = new HashMap<>();
                        changeOtchot.put("title", todayDate);
                        changeOtchot.put("sumYopilganOrder", String.valueOf(Integer.parseInt(sumYopilganOrder)
                                + Integer.parseInt(ordSum.substring(7))));
                        if (orderCost.length()>14) {
                            changeOtchot.put("costYopilganOrder", String.valueOf(Integer.parseInt(costYopilganOrder)
                                    + Integer.parseInt(orderCost.substring(16))));
                        }
                        firestore.collection("Otchotlar")
                                .document(otchotId)
                                .update(changeOtchot)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("sumYopilganOrder", "Sum And Cost Yopilgan" +
                                            " updated successfully");
                                }).addOnFailureListener(e ->
                                        Log.d("sumYopilganOrder", "Sum And Cost Yopilgan" +
                                                " updated unsuccessfully" + e.getMessage()));
                    } else {
                        HashMap<String, Object> newOtchot = new HashMap<>();
                        newOtchot.put("title", todayDate);
                        newOtchot.put("sumYopilganOrder", Integer.parseInt(ordSum.substring(7)));
                        if (orderCost.length()>14) {
                            newOtchot.put("costYopilganOrder", Integer.parseInt(orderCost.substring(16)));
                        }

                        firestore.collection("Otchotlar")
                                .add(newOtchot)
                                .addOnSuccessListener(documentReference -> Log.d("sumYopilganOrder",
                                        "Sum And Cost Yopilgan Order successfully"))
                                .addOnFailureListener(e -> Log.e("sumYopilganOrder",
                                        "Sum And Cost Yopilgan Unsuccessfully" + e.getMessage()));
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(OrderDetail.this, "error to get otchotlar " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void addYopilganOrderToOtchotlar() {
        // get today date
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(date);

        firestore.collection("Otchotlar").whereEqualTo("title", todayDate).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String otchotId = documentSnapshot.getId();
                        String currentCount = documentSnapshot.getString("countYopilganOrders") != null ?
                                documentSnapshot.getString("countYopilganOrders") : "0";

                        firestore.collection("Otchotlar")
                                .document(otchotId)
                                .update("countYopilganOrders", String.valueOf(Integer.parseInt(currentCount) + 1))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Count Yopilgan Otchotlar", "countYopilganOrders updated successfully");
                                }).addOnFailureListener(e ->
                                        Log.d("Count Yopilgan Otchotlar", "countYopilganOrders updated unsuccessfully"));
                    } else {
                        HashMap<String, Object> newOtchot = new HashMap<>();
                        newOtchot.put("title", todayDate);
                        newOtchot.put("countYopilganOrders", 1);

                        firestore.collection("Otchotlar")
                                .add(newOtchot)
                                .addOnSuccessListener(documentReference -> Log.d("Count Yopilgan Order", "Count Yopilgan otchot successfully"))
                                .addOnFailureListener(e -> Log.e("Count Yopilgan Order", "Error count closing otchot", e));
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(OrderDetail.this, "error to get otchotlar " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void collectSumChiqqanOrderToOtchotlar(String ordSum, String orderCost) {
        // get today date
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(date);

        firestore.collection("Otchotlar").whereEqualTo("title", todayDate).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String otchotId = documentSnapshot.getId();
                        String sumChiqqanOrder = documentSnapshot.getString("sumChiqqanOrder") != null ?
                                documentSnapshot.getString("sumChiqqanOrder") : "0";
                        String costChiqqanOrder = documentSnapshot.getString("costChiqqanOrder") != null ?
                                documentSnapshot.getString("costChiqqanOrder") : "0";

                        HashMap<String, Object> changeOtchot = new HashMap<>();
                        changeOtchot.put("title", todayDate);
                        changeOtchot.put("sumChiqqanOrder", String.valueOf(Integer.parseInt(sumChiqqanOrder)
                                + Integer.parseInt(ordSum.substring(7))));
                        if (orderCost.length()>14) {
                            changeOtchot.put("costChiqqanOrder", String.valueOf(Integer.parseInt(costChiqqanOrder)
                                    + Integer.parseInt(orderCost.substring(16))));
                        }
                        firestore.collection("Otchotlar")
                                .document(otchotId)
                                .update(changeOtchot)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("sumAndCostChiqqanOrders", "Sum And Cost Chiqqan" +
                                            " updated successfully");
                                }).addOnFailureListener(e ->
                                        Log.d("sumAndCostChiqqanOrders", "Sum And Cost Chiqqan" +
                                                " updated unsuccessfully" + e.getMessage()));
                    } else {
                        HashMap<String, Object> newOtchot = new HashMap<>();
                        newOtchot.put("title", todayDate);
                        newOtchot.put("sumChiqqanOrder", Integer.parseInt(ordSum.substring(7)));
                        if (orderCost.length()>14) {
                            newOtchot.put("costChiqqanOrder", Integer.parseInt(orderCost.substring(16)));
                        }

                        firestore.collection("Otchotlar")
                                .add(newOtchot)
                                .addOnSuccessListener(documentReference -> Log.d("sumAndCostChiqqanOrders",
                                        "Sum And Cost Chiqqan Order successfully"))
                                .addOnFailureListener(e -> Log.e("sumAndCostChiqqanOrders",
                                        "Sum And Cost Chiqqan Unsuccessfully" + e.getMessage()));
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(OrderDetail.this, "error to get otchotlar " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void addChiqqanOrderToOtchotlar() {
        // get today date
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(date);

        firestore.collection("Otchotlar").whereEqualTo("title", todayDate).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()){
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String otchotId = documentSnapshot.getId();
                        String currentCount = documentSnapshot.getString("countChiqqanOrders") != null ?
                                documentSnapshot.getString("countChiqqanOrders") : "0";

                        firestore.collection("Otchotlar")
                                .document(otchotId)
                                .update("countChiqqanOrders", String.valueOf(Integer.parseInt(currentCount) + 1))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Count Chiqqan Orders", "countNewOrders updated successfully");
                                }).addOnFailureListener(e ->
                                        Log.d("Count Chiqqan Orders", "countNewOrders updated successfully"));
                    } else {
                        HashMap<String, Object> newOtchot = new HashMap<>();
                        newOtchot.put("title", todayDate);
                        newOtchot.put("countChiqqanOrders", 1);

                        firestore.collection("Otchotlar")
                                .add(newOtchot)
                                .addOnSuccessListener(documentReference -> Log.d("Count Chiqqan Orders", "Count Chiqqan order successfully"))
                                .addOnFailureListener(e -> Log.e("Count Chiqqan Orders", "Error counting chiqqan orders", e));
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(OrderDetail.this, "error to get otchotlar " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
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
            progressDialog.dismiss();
            if (task.isSuccessful()){
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
        progressDialog.show(); // ProgressDialog ni ko'rsatish

        CollectionReference objectsRef = firestore.collection("OrderObjects");
        objectsRef.whereEqualTo("orderId", orderId).get().addOnCompleteListener(task -> {
            progressDialog.dismiss(); // ProgressDialog ni yopish

            if (task.isSuccessful()) {
                objectsArrayList.clear(); // Ro'yxatni tozalash

                for (DocumentSnapshot snapshot : task.getResult()) {
                    ModelOrderObject modelOrderObject = snapshot.toObject(ModelOrderObject.class);
                    if (modelOrderObject != null) {
                        objectsArrayList.add(modelOrderObject); // Ro'yxatni to'ldirish
                    }
                }

                if (objectsArrayList.isEmpty()) {
                    orderObjectsRV.setVisibility(View.GONE); // Ro'yxat bo'sh bo'lsa, RecyclerView ni yashirish
                } else {
                    orderObjectsRV.setVisibility(View.VISIBLE); // Ro'yxat bo'sh bo'lmasa, RecyclerView ni ko'rsatish
                }

                if (adapterOrderObject == null) {
                    // Adapter yaratilmagan bo'lsa, yangi yaratish
                    adapterOrderObject = new AdapterOrderObject(OrderDetail.this, objectsArrayList, sharedPreferences);
                    orderObjectsRV.setAdapter(adapterOrderObject);
                } else {
                    // Adapter allaqachon yaratilgan bo'lsa, yangidan yaratish
                    adapterOrderObject.setOrderObjects(objectsArrayList);
                    adapterOrderObject.notifyDataSetChanged(); // Adapterni yangilash
                }
            } else {
                Toast.makeText(OrderDetail.this, "Qismlar yuklashda xato: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (adapterOrderObject != null) {
            adapterOrderObject.dismissAllDialogs(); // Adapterdagi barcha dialoglarni dismiss qilish
        }
        if (adapterProductOrder != null) {
            adapterProductOrder.dismissAllDialogs(); // Adapterdagi barcha dialoglarni dismiss qilish
        }
        super.onBackPressed(); // Activityni yakunlash va oldingi activityga qaytish
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
    private void deleteCutPartProductOrderByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("CutPartProduct");
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
    private void deleteCutPartProductObjectByOrder(String deletedOrderId) {
        CollectionReference orderPaysRef = firestore.collection("CutPartProduct");
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("orderId")){
            orderId = intent.getStringExtra("orderId");
            loadOrderDetail(orderId);
        }
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