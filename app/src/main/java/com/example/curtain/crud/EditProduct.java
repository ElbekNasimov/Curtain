package com.example.curtain.crud;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.curtain.utilities.Save;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class EditProduct extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ImageButton backBtn;
    private TextInputEditText titleET, catET, descET, priceET, costET, productHeightET, productMassET,
            productCompanyET, productColorET;
    private TextInputLayout labelCost;
    private TextView barcodeET;
    private Button editPrdBtn;
    private ProgressDialog progressDialog;
    private String prID;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        init();

        String sharedUserType = Save.read(getApplicationContext(), "USER_TYPE", "user_type", "");
        if (sharedUserType.isEmpty()) {
            startActivity(new Intent(EditProduct.this, LoginActivity.class));
            finish();
        } else {
            if (sharedUserType.equals("superAdmin")) {
                labelCost.setVisibility(View.VISIBLE);
            } else {
                labelCost.setVisibility(View.GONE);
            }
        }

        loadProductDetails(); // to set on view

        backBtn.setOnClickListener(v -> onBackPressed());

        catET.setOnClickListener(view -> categoryDialog());

        productColorET.setOnClickListener(view -> colorDialog());

        barcodeET.setOnClickListener(v -> {
            Toast.makeText(EditProduct.this, "ScanCode", Toast.LENGTH_SHORT).show();
            scanCode();
        });

        editPrdBtn.setOnClickListener(view -> editData());

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

    private void init(){
        prID = getIntent().getStringExtra("prId");

        backBtn = findViewById(R.id.backBtn);
        titleET = findViewById(R.id.titleET);
        barcodeET = findViewById(R.id.barcodeET);
        descET = findViewById(R.id.descET);
        priceET = findViewById(R.id.priceET);
        costET = findViewById(R.id.costET);
        labelCost = findViewById(R.id.labelCost);
        catET = findViewById(R.id.catET);
        editPrdBtn = findViewById(R.id.editPrdBtn);
        productHeightET = findViewById(R.id.productHeightET);
        productMassET = findViewById(R.id.productMassET);
        productCompanyET = findViewById(R.id.productCompanyET);
        productColorET = findViewById(R.id.productColorET);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void loadProductDetails() {
        progressDialog.setMessage("Loading product details");
        progressDialog.show();
        DocumentReference productRef = firestore.collection("Products").document(prID);
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()){
                    String prTitle = doc.getString("prTitle");
                    String prCat = doc.getString("prCat");   // Assuming "prCat" field exists
                    String prPrice = doc.getString("prPrice"); // Assuming "prPrice" field exists

                    // Handle optional fields (check for null before accessing)
                    if (doc.contains("prBarcode")) {  // Check if field exists
                        String barcode = doc.getString("prBarcode");
                        barcodeET.setText(barcode);
                    } else {
                        barcodeET.setText("");
                    }
                    if (doc.contains("prDesc")) { // Check if field exists
                        String desc = doc.getString("prDesc");
                        descET.setText(desc);
                    } else {
                        descET.setText("");
                    }
                    if (doc.contains("prCost")) {  // Check if field exists
                        String cost = doc.getString("prCost");
                        costET.setText(cost);
                    } else {
                        costET.setText("");
                    }
                    titleET.setText(prTitle);
                    catET.setText(prCat);
                    priceET.setText(prPrice);
                    if (doc.contains("prMass")) {  // Check if field exists
                        String prMass = doc.getString("prMass");
                        productMassET.setText(prMass);
                    } else {
                        productMassET.setText("");
                    }
                    if (doc.contains("prComp")) {  // Check if field exists
                        String prComp = doc.getString("prComp");
                        productCompanyET.setText(prComp);
                    } else {
                        productCompanyET.setText("");
                    }
                    if (doc.contains("prHeight")) {  // Check if field exists
                        String prHeight = doc.getString("prHeight");
                        productHeightET.setText(prHeight);
                    } else {
                        productHeightET.setText("");
                    }
                    if (doc.contains("prColor")) {  // Check if field exists
                        String prColor = doc.getString("prColor");
                        productColorET.setText(prColor);
                    } else {
                        productColorET.setText("");
                    }

                } else {
                    Toast.makeText(EditProduct.this, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                // Handle errors (consider logging or displaying an error message)
                Toast.makeText(EditProduct.this, "Error fetching product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String title, category, barcode, desc, price, cost, productHeight, productMass, productCompany, productColor;

    private void editData() {

        title = titleET.getText().toString().trim();
        category = catET.getText().toString().trim();
        barcode = Objects.requireNonNull(barcodeET.getText()).toString().trim();
        desc = descET.getText().toString().trim();
        price = Objects.requireNonNull(priceET.getText()).toString().trim();
        cost = Objects.requireNonNull(costET.getText()).toString().trim();
        productHeight = productHeightET.getText().toString().trim();
        productMass = productMassET.getText().toString().trim();
        productCompany = productCompanyET.getText().toString().trim();
        productColor = productColorET.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Product Name...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show();
            return;
        }

        String editTimestamps = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("prTitle", "" + title);
        hashMap.put("prCat", "" + category);
        if (!price.isEmpty()){
            hashMap.put("prPrice", "" + price);
        }
        if (!cost.isEmpty()) {
            hashMap.put("prCost", "" + cost);
        }
        if (!barcode.isEmpty()) {
            hashMap.put("prBarcode", "" + barcode);
        }
        if (!desc.isEmpty()) {
            hashMap.put("prDesc","" + desc);
        }
        if (!productHeight.isEmpty()) {
            hashMap.put("prHeight", "" + productHeight);
        }
        if (!productColor.isEmpty()) {
            hashMap.put("prColor", "" + productColor);
        }
        if (!productMass.isEmpty()) {
            hashMap.put("prMass", "" + productMass);
        }
        if (!productCompany.isEmpty()) {
            hashMap.put("prComp", "" + productCompany);
        }

        Date prDate = new Date(Long.parseLong(editTimestamps));
        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String edit_at = sdfFormat.format(prDate);
        hashMap.put("prEditAt", "" + edit_at);

        String sharedUsername = Save.read(getApplicationContext(), "USER_TYPE", "username", "");
        if (Objects.requireNonNull(auth.getCurrentUser()).getDisplayName()!=null) {
            hashMap.put("prEditBy", "" + auth.getCurrentUser().getDisplayName());
        } else {
            hashMap.put("prEditBy", "" + sharedUsername);
        }

        progressDialog.setMessage("Updating Product");
        progressDialog.show();
        // update to db

//        firestore.collection("Products").whereEqualTo("prTitle", title.toLowerCase()).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()){
//                progressDialog.dismiss();
//                if (!task.getResult().isEmpty()) {
//                    Toast.makeText(EditProduct.this, "Bunaqa product qo'shilgan", Toast.LENGTH_SHORT).show();
//                } else {
                    DocumentReference productRef = firestore.collection("Products").document(prID);
                    productRef.update(hashMap).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProduct.this, "Updated...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProduct.this, MainActivity.class));
                        finish();
                    }).addOnFailureListener(e -> Toast.makeText(EditProduct.this,
                            "Not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                }
//            } else {
//                Toast.makeText(EditProduct.this, "error " +
//                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });


    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get picked location
                String category = Constants.categories[which];
                // set picked location
                catET.setText(category);
            }
        }).show();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher =
            registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            barcodeET.setText(result.getContents());
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProduct.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
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