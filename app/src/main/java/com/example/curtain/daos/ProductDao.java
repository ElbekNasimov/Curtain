package com.example.curtain.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.curtain.entities.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAllProducts();

    @Query("DELETE FROM products")
    void deleteAllProducts();
}
