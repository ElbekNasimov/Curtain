package com.example.curtain.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.R;
import com.example.curtain.adapter.AdapterCutPartsList;
import com.example.curtain.model.ModelCutPartsList;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CutPartsListActivity extends AppCompatActivity {

    private RecyclerView partCPLRV;
    private TextView noCutPartsListTV;
    private FirebaseFirestore firestore;
    private AdapterCutPartsList adapterCutPartsList;
    private ArrayList<ModelCutPartsList> cutPartsList;
    private ProgressDialog progressDialog;
    private String prID;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_parts_list);

        init();

        loadCutParts();
        noCutPartsListTV.setVisibility(View.GONE);

    }

    private void init() {
        partCPLRV = findViewById(R.id.partCPLRV);
        noCutPartsListTV = findViewById(R.id.noCutPartsListTV);

        firestore = FirebaseFirestore.getInstance();

        cutPartsList = new ArrayList<>();

        prID = getIntent().getStringExtra("prId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void loadCutParts() {
        progressDialog.show();
        CollectionReference partsRef = firestore.collection("CutPartProductOrder");
        partsRef.whereEqualTo("productId", prID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                cutPartsList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelCutPartsList modelCutPartsList = snapshot.toObject(ModelCutPartsList.class);
                    cutPartsList.add(modelCutPartsList);
                }
                if (cutPartsList.isEmpty()){
                    partCPLRV.setVisibility(View.GONE);
                    noCutPartsListTV.setVisibility(View.VISIBLE);
                }
                adapterCutPartsList = new AdapterCutPartsList(CutPartsListActivity.this, cutPartsList);
                partCPLRV.setAdapter(adapterCutPartsList);
            } else {
                progressDialog.dismiss();
                Toast.makeText(CutPartsListActivity.this, "qismlar yuklashda xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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