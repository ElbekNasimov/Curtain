package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.model.ModelOrderPays;

import java.util.ArrayList;

public class AdapterOrderPay extends RecyclerView.Adapter<AdapterOrderPay.HolderOrderPay> {

    public ArrayList<ModelOrderPays> paysArrayList;
    private Context context;

    public AdapterOrderPay(Context context, ArrayList<ModelOrderPays> paysArrayList) {
        this.context = context;
        this.paysArrayList = paysArrayList;
    }

    @NonNull
    @Override
    public AdapterOrderPay.HolderOrderPay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_pay, parent, false);
        return new HolderOrderPay(view);
    }

    class HolderOrderPay extends RecyclerView.ViewHolder {
        private TextView orderPayTV, orderPayDateTV;
        public HolderOrderPay(@NonNull View itemView) {
            super(itemView);

            orderPayTV = itemView.findViewById(R.id.orderPayTV);
            orderPayDateTV = itemView.findViewById(R.id.orderPayDateTV);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderPay.HolderOrderPay holder, int position) {

        final ModelOrderPays modelOrderPays = paysArrayList.get(position);

        String orderPay = modelOrderPays.getOrderPay();
        String orderPayDate = modelOrderPays.getCreated_at();

        if (orderPay!=null) {
            holder.orderPayTV.setText(String.format("%s $ to'landi ", orderPay));
        } else {
            holder.orderPayTV.setText("");
        }

        holder.orderPayDateTV.setText(String.format("%s da", orderPayDate));

    }

    @Override
    public int getItemCount() {
        return paysArrayList.size();
    }
}
