package com.example.curtain.filter;

import android.util.Log;
import android.widget.Filter;

import com.example.curtain.adapter.AdapterObjectProducts;
import com.example.curtain.adapter.AdapterProduct;
import com.example.curtain.model.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProduct adapter;
    private AdapterObjectProducts adapterObjectProducts;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProduct adapter, ArrayList<ModelProduct> productList) {
        this.adapter = adapter;
        this.filterList = productList;
    }

    public FilterProduct(AdapterObjectProducts adapterObjectProducts, ArrayList<ModelProduct> productList) {
        this.adapterObjectProducts = adapterObjectProducts;
        this.filterList = productList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // validate data for search query
        if (constraint != null && constraint.length() > 0){
            // search field not empty, searching something, perform search

            // change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            // store our filtered list
            ArrayList<ModelProduct> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                // check, search by title and category
                if (filterList.get(i).getPrTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getPrCat().toUpperCase().contains(constraint)) {
                    // add filtered data to list
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
            Log.i("ModelProduct", String.valueOf(filterModels.size()));
        } else {
            // search field empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        Log.i("FilterProduct", results.values.toString());
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (adapter!=null){
            adapter.productList = (ArrayList<ModelProduct>) results.values;
        // refresh adapter
            adapter.notifyDataSetChanged();
        } else if (adapterObjectProducts !=null){
            adapterObjectProducts.productList=(ArrayList<ModelProduct>) results.values;
            adapterObjectProducts.notifyDataSetChanged();
        }
    }
}