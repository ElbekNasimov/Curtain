package com.example.curtain.utilities;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.curtain.entities.Product;
import com.example.curtain.repositories.ProductRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductSyncManager {

    private FirebaseFirestore firestore;
    private ProductRepository productRepository;

    public ProductSyncManager(Application application){
        firestore = FirebaseFirestore.getInstance();
        productRepository = new ProductRepository(application);
    }

//    public void syncProducts(Runnable onSuccess, OnFailureListener onFailureListener){
//        firestore.collection("Products").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                List<Product> products = new ArrayList<>();
//                for (DocumentSnapshot document: task.getResult()){
//                    products.add(document.toObject(Product.class));
//                }
//                productRepository.deleteAllProducts();
//                productRepository.insertProducts(products);
//                onSuccess.run();
//            } else {
//                // Handle the error
//                // You can log the error or show an error message to the user
//                 Log.e("ProductSyncManager", "Error getting documents: ", task.getException());
//                 onFailureListener.onFailure(task.getException());
//            }
//        });
//    }
    public interface OnFailureListener {
        void onFailure(Exception e);
    }
}
