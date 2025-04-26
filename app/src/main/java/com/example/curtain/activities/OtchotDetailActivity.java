package com.example.curtain.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curtain.R;
import com.example.curtain.adapter.AdapterOylikXarajat;
import com.example.curtain.databinding.ActivityOtchotDetailBinding;
import com.example.curtain.model.ModelOylikXarajat;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class OtchotDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String otchotId, otchotTitle;
    private ProgressDialog progressDialog;
    private ArrayList<ModelOylikXarajat> monthXarajatArrayList;
    private AdapterOylikXarajat adapterOylikXarajat;
    private ActivityOtchotDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtchotDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        binding.addXarajatToMonthBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(OtchotDetailActivity.this);
            LayoutInflater inflater = LayoutInflater.from(OtchotDetailActivity.this.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_xarajat_to_month, null);
            alertDialog.setTitle("Xarajat");

            EditText xarajatSumET = dialogView.findViewById(R.id.xarajatSumET);
            EditText xarajatDescET = dialogView.findViewById(R.id.xarajatDescET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        String timestamps = "" + System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("oylikXarajatSumId", timestamps);
                        hashMap.put("otchotId", otchotId);
                        hashMap.put("otchotTitle", otchotTitle);
                        hashMap.put("xarajatSum",xarajatSumET.getText().toString().trim());
                        hashMap.put("xarajatDesc", xarajatDescET.getText().toString().trim());

                        progressDialog.setMessage("Saqlanmoqda...");
                        progressDialog.show();
                        if (!TextUtils.isEmpty(xarajatSumET.getText())) {
                            firestore.collection("OylikXarajat").document(timestamps).set(hashMap)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()){
                                            String xarajat = xarajatSumET.getText().toString().trim();
                                            changeOtchotXarajat(otchotId, xarajat, otchotTitle);

                                        } else {
                                            Toast.makeText(OtchotDetailActivity.this, "Qo'shishda muammo "
                                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(OtchotDetailActivity.this, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

        binding.monthXarajatRV.setVisibility(View.GONE);
        binding.xarajatHistoryTV.setOnClickListener(view -> {
            String open = getResources().getString(R.string.xarajatlarOpen);
            String close = getResources().getString(R.string.xarajatlarClose);
            if (binding.xarajatHistoryTV.getText().toString().trim().equals(open)){
                binding.monthXarajatRV.setVisibility(View.VISIBLE);
                binding.xarajatHistoryTV.setText(close);
            } else {
                binding.monthXarajatRV.setVisibility(View.GONE);
                binding.xarajatHistoryTV.setText(open);
            }
        });

        loadOylikXarajat(otchotId);

        loadOtchotDetail(otchotId);
    }

    private void changeOtchotXarajat(String otchotId, String xarajat, String otchotTitle) {
        DocumentReference otchotRef = firestore.collection("Otchotlar").document(otchotId);
        otchotRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String getOtchotId = documentSnapshot.getString("otchotId");
                String getOtchotTitle = documentSnapshot.getString("otchotTitle");
                String xarajatSum = documentSnapshot.getString("xarajatSum");

                if (xarajatSum != null) {
                    int sum = Integer.parseInt(xarajatSum) + Integer.parseInt(xarajat);
                    otchotRef.update("xarajatSum", String.valueOf(sum));
                } else {
                    otchotRef.update("xarajatSum", xarajat);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(OtchotDetailActivity.this, "otchotlar yuklashda xatolik: "
                + e.getMessage(), Toast.LENGTH_SHORT).show());
        
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("otchotId", otchotId);
        otchotRef.update(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            loadOylikXarajat(otchotId);
            loadOtchotDetail(otchotId);
            Toast.makeText(OtchotDetailActivity.this, "Xarajat o'zgartirildi", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(OtchotDetailActivity.this, "Xarajat o'zgartirishda xatolik: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadOylikXarajat(String otchotId) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());

        progressDialog.setMessage("load order pays");
        progressDialog.show();

        CollectionReference partsRef = firestore.collection("OylikXarajat");
        partsRef.whereEqualTo("otchotId", otchotId).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()){

                progressDialog.dismiss();
                monthXarajatArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){

                    ModelOylikXarajat modelOylikXarajat = snapshot.toObject(ModelOylikXarajat.class);
                    monthXarajatArrayList.add(modelOylikXarajat);
                }
                if (monthXarajatArrayList.isEmpty()){
                    binding.monthXarajatRV.setVisibility(View.GONE);
                }
                adapterOylikXarajat = new AdapterOylikXarajat(OtchotDetailActivity.this, monthXarajatArrayList);
                binding.monthXarajatRV.setAdapter(adapterOylikXarajat);
                adapterOylikXarajat.notifyDataSetChanged();
            } else {
                progressDialog.dismiss();
                Toast.makeText(OtchotDetailActivity.this, "qismlar yuklashda xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOtchotDetail(String otchotId) {
        progressDialog.setMessage("Loading order Detail");
        progressDialog.show();
        DocumentReference otchotRef = firestore.collection("Otchotlar").document(otchotId);
        otchotRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                progressDialog.dismiss();
                String otchotTitle = documentSnapshot.getString("title");                     // +
                String countNewOrders = documentSnapshot.getString("countNewOrders");         // +
                String countChiqqanOrders = documentSnapshot.getString("countChiqqanOrders"); // +
                String sumChiqqanOrder = documentSnapshot.getString("sumChiqqanOrder");     // +
                String countYopilganOrders = documentSnapshot.getString("countYopilganOrders");
                String costChiqqanOrder = documentSnapshot.getString("costChiqqanOrder");
                String sumYopilganOrder = documentSnapshot.getString("sumYopilganOrder");    // +
                String costYopilganOrder = documentSnapshot.getString("costYopilganOrder");
                String xarajatSum = documentSnapshot.getString("xarajatSum");               // +

                if (otchotTitle!=null){
                    binding.otchotDateTV.setText(otchotTitle);
                }
                if (xarajatSum!=null){
                    binding.sumXarajatTV.setText(String.format("Xarajatlar %s", xarajatSum));
                }
                if (countNewOrders!=null){
                    binding.countNewOrdersTV.setText(String.format("Yangi smetalar soni %s", countNewOrders));
                }

                if (countChiqqanOrders!=null){
                    binding.countChiqqanOrdersTV.setText(String.format("Chiqqan smetalar soni %s", countChiqqanOrders));
                }
                if (sumChiqqanOrder!=null){
                    binding.sumChiqqanOrderTV.setText(String.format("Chiqqan smetalar summasi %s",sumChiqqanOrder));
                }
                if (costChiqqanOrder!=null){
                    binding.costChiqqanOrderTV.setText(String.format("Yopilgan smetalar tannarxi %s", costChiqqanOrder));
                }

                if (countYopilganOrders!=null){
                    binding.countYopilganOrdersTV.setText(String.format("Yopilgan smetalar soni %s", countYopilganOrders));
                }
                if (sumYopilganOrder!=null){
                    binding.sumYopilganOrdersTV.setText(String.format("Yopilgan smetalar summasi %s", sumYopilganOrder));
                }
                if (costYopilganOrder!=null){
                    binding.costYopilganOrdersTV.setText(String.format("Yopilgan smetalar tannarxi %s", costYopilganOrder));
                }




                if (costChiqqanOrder!=null){
                    binding.costChiqqanOrderTV.setText(costChiqqanOrder);
                }

            } else {
                progressDialog.dismiss();
                Toast.makeText(OtchotDetailActivity.this, "otchot topilmadi", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(OtchotDetailActivity.this, "otchotlar yuklashda xatolik: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void init() {
        firestore = FirebaseFirestore.getInstance();
        if (getIntent().hasExtra("otchotId")){
            otchotId = getIntent().getStringExtra("otchotId");
        }
        if (getIntent().hasExtra("otchotTitle")){
            otchotTitle = getIntent().getStringExtra("otchotTitle");
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);

        monthXarajatArrayList = new ArrayList<>();
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