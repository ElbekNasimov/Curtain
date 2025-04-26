package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.model.ModelOylikXarajat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterOylikXarajat extends RecyclerView.Adapter<AdapterOylikXarajat.HolderOylikXarajat> {

    public ArrayList<ModelOylikXarajat> monthXarajatArrayList;
    private Context context;

    public AdapterOylikXarajat(Context context, ArrayList<ModelOylikXarajat> monthXarajatArrayList) {
        this.context = context;
        this.monthXarajatArrayList = monthXarajatArrayList;
    }

    @Override
    public HolderOylikXarajat onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_month_xarajat, parent, false);
        return new HolderOylikXarajat(view);
    }

    @Override
    public void onBindViewHolder(HolderOylikXarajat holder, int position) {
        final ModelOylikXarajat modelOylikXarajat = monthXarajatArrayList.get(position);
        String xarajatSum = modelOylikXarajat.getXarajatSum();
        String xarajatReason = modelOylikXarajat.getXarajatDesc();
        String xarajatDate = modelOylikXarajat.getOylikXarajatSumId();

        long timestamp = Long.parseLong(xarajatDate);
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.xarajatDateTV.setText(formattedDate);
        holder.xarajatSumTV.setText(String.format("%s $", xarajatSum));
        holder.xarajatReasonTV.setText(xarajatReason);
    }

    @Override
    public int getItemCount() {
        return monthXarajatArrayList.size();
    }

    class HolderOylikXarajat extends RecyclerView.ViewHolder {
        private TextView xarajatSumTV, xarajatReasonTV, xarajatDateTV;
        public HolderOylikXarajat(View itemView) {
            super(itemView);
            xarajatSumTV = itemView.findViewById(R.id.xarajatSumTV);
            xarajatReasonTV = itemView.findViewById(R.id.xarajatReasonTV);
            xarajatDateTV = itemView.findViewById(R.id.xarajatDateTV);
        }
    }
}
