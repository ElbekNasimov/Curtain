package com.example.curtain.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtchotDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ImageButton otchotPrintBtn, otchotBackBtn;
    private TextView otchotDateTV, countNewOrdersTV, countChiqqanOrdersTV, sumChiqqanOrderTV, costChiqqanOrderTV,
            sumXarajatTV, xarajatHistoryTV, orderHistoryTV;
    private Button addXarajatToMonthBtn;
    private RecyclerView monthXarajatRV, orderHistoryRV;
    private String otchotId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otchot_detail);

        init();

//        loadOtchotDetail(otchotId);
        Toast.makeText(this, "otchotId " + otchotId, Toast.LENGTH_SHORT).show();
    }

    private void loadOtchotDetail(String otchotId) {
        progressDialog.setMessage("Loading order Detail");
        progressDialog.show();
        DocumentReference otchotRef = firestore.collection("Otchotlar").document(otchotId);

    }

    private void init() {
        otchotPrintBtn = findViewById(R.id.otchotPrintBtn);
        otchotBackBtn = findViewById(R.id.otchotBackBtn);

        otchotDateTV = findViewById(R.id.otchotDateTV);
        countNewOrdersTV = findViewById(R.id.countNewOrdersTV);
        countChiqqanOrdersTV = findViewById(R.id.countChiqqanOrdersTV);
        sumChiqqanOrderTV = findViewById(R.id.sumChiqqanOrderTV);
        costChiqqanOrderTV = findViewById(R.id.costChiqqanOrderTV);
        sumXarajatTV = findViewById(R.id.sumXarajatTV);
        xarajatHistoryTV = findViewById(R.id.xarajatHistoryTV);
        orderHistoryTV = findViewById(R.id.orderHistoryTV);
        addXarajatToMonthBtn = findViewById(R.id.addXarajatToMonthBtn);

        monthXarajatRV = findViewById(R.id.monthXarajatRV);
        orderHistoryRV = findViewById(R.id.orderHistoryRV);

        if (getIntent().hasExtra("otchotId")){
            otchotId = getIntent().getStringExtra("otchotId");
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("otchotId")){
            otchotId = intent.getStringExtra("otchotId");
            loadOtchotDetail(otchotId);
        }
    }
}