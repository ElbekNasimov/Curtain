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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.constants.Constants;
import com.example.curtain.model.ModelPart;
import com.example.curtain.model.ModelReservation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class AdapterPart extends RecyclerView.Adapter<AdapterPart.HolderPart> implements AdapterReservation.OnReservationChangeListener{

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_part_detail, parent, false);
        return new HolderPart(view);
    }

    class HolderPart extends RecyclerView.ViewHolder {
        private TextView quanTV, measTV, locTV, isReservePartTV, byReservedPartTV, descPartTV, inStockTV;
        private ImageButton editPartBtn, delPartBtn;
        private TextView availableLengthTV, totalReservedTV;
        private RecyclerView reservationRV;



        public HolderPart(@NonNull View itemView) {
            super(itemView);

            quanTV = itemView.findViewById(R.id.quanTV);
            measTV = itemView.findViewById(R.id.measTV);
            locTV = itemView.findViewById(R.id.locTV);
            editPartBtn = itemView.findViewById(R.id.editPartBtn);
            delPartBtn = itemView.findViewById(R.id.delPartBtn);
            isReservePartTV = itemView.findViewById(R.id.isReservePartTV);
            byReservedPartTV = itemView.findViewById(R.id.byReservedPartTV);
            descPartTV = itemView.findViewById(R.id.descPartTV);
            inStockTV = itemView.findViewById(R.id.inStockTV);

            availableLengthTV = itemView.findViewById(R.id.availableLengthTV);
            totalReservedTV = itemView.findViewById(R.id.totalReservedTV);
            reservationRV = itemView.findViewById(R.id.reservationsRV);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPart.HolderPart holder, int position) {

        final ModelPart modelPart = partList.get(position);
        String qty = modelPart.getPartLen();
        String loc = modelPart.getPartLoc();
        String meas = modelPart.getPartMeas();
        String inStock = modelPart.getIsStock();
        String partId = modelPart.getPartId();
        String byReserved = modelPart.getByReservedPart();
        String descPart = modelPart.getDescPart();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference partRef = firestore.collection("Parts").document(partId);

        holder.quanTV.setText(qty != null ? qty : "");
        holder.measTV.setText(meas != null ? meas : "");
        holder.locTV.setText(loc != null ? loc : "");
        if (descPart != null && !descPart.isEmpty()) {
            holder.descPartTV.setText(descPart);
            holder.descPartTV.setVisibility(View.VISIBLE); // Agar ilgari GONE bo‘lgan bo‘lsa, yana ko‘rsatsin
        } else {
            holder.descPartTV.setVisibility(View.GONE);
        }
        holder.inStockTV.setText(inStock != null ? inStock : "");

        sharedPreferences = context.getApplicationContext().getSharedPreferences("USER_TYPE", context.MODE_PRIVATE);

        String sharedUserType = sharedPreferences.getString("user_type", "");
        String sharedUserName = sharedPreferences.getString("username", "");

        // bronlarni ko'rsatish uchun
        loadReservations(partId, modelPart, holder, position);

        // availableLengthTV ni ko'rsatish
        double availableLength = modelPart.calculateAvailableLength();
        double totalReserved = modelPart.calculateReservedLength();

        if (sharedUserType.equals("viewer")) {
            holder.isReservePartTV.setVisibility(View.GONE);
        }
            // bron bekor qilish button
            if (availableLength > 0) {
                holder.isReservePartTV.setText("Bron qilish");
                holder.isReservePartTV.setEnabled(true);
                holder.isReservePartTV.setOnClickListener(v -> {

                    showPartialReservationDialog(partId, position, sharedUserName, availableLength);

                });
            } else {
                holder.isReservePartTV.setText("To'liq bron qilingan");
                holder.isReservePartTV.setEnabled(false);
                holder.isReservePartTV.setTextColor(context.getResources().getColor(R.color.red));
            }

        // admin buttons visibility
        if (sharedUserType == null || !sharedUserType.equals("sklad") && !sharedUserType.equals("admin")
                && !sharedUserType.equals("superAdmin")){
            holder.delPartBtn.setVisibility(View.GONE);
            holder.editPartBtn.setVisibility(View.GONE);
        }

        // partni ostatkasi, agar ostatkada bo'lsa, shu orqali "bor" so'zi chiqadi
        if (sharedUserType.equals("superAdmin")) {
            holder.quanTV.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Kusok holati")
                        .setMessage("Shu kusok bormi?")
                        .setPositiveButton("Ha", (dialog, which) -> {
                            // Firestorega "isStock": "bor" yoziladi
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isStock", "bor");

                            partRef.update(hashMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Kusok mavjud deb belgilandi", Toast.LENGTH_SHORT).show();
                                        modelPart.setIsStock("bor"); // Modelni yangilash
                                        notifyItemChanged(position); // UI yangilash (agar kerak bo‘lsa)
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Xatolik: Ma'lumot saqlanmadi\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        })
                        .setNegativeButton("Yo‘q", (dialog, which) -> {
                            dialog.dismiss(); // hech narsa qilinmaydi
                        })
                        .show();
            });
        } else {
            holder.quanTV.setOnClickListener(null); // SuperAdmin emas, hech narsa qilinmaydi
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

    private void loadReservations(String partId, ModelPart modelPart, HolderPart holder, int position) {
        FirebaseFirestore.getInstance()
                .collection("Parts").document(partId)
                .collection("Reservations")
                .whereEqualTo("status", "active").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<ModelReservation> reservations = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ModelReservation reservation = document.toObject(ModelReservation.class);
                        if (reservation != null) {
                            reservations.add(reservation);
                        }
                    }

                    modelPart.setReservations(reservations);

                    // Nested RecyclerView uchun adapterni o'rnatish
                    if (reservations.size()>0) {
                        holder.reservationRV.setVisibility(View.VISIBLE);
                        holder.byReservedPartTV.setVisibility(View.VISIBLE);

                        AdapterReservation adapterReservation = new AdapterReservation(context, reservations, partId, this);
                        holder.reservationRV.setLayoutManager(new LinearLayoutManager(context));
                        holder.reservationRV.setAdapter(adapterReservation);

                        holder.byReservedPartTV.setText(String.format("%d ta bron qilgan:", reservations.size()));

                    } else {
                        holder.reservationRV.setVisibility(View.GONE);
                        holder.byReservedPartTV.setVisibility(View.GONE);
                    }
                    // available length ko'rsatish
                    double availableLength = modelPart.calculateAvailableLength();
                    double totalReserved = modelPart.calculateReservedLength();

                    holder.availableLengthTV.setText("Mavjud uzunlik: " + availableLength);
                    holder.totalReservedTV.setText(String.format("Bron: %.1fm", totalReserved));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Bronlarni yuklashda xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Partial reservation dialog
    private void showPartialReservationDialog(String partId, int position, String designerName, double maxLength) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertDialogView = inflater.inflate(R.layout.dialog_reserve_part, null);

        TextView maxLengthTV = alertDialogView.findViewById(R.id.maxLengthTV);
        EditText reserveLengthET = alertDialogView.findViewById(R.id.reserveLengthET);
        EditText reserveClientET = alertDialogView.findViewById(R.id.reserveClientET);

        maxLengthTV.setText(String.format("Maksimal: %.1fm", maxLength));
        new AlertDialog.Builder(context).setView(alertDialogView).setTitle("Bron qilish")
                .setMessage("Necha metr bron qilmoqchisiz?")
                .setPositiveButton("Bron qilish", ((dialog, which) -> {
                    String lengthStr = reserveLengthET.getText().toString().trim();
                    String reservedFor = reserveClientET.getText().toString().trim();

                    if (TextUtils.isEmpty(lengthStr) || TextUtils.isEmpty(reservedFor)) {
                        Toast.makeText(context, "Iltimos, bron qilish uchun uzunlik va mijoz nomini kiriting", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try{
                        double requestedLength = Double.parseDouble(lengthStr);
                        if (requestedLength<=0) {
                            Toast.makeText(context, "Iltimos, musbat uzunlik kiriting", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (requestedLength > maxLength) {
                            Toast.makeText(context, "Bron qilish uchun maksimal uzunlikdan oshib ketdingiz", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        createPartialReservation(partId, position, designerName, lengthStr, reservedFor);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Iltimos, to'g'ri uzunlik kiriting", Toast.LENGTH_SHORT).show();
                        return;
                    }
                })).setNegativeButton("Bekor qilish", (dialog, which) -> dialog.dismiss()).show();
    }

    private void createPartialReservation(String partId, int position, String designerName,
                                         String requestedLength, String reservedFor){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Bron qilinmoqda...");
        progressDialog.setCancelable(false); // Foydalanuvchi dialogni bekor qila olmasin
        progressDialog.show(); // Dialogni ko'rsatish

        String reservationId = UUID.randomUUID().toString(); // Yangi bron ID yaratish
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        HashMap<String, Object> reservationData = new HashMap<>();
        reservationData.put("reservationId", reservationId);
        reservationData.put("reservedBy", designerName);
        reservationData.put("reservedLength", requestedLength);
        reservationData.put("reservedFor", reservedFor);
        reservationData.put("reservedDate", currentDate);
        reservationData.put("status", "active");
        reservationData.put("reservedPartId", partId);

        FirebaseFirestore.getInstance()
                .collection("Parts").document(partId)
                .collection("Reservations").document(reservationId)
                .set(reservationData)
                        .addOnSuccessListener(aVoid -> {
                            // Bron muvaffaqiyatli yaratildi
                            progressDialog.dismiss(); // Dialogni yopish
                            ModelReservation newReservation = new ModelReservation(
                                    reservationId, designerName, requestedLength, reservedFor,
                                    currentDate, "active", partId);

                            if (partList.get(position).getReservations() == null) {
                                partList.get(position).setReservations(new ArrayList<>());
                            }
                            partList.get(position).getReservations().add(newReservation);
                            notifyItemChanged(position); // UI ni yangilash

                            Toast.makeText(context, "Bron yaratildi", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Bron yaratishda xatolik
                            progressDialog.dismiss(); // Dialogni yopish
                            Toast.makeText(context, "Bron yaratishda xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

        progressDialog.dismiss(); // Dialogni yopish
        Toast.makeText(context, "Bron qilindi", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReservationChanged() {
        // Bu metod AdapterReservation dan chaqiriladi, agar bronlar o'zgargan bo'lsa
        notifyDataSetChanged(); // Adapterni yangilash
    }

    @Override
    public int getItemCount() {
        return partList.size();
    }
}