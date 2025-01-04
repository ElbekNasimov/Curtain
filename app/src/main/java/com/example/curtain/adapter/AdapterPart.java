package com.example.curtain.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.crud.EditProduct;
import com.example.curtain.model.ModelPart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterPart extends RecyclerView.Adapter<AdapterPart.HolderPart>{

    public ArrayList<ModelPart> partList;

    private Context context;

    private SharedPreferences sharedPreferences;

    public AdapterPart(Context context, ArrayList<ModelPart> partList){
        this.context = context;
        this.partList = partList;
    }

    @NonNull
    @Override
    public AdapterPart.HolderPart onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_product_detail, parent, false);
        return new HolderPart(view);
    }
    class HolderPart extends RecyclerView.ViewHolder {
        private TextView quanTV, measTV, locTV;
        private ImageButton editPartBtn, delPartBtn;

        public HolderPart(@NonNull View itemView) {
            super(itemView);

            quanTV = itemView.findViewById(R.id.quanTV);
            measTV = itemView.findViewById(R.id.measTV);
            locTV = itemView.findViewById(R.id.locTV);
            editPartBtn = itemView.findViewById(R.id.editPartBtn);
            delPartBtn = itemView.findViewById(R.id.delPartBtn);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPart.HolderPart holder, int position) {

        final ModelPart modelPart = partList.get(position);
        String qty = modelPart.getPartLen();
        String loc = modelPart.getPartLoc();
        String meas = modelPart.getPartMeas();

        String partId = modelPart.getPartId();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference partRef = firestore.collection("Parts").document(partId);

        holder.quanTV.setText(qty);
        holder.measTV.setText(meas);
        holder.locTV.setText(loc);

        String sharedUserType = context.getApplicationContext().getSharedPreferences("USER_TYPE", context.MODE_PRIVATE)
                        .getString("user_type", "");

        if (!sharedUserType.equals("sklad") && !sharedUserType.equals("admin") && !sharedUserType.equals("superAdmin")){
            holder.delPartBtn.setVisibility(View.GONE);
            holder.editPartBtn.setVisibility(View.GONE);
        }

        holder.delPartBtn.setOnClickListener(view ->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Part").setMessage("O'chirmoqchimisiz?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // delete
                        partRef.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(context, "Kusok o'chirildi", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "kusok o'chirishda xato "
                                        + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(context, "Error at Deleted Part..."
                                + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        holder.editPartBtn.setOnClickListener(view -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
            alertDialog.setTitle("Change");

            EditText editPartET = dialogView.findViewById(R.id.editPartET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("partLen", editPartET.getText().toString().trim());
                        if (!TextUtils.isEmpty(editPartET.getText())) {
                            partRef.update(hashMap).addOnSuccessListener(unused ->
                                    Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                                                    + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(context, "Miqdor kiritilmagan yoki xato ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return partList.size();
    }

}
