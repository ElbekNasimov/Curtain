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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.activities.LoginActivity;
import com.example.curtain.activities.OtchotActivity;
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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // write function, that must check all data every week. If partProduct has two len=0, func remove one of them
    // har hafta part uzunligini tekshiradigan funksiya yozish kerak, agar len=0 bittadan ko'p bo'lsa, ularni o'chirsin

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RelativeLayout productsRL, ordersRL;
    private GridView ordersGV;
    private ImageButton addProductBtn, logoutBtn, filterPrBtn, scannerPrBtn, excelPrintBtn;
    private ImageButton addOrderBtn;
    private TextView usernameTV, userTypeTV, tabProdsTV, tabOrdersTV, filterPrTV, emptyTV, usersListTV, otchotTV;
    private EditText searchET, searchOrderET;
    private RecyclerView productRV;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private ArrayList<ModelOrder> orderList;
    private AdapterProduct adapterProduct;
    private AdapterOrder adapterOrder;
    private SharedPreferences sharedPreferences;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Fragmenti  4 ta bo'ladi - Main, Orders, Statistics and History'

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


        // qayta ko'rish kerak
//        SyncScheduler.scheduleSync(this);

        loadOrders();

        usersListTV.setVisibility(View.GONE);
        otchotTV.setVisibility(View.GONE);

        if (sharedUserType.equals(Constants.userTypes[4])){
            usersListTV.setVisibility(View.VISIBLE);
            otchotTV.setVisibility(View.VISIBLE);
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

        if (sharedUserType.equals("viewer")){
            addOrderBtn.setVisibility(View.GONE);
            addProductBtn.setVisibility(View.GONE);
        }

        addOrderBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddOrder.class));
        });

        emptyTV.setVisibility(View.GONE);

        showProductsUI();

        usersListTV.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UsersListActivity.class)));
        otchotTV.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, OtchotActivity.class)));

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

        if (!sharedUserType.equals("superAdmin")){
            excelPrintBtn.setVisibility(View.GONE);
        }

        excelPrintBtn.setOnClickListener(view -> {
            progressDialog.setMessage("Faylga saqlanmoqda");
            progressDialog.show();
            downloadExcel();
        });

        logoutBtn.setOnClickListener(view -> {
//            if (sharedUserType.equals("superAdmin")) {
                progressDialog.setMessage(getResources().getString(R.string.wait));
                SharedPreferences preferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
                preferences.edit().remove("user_type").apply();
                preferences.edit().remove("username").apply();
                preferences.edit().remove("user_status").apply();
                mAuth.signOut();
                finishAffinity();
                checkUser();
//            } else {
//                Toast.makeText(MainActivity.this, "Sizga ruxsat berilmagan", Toast.LENGTH_SHORT).show();
//            }
        });
    }

    private void downloadExcel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents != null && !documents.isEmpty()){
                            createExcelFile(documents);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, " excel saqlashda Ma'lumot topilmadi", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Exception exception = task.getException();
                        if (exception != null) {
                            Toast.makeText(this, "excel saqlashda xatolik " + exception.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No'malum xatolik excel saqlashda",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                   progressDialog.dismiss();
                    Toast.makeText(this, "Excel saqlashda xatolik " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createExcelFile(List<DocumentSnapshot> documents) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        // sarlavha qatorini yozish
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Nomi");
        headerRow.createCell(1).setCellValue("Barcode");
        headerRow.createCell(2).setCellValue("Narxi");
        headerRow.createCell(3).setCellValue("Eni");

        // ma'lumotlarni yozish
        int rowNum = 1;
        for (DocumentSnapshot document: documents){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(document.getString("prTitle"));
            row.createCell(1).setCellValue(document.getString("prBarcode"));
            row.createCell(2).setCellValue(document.getString("prPrice"));
            row.createCell(3).setCellValue(document.getString("prHeight"));
        }

        // Excel faylini saqlash
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Smetalar");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "products.xlsx");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            progressDialog.dismiss();
            Toast.makeText(this, "Excel fayl saqlandi", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Excel faylni saqlashda xatolik " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            progressDialog.dismiss();
            try {
                workbook.close();
            } catch (IOException e){
                Toast.makeText(this, "Workbookni yopishda xatolik " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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
        excelPrintBtn = findViewById(R.id.excelPrintBtn);
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
        otchotTV = findViewById(R.id.otchotTV);

        productRV = findViewById(R.id.productRV);

        productList = new ArrayList<>();
        orderList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);

        handler = new Handler(Looper.getMainLooper());
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
        String sharedUserType = sharedPreferences.getString("user_type", "");
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
            if (sharedUserType.equals("superAdmin")) {
                Toast.makeText(this, "products" + productList.size(), Toast.LENGTH_SHORT).show();
            }
            productRV.setAdapter(adapterProduct);
            adapterProduct.notifyDataSetChanged(); // UI ni yangilash
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
                if (sharedUserType.equals("superAdmin")) {
                    Toast.makeText(this, "orders" + orderList.size(), Toast.LENGTH_SHORT).show();
                }
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
        handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onDestroy() {
        // ProgressDialog'ni yopish
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // BroadcastReceiver'ni tozalash
        try {
            unregisterReceiver(networkChangeListener);
        } catch (IllegalArgumentException e) {
            // Receiver ro'yxatdan o'tmagan bo'lishi mumkin
        }

        // Handler'ni tozalash
        handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Har safar MainActivity qayta faol holatga kelganda loadOrders()ni qayta chaqiramiz
        loadOrders();
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