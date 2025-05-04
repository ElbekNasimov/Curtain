package com.example.curtain.worker;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CreateDailyReportWorker extends Worker {
    public CreateDailyReportWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Hozirgi sana va vaqtni olish
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());
        String otchotId = "" + System.currentTimeMillis();

        HashMap<String , Object> reportData = new HashMap<>();
        reportData.put("title", todayDate);
        reportData.put("otchotId", otchotId);

        firestore.collection("Otchotlar")
                .document(otchotId)
                .set(reportData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully written to Firestore
                    Log.w("DailyReportWorker", "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    // Failed to write to Firestore
                    // show Toast message
                    Log.w("DailyReportWorker", "Error writing document", e);
                    Toast.makeText(getApplicationContext(), "Kunlik document ochishda xato" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        return Result.success();
    }
}
