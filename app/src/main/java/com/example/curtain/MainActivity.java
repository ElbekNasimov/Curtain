package com.example.curtain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.activities.LoginActivity;
import com.example.curtain.activities.UsersListActivity;
import com.example.curtain.adapter.AdapterOrder;
import com.example.curtain.adapter.AdapterProduct;
import com.example.curtain.constants.CaptureAct;
import com.example.curtain.constants.Constants;
import com.example.curtain.crud.AddOrder;
import com.example.curtain.crud.AddProduct;
import com.example.curtain.model.ModelOrder;
import com.example.curtain.model.ModelProduct;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // write function, that must check all data every week. If partProduct has two len=0, func remove one of them
    // har hafta part uzunligini tekshiradigan funksiya yozish kerak, agar len=0 bittadan ko'p bo'lsa, ularni o'chirsin

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RelativeLayout productsRL, ordersRL;
    private LinearLayout orderCreatedByLL;
    private GridView ordersGV;
    private ImageButton addProductBtn, logoutBtn, filterPrBtn, scannerPrBtn;
    private ImageButton addOrderBtn;
    private TextView usernameTV, userTypeTV, tabProdsTV, tabOrdersTV, filterPrTV, emptyTV, usersListTV;
    private EditText searchET, searchOrderET;
    private RecyclerView productRV;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private ArrayList<ModelOrder> orderList;
    private AdapterProduct adapterProduct;
    private AdapterOrder adapterOrder;
    private SharedPreferences sharedPreferences;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Fragmenti  4 ta bo'ladi - Main, Orders, Statistics nad History'

        init();

        String userStatus = sharedPreferences.getString("user_status", "");
        String sharedUserType = getUserType(sharedPreferences);
        if (userStatus.equals("ENABLE")){
            checkUser();
        } else if (userStatus.equals("DISABLE")){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            checkUser();
        }

        String select="Hammasi";
        loadProducts(select);

        loadOrders();

        usersListTV.setVisibility(View.GONE);

        if (sharedUserType.equals(Constants.userTypes[4])){
            usersListTV.setVisibility(View.VISIBLE);
        }

        if (sharedUserType.equals("dizayner") || sharedUserType.equals("bichuvchi")){
            addProductBtn.setVisibility(View.GONE);
        } else {
            addProductBtn.setOnClickListener(v ->{
                startActivity(new Intent(MainActivity.this, AddProduct.class));
            });
        }

        if (sharedUserType.equals("sklad") || sharedUserType.equals("bichuvchi")){
            addOrderBtn.setVisibility(View.GONE);
        }

        addOrderBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddOrder.class));
        });

        emptyTV.setVisibility(View.GONE);

//        SharedPreferences sharedFromOrders = getSharedPreferences("FROM_ORDERS", MODE_PRIVATE);
//        String sharedFrom = sharedFromOrders.getString("from_orders", "");
//        if (!sharedFrom.isEmpty()){
//            showOrdersUI();
//            sharedFromOrders.edit().remove("from_orders").apply();
//        } else {
            showProductsUI();
