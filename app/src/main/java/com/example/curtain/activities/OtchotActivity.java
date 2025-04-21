package com.example.curtain.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.curtain.R;
import com.example.curtain.adapter.AdapterOtchot;
import com.example.curtain.adapter.AdapterUser;
import com.example.curtain.model.ModelOtchot;
import com.example.curtain.model.ModelUser;
import com.example.curtain.utilities.NetworkChangeListener;
import com.example.curtain.worker.CreateDailyReportWorker;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class OtchotActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private ArrayList<ModelOtchot> otchotList;
    private String sharedUserType;
    private RecyclerView otchotListRV;
    private ProgressDialog progressDialog;
    private AdapterOtchot adapterOtchot;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otchot_activity);

        init();


        // if sharedUserType equals "superAdmin" then create new document to Firestore with date and time using workManager
        if (sharedUserType.equals("superAdmin")) {
            // create new document to Firestore with date and time using workManager
            setupDailyReportWork();
        }


        loadOtchotOylar();

    }

    private void loadOtchotOylar() {
        // get al items from "Otchotlar" collection
        CollectionReference collectionReference  = firestore.collection("Otchotlar");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()){
                    Toast.makeText(OtchotActivity.this, "task.getResult empty ekan", Toast.LENGTH_SHORT).show();
                    return;
                }
                otchotList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    ModelOtchot modelOtchot = document.toObject(ModelOtchot.class);
                    otchotList.add(modelOtchot);
                }

                adapterOtchot = new AdapterOtchot(OtchotActivity.this, otchotList, sharedPreferences);
                otchotListRV.setHasFixedSize(true);
                otchotListRV.setAdapter(adapterOtchot);
                adapterOtchot.notifyDataSetChanged(); // UI ni yangilash

            } else {
                // show Toast message
                Toast.makeText(this, "Otchot yuklashda xato: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setupDailyReportWork() {
        Calendar now = Calendar.getInstance();

        // Ertangi soat 00:00 ni belgilash
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 0);
        nextRun.set(Calendar.MINUTE, 0);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        // Agar hozirgi vaqt ertangi soatdan keyin bo'lsa, ertangi soatni belgilash
        if (now.after(nextRun)) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Boshlang'ich kechikishni hisoblash
        long initialDelay = nextRun.getTimeInMillis() - System.currentTimeMillis();
        // Periodik ishni rejalashtirish
        PeriodicWorkRequest dailyReportWork = new PeriodicWorkRequest.Builder(CreateDailyReportWorker.class, 24,
                TimeUnit.HOURS).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("DailyReportWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyReportWork);
    }

    private void init(){
        firestore = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);
        sharedUserType = sharedPreferences.getString("user_type", "");

        otchotList = new ArrayList<>();

        otchotListRV = findViewById(R.id.otchotListRV);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
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