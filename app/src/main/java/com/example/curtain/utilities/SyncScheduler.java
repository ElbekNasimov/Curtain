package com.example.curtain.utilities;

import android.content.Context;
import android.content.PeriodicSync;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.curtain.worker.SyncWorker;

import java.util.concurrent.TimeUnit;

public class SyncScheduler {
    public static void scheduleSync(Context context){
//        PeriodicWorkRequest syncRequest = new
//                PeriodicWorkRequest.Builder(SyncWorker.class, 2, TimeUnit.HOURS).build();
//        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}
