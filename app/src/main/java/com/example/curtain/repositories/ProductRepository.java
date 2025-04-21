package com.example.curtain.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.curtain.appDatabases.AppDatabase;
import com.example.curtain.daos.ProductDao;
import com.example.curtain.entities.Product;

import java.util.List;

public class ProductRepository {
    private ProductDao productDao;
    private LiveData<List<Product>> allProducts;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public void insertProducts(List<Product> products) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.insertProducts(products);
        });
    }

    public void deleteAllProducts() {
        AppDatabase.databaseWriteExecutor.execute(productDao::deleteAllProducts);
    }
}
