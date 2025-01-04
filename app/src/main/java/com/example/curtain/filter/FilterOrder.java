package com.example.curtain.filter;

import android.util.Log;
import android.widget.Filter;

import com.example.curtain.adapter.AdapterOrder;
import com.example.curtain.model.ModelOrder;

import java.util.ArrayList;

public class FilterOrder extends Filter {

    private AdapterOrder adapter;
    private ArrayList<ModelOrder> filterList;

    public FilterOrder(AdapterOrder adapter, ArrayList<ModelOrder> orderList) {
        this.adapter = adapter;
        this.filterList = orderList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        // validate data for search query
        if (charSequence != null && charSequence.length() > 0){
            // search field not empty, searching something, perform search

            // change to upper case, to make case insensitive
            charSequence = charSequence.toString().toUpperCase();
            // store our filtered list
            ArrayList<ModelOrder> filterOrders = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                // check, search by title and category
                if (filterList.get(i).getOrderNumber().toUpperCase().contains(charSequence)
                        || filterList.get(i).getOrderName().toUpperCase().contains(charSequence)
                        || filterList.get(i).getCreated_by().toUpperCase().contains(charSequence)) {
                    // add filtered data to list
                    filterOrders.add(filterList.get(i));
                }
            }
            Log.i("ModelOrder", String.valueOf(filterOrders.size()));
            results.count = filterOrders.size();
            results.values = filterOrders;
        } else {
            // search field empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        Log.i("FilterOrder", results.values.toString());
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.orderList = (ArrayList<ModelOrder>) filterResults.values;
        // refresh adapter
        adapter.notifyDataSetChanged();
    }
}