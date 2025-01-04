package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.model.ModelPart;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AdapterCutPartPrOrder extends RecyclerView.Adapter<AdapterCutPartPrOrder.HolderCutPartPrOrder> {
    public ArrayList<ModelPart> partList;
    private Context context;
    private TextInputEditText chosenPartPrOrderET;
    private TextView chosenPartIdPrOrderTV;

    public AdapterCutPartPrOrder(Context context, ArrayList<ModelPart> partList, TextInputEditText chosenPartPrOrderET
            , TextView chosenPartIdPrOrderTV) {
        this.context = context;
        this.partList = partList;
        this.chosenPartPrOrderET = chosenPartPrOrderET;
        this.chosenPartIdPrOrderTV = chosenPartIdPrOrderTV;
    }


    @NonNull
    @Override
    public AdapterCutPartPrOrder.HolderCutPartPrOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_part_cut_product_order, parent, false);
        return new AdapterCutPartPrOrder.HolderCutPartPrOrder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCutPartPrOrder.HolderCutPartPrOrder holder, int position) {
        final ModelPart modelPart = partList.get(position);
        String qty = modelPart.getPartLen();
        String partID = modelPart.getPartId();
        holder.partCutPrOrderTV.setText(qty);
        holder.cutPartIDPrOrderTV.setText(partID);

        holder.itemView.setOnClickListener(view -> {
            chosenPartPrOrderET.setText(qty);
            chosenPartIdPrOrderTV.setText(partID);
        });

        holder.cutPartIDPrOrderTV.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return partList.size();
    }

    public class HolderCutPartPrOrder extends RecyclerView.ViewHolder {

        private TextView partCutPrOrderTV, cutPartIDPrOrderTV;
        public HolderCutPartPrOrder(@NonNull View itemView) {
            super(itemView);
            partCutPrOrderTV = itemView.findViewById(R.id.partCutPrOrderTV);
            cutPartIDPrOrderTV = itemView.findViewById(R.id.cutPartIDPrOrderTV);
        }
    }
}
