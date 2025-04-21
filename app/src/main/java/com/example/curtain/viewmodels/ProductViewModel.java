package com.example.curtain.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.curtain.entities.Product;
import com.example.curtain.repositories.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private ProductRepository productRepository;
    private LiveData<List<Product>> allProducts;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        allProducts = productRepository.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public void insertProducts(List<Product> products){
        productRepository.insertProducts(products);
    }

    public void deleteAllProducts(){
        productRepository.deleteAllProducts();
    }
}
