package com.example.curtain.worker;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.curtain.utilities.ProductSyncManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;


public class SyncWorker extends ListenableWorker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();
        ProductSyncManager syncManager = new ProductSyncManager((Application) getApplicationContext());
//        syncManager.syncProducts(() -> {
//            // Sync successful
//            // You can perform any additional actions here if needed
//            // For example, show a Toast message
//            Toast.makeText(getApplicationContext(), "Sync successful", Toast.LENGTH_SHORT).show();
//            future.set(Result.success());
//        }, e -> {
//            // Handle the error
//            // You can log the error or show an error message to the user
//            e.printStackTrace();
//            future.set(Result.failure());
//        });
        return future;
    }

    // GitHub Copilot
//    @NonNull
//    @Override
//    public Result doWork() {
//        ProductSyncManager syncManager = new ProductSyncManager((Application) getApplicationContext());
//        syncManager.syncProducts(()->
//                {
//                    // Sync successful
//                    // You can perform any additional actions here if needed
//                    // For example, show a Toast message
//                    Toast.makeText(getApplicationContext(), "Sync successful", Toast.LENGTH_SHORT).show();
//                },
//                e -> {
//                    // Handle the error
//                    // You can log the error or show an error message to the user
//                    e.printStackTrace();
//                });
//        return Result.success();
//    }
}
