package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.curtain.MainActivity;
import com.example.curtain.R;
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.filter.FilterOrder;
import com.example.curtain.model.ModelOrder;

import java.util.ArrayList;

public class AdapterOrder extends ArrayAdapter<ModelOrder> implements Filterable {
    public ArrayList<ModelOrder> orderList, filterList;
    private FilterOrder filterOrder;
    private Context context;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    public AdapterOrder(Context context, ArrayList<ModelOrder> orderList, SharedPreferences sharedPreferences) {
        super(context, 0, orderList);
        this.context = context;
        this.filterList = orderList;
        this.orderList = orderList;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Filter getFilter() {
        if (filterOrder == null){
            filterOrder = new FilterOrder(this, filterList);
            Toast.makeText(context, "filterOrder " + filterOrder.toString().toUpperCase(), Toast.LENGTH_SHORT).show();
        }
        return filterOrder;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItemView = convertView;
        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.order_card, parent, false);
        }

        progressDialog = new ProgressDialog(parent.getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        ModelOrder modelOrder = getItem(position);
        TextView orderNumberTV = listItemView.findViewById(R.id.orderNumberTV);
        TextView orderNameTV = listItemView.findViewById(R.id.orderNameTV);
        TextView orderSumTV = listItemView.findViewById(R.id.orderSumTV);
        TextView orderCreatedByTV = listItemView.findViewById(R.id.orderCreatedByTV);
        TextView orderCategoryTV = listItemView.findViewById(R.id.orderCategoryTV);
        TextView orderStatusInCardTV = listItemView.findViewById(R.id.orderStatusInCardTV);

        String orderId = modelOrder.getOrderId();

        String sharedUserType = sharedPreferences.getString("user_type", "");
        if (sharedUserType.equals("dizayner")){
            orderCreatedByTV.setVisibility(View.GONE);
        }

        orderNumberTV.setText(modelOrder.getOrderNumber());
        orderNameTV.setText(modelOrder.getOrderName());
        orderSumTV.setText(modelOrder.getOrderSum());
        orderCreatedByTV.setText(modelOrder.getCreated_by());
        orderCreatedByTV.setPaintFlags(orderCreatedByTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        orderCategoryTV.setText(modelOrder.getOrderCat());
        orderStatusInCardTV.setText(modelOrder.getOrderStatus());

        listItemView.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), OrderDetail.class);
            intent.putExtra("orderId", orderId);
            context.startActivity(intent);
        });
        return listItemView;
    }
}