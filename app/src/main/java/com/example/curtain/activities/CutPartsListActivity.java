package com.example.curtain.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.curtain.R;
import com.example.curtain.adapter.AdapterCutPartsList;
import com.example.curtain.databinding.ActivityCutPartsListBinding;
import com.example.curtain.model.ModelCutPartsList;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CutPartsListActivity extends AppCompatActivity {

    private ActivityCutPartsListBinding binding;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private AdapterCutPartsList adapterCutPartsList;
    private ProgressDialog progressDialog;
    private String prID;
    private final NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCutPartsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prID = getIntent().getStringExtra("prId");

        initProgressDialog();
        loadCutParts();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void loadCutParts() {
        progressDialog.show();

        CollectionReference partsRef = firestore.collection("CutPartProduct");
        partsRef.whereEqualTo("productId", prID)
                .get()
                .addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful() && task.getResult() != null) {
                            ArrayList<ModelCutPartsList> cutPartsList = new ArrayList<>();
                            for (DocumentSnapshot snapshot : task.getResult()){
                                ModelCutPartsList modelCutPartsList = snapshot.toObject(ModelCutPartsList.class);
                                if (modelCutPartsList != null) {
                                    cutPartsList.add(modelCutPartsList);
                                }
                            }
                            updateUI(cutPartsList);
                        } else {
                            showToast("qismlar yuklashda xato " + (task.getException() != null ?
                                    task.getException().getMessage() : "No'malum xato"));

                        }
                    } finally {
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateUI(ArrayList<ModelCutPartsList> cutPartsList) {
        boolean isEmpty = cutPartsList == null || cutPartsList.isEmpty();
        binding.partCPLRV.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.noCutPartsListTV.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (adapterCutPartsList == null) {
            adapterCutPartsList = new AdapterCutPartsList(this);
            binding.partCPLRV.setAdapter(adapterCutPartsList);
        }
        adapterCutPartsList.submitList(cutPartsList);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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