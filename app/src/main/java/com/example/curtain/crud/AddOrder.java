package com.example.curtain.crud;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.example.curtain.MainActivity;
import com.example.curtain.R;
import com.example.curtain.activities.UsersListActivity;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class AddOrder extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseFirestore addOrderToFirestore;
    private  ProgressDialog progressDialog;
    private Button saveOrderBtn;
    private TextInputEditText orderNumberET, orderCatET,  orderNameET, orderPhoneET, orderSumET,
            orderLocET, orderDescET, orderDeadlineET;
    private String currentUsername;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        init();

        saveOrderBtn.setOnClickListener(view -> input_data());

        orderCatET.setOnClickListener(view -> categoryDialog());

        orderLocET.setOnClickListener(view -> locationDialog());

        orderDeadlineET.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this, (datePicker, i, i1, i2) -> {
                String deadline = i2+ "/" + (i1+1) + "/" + i;
                orderDeadlineET.setText(deadline);
            }, year, month, day);

            datePickerDialog.show();
        });

    }

    private void init(){
        saveOrderBtn = findViewById(R.id.saveOrderBtn);
        orderNumberET = findViewById(R.id.orderNumberET);
        orderNameET = findViewById(R.id.orderNameET);
        orderPhoneET = findViewById(R.id.orderPhoneET);
        orderSumET = findViewById(R.id.orderSumET);

        orderCatET = findViewById(R.id.orderCatET);
        orderLocET = findViewById(R.id.orderLocET);
        orderDeadlineET = findViewById(R.id.orderDeadlineET);
        orderDescET = findViewById(R.id.orderDescET);

        mAuth = FirebaseAuth.getInstance();
        addOrderToFirestore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private String ordNumber, ordName, ordPhone, ordSum, ordCat, ordLoc, ordDeadline, ordDesc;
    private void input_data() {

        ordNumber = orderNumberET.getText().toString().trim();
        ordName = orderNameET.getText().toString().trim();
        ordPhone = orderPhoneET.getText().toString().trim();
        ordSum = orderSumET.getText().toString().trim();
        ordCat = orderCatET.getText().toString().trim();
        ordLoc = orderLocET.getText().toString().trim();
        ordDeadline = orderDeadlineET.getText().toString().trim();
        ordDesc = orderDescET.getText().toString().trim();

        if (TextUtils.isEmpty(ordNumber)) {
            Toast.makeText(this, "Smeta raqamini kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ordCat)) {
            Toast.makeText(this, "Kategoriyani tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ordName)) {
            Toast.makeText(this, "Klient ismini kiriting...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ordLoc)) {
            Toast.makeText(this, "Manzilni tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Adding Smeta...");
        progressDialog.show();
        addOrderToFirestore.collection("Orders").whereEqualTo("orderNumber", ordNumber).
                get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                if (!task.getResult().isEmpty()){
                    Toast.makeText(AddOrder.this, "Bunaqa smeta qo'shilgan", Toast.LENGTH_SHORT).show();
                } else {
                    orderAdd();
                }
            } else {
                Toast.makeText(AddOrder.this, "error at add order " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void orderAdd() {
        String orderID = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + orderID);
        hashMap.put("orderNumber", "" + ordNumber);
        hashMap.put("orderName", "" + ordName);
        hashMap.put("orderCat", "" + ordCat);
        if (!ordSum.isEmpty()) {
            hashMap.put("orderSum", "" + ordSum);
        }
        if (!ordPhone.isEmpty()) {
            hashMap.put("orderPhone", "" + ordPhone);
        }
        if (!ordDesc.isEmpty()) {
            hashMap.put("orderDesc", "" + ordDesc);
        }
        if (!ordLoc.isEmpty()) {
            hashMap.put("orderLoc", "" + ordLoc);
        }
        if (!ordDeadline.isEmpty()) {
            hashMap.put("orderDeadline", "" + ordDeadline);
        }
        if (ordLoc.equals("Viloyat")){
            hashMap.put("orderPercent", "3.5");
        } else if (ordLoc.equals("Chet el")){
            hashMap.put("orderPercent", "2");
        } else {
            hashMap.put("orderPercent", "3");
        }
        if (ordCat.equals("Stirka")){
            hashMap.put("orderPercent", "10");
        }
        hashMap.put("orderStatus", "Yangi");
        Date prDate = new Date(Long.parseLong(orderID));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String created_at = dateFormat.format(prDate);
        hashMap.put("created_at", "" + created_at);

        if (Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName()!=null) {
            hashMap.put("created_by", "" + mAuth.getCurrentUser().getDisplayName());
        } else {
            hashMap.put("created_by", "" + currentUsername);
        }

        addOrderToFirestore.collection("Orders").document(orderID).set(hashMap).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()){
                Toast.makeText(AddOrder.this, "Yangi smeta qo'shildi", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddOrder.this, MainActivity.class));
            } else {
                Toast.makeText(AddOrder.this, "error to add smeta "
                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smeta turi").setItems(Constants.orderCats, (dialog, which) -> {
            // get picked location
            String category = Constants.orderCats[which];
            // set picked location
            orderCatET.setText(category);
        }).show();
    }

    private void locationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lokatsiya").setItems(Constants.orderLocations, (dialog, which) -> {
            // get picked location
            String location = Constants.orderLocations[which];
            // set picked location
            orderLocET.setText(location);
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