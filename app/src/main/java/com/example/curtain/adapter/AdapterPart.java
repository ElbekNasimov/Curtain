package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.curtain.constants.Constants;
import com.example.curtain.model.ModelPart;
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

    public void setPartsList(ArrayList<ModelPart> partsList) {
        this.partList = partsList;
        notifyDataSetChanged(); // UI ni yangilash
    }

    @NonNull
    @Override
    public AdapterPart.HolderPart onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_product_detail, parent, false);
        return new HolderPart(view);
    }

    class HolderPart extends RecyclerView.ViewHolder {
        private TextView quanTV, measTV, locTV, isReservePartTV, byReservedPartTV;
        private ImageButton editPartBtn, delPartBtn;

        public HolderPart(@NonNull View itemView) {
            super(itemView);

            quanTV = itemView.findViewById(R.id.quanTV);
            measTV = itemView.findViewById(R.id.measTV);
            locTV = itemView.findViewById(R.id.locTV);
            editPartBtn = itemView.findViewById(R.id.editPartBtn);
            delPartBtn = itemView.findViewById(R.id.delPartBtn);
            isReservePartTV = itemView.findViewById(R.id.isReservePartTV);
            byReservedPartTV = itemView.findViewById(R.id.byReservedPartTV);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPart.HolderPart holder, int position) {

        final ModelPart modelPart = partList.get(position);
        String qty = modelPart.getPartLen();
        String loc = modelPart.getPartLoc();
        String meas = modelPart.getPartMeas();

        String partId = modelPart.getPartId();
        String isReserve = modelPart.getIsReservePart();
        String byReserved = modelPart.getByReservedPart();
        String formattedByReserved = byReserved != null ? Constants.capitalizeFirstLetter(byReserved) : "";

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference partRef = firestore.collection("Parts").document(partId);

        holder.quanTV.setText(qty != null ? qty : "0");
        holder.measTV.setText(meas != null ? meas : "");
        holder.locTV.setText(loc != null ? loc : "");

        sharedPreferences = context.getApplicationContext().getSharedPreferences("USER_TYPE", context.MODE_PRIVATE);

        String sharedUserType = sharedPreferences.getString("user_type", "");
        String sharedUserName = sharedPreferences.getString("username", "");

        if (isReserve != null && isReserve.equals("true") && !byReserved.isEmpty()) {
            // Agar mahsulot bron qilingan bo'lsa
            if (sharedUserName.equals(byReserved) || sharedUserType.equals("superAdmin")) {
                // Agar foydalanuvchi bron qilgan foydalanuvchi yoki superAdmin bo'lsa
                holder.isReservePartTV.setText("Bron qaytarish");
                holder.isReservePartTV.setEnabled(true);
                holder.isReservePartTV.setOnClickListener(v -> cancelReservation(partId, position));
            } else {
                // Boshqa foydalanuvchilar uchun
                holder.isReservePartTV.setText("Bron qilindi");
                holder.isReservePartTV.setTextColor(context.getResources().getColor(R.color.red));
                holder.isReservePartTV.setEnabled(false);
            }
            holder.byReservedPartTV.setText(String.format("%s tomonidan", formattedByReserved));
            holder.byReservedPartTV.setVisibility(View.VISIBLE);
        } else {
            // Agar mahsulot bron qilinmagan bo'lsa
            holder.isReservePartTV.setText("Bron qilish");
            holder.isReservePartTV.setEnabled(true);
            holder.isReservePartTV.setOnClickListener(v -> reservePart(partId, position, sharedUserName));
            holder.byReservedPartTV.setVisibility(View.GONE);
        }

        if (sharedUserType == null || !sharedUserType.equals("sklad") && !sharedUserType.equals("admin")
                && !sharedUserType.equals("superAdmin")){
            holder.delPartBtn.setVisibility(View.GONE);
            holder.editPartBtn.setVisibility(View.GONE);
        }

        holder.delPartBtn.setOnClickListener(view ->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Kusok o'chirish").setMessage("O'chirmoqchimisiz?")
                    .setPositiveButton("O'chirish", (dialog, which) -> {
                        // delete
                        partRef.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){

                                partList.remove(position); // Ro'yxatdan o'chirish
                                notifyItemRemoved(position); // UI ni yangilash
                                notifyItemRangeChanged(position, partList.size());

                                Toast.makeText(context, "Kusok o'chirildi", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "kusok o'chirishda xato "
                                        + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(context, "Xatolik: Qism o'chirilmadi"
                                + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        holder.editPartBtn.setOnClickListener(view -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
            alertDialog.setTitle("O'zgartirish");

            EditText editPartET = dialogView.findViewById(R.id.editPartET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("partLen", ""+editPartET.getText().toString().trim());
                        if (!TextUtils.isEmpty(editPartET.getText().toString().trim())) {
                            partRef.update(hashMap).addOnSuccessListener(unused -> {
                                        modelPart.setPartLen(editPartET.getText().toString().trim()); // Modelni yangilash
                                        notifyItemChanged(position); // UI ni yangilash
                                        Toast.makeText(context, "O'zgardi", Toast.LENGTH_SHORT).show();
                                 })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Xatolik: Qism o'zgarmadi: "
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

    private void reservePart(String partId, int position, String sharedUserName) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Bron qilinmoqda...");
        progressDialog.setCancelable(false); // Foydalanuvchi dialogni bekor qila olmasin
        progressDialog.show(); // Dialogni ko'rsatish

        DocumentReference partRef = FirebaseFirestore.getInstance().collection("Parts").document(partId);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("isReservePart", "true");
        updates.put("byReservedPart", sharedUserName);

        partRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss(); // Dialogni yopish
                    // Ma'lumotlarni yangilash
                    partList.get(position).setIsReservePart("true");
                    partList.get(position).setByReservedPart(sharedUserName);
                    notifyItemChanged(position); // UI ni yangilash
                    Toast.makeText(context, "Bron qilindi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // Dialogni yopish
                    Toast.makeText(context, "Bron qilishda xatolik", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelReservation(String partId, int position) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Bron bekor qilinmoqda...");
        progressDialog.setCancelable(false); // Foydalanuvchi dialogni bekor qila olmasin
        progressDialog.show(); // Dialogni ko'rsatish

        DocumentReference partRef = FirebaseFirestore.getInstance().collection("Parts").document(partId);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("isReservePart", "false");
        updates.put("byReservedPart", "");

        partRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss(); // Dialogni yopish
                    // Ma'lumotlarni yangilash
                    partList.get(position).setIsReservePart("false");
                    partList.get(position).setByReservedPart("");
                    notifyItemChanged(position); // UI ni yangilash
                    Toast.makeText(context, "Bron bekor qilindi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // Dialogni yopish
                    Toast.makeText(context, "Bronni bekor qilishda xatolik", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return partList.size();
    }

}