//        }

        usersListTV.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UsersListActivity.class)));
        // search
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapterProduct!=null){
                    adapterProduct.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchOrderET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (adapterOrder!=null){
                    adapterOrder.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tabProdsTV.setOnClickListener(v -> {
            // load products
            showProductsUI();
        });

        tabOrdersTV.setOnClickListener(v -> {
            // load orders
            showOrdersUI();
        });

        filterPrBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Kategoriyani tanlang").setItems(Constants.categories1, (dialog, which) -> {
                // get selected item
                String selected = Constants.categories1[which];
                filterPrTV.setText(selected);
                loadProducts(selected);
            }).show();
        });
        

        scannerPrBtn.setOnClickListener(view -> scanBarcode());

        logoutBtn.setOnClickListener(view -> {
            progressDialog.setMessage(getResources().getString(R.string.wait));
            SharedPreferences preferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
            preferences.edit().remove("user_type").apply();
            preferences.edit().remove("username").apply();
            preferences.edit().remove("user_status").apply();
            mAuth.signOut();
            finishAffinity();
            checkUser();
        });
    }

    private String getUserType(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("user_type", "");
    }
    private void init(){
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        productsRL = findViewById(R.id.productsRL);
        addProductBtn = findViewById(R.id.addProductBtn);
        ordersRL = findViewById(R.id.ordersRL);
        addOrderBtn = findViewById(R.id.addOrderBtn);
        filterPrBtn = findViewById(R.id.filterPrBtn);
        scannerPrBtn = findViewById(R.id.scannerPrBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        ordersGV = findViewById(R.id.ordersGV);
        usernameTV = findViewById(R.id.usernameTV);
        userTypeTV = findViewById(R.id.userTypeTV);
        tabProdsTV = findViewById(R.id.tabProdsTV);
        tabOrdersTV = findViewById(R.id.tabOrdersTV);
        filterPrTV = findViewById(R.id.filterPrTV);
        searchET = findViewById(R.id.searchET);
        searchOrderET = findViewById(R.id.searchOrderET);
        emptyTV = findViewById(R.id.emptyTV);
        usersListTV = findViewById(R.id.usersListTV);

        productRV = findViewById(R.id.productRV);
        orderCreatedByLL = findViewById(R.id.orderCreatedByLL);

        productList = new ArrayList<>();
        orderList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);
    }

    private void checkUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            usernameTV.setText(mAuth.getCurrentUser().getDisplayName().toUpperCase());
            String sharedUserType = getUserType(sharedPreferences);
            userTypeTV.setText(sharedUserType);
        }
    }

    private void loadProducts(String selected){
        progressDialog.show();

        CollectionReference collectionReference  = firebaseFirestore.collection("Products");
        collectionReference.addSnapshotListener((snapshots, error) -> {
            if (error!=null){
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "mahsulotlar yuklashda xato "
                        + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
            assert snapshots != null;
            if (snapshots.isEmpty()){
//                    Process data from all documents in the snapshot
                progressDialog.dismiss();
                productRV.setVisibility(View.GONE);
                emptyTV.setVisibility(View.VISIBLE);
            }
                progressDialog.dismiss();
                emptyTV.setVisibility(View.GONE);

            productList.clear();
            for (QueryDocumentSnapshot documentSnapshot:snapshots){
                ModelProduct modelProduct = documentSnapshot.toObject(ModelProduct.class);
                if (selected.equals("Hammasi") || selected.equals("")){
                    productList.add(modelProduct);
                } else if (selected.equals(modelProduct.getPrCat())){
                    productList.add(modelProduct);
                }
            }
            adapterProduct = new AdapterProduct(MainActivity.this, productList, sharedPreferences);
            productRV.setAdapter(adapterProduct);
        });
    }

    private void loadOrders() {
        progressDialog.show();
        String currentUsername = sharedPreferences.getString("username", "");
        String sharedUserType = sharedPreferences.getString("user_type", "");
        Query query;
        if (sharedUserType.equals("dizayner")) {
            query = firebaseFirestore.collection("Orders").whereEqualTo("created_by", currentUsername);
        } else {
            if (sharedUserType.equals("sklad") || sharedUserType.equals("bichuvchi")) {
                query = firebaseFirestore.collection("Orders").whereNotEqualTo("orderStatus", "Yangi");
            } else {
                query = firebaseFirestore.collection("Orders");
            }
        }

        query.get().addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                progressDialog.dismiss();
                List<DocumentSnapshot> list = snapshots.getDocuments();
                orderList.clear();
                for (DocumentSnapshot doc : list) {
                    ModelOrder modelOrder = doc.toObject(ModelOrder.class);
                    orderList.add(modelOrder);
                }
                adapterOrder = new AdapterOrder(MainActivity.this, orderList, sharedPreferences);
                ordersGV.setAdapter(adapterOrder);
            } else {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "orderlar yuklashda xato " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showProductsUI(){
        // show products UI and hide orders UI
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabProdsTV.setTextColor(getResources().getColor(R.color.black));
        tabProdsTV.setBackgroundResource(R.drawable.shape_rect04);
        tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        // show orders UI and hide products UI
        productsRL.setVisibility(View.GONE);
        ordersRL.setVisibility(View.VISIBLE);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);
        tabProdsTV.setTextColor(getResources().getColor(R.color.white));
        tabProdsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void scanBarcode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class); // Assuming CaptureAct extends AppCompatActivity
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    progressDialog.dismiss();
                    String barcode = result.getContents();
                    searchET.setText(barcode); // Set barcode in searchET
                    filterProductsByBarcode(barcode);
                }
            });

    private void filterProductsByBarcode(String barcode) {
        CollectionReference collectionReference = firebaseFirestore.collection("Products");
        collectionReference.whereEqualTo("prBarcode", barcode).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
//                    Handle error
                Toast.makeText(MainActivity.this, "Error fetching product "
                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (task.getResult().isEmpty()){
                Toast.makeText(MainActivity.this, "Yo'q", Toast.LENGTH_SHORT).show();
                return;
            }
            productList.clear();
            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                ModelProduct modelProduct = documentSnapshot.toObject(ModelProduct.class);
                productList.add(modelProduct);
            }
            adapterProduct = new AdapterProduct(MainActivity.this, productList, sharedPreferences);
            productRV.setAdapter(adapterProduct);
        });
    }

//    Clicking the back button twice to exit an activity

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce){
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Chiqish uchun 2 marta bosing", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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