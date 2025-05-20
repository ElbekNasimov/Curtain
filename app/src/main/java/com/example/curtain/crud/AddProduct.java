package com.example.curtain.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.MainActivity;
import com.example.curtain.R;
import com.example.curtain.activities.LoginActivity;
import com.example.curtain.constants.CaptureAct;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class AddProduct extends AppCompatActivity {

//    Add switch compat for get products

//    <androidx.appcompat.widget.SwitchCompat
//    android:id="@+id/discountSC"
//    android:layout_width="match_parent"
//    android:layout_height="wrap_content"
//    android:layout_below="@id/qrcodeTV"
//    android:textSize="17sp"
//    android:textColor="@color/grey2"
//    android:layout_marginStart="5dp"
//    android:layout_marginEnd="5dp"
//    android:background="@drawable/shape_rect02"
//    android:drawableStart="@drawable/ic_disc_grey"
//    android:drawablePadding="10dp"
//    android:text="@string/discount" />


    private ImageButton backBtn;
    private Button addPrdBtn;
    private TextInputEditText titleET, catET, priceET, costET,  descET, productHeightET, productColorET,
            productMassET, productCompanyET;
    private TextInputLayout labelColor;
    private SwitchCompat abbosSC, podzakazSC;
    private TextInputLayout labelCost;
    private TextView barcodeET;
    private FirebaseAuth mAuth;
    private FirebaseFirestore addPrToFireStore;
    private String currentUser, currentUsername;
    private ProgressDialog progressDialog;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        init();

        SharedPreferences sharedPreferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
        currentUser = sharedPreferences.getString("user_type", "");
        currentUsername = sharedPreferences.getString("username", "");

        if (currentUser.isEmpty()) {
            startActivity(new Intent(AddProduct.this, LoginActivity.class));
            finish();
        } else {
            if (currentUser.equals("superAdmin")) {
                labelCost.setVisibility(View.VISIBLE);
            } else {
                labelCost.setVisibility(View.GONE);
            }
        }

        backBtn.setOnClickListener(view -> {
            startActivity(new Intent(AddProduct.this, MainActivity.class));
            finish();
        });

        catET.setOnClickListener(view -> categoryDialog());

        productColorET.setOnClickListener(view -> colorDialog());

        addPrdBtn.setOnClickListener(view -> input_data());

        barcodeET.setOnClickListener(v -> {
            scanCode();
        });


    }

    public void init(){
        backBtn = findViewById(R.id.backBtn);
        addPrdBtn = findViewById(R.id.addPrdBtn);
        titleET = findViewById(R.id.titleET);
        catET = findViewById(R.id.catET);
        priceET = findViewById(R.id.priceET);
        costET = findViewById(R.id.costET);
        labelCost = findViewById(R.id.labelCost);
        barcodeET = findViewById(R.id.barcodeET);
        descET = findViewById(R.id.descET);
        productHeightET = findViewById(R.id.productHeightET);
        productColorET = findViewById(R.id.productColorET);
        productMassET = findViewById(R.id.productMassET);
        productCompanyET = findViewById(R.id.productCompanyET);

        abbosSC = findViewById(R.id.abbosSC);
        podzakazSC = findViewById(R.id.podzakazSC);

        labelColor = findViewById(R.id.labelColor);

        mAuth = FirebaseAuth.getInstance();
        addPrToFireStore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mahsulot kategoriyasi").setItems(Constants.categories, (dialog, which) -> {
            // get picked location
            String category = Constants.categories[which];
            // set picked location
            catET.setText(category);
        }).show();
    }

    private void colorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mahsulot rangi").setItems(Constants.productColor, (dialog, which) -> {
            // get picked color
            String color = Constants.productColor[which];
            // set picked color
            productColorET.setText(color);
        }).show();
    }

    private String title, prCat, price, cost, prBarcode, desc, productHeight, productColor, productMass, productCompany;
    private boolean isAbbos = false, isPodzakaz = false;
    private void input_data() {
        title = titleET.getText().toString().trim().toLowerCase();
        prCat = catET.getText().toString().trim();
        price = priceET.getText().toString().trim();
        cost = costET.getText().toString().trim();
        prBarcode = barcodeET.getText().toString().trim();
        desc = descET.getText().toString().trim();
        productHeight = productHeightET.getText().toString().trim();
        productColor = productColorET.getText().toString().trim();
        productMass = productMassET.getText().toString().trim();
        productCompany = productCompanyET.getText().toString().trim();
        isAbbos = abbosSC.isChecked();
        isPodzakaz = podzakazSC.isChecked();

        String productID = "" + System.currentTimeMillis();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Mahsulot nomi kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(prCat)) {
            Toast.makeText(this, "Kategoriyani tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cost.isEmpty()) {
            if (!price.isEmpty() && (Float.parseFloat(cost) > Float.parseFloat(price))) {
                Toast.makeText(this, "Narxlarni tekshiring", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        addPrToFireStore.collection("Products").whereEqualTo("prTitle", title.toLowerCase()).get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                if (!task.getResult().isEmpty()){
                    Toast.makeText(AddProduct.this, "Bunaqa product qo'shilgan", Toast.LENGTH_SHORT).show();
                } else {
                    productAdd(productID);
                }
            } else {
                Toast.makeText(AddProduct.this, "error at add product " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void productAdd(String productID) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("prId", productID);
        hashMap.put("prTitle", title);
        hashMap.put("prCat", prCat);
        if (!price.isEmpty()) {
            hashMap.put("prPrice", price);
        }
        if (!cost.isEmpty()) {
            hashMap.put("prCost", cost);
        }
        if (!prBarcode.isEmpty()) {
            hashMap.put("prBarcode", prBarcode);
        } else {
            hashMap.put("prBarcode", productID);
        }
        if (!desc.isEmpty()) {
            hashMap.put("prDesc", desc);
        }
        if (!productHeight.isEmpty()) {
            hashMap.put("prHeight", productHeight);
        }
        if (!productColor.isEmpty()) {
            hashMap.put("prColor", productColor);
        }
        if (!productMass.isEmpty()) {
            hashMap.put("prMass", productMass);
        }
        if (!productCompany.isEmpty()) {
            hashMap.put("prComp", productCompany);
        }
        Date prDate = new Date(Long.parseLong(productID));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String created_at = dateFormat.format(prDate);
        hashMap.put("created_at", created_at);
        if (Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName()!=null) {
            hashMap.put("created_by", mAuth.getCurrentUser().getDisplayName());
        } else {
            hashMap.put("created_by", currentUsername);
        }

        if (isAbbos) {
            hashMap.put("isAbbos", "true");
        }

        if (isPodzakaz) {
            hashMap.put("isPodzakaz", "true");
        }

        addPrToFireStore.collection("Products").document(productID).set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(AddProduct.this, "Yangi mahsulot qo'shildi", Toast.LENGTH_SHORT).show();
                    clearData();
                } else {
                    Toast.makeText(AddProduct.this, "error to add product " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void clearData() {
        titleET.setText("");
        barcodeET.setText("");
        costET.setText("");
        descET.setText("");
        priceET.setText("");
        catET.setText("");
        productHeightET.setText("");
        productMassET.setText("");
        productCompanyET.setText("");
        productColorET.setText("");
        abbosSC.setChecked(false);
        podzakazSC.setChecked(false);
    }

    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            barcodeET.setText(result.getContents());
            AlertDialog.Builder builder = new AlertDialog.Builder(AddProduct.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        } else {
            Toast.makeText(AddProduct.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
        }
    });


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