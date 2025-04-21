package com.example.curtain.crud;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.curtain.R;
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EditOrder extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private ImageButton backBtn;
    private Button editOrderBtn;
    private ProgressDialog progressDialog;
    private TextInputEditText editOrderNumberET, editOrderCatET, editOrderNameET, editOrderPhoneET, editOrderSumET, editOrderLocET, editOrderDescET;
    private String orderID;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        init();

        backBtn.setOnClickListener(view ->onBackPressed());

        loadEditOrder();

        editOrderCatET.setOnClickListener(view -> categoryDialog());
        editOrderLocET.setOnClickListener(view -> locationDialog());

        editOrderBtn.setOnClickListener(view -> {
            editOrder();
        });
    }

    private String editOrderNumber, editOrderCat, editOrderName, editOrderPhone, editOrderSum, editOrderLoc,
            editOrderDesc;
    private void editOrder() {

        editOrderNumber = editOrderNumberET.getText().toString().trim();
        editOrderCat = editOrderCatET.getText().toString().trim();
        editOrderName = editOrderNameET.getText().toString().trim();
        editOrderPhone = editOrderPhoneET.getText().toString().trim();
        editOrderSum = editOrderSumET.getText().toString().trim();
        editOrderLoc = editOrderLocET.getText().toString().trim();
        editOrderDesc = editOrderDescET.getText().toString().trim();

        if (TextUtils.isEmpty(editOrderNumber)) {
            Toast.makeText(this, "Smeta raqamini kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(editOrderCat)) {
            Toast.makeText(this, "Kategoriyani tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(editOrderName)) {
            Toast.makeText(this, "Klient ismini kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(editOrderLoc)) {
            Toast.makeText(this, "Manzilni tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating Order");
        progressDialog.show();

        String editTimestamps = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderNumber", editOrderNumber);
        hashMap.put("orderName", editOrderName);
        hashMap.put("orderCat", editOrderCat);
        if (!editOrderSum.isEmpty()) {
            hashMap.put("orderSum", "" + editOrderSum);
        }
        if (!editOrderPhone.isEmpty()) {
            hashMap.put("orderPhone", "" + editOrderPhone);
        }
        if (!editOrderDesc.isEmpty()) {
            hashMap.put("orderDesc", "" + editOrderDesc);
        }
        if (!editOrderLoc.isEmpty()) {
            hashMap.put("orderLoc", "" + editOrderLoc);
        }
        if (editOrderLoc.equals("Viloyat")){
            hashMap.put("orderPercent", "3.5");
        } else {
            hashMap.put("orderPercent", "3");
        }
        if (editOrderCat.equals("Stirka")){
            hashMap.put("orderPercent", "10");
        }

        hashMap.put("edited_at", editTimestamps);

        DocumentReference productRef = firestore.collection("Orders").document(orderID);
        productRef.update(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Updated...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OrderDetail.class);
            intent.putExtra("orderId", orderID);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this,
                    "Not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }
    private void loadEditOrder() {
        progressDialog.setMessage("Loading Product");
        progressDialog.show();
        DocumentReference productRef = firestore.collection("Orders").document(orderID);
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()){
                    String orderNumber = doc.getString("orderNumber");
                    String orderCat = doc.getString("orderCat");   // Assuming "prCat" field exists
                    String orderName = doc.getString("orderName"); // Assuming "prPrice" field exists
                    // Handle optional fields (check for null before accessing)
                    if (doc.contains("orderPhone")) {  // Check if field exists
                        String orderPhone = doc.getString("orderPhone");
                        editOrderPhoneET.setText(orderPhone);
                    } else {
                        editOrderPhoneET.setText("");
                    }
                    if (doc.contains("orderSum")) { // Check if field exists
                        String orderSum = doc.getString("orderSum");
                        editOrderSumET.setText(orderSum);
                    } else {
                        editOrderSumET.setText("");
                    }

                    if (doc.contains("orderDesc")) {  // Check if field exists
                        String orderDesc = doc.getString("orderDesc");
                        editOrderDescET.setText(orderDesc);
                    } else {
                        editOrderDescET.setText("");
                    }
                    String orderLoc = doc.getString("orderLoc");   // Assuming "prCat" field exists
                    editOrderNumberET.setText(orderNumber);
                    editOrderCatET.setText(orderCat);
                    editOrderNameET.setText(orderName);
                    editOrderLocET.setText(orderLoc);
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(EditOrder.this, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                // Handle errors (consider logging or displaying an error message)
                Toast.makeText(EditOrder.this, "Error fetching product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(){

        firestore = FirebaseFirestore.getInstance();

        orderID = getIntent().getStringExtra("orderId");
        backBtn = findViewById(R.id.backBtn);
        editOrderBtn = findViewById(R.id.editOrderBtn);
        editOrderNumberET = findViewById(R.id.editOrderNumberET);
        editOrderCatET = findViewById(R.id.editOrderCatET);
        editOrderNameET = findViewById(R.id.editOrderNameET);
        editOrderPhoneET = findViewById(R.id.editOrderPhoneET);
        editOrderSumET = findViewById(R.id.editOrderSumET);
        editOrderLocET = findViewById(R.id.editOrderLocET);
        editOrderDescET = findViewById(R.id.editOrderDescET);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);

    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smeta turi").setItems(Constants.orderCats, (dialog, which) -> {
            // get picked location
            String category = Constants.orderCats[which];
            // set picked location
            editOrderCatET.setText(category);
        }).show();
    }

    private void locationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lokatsiya").setItems(Constants.orderLocations, (dialog, which) -> {
            // get picked location
            String location = Constants.orderLocations[which];
            // set picked location
            editOrderLocET.setText(location);
        }).show();
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