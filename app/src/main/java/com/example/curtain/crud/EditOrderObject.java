package com.example.curtain.crud;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

import com.example.curtain.R;
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EditOrderObject extends AppCompatActivity {

    String orderObjectId;
    private FirebaseFirestore firestore;
    private TextInputEditText editObjOrderRoomET, editOrderRoomET, editObjOrderDescET;
    private TextView getOrderIdTV;
    private ImageButton backBtn;
    private Button editOrderBtn;
    private ProgressDialog progressDialog;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order_object);

        init();

        backBtn.setOnClickListener(view ->onBackPressed());

        loadEditOrder();

        editObjOrderRoomET.setOnClickListener(view -> objRoomDialog());
        editOrderRoomET.setOnClickListener(view -> orderRoomDialog());

        editOrderBtn.setOnClickListener(view -> {
            editObjOrder();
        });

        getOrderIdTV.setVisibility(View.GONE);

    }

    private String editObjOrderRoom, editOrderRoom, editObjOrderDesc, orderId;
    private void editObjOrder() {

        editObjOrderRoom = editObjOrderRoomET.getText().toString().trim();
        editOrderRoom = editOrderRoomET.getText().toString().trim();
        editObjOrderDesc = editObjOrderDescET.getText().toString().trim();
        orderId = getOrderIdTV.getText().toString().trim();

        if (TextUtils.isEmpty(editObjOrderRoom)) {
            Toast.makeText(this, "Etajni tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(editOrderRoom)) {
            Toast.makeText(this, "Xonani tanlang...", Toast.LENGTH_SHORT).show();
            return;
        }



        progressDialog.setMessage("O'zgartirish");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("objRoom", ""+editObjOrderRoom);
        hashMap.put("orderRoom", ""+editOrderRoom);


        if (!editObjOrderDesc.isEmpty()) {
            hashMap.put("objDescET", ""+editObjOrderDesc);
        }

        DocumentReference productRef = firestore.collection("OrderObjects").document(orderObjectId);
        productRef.update(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Updated...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OrderDetail.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this,
                    "Not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void objRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Etaj").setItems(Constants.objRooms1, (dialog, which) -> {
            // get picked location
            String objRoom = Constants.objRooms1[which];
            // set picked location
            editObjOrderRoomET.setText(objRoom);
        }).show();
    }

    private void orderRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xona").setItems(Constants.orderRooms1, (dialog, which) -> {
            // get picked location
            String orderRoom = Constants.orderRooms1[which];
            // set picked location
            editOrderRoomET.setText(orderRoom);
        }).show();
    }

    private void loadEditOrder() {
        progressDialog.setMessage("Loading Product");
        progressDialog.show();
        DocumentReference productRef = firestore.collection("OrderObjects").document(orderObjectId);
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()){
                    String objRoom = doc.getString("objRoom");   // qavati
                    String orderRoom = doc.getString("orderRoom");   // Assuming "prCat" field exists
                    String orderId = doc.getString("orderId");
                    if (doc.contains("objDescET")) {  // Check if field exists
                        String objDescET = doc.getString("objDescET");
                        editObjOrderDescET.setText(objDescET);
                    } else {
                        editObjOrderDescET.setText("");
                    }

                    editObjOrderRoomET.setText(objRoom);
                    editOrderRoomET.setText(orderRoom);
                    getOrderIdTV.setText(orderId);
                }
            } else {
                progressDialog.dismiss();
                // Handle errors (consider logging or displaying an error message)
                Toast.makeText(EditOrderObject.this, "Error fetching product: " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(){
        orderObjectId = getIntent().getStringExtra("orderObjectId");

        firestore = FirebaseFirestore.getInstance();

        editObjOrderRoomET = findViewById(R.id.editObjOrderRoomET);
        editOrderRoomET = findViewById(R.id.editOrderRoomET);
        editObjOrderDescET = findViewById(R.id.editObjOrderDescET);
        backBtn = findViewById(R.id.backBtn);
        editOrderBtn = findViewById(R.id.editOrderBtn);
        getOrderIdTV = findViewById(R.id.getOrderIdTV);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);
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