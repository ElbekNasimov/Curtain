package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.filter.FilterProduct;
import com.example.curtain.model.ModelProduct;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AdapterObjectProducts extends RecyclerView.Adapter<AdapterObjectProducts.HolderObjectProducts>
        implements Filterable {

    /**
     * objectga yoki orderga parda tanlashda chiqadigan ro'yxat
     *  row object products item
     */

    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filterProduct;
    private TextInputEditText searchPrObjET;
    private TextView searchPrIdObjET;
    private Context context;

    public AdapterObjectProducts(Context context, ArrayList<ModelProduct> productList,
                                 TextInputEditText searchPrObjET, TextView searchPrIdObjET) {
        this.context = context;
        this.productList=productList;
        this.searchPrObjET=searchPrObjET;
        this.searchPrIdObjET = searchPrIdObjET;
        this.filterList=productList;
    }

    @Override
    public Filter getFilter() {
        if (filterProduct == null){
            filterProduct = new FilterProduct(this, filterList);
        }
        return filterProduct;
    }

    @NonNull
    @Override
    public HolderObjectProducts onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_object_products_item, parent, false);
        return new AdapterObjectProducts.HolderObjectProducts(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderObjectProducts holder, int position) {
        final ModelProduct modelProduct = productList.get(position);

        String title = modelProduct.getPrTitle();
        String price = modelProduct.getPrPrice();
        String productId = modelProduct.getPrId();

        holder.objectProductIdTV.setVisibility(View.GONE);
        holder.objectProductTitleTV.setText(title);
        holder.objectProductIdTV.setText(productId);
        if (price!=null) {
            holder.objectProductPriceTV.setText(String.format("$ %s", price));
        } else {
            holder.objectProductPriceTV.setText(String.format("$ %s", "0"));
        }
        holder.itemView.setOnClickListener(view -> {
            searchPrObjET.setText(title);
            searchPrIdObjET.setText(productId);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class HolderObjectProducts extends RecyclerView.ViewHolder {
        private TextView objectProductTitleTV, objectProductPriceTV, objectProductIdTV;
        public HolderObjectProducts(@NonNull View itemView) {
            super(itemView);
            objectProductTitleTV = itemView.findViewById(R.id.objectProductTitleTV);
            objectProductPriceTV = itemView.findViewById(R.id.objectProductPriceTV);
            objectProductIdTV = itemView.findViewById(R.id.objectProductIdTV);
        }
    }
}
