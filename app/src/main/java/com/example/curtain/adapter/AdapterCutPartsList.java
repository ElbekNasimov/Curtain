package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.databinding.RowCutPartsListBinding;
import com.example.curtain.model.ModelCutPartsList;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdapterCutPartsList extends ListAdapter<ModelCutPartsList, AdapterCutPartsList.HolderCutPartsList> {
    private final Context context;
    private final FirebaseFirestore firestore;
    public AdapterCutPartsList(Context context) {
        super(new DiffCallback());
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public HolderCutPartsList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowCutPartsListBinding binding = RowCutPartsListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HolderCutPartsList(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCutPartsList holder, int position) {
        ModelCutPartsList modelCutPartsList = getItem(position);

        holder.binding.quanCPLTV.setText(modelCutPartsList.getPartCutPrObjLen());
        holder.binding.measCPLTV.setText("m");
        try {

            Date prDate = new Date(Long.parseLong(modelCutPartsList.getCutIdPartProductOrder()));
            SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.binding.cutDateCPLTV.setText(sdfFormat.format(prDate));
        } catch (Exception e){
            holder.binding.cutDateCPLTV.setText("???");

        }

        holder.binding.getRoot().setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_cut_parts_list_info, null);
            alertDialog.setTitle("Info");

            TextView infoOrderByNameTV = dialogView.findViewById(R.id.infoOrderByNameTV);
            TextView infoOrderNameTV = dialogView.findViewById(R.id.infoOrderNameTV);
            TextView infoOrderNumberTV = dialogView.findViewById(R.id.infoOrderNumberTV);
            firestore.collection("Orders").document(modelCutPartsList.getOrderId())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot doc = task.getResult();
                            infoOrderByNameTV.setText(String.format("Dizayner: "+ safeToUpper(doc.getString("created_by"))));
                            infoOrderNameTV.setText(String.format("Klient: %s", safeToUpper(doc.getString("orderName"))));
                            infoOrderNumberTV.setText(String.format("Smeta: %s", safeToUpper(doc.getString("orderNumber"))));
                        } else {
                            Toast.makeText(context, "Ma'lumot topilmadi " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
            alertDialog.setView(dialogView).setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.create().show();
        });
    }

    private String safeToUpper(String str) {
        return str != null ? str.toUpperCase(Locale.getDefault()) : "N/A";
    }

    static class HolderCutPartsList extends RecyclerView.ViewHolder{
        RowCutPartsListBinding binding;

        public HolderCutPartsList(RowCutPartsListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<ModelCutPartsList>{
        @Override
        public boolean areItemsTheSame(@NonNull ModelCutPartsList oldItem, @NonNull ModelCutPartsList newItem) {
            return oldItem.getCutIdPartProductOrder().equals(newItem.getCutIdPartProductOrder());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ModelCutPartsList oldItem, @NonNull ModelCutPartsList newItem) {
            return oldItem.equals(newItem);
        }
    }
}
