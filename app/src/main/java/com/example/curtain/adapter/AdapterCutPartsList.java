package com.example.curtain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.model.ModelCutPartsList;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterCutPartsList extends RecyclerView.Adapter<AdapterCutPartsList.HolderCutPartsList> {
    private Context context;
    private ArrayList<ModelCutPartsList> cutPartsList;

    private FirebaseFirestore firestore;
    public AdapterCutPartsList(Context context, ArrayList<ModelCutPartsList> cutPartsList) {
        this.context = context;
        this.cutPartsList = cutPartsList;
    }

    @NonNull
    @Override
    public AdapterCutPartsList.HolderCutPartsList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cut_parts_list, parent, false);
        return new HolderCutPartsList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCutPartsList holder, int position) {

        firestore = FirebaseFirestore.getInstance();

        final ModelCutPartsList modelCutPartsList = cutPartsList.get(position);

        String orderId = modelCutPartsList.getOrderId();
        String quantity = modelCutPartsList.getPartCutPrObjLen();
        String cutDate = modelCutPartsList.getCutIdPartProductOrder();

        holder.quanCPLTV.setText(quantity);

        Date prDate = new Date(Long.parseLong(cutDate));
        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String date = sdfFormat.format(prDate);
        holder.cutDateCPLTV.setText(date);

        holder.measCPLTV.setText("m");

        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_cut_parts_list_info, null);
            alertDialog.setTitle("Info");

            TextView infoOrderByNameTV = dialogView.findViewById(R.id.infoOrderByNameTV);
            TextView infoOrderNameTV = dialogView.findViewById(R.id.infoOrderNameTV);
            TextView infoOrderNumberTV = dialogView.findViewById(R.id.infoOrderNumberTV);

            DocumentReference orderRef = firestore.collection("Orders").document(orderId);
            orderRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){
                        infoOrderByNameTV.setText(String.format("Dizayner: %s", doc.getString("created_by").toUpperCase()));
                        infoOrderNameTV.setText(String.format("Klient: %s", doc.getString("orderName").toUpperCase()));
                        infoOrderNumberTV.setText(String.format("Smeta: %s", doc.getString("orderNumber").toUpperCase()));
                    } else {
                        Toast.makeText(context, "Tegishli ma'lumot topilmadi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "xatolik " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.setView(dialogView).setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });
    }


    public class HolderCutPartsList extends RecyclerView.ViewHolder{

        TextView quanCPLTV, measCPLTV, cutDateCPLTV;

        public HolderCutPartsList(@NonNull View itemView) {
            super(itemView);
            quanCPLTV = itemView.findViewById(R.id.quanCPLTV);
            measCPLTV = itemView.findViewById(R.id.measCPLTV);
            cutDateCPLTV = itemView.findViewById(R.id.cutDateCPLTV);
        }
    }

    @Override
    public int getItemCount() {
        return cutPartsList.size();
    }
}
